package com.example.domain

import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.PaymentMethodEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.AppSettings
import com.example.data.model.BudgetAlertNotification
import com.example.data.model.BudgetCardUi
import com.example.data.model.BudgetFilter
import com.example.data.model.BudgetsUiState
import com.example.data.model.CategorySpendSummary
import com.example.data.model.CurrencyDisplayOption
import com.example.data.model.DailyChartPoint
import com.example.data.model.DashboardUiState
import com.example.data.model.MonthlyBudgetHistoryItem
import com.example.data.model.MonthlyComparisonBar
import com.example.data.model.ReportPeriod
import com.example.data.model.ReportsUiState
import com.example.data.model.TransactionFilterState
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.data.model.VaultInsight
import com.example.util.DateUtils
import com.example.util.MoneyMath
import java.util.Calendar
import kotlin.math.abs

object FinanceEngine {

    fun mapTransactionItems(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        paymentMethods: List<PaymentMethodEntity>
    ): List<TransactionItem> {
        val catMap = categories.associateBy { it.id }
        val methodMap = paymentMethods.associateBy { it.id }
        return transactions.map { tx ->
            TransactionItem(
                entity = tx,
                category = catMap[tx.categoryId],
                paymentMethod = methodMap[tx.paymentMethodId],
                toPaymentMethod = tx.toPaymentMethodId?.let { methodMap[it] }
            )
        }
    }

    fun filterTransactions(
        items: List<TransactionItem>,
        filter: TransactionFilterState,
        selectedYearMonth: String,
        cycleStartDay: Int
    ): List<TransactionItem> {
        val cycleRange = DateUtils.getCycleRange(selectedYearMonth, cycleStartDay)
        val startBound = filter.customStartMillis ?: if (filter.useSelectedMonth) cycleRange.startMillis else null
        val endBound = filter.customEndMillis ?: if (filter.useSelectedMonth) cycleRange.endMillis else null
        val query = filter.query.trim().lowercase()

        return items.filter { item ->
            val tx = item.entity
            if (filter.typeFilter != null && tx.type != filter.typeFilter) return@filter false
            if (startBound != null && tx.timestamp < startBound) return@filter false
            if (endBound != null && tx.timestamp > endBound) return@filter false
            if (filter.selectedCategoryIds.isNotEmpty() && tx.categoryId !in filter.selectedCategoryIds) {
                return@filter false
            }
            if (filter.selectedPaymentMethodIds.isNotEmpty() &&
                tx.paymentMethodId !in filter.selectedPaymentMethodIds &&
                (tx.toPaymentMethodId == null || tx.toPaymentMethodId !in filter.selectedPaymentMethodIds)
            ) {
                return@filter false
            }
            if (query.isNotEmpty()) {
                val haystack = listOfNotNull(
                    tx.merchant,
                    tx.note,
                    item.category?.name,
                    item.paymentMethod?.name,
                    item.toPaymentMethod?.name
                ).joinToString(" ").lowercase()
                if (!haystack.contains(query)) return@filter false
            }
            true
        }
    }

    fun computeDashboard(
        profile: UserProfileEntity?,
        items: List<TransactionItem>,
        categories: List<CategoryEntity>,
        budgets: List<BudgetEntity>,
        settings: AppSettings,
        selectedYearMonth: String,
        nowMillis: Long = System.currentTimeMillis()
    ): DashboardUiState {
        val cycle = DateUtils.getCycleRange(selectedYearMonth, settings.cycleStartDay, nowMillis)
        val prevYm = DateUtils.shiftYearMonthKey(selectedYearMonth, -1)
        val prevCycle = DateUtils.getCycleRange(prevYm, settings.cycleStartDay, nowMillis)

        // Net balance across all recorded history + opening balance (transfers are neutral)
        val allIncome = items.filter { it.entity.type == TransactionType.INCOME }.sumOf { it.entity.amountCents }
        val allExpense = items.filter { it.entity.type == TransactionType.EXPENSE }.sumOf { it.entity.amountCents }
        val opening = profile?.openingBalanceCents ?: 0L
        val totalNetBalance = opening + allIncome - allExpense

        val cycleItems = items.filter { it.entity.timestamp in cycle.startMillis..cycle.endMillis }
        val periodIncome = cycleItems.filter { it.entity.type == TransactionType.INCOME }.sumOf { it.entity.amountCents }
        val periodExpense = cycleItems.filter { it.entity.type == TransactionType.EXPENSE }.sumOf { it.entity.amountCents }

        val prevExpense = items
            .filter { it.entity.type == TransactionType.EXPENSE && it.entity.timestamp in prevCycle.startMillis..prevCycle.endMillis }
            .sumOf { it.entity.amountCents }

        val overallBudget = budgets.firstOrNull { it.yearMonth == selectedYearMonth && it.categoryId == null }
        val availableToSpend = when {
            periodIncome > 0L -> periodIncome - periodExpense
            overallBudget != null && overallBudget.limitCents > 0L -> overallBudget.limitCents - periodExpense
            else -> totalNetBalance
        }

        val dailyVelocity = buildDailySeries(
            startMillis = cycle.startMillis,
            endMillis = cycle.endMillis,
            currentExpenses = cycleItems.filter { it.entity.type == TransactionType.EXPENSE },
            previousStartMillis = prevCycle.startMillis,
            previousEndMillis = prevCycle.endMillis,
            previousExpenses = items.filter {
                it.entity.type == TransactionType.EXPENSE && it.entity.timestamp in prevCycle.startMillis..prevCycle.endMillis
            }
        )

        val categoryBreakdown = computeCategoryBreakdown(
            items = cycleItems,
            categories = categories,
            totalExpenseCents = periodExpense
        )

        val budgetsState = computeBudgets(
            items = items,
            categories = categories,
            budgets = budgets,
            settings = settings,
            selectedYearMonth = selectedYearMonth,
            activeFilter = BudgetFilter.ALL,
            nowMillis = nowMillis
        )
        val alerts = buildBudgetAlerts(budgetsState, settings.currencyOption)

        return DashboardUiState(
            profile = profile,
            totalNetBalanceCents = totalNetBalance,
            periodIncomeCents = periodIncome,
            periodExpenseCents = periodExpense,
            availableToSpendCents = availableToSpend,
            monthlyBudgetCapCents = budgetsState.overallCapCents,
            expenseDeltaPercentVsPrevMonth = MoneyMath.deltaPercentage(periodExpense, prevExpense),
            dailyVelocityPoints = dailyVelocity,
            categoryBreakdown = categoryBreakdown,
            recentTransactions = items.take(6),
            alerts = alerts,
            cycleStartMillis = cycle.startMillis,
            cycleEndMillis = cycle.endMillis
        )
    }

    fun computeBudgets(
        items: List<TransactionItem>,
        categories: List<CategoryEntity>,
        budgets: List<BudgetEntity>,
        settings: AppSettings,
        selectedYearMonth: String,
        activeFilter: BudgetFilter,
        nowMillis: Long = System.currentTimeMillis()
    ): BudgetsUiState {
        val cycle = DateUtils.getCycleRange(selectedYearMonth, settings.cycleStartDay, nowMillis)
        val monthExpenses = items.filter {
            it.entity.type == TransactionType.EXPENSE &&
                it.entity.timestamp in cycle.startMillis..cycle.endMillis
        }
        val monthBudgets = budgets.filter { it.yearMonth == selectedYearMonth }
        val overallEntity = monthBudgets.firstOrNull { it.categoryId == null }
        val catBudgets = monthBudgets.filter { it.categoryId != null }
        val catMap = categories.associateBy { it.id }

        val categoryCards = catBudgets.mapNotNull { budget ->
            val category = catMap[budget.categoryId] ?: return@mapNotNull null
            val spent = monthExpenses.filter { it.entity.categoryId == category.id }.sumOf { it.entity.amountCents }
            val remaining = budget.limitCents - spent
            val pct = MoneyMath.percentage(spent, budget.limitCents)
            val isOver = spent > budget.limitCents
            val isNear = !isOver && pct >= budget.alertThresholdPercent
            val pace = MoneyMath.safeDailyPace(remaining.coerceAtLeast(0L), cycle.daysRemaining)
            BudgetCardUi(
                budget = budget,
                category = category,
                spentCents = spent,
                remainingCents = remaining,
                progressFraction = if (budget.limitCents > 0L) (spent.toFloat() / budget.limitCents.toFloat()).coerceIn(0f, 1f) else 0f,
                percentUsed = pct,
                safeDailyPaceCents = pace,
                isNearLimit = isNear,
                isOverspent = isOver
            )
        }.sortedByDescending { it.percentUsed }

        val overallSpent = monthExpenses.sumOf { it.entity.amountCents }
        val overallCap = overallEntity?.limitCents ?: catBudgets.sumOf { it.limitCents }
        val overallRemaining = overallCap - overallSpent
        val overallPercent = MoneyMath.percentage(overallSpent, overallCap)
        val safePace = MoneyMath.safeDailyPace(overallRemaining.coerceAtLeast(0L), cycle.daysRemaining)
        val safePaceBenchmarkPercent = ((cycle.daysElapsed.toFloat() / cycle.totalDays.toFloat()) * 100f)
            .toInt().coerceIn(5, 95)

        // Monthly budget history across all months present in budgets or transactions
        val allYearMonths = (budgets.map { it.yearMonth } + selectedYearMonth).distinct().sortedDescending()
        val history = allYearMonths.map { ym ->
            val ymCycle = DateUtils.getCycleRange(ym, settings.cycleStartDay, nowMillis)
            val ymExpenses = items.filter {
                it.entity.type == TransactionType.EXPENSE && it.entity.timestamp in ymCycle.startMillis..ymCycle.endMillis
            }.sumOf { it.entity.amountCents }
            val ymBudgets = budgets.filter { it.yearMonth == ym }
            val ymCap = ymBudgets.firstOrNull { it.categoryId == null }?.limitCents
                ?: ymBudgets.filter { it.categoryId != null }.sumOf { it.limitCents }
            MonthlyBudgetHistoryItem(
                yearMonth = ym,
                monthLabel = DateUtils.formatMonthYear(ym),
                capCents = ymCap,
                spentCents = ymExpenses,
                percentUsed = MoneyMath.percentage(ymExpenses, ymCap)
            )
        }

        val usedCategoryIds = catBudgets.mapNotNull { it.categoryId }.toSet()
        val availableCategories = categories.filter {
            it.type == TransactionType.EXPENSE && it.id !in usedCategoryIds
        }

        return BudgetsUiState(
            yearMonth = selectedYearMonth,
            cycleLabel = DateUtils.formatMonthYear(selectedYearMonth),
            daysRemainingInCycle = cycle.daysRemaining,
            cycleEndsText = "Ends ${DateUtils.formatMonthDay(cycle.endMillis)}",
            overallBudget = overallEntity,
            overallCapCents = overallCap,
            overallSpentCents = overallSpent,
            overallRemainingCents = overallRemaining,
            overallPercentUsed = overallPercent,
            safeDailyPaceCents = safePace,
            safePaceBenchmarkPercent = safePaceBenchmarkPercent,
            categoryBudgets = categoryCards,
            nearLimitCount = categoryCards.count { it.isNearLimit },
            overspentCount = categoryCards.count { it.isOverspent },
            activeFilter = activeFilter,
            history = history,
            availableCategories = availableCategories
        )
    }

    fun computeReports(
        items: List<TransactionItem>,
        categories: List<CategoryEntity>,
        budgets: List<BudgetEntity>,
        settings: AppSettings,
        selectedYearMonth: String,
        period: ReportPeriod,
        customStartMillis: Long?,
        customEndMillis: Long?,
        nowMillis: Long = System.currentTimeMillis()
    ): ReportsUiState {
        val (startMillis, endMillis) = when (period) {
            ReportPeriod.WEEKLY -> DateUtils.getWeekRange(settings.firstDayOfWeek, nowMillis)
            ReportPeriod.MONTHLY -> {
                val c = DateUtils.getCycleRange(selectedYearMonth, settings.cycleStartDay, nowMillis)
                c.startMillis to c.endMillis
            }
            ReportPeriod.QUARTERLY -> DateUtils.getQuarterRange(selectedYearMonth)
            ReportPeriod.YEARLY -> DateUtils.getYearRange(selectedYearMonth)
            ReportPeriod.CUSTOM -> {
                if (customStartMillis != null && customEndMillis != null) {
                    DateUtils.startOfDay(minOf(customStartMillis, customEndMillis)) to
                        DateUtils.endOfDay(maxOf(customStartMillis, customEndMillis))
                } else {
                    val c = DateUtils.getCycleRange(selectedYearMonth, settings.cycleStartDay, nowMillis)
                    c.startMillis to c.endMillis
                }
            }
        }

        val spanMs = (endMillis - startMillis).coerceAtLeast(86_400_000L)
        val prevStart = startMillis - spanMs - 1L
        val prevEnd = startMillis - 1L

        val periodItems = items.filter { it.entity.timestamp in startMillis..endMillis }
        val prevItems = items.filter { it.entity.timestamp in prevStart..prevEnd }

        val inflow = periodItems.filter { it.entity.type == TransactionType.INCOME }.sumOf { it.entity.amountCents }
        val outflow = periodItems.filter { it.entity.type == TransactionType.EXPENSE }.sumOf { it.entity.amountCents }
        val prevInflow = prevItems.filter { it.entity.type == TransactionType.INCOME }.sumOf { it.entity.amountCents }
        val prevOutflow = prevItems.filter { it.entity.type == TransactionType.EXPENSE }.sumOf { it.entity.amountCents }
        val netCashFlow = inflow - outflow
        val savingsRate = if (inflow > 0L && netCashFlow > 0L) MoneyMath.percentage(netCashFlow, inflow) else 0

        val totalDaysInRange = (((endMillis - startMillis) / 86_400_000L) + 1).toInt().coerceAtLeast(1)
        val distinctDaysLogged = periodItems
            .map { DateUtils.startOfDay(it.entity.timestamp) }
            .distinct()
            .count()

        val activeDays = distinctDaysLogged.coerceAtLeast(1)
        val avgDailyBurn = outflow / activeDays

        val monthBudgets = budgets.filter { it.yearMonth == selectedYearMonth }
        val budgetCap = monthBudgets.firstOrNull { it.categoryId == null }?.limitCents
            ?: monthBudgets.filter { it.categoryId != null }.sumOf { it.limitCents }

        val dailyPoints = buildDailySeries(
            startMillis = startMillis,
            endMillis = endMillis,
            currentExpenses = periodItems.filter { it.entity.type == TransactionType.EXPENSE },
            previousStartMillis = prevStart,
            previousEndMillis = prevEnd,
            previousExpenses = prevItems.filter { it.entity.type == TransactionType.EXPENSE }
        )

        val categoryDistribution = computeCategoryBreakdown(
            items = periodItems,
            categories = categories,
            totalExpenseCents = outflow
        )

        // Last 6 months Income vs Expense comparison
        val monthlyComparisons = (5 downTo 0).map { offset ->
            val ym = DateUtils.shiftYearMonthKey(selectedYearMonth, -offset)
            val range = DateUtils.getCycleRange(ym, settings.cycleStartDay, nowMillis)
            val ymItems = items.filter { it.entity.timestamp in range.startMillis..range.endMillis }
            MonthlyComparisonBar(
                label = DateUtils.formatShortMonth(ym),
                incomeCents = ymItems.filter { it.entity.type == TransactionType.INCOME }.sumOf { it.entity.amountCents },
                expenseCents = ymItems.filter { it.entity.type == TransactionType.EXPENSE }.sumOf { it.entity.amountCents }
            )
        }

        val insights = buildVaultInsights(
            periodItems = periodItems,
            prevItems = prevItems,
            inflow = inflow,
            outflow = outflow,
            prevOutflow = prevOutflow,
            categoryDistribution = categoryDistribution,
            currency = settings.currencyOption
        )

        return ReportsUiState(
            period = period,
            rangeLabel = DateUtils.formatRangeLabel(startMillis, endMillis),
            daysLogged = distinctDaysLogged,
            totalDaysInRange = totalDaysInRange,
            totalInflowCents = inflow,
            totalOutflowCents = outflow,
            netCashFlowCents = netCashFlow,
            savingsRatePercent = savingsRate,
            avgDailyBurnCents = avgDailyBurn,
            budgetCapCents = budgetCap,
            inflowDeltaVsPrevPercent = MoneyMath.deltaPercentage(inflow, prevInflow),
            outflowDeltaVsBudgetPercent = if (budgetCap > 0L) {
                MoneyMath.deltaPercentage(outflow, budgetCap)
            } else {
                MoneyMath.deltaPercentage(outflow, prevOutflow)
            },
            dailyPoints = dailyPoints,
            categoryDistribution = categoryDistribution,
            monthlyComparisons = monthlyComparisons,
            insights = insights,
            filteredTransactions = periodItems,
            customStartMillis = customStartMillis,
            customEndMillis = customEndMillis
        )
    }

    private fun computeCategoryBreakdown(
        items: List<TransactionItem>,
        categories: List<CategoryEntity>,
        totalExpenseCents: Long
    ): List<CategorySpendSummary> {
        val expenses = items.filter { it.entity.type == TransactionType.EXPENSE }
        if (expenses.isEmpty()) return emptyList()
        val catMap = categories.associateBy { it.id }

        return expenses.groupBy { it.entity.categoryId }
            .mapNotNull { (catId, list) ->
                val cat = catMap[catId] ?: return@mapNotNull null
                val total = list.sumOf { it.entity.amountCents }
                val merchants = list.mapNotNull { it.entity.merchant.takeIf(String::isNotBlank) }
                    .distinct()
                    .take(3)
                    .joinToString(", ")
                    .ifBlank { cat.subtitle }
                CategorySpendSummary(
                    category = cat,
                    totalCents = total,
                    percentage = MoneyMath.percentage(total, totalExpenseCents),
                    transactionCount = list.size,
                    sampleMerchants = merchants
                )
            }
            .sortedByDescending { it.totalCents }
    }

    private fun buildDailySeries(
        startMillis: Long,
        endMillis: Long,
        currentExpenses: List<TransactionItem>,
        previousStartMillis: Long,
        previousEndMillis: Long,
        previousExpenses: List<TransactionItem>
    ): List<DailyChartPoint> {
        val totalDays = (((endMillis - startMillis) / 86_400_000L) + 1).toInt().coerceIn(1, 366)
        // If range is large (Quarterly / Yearly), bucket into <= 16 evenly spaced buckets so chart stays crisp
        val bucketCount = if (totalDays <= 35) totalDays else 14
        val bucketSpanMs = ((endMillis - startMillis + 1).coerceAtLeast(1L)) / bucketCount

        val points = mutableListOf<DailyChartPoint>()
        var runningCumulative = 0L
        val prevTotal = previousExpenses.sumOf { it.entity.amountCents }
        val prevBucketAvg = prevTotal / bucketCount

        for (i in 0 until bucketCount) {
            val bStart = startMillis + (i * bucketSpanMs)
            val bEnd = if (i == bucketCount - 1) endMillis else (startMillis + ((i + 1) * bucketSpanMs) - 1L)
            val bucketItems = currentExpenses.filter { it.entity.timestamp in bStart..bEnd }
            val daySum = bucketItems.sumOf { it.entity.amountCents }
            runningCumulative += daySum

            val prevSpanMs = (previousEndMillis - previousStartMillis + 1).coerceAtLeast(1L) / bucketCount
            val pStart = previousStartMillis + (i * prevSpanMs)
            val pEnd = if (i == bucketCount - 1) previousEndMillis else (previousStartMillis + ((i + 1) * prevSpanMs) - 1L)
            val prevDaySum = previousExpenses.filter { it.entity.timestamp in pStart..pEnd }.sumOf { it.entity.amountCents }

            val topMerchant = bucketItems.maxByOrNull { it.entity.amountCents }?.let {
                it.entity.merchant.ifBlank { it.category?.name ?: "" }
            }.orEmpty()

            points.add(
                DailyChartPoint(
                    dayLabel = DateUtils.formatMonthDay(bStart),
                    timestampMillis = bStart,
                    amountCents = daySum,
                    cumulativeCents = runningCumulative,
                    benchmarkCents = if (prevDaySum > 0L) prevDaySum else prevBucketAvg,
                    topMerchant = topMerchant
                )
            )
        }
        return points
    }

    private fun buildBudgetAlerts(
        budgets: BudgetsUiState,
        currency: CurrencyDisplayOption
    ): List<BudgetAlertNotification> {
        val alerts = mutableListOf<BudgetAlertNotification>()
        if (budgets.overallCapCents > 0L && budgets.overallPercentUsed >= 80) {
            val isOver = budgets.overallSpentCents > budgets.overallCapCents
            alerts.add(
                BudgetAlertNotification(
                    id = "overall_${budgets.yearMonth}",
                    title = if (isOver) "Monthly Budget Exceeded" else "Monthly Cap Approaching",
                    message = "You have spent ${MoneyMath.formatAmount(budgets.overallSpentCents, currency)} of your ${MoneyMath.formatAmount(budgets.overallCapCents, currency)} limit (${budgets.overallPercentUsed}%).",
                    categoryName = "Total Monthly Budget",
                    percentUsed = budgets.overallPercentUsed,
                    isOverspent = isOver
                )
            )
        }
        budgets.categoryBudgets
            .filter { it.isOverspent || it.isNearLimit }
            .forEach { card ->
                alerts.add(
                    BudgetAlertNotification(
                        id = "cat_${card.budget.id}",
                        title = if (card.isOverspent) "${card.category.name} Overspent" else "${card.category.name} Near Limit",
                        message = "Spent ${MoneyMath.formatAmount(card.spentCents, currency)} of ${MoneyMath.formatAmount(card.budget.limitCents, currency)} (${card.percentUsed}%).",
                        categoryName = card.category.name,
                        percentUsed = card.percentUsed,
                        isOverspent = card.isOverspent
                    )
                )
            }
        return alerts
    }

    private fun buildVaultInsights(
        periodItems: List<TransactionItem>,
        prevItems: List<TransactionItem>,
        inflow: Long,
        outflow: Long,
        prevOutflow: Long,
        categoryDistribution: List<CategorySpendSummary>,
        currency: CurrencyDisplayOption
    ): List<VaultInsight> {
        val insights = mutableListOf<VaultInsight>()

        val topCategory = categoryDistribution.firstOrNull()
        if (topCategory != null) {
            val prevCatSpend = prevItems
                .filter { it.entity.type == TransactionType.EXPENSE && it.entity.categoryId == topCategory.category.id }
                .sumOf { it.entity.amountCents }
            val delta = MoneyMath.deltaPercentage(topCategory.totalCents, prevCatSpend)
            val badge = when {
                delta != null && delta > 0 -> "+$delta% Shift"
                delta != null && delta < 0 -> "$delta% Shift"
                else -> "${topCategory.percentage}% Share"
            }
            insights.add(
                VaultInsight(
                    id = "top_category",
                    title = "Highest Outflow: ${topCategory.category.name}",
                    badgeText = badge,
                    description = "${topCategory.category.name} accounts for ${MoneyMath.formatAmount(topCategory.totalCents, currency)} (${topCategory.percentage}% of outflow) across ${topCategory.transactionCount} transactions.",
                    isPositive = delta != null && delta <= 0
                )
            )
        }

        if (inflow > 0L) {
            val net = inflow - outflow
            val rate = MoneyMath.percentage(net.coerceAtLeast(0L), inflow)
            insights.add(
                VaultInsight(
                    id = "savings_health",
                    title = if (net >= 0L) "Positive Cash Retention" else "Outflow Exceeds Inflow",
                    badgeText = if (net >= 0L) "$rate% Saved" else "Deficit",
                    description = if (net >= 0L) {
                        "You retained ${MoneyMath.formatAmount(net, currency)} from ${MoneyMath.formatAmount(inflow, currency)} inflow during this period."
                    } else {
                        "Expenses exceeded income by ${MoneyMath.formatAmount(abs(net), currency)} in this window."
                    },
                    isPositive = net >= 0L
                )
            )
        } else if (prevOutflow > 0L && outflow > 0L) {
            val delta = MoneyMath.deltaPercentage(outflow, prevOutflow) ?: 0
            insights.add(
                VaultInsight(
                    id = "pace_comparison",
                    title = if (delta <= 0) "Spend Pace Improved" else "Elevated Spend Velocity",
                    badgeText = if (delta >= 0) "+$delta% vs Prior" else "$delta% vs Prior",
                    description = "Total outflow of ${MoneyMath.formatAmount(outflow, currency)} compared against ${MoneyMath.formatAmount(prevOutflow, currency)} in the prior cycle.",
                    isPositive = delta <= 0
                )
            )
        }

        val largestExpense = periodItems
            .filter { it.entity.type == TransactionType.EXPENSE }
            .maxByOrNull { it.entity.amountCents }
        if (largestExpense != null && insights.size < 3) {
            val label = largestExpense.entity.merchant.ifBlank { largestExpense.category?.name ?: "Expense" }
            insights.add(
                VaultInsight(
                    id = "largest_tx",
                    title = "Peak Single Outflow",
                    badgeText = MoneyMath.formatCompact(largestExpense.entity.amountCents),
                    description = "Largest transaction was $label for ${MoneyMath.formatAmount(largestExpense.entity.amountCents, currency)} via ${largestExpense.paymentMethod?.name ?: "Cash"}.",
                    isPositive = false
                )
            )
        }
        return insights
    }
}
