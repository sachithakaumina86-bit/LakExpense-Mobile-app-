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
import com.example.data.model.TransactionDayGroup
import com.example.data.model.TransactionFilterState
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.data.model.TransactionsListUiState
import com.example.data.model.VaultInsight
import com.example.util.DateUtils
import com.example.util.MoneyMath
import kotlin.math.abs

object FinanceEngine {

    fun mapTransactionItems(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        paymentMethods: List<PaymentMethodEntity>
    ): List<TransactionItem> {
        val catMap = HashMap<Long, CategoryEntity>(categories.size * 2)
        for (cat in categories) catMap[cat.id] = cat
        val methodMap = HashMap<Long, PaymentMethodEntity>(paymentMethods.size * 2)
        for (method in paymentMethods) methodMap[method.id] = method

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
        val hasSpace = query.contains(' ')

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
                val directMatch = tx.merchant.contains(query, ignoreCase = true) ||
                    tx.note.contains(query, ignoreCase = true) ||
                    (item.category?.name?.contains(query, ignoreCase = true) == true) ||
                    (item.paymentMethod?.name?.contains(query, ignoreCase = true) == true) ||
                    (item.toPaymentMethod?.name?.contains(query, ignoreCase = true) == true)
                if (directMatch) return@filter true
                if (!hasSpace) return@filter false

                val haystack = buildString {
                    append(tx.merchant).append(' ')
                    append(tx.note).append(' ')
                    item.category?.name?.let { append(it).append(' ') }
                    item.paymentMethod?.name?.let { append(it).append(' ') }
                    item.toPaymentMethod?.name?.let { append(it) }
                }.lowercase()
                if (!haystack.contains(query)) return@filter false
            }
            true
        }
    }

    fun groupTransactionsByDay(
        items: List<TransactionItem>,
        nowMillis: Long = System.currentTimeMillis()
    ): TransactionsListUiState {
        if (items.isEmpty()) return TransactionsListUiState()

        var totalSpent = 0L
        var totalEarned = 0L
        val grouped = LinkedHashMap<Long, MutableList<TransactionItem>>()
        val dayNetMap = HashMap<Long, Long>()

        for (item in items) {
            val tx = item.entity
            when (tx.type) {
                TransactionType.EXPENSE -> totalSpent += tx.amountCents
                TransactionType.INCOME -> totalEarned += tx.amountCents
                TransactionType.TRANSFER -> Unit
            }
            val dayStart = DateUtils.startOfDay(tx.timestamp)
            val list = grouped.getOrPut(dayStart) { ArrayList() }
            list.add(item)
            val delta = when (tx.type) {
                TransactionType.INCOME -> tx.amountCents
                TransactionType.EXPENSE -> -tx.amountCents
                TransactionType.TRANSFER -> 0L
            }
            dayNetMap[dayStart] = (dayNetMap[dayStart] ?: 0L) + delta
        }

        val dayGroups = grouped.entries
            .sortedByDescending { it.key }
            .map { (dayStart, dayItems) ->
                val (prefix, dateStr) = DateUtils.formatRelativeDayHeader(dayStart, nowMillis)
                TransactionDayGroup(
                    dayStartMillis = dayStart,
                    dayPrefix = prefix,
                    dayDateStr = dateStr,
                    dayNetCents = dayNetMap[dayStart] ?: 0L,
                    items = dayItems
                )
            }

        return TransactionsListUiState(
            totalSpentCents = totalSpent,
            totalEarnedCents = totalEarned,
            netCashCents = totalEarned - totalSpent,
            totalCount = items.size,
            dayGroups = dayGroups
        )
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

        var allIncome = 0L
        var allExpense = 0L
        var periodIncome = 0L
        var periodExpense = 0L
        var prevExpense = 0L

        val cycleExpenses = ArrayList<TransactionItem>()
        val prevExpenses = ArrayList<TransactionItem>()

        for (item in items) {
            val tx = item.entity
            val ts = tx.timestamp
            val amount = tx.amountCents
            val inCycle = ts in cycle.startMillis..cycle.endMillis
            val inPrev = ts in prevCycle.startMillis..prevCycle.endMillis

            when (tx.type) {
                TransactionType.INCOME -> {
                    allIncome += amount
                    if (inCycle) periodIncome += amount
                }
                TransactionType.EXPENSE -> {
                    allExpense += amount
                    if (inCycle) {
                        periodExpense += amount
                        cycleExpenses.add(item)
                    }
                    if (inPrev) {
                        prevExpense += amount
                        prevExpenses.add(item)
                    }
                }
                TransactionType.TRANSFER -> Unit
            }
        }

        val opening = profile?.openingBalanceCents ?: 0L
        val totalNetBalance = opening + allIncome - allExpense

        val catMap = HashMap<Long, CategoryEntity>(categories.size * 2)
        for (cat in categories) catMap[cat.id] = cat

        // Compute current-month budget cap and alerts without multi-month history
        var overallEntity: BudgetEntity? = null
        val catBudgets = ArrayList<BudgetEntity>()
        for (b in budgets) {
            if (b.yearMonth == selectedYearMonth) {
                if (b.categoryId == null) overallEntity = b else catBudgets.add(b)
            }
        }

        val overallCap = overallEntity?.limitCents ?: catBudgets.sumOf { it.limitCents }
        val availableToSpend = when {
            periodIncome > 0L -> periodIncome - periodExpense
            overallEntity != null && overallEntity.limitCents > 0L -> overallEntity.limitCents - periodExpense
            else -> totalNetBalance
        }

        val dailyVelocity = buildDailySeries(
            startMillis = cycle.startMillis,
            endMillis = cycle.endMillis,
            currentExpenses = cycleExpenses,
            previousStartMillis = prevCycle.startMillis,
            previousEndMillis = prevCycle.endMillis,
            previousExpenses = prevExpenses
        )

        val categoryBreakdown = computeCategoryBreakdownFromExpenses(
            expenses = cycleExpenses,
            catMap = catMap,
            totalExpenseCents = periodExpense
        )

        val spentByCategory = HashMap<Long, Long>()
        for (exp in cycleExpenses) {
            val cid = exp.entity.categoryId
            spentByCategory[cid] = (spentByCategory[cid] ?: 0L) + exp.entity.amountCents
        }
        val categoryCards = buildCategoryBudgetCards(
            catBudgets = catBudgets,
            catMap = catMap,
            spentByCategory = spentByCategory,
            daysRemaining = cycle.daysRemaining
        )
        val overallPercent = MoneyMath.percentage(periodExpense, overallCap)
        val alerts = buildBudgetAlertsFromParts(
            yearMonth = selectedYearMonth,
            overallCap = overallCap,
            overallSpent = periodExpense,
            overallPercentUsed = overallPercent,
            categoryBudgets = categoryCards,
            currency = settings.currencyOption
        )

        return DashboardUiState(
            profile = profile,
            totalNetBalanceCents = totalNetBalance,
            periodIncomeCents = periodIncome,
            periodExpenseCents = periodExpense,
            availableToSpendCents = availableToSpend,
            monthlyBudgetCapCents = overallCap,
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
        val catMap = HashMap<Long, CategoryEntity>(categories.size * 2)
        for (cat in categories) catMap[cat.id] = cat

        val spentByCategory = HashMap<Long, Long>()
        var overallSpent = 0L

        val allYearMonths = (budgets.map { it.yearMonth } + selectedYearMonth).distinct().sortedDescending()
        val ymCycles = allYearMonths.map { DateUtils.getCycleRange(it, settings.cycleStartDay, nowMillis) }
        val ymExpenseSums = LongArray(allYearMonths.size)

        for (item in items) {
            val tx = item.entity
            if (tx.type != TransactionType.EXPENSE) continue
            val ts = tx.timestamp
            val amt = tx.amountCents

            if (ts in cycle.startMillis..cycle.endMillis) {
                overallSpent += amt
                spentByCategory[tx.categoryId] = (spentByCategory[tx.categoryId] ?: 0L) + amt
            }

            for (i in ymCycles.indices) {
                val c = ymCycles[i]
                if (ts in c.startMillis..c.endMillis) {
                    ymExpenseSums[i] += amt
                }
            }
        }

        val budgetsByMonth = budgets.groupBy { it.yearMonth }
        val monthBudgets = budgetsByMonth[selectedYearMonth].orEmpty()
        val overallEntity = monthBudgets.firstOrNull { it.categoryId == null }
        val catBudgets = monthBudgets.filter { it.categoryId != null }

        val categoryCards = buildCategoryBudgetCards(
            catBudgets = catBudgets,
            catMap = catMap,
            spentByCategory = spentByCategory,
            daysRemaining = cycle.daysRemaining
        )

        val overallCap = overallEntity?.limitCents ?: catBudgets.sumOf { it.limitCents }
        val overallRemaining = overallCap - overallSpent
        val overallPercent = MoneyMath.percentage(overallSpent, overallCap)
        val safePace = MoneyMath.safeDailyPace(overallRemaining.coerceAtLeast(0L), cycle.daysRemaining)
        val safePaceBenchmarkPercent = ((cycle.daysElapsed.toFloat() / cycle.totalDays.toFloat()) * 100f)
            .toInt().coerceIn(5, 95)

        val history = allYearMonths.mapIndexed { idx, ym ->
            val ymBudgets = budgetsByMonth[ym].orEmpty()
            val ymCap = ymBudgets.firstOrNull { it.categoryId == null }?.limitCents
                ?: ymBudgets.filter { it.categoryId != null }.sumOf { it.limitCents }
            val ymExpenses = ymExpenseSums[idx]
            MonthlyBudgetHistoryItem(
                yearMonth = ym,
                monthLabel = DateUtils.formatMonthYear(ym),
                capCents = ymCap,
                spentCents = ymExpenses,
                percentUsed = MoneyMath.percentage(ymExpenses, ymCap)
            )
        }

        val usedCategoryIds = catBudgets.mapNotNullTo(HashSet()) { it.categoryId }
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

    private fun buildCategoryBudgetCards(
        catBudgets: List<BudgetEntity>,
        catMap: Map<Long, CategoryEntity>,
        spentByCategory: Map<Long, Long>,
        daysRemaining: Int
    ): List<BudgetCardUi> {
        return catBudgets.mapNotNull { budget ->
            val category = catMap[budget.categoryId] ?: return@mapNotNull null
            val spent = spentByCategory[category.id] ?: 0L
            val remaining = budget.limitCents - spent
            val pct = MoneyMath.percentage(spent, budget.limitCents)
            val isOver = spent > budget.limitCents
            val isNear = !isOver && pct >= budget.alertThresholdPercent
            val pace = MoneyMath.safeDailyPace(remaining.coerceAtLeast(0L), daysRemaining)
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

        val monthYms = Array(6) { idx ->
            DateUtils.shiftYearMonthKey(selectedYearMonth, -(5 - idx))
        }
        val monthCycles = Array(6) { idx ->
            DateUtils.getCycleRange(monthYms[idx], settings.cycleStartDay, nowMillis)
        }
        val monthIncomes = LongArray(6)
        val monthExpenses = LongArray(6)

        val periodItems = ArrayList<TransactionItem>()
        val periodExpenses = ArrayList<TransactionItem>()
        val prevExpenses = ArrayList<TransactionItem>()
        val distinctDays = HashSet<Long>()

        var inflow = 0L
        var outflow = 0L
        var prevInflow = 0L
        var prevOutflow = 0L
        var largestExpense: TransactionItem? = null
        val prevExpenseByCategory = HashMap<Long, Long>()

        for (item in items) {
            val tx = item.entity
            val ts = tx.timestamp
            val amt = tx.amountCents
            val type = tx.type

            if (ts in startMillis..endMillis) {
                periodItems.add(item)
                distinctDays.add(DateUtils.startOfDay(ts))
                when (type) {
                    TransactionType.INCOME -> inflow += amt
                    TransactionType.EXPENSE -> {
                        outflow += amt
                        periodExpenses.add(item)
                        if (largestExpense == null || amt > largestExpense.entity.amountCents) {
                            largestExpense = item
                        }
                    }
                    TransactionType.TRANSFER -> Unit
                }
            } else if (ts in prevStart..prevEnd) {
                when (type) {
                    TransactionType.INCOME -> prevInflow += amt
                    TransactionType.EXPENSE -> {
                        prevOutflow += amt
                        prevExpenses.add(item)
                        prevExpenseByCategory[tx.categoryId] = (prevExpenseByCategory[tx.categoryId] ?: 0L) + amt
                    }
                    TransactionType.TRANSFER -> Unit
                }
            }

            if (type == TransactionType.INCOME || type == TransactionType.EXPENSE) {
                for (m in 0 until 6) {
                    val range = monthCycles[m]
                    if (ts in range.startMillis..range.endMillis) {
                        if (type == TransactionType.INCOME) monthIncomes[m] += amt
                        else monthExpenses[m] += amt
                    }
                }
            }
        }

        val netCashFlow = inflow - outflow
        val savingsRate = if (inflow > 0L && netCashFlow > 0L) MoneyMath.percentage(netCashFlow, inflow) else 0
        val totalDaysInRange = (((endMillis - startMillis) / 86_400_000L) + 1).toInt().coerceAtLeast(1)
        val distinctDaysLogged = distinctDays.size
        val activeDays = distinctDaysLogged.coerceAtLeast(1)
        val avgDailyBurn = outflow / activeDays

        var overallCapFromMonth: Long? = null
        var catCapSum = 0L
        for (b in budgets) {
            if (b.yearMonth == selectedYearMonth) {
                if (b.categoryId == null) overallCapFromMonth = b.limitCents
                else catCapSum += b.limitCents
            }
        }
        val budgetCap = overallCapFromMonth ?: catCapSum

        val dailyPoints = buildDailySeries(
            startMillis = startMillis,
            endMillis = endMillis,
            currentExpenses = periodExpenses,
            previousStartMillis = prevStart,
            previousEndMillis = prevEnd,
            previousExpenses = prevExpenses
        )

        val catMap = HashMap<Long, CategoryEntity>(categories.size * 2)
        for (cat in categories) catMap[cat.id] = cat
        val categoryDistribution = computeCategoryBreakdownFromExpenses(
            expenses = periodExpenses,
            catMap = catMap,
            totalExpenseCents = outflow
        )

        val monthlyComparisons = List(6) { idx ->
            MonthlyComparisonBar(
                label = DateUtils.formatShortMonth(monthYms[idx]),
                incomeCents = monthIncomes[idx],
                expenseCents = monthExpenses[idx]
            )
        }

        val insights = buildVaultInsights(
            largestExpense = largestExpense,
            prevExpenseByCategory = prevExpenseByCategory,
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

    private fun computeCategoryBreakdownFromExpenses(
        expenses: List<TransactionItem>,
        catMap: Map<Long, CategoryEntity>,
        totalExpenseCents: Long
    ): List<CategorySpendSummary> {
        if (expenses.isEmpty()) return emptyList()

        return expenses.groupBy { it.entity.categoryId }
            .mapNotNull { (catId, list) ->
                val cat = catMap[catId] ?: return@mapNotNull null
                var total = 0L
                val merchantSet = LinkedHashSet<String>(4)
                for (item in list) {
                    total += item.entity.amountCents
                    val m = item.entity.merchant
                    if (m.isNotBlank() && merchantSet.size < 3) {
                        merchantSet.add(m)
                    }
                }
                val merchants = if (merchantSet.isEmpty()) cat.subtitle else merchantSet.joinToString(", ")
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
        val prevSpanMs = (previousEndMillis - previousStartMillis + 1).coerceAtLeast(1L) / bucketCount

        val daySums = LongArray(bucketCount)
        val peakAmounts = LongArray(bucketCount)
        val peakMerchants = arrayOfNulls<String>(bucketCount)

        for (item in currentExpenses) {
            val ts = item.entity.timestamp
            if (ts < startMillis || ts > endMillis) continue
            val idx = ((ts - startMillis) / bucketSpanMs).toInt().coerceIn(0, bucketCount - 1)
            val amt = item.entity.amountCents
            daySums[idx] += amt
            if (amt >= peakAmounts[idx]) {
                peakAmounts[idx] = amt
                peakMerchants[idx] = item.entity.merchant.ifBlank { item.category?.name ?: "" }
            }
        }

        val prevSums = LongArray(bucketCount)
        var prevTotal = 0L
        for (item in previousExpenses) {
            val ts = item.entity.timestamp
            val amt = item.entity.amountCents
            prevTotal += amt
            if (ts in previousStartMillis..previousEndMillis) {
                val idx = ((ts - previousStartMillis) / prevSpanMs).toInt().coerceIn(0, bucketCount - 1)
                prevSums[idx] += amt
            }
        }
        val prevBucketAvg = prevTotal / bucketCount

        val points = ArrayList<DailyChartPoint>(bucketCount)
        var runningCumulative = 0L
        for (i in 0 until bucketCount) {
            val bStart = startMillis + (i * bucketSpanMs)
            val daySum = daySums[i]
            runningCumulative += daySum
            val prevDaySum = prevSums[i]
            points.add(
                DailyChartPoint(
                    dayLabel = DateUtils.formatMonthDay(bStart),
                    timestampMillis = bStart,
                    amountCents = daySum,
                    cumulativeCents = runningCumulative,
                    benchmarkCents = if (prevDaySum > 0L) prevDaySum else prevBucketAvg,
                    topMerchant = peakMerchants[i].orEmpty()
                )
            )
        }
        return points
    }

    private fun buildBudgetAlertsFromParts(
        yearMonth: String,
        overallCap: Long,
        overallSpent: Long,
        overallPercentUsed: Int,
        categoryBudgets: List<BudgetCardUi>,
        currency: CurrencyDisplayOption
    ): List<BudgetAlertNotification> {
        val alerts = mutableListOf<BudgetAlertNotification>()
        if (overallCap > 0L && overallPercentUsed >= 80) {
            val isOver = overallSpent > overallCap
            alerts.add(
                BudgetAlertNotification(
                    id = "overall_$yearMonth",
                    title = if (isOver) "Monthly Budget Exceeded" else "Monthly Cap Approaching",
                    message = "You have spent ${MoneyMath.formatAmount(overallSpent, currency)} of your ${MoneyMath.formatAmount(overallCap, currency)} limit ($overallPercentUsed%).",
                    categoryName = "Total Monthly Budget",
                    percentUsed = overallPercentUsed,
                    isOverspent = isOver
                )
            )
        }
        categoryBudgets
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
        largestExpense: TransactionItem?,
        prevExpenseByCategory: Map<Long, Long>,
        inflow: Long,
        outflow: Long,
        prevOutflow: Long,
        categoryDistribution: List<CategorySpendSummary>,
        currency: CurrencyDisplayOption
    ): List<VaultInsight> {
        val insights = mutableListOf<VaultInsight>()

        val topCategory = categoryDistribution.firstOrNull()
        if (topCategory != null) {
            val prevCatSpend = prevExpenseByCategory[topCategory.category.id] ?: 0L
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
