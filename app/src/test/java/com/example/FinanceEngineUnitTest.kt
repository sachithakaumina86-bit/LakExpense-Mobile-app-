package com.example

import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.PaymentMethodEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.AppSettings
import com.example.data.model.BudgetFilter
import com.example.data.model.CurrencyDisplayOption
import com.example.data.model.ReportPeriod
import com.example.data.model.TransactionType
import com.example.domain.FinanceEngine
import com.example.util.DateUtils
import com.example.util.MoneyMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class FinanceEngineUnitTest {

    @Test
    fun `MoneyMath BigDecimal formatting and parsing are exact`() {
        assertEquals(485_000L, MoneyMath.parseToCents("4850"))
        assertEquals(485_050L, MoneyMath.parseToCents("Rs. 4,850.50"))
        assertNull(MoneyMath.parseToCents("-100"))
        assertEquals("Rs. 124,500.00", MoneyMath.formatAmount(12_450_000L, CurrencyDisplayOption.RS_PREFIX))
        assertEquals("+LKR 45,000.00", MoneyMath.formatAmount(4_500_000L, CurrencyDisplayOption.LKR_PREFIX, showSign = true))
        assertEquals(7_413_00L, MoneyMath.safeDailyPace(81_550_00L, 11) / 100L * 100L) // floor check
    }

    @Test
    fun `DateUtils cycle range respects custom start day`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 20, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cycle = DateUtils.getCycleRange("2026-10", cycleStartDay = 15, nowMillis = cal.timeInMillis)
        val startCal = Calendar.getInstance().apply { timeInMillis = cycle.startMillis }
        assertEquals(15, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.OCTOBER, startCal.get(Calendar.MONTH))
    }

    @Test
    fun `FinanceEngine excludes transfers from income expense and budgets`() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 20, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val profile = UserProfileEntity(
            fullName = "Kasun Kalhara",
            email = "kasun@email.lk",
            location = "Colombo",
            openingBalanceCents = 10_000_000L // Rs. 100,000
        )
        val foodCat = CategoryEntity(id = 1L, name = "Groceries", subtitle = "Keells", iconKey = "shopping_cart", colorHex = "#10B981", type = TransactionType.EXPENSE)
        val salaryCat = CategoryEntity(id = 2L, name = "Salary", subtitle = "Payroll", iconKey = "payments", colorHex = "#34D399", type = TransactionType.INCOME)
        val transferCat = CategoryEntity(id = 3L, name = "Transfer", subtitle = "Accounts", iconKey = "swap_horiz", colorHex = "#38BDF8", type = TransactionType.TRANSFER)

        val cash = PaymentMethodEntity(id = 1L, name = "Cash", iconKey = "payments")
        val bank = PaymentMethodEntity(id = 2L, name = "Commercial Bank", iconKey = "account_balance")

        val txs = listOf(
            TransactionEntity(id = 1L, type = TransactionType.INCOME, amountCents = 18_500_000L, categoryId = 2L, paymentMethodId = 2L, merchant = "Salary", note = "", timestamp = now),
            TransactionEntity(id = 2L, type = TransactionType.EXPENSE, amountCents = 4_250_000L, categoryId = 1L, paymentMethodId = 1L, merchant = "Keells", note = "", timestamp = now),
            TransactionEntity(id = 3L, type = TransactionType.TRANSFER, amountCents = 5_000_000L, categoryId = 3L, paymentMethodId = 2L, toPaymentMethodId = 1L, merchant = "ATM", note = "", timestamp = now)
        )
        val items = FinanceEngine.mapTransactionItems(txs, listOf(foodCat, salaryCat, transferCat), listOf(cash, bank))
        val budgets = listOf(
            BudgetEntity(id = 1L, yearMonth = "2026-10", categoryId = null, limitCents = 10_000_000L, alertThresholdPercent = 80),
            BudgetEntity(id = 2L, yearMonth = "2026-10", categoryId = 1L, limitCents = 5_000_000L, alertThresholdPercent = 80)
        )
        val settings = AppSettings()

        val dash = FinanceEngine.computeDashboard(profile, items, listOf(foodCat, salaryCat, transferCat), budgets, settings, "2026-10", now)
        assertEquals(24_250_000L, dash.totalNetBalanceCents) // 100k + 185k - 42.5k
        assertEquals(18_500_000L, dash.periodIncomeCents)
        assertEquals(4_250_000L, dash.periodExpenseCents)

        val budgetState = FinanceEngine.computeBudgets(items, listOf(foodCat, salaryCat, transferCat), budgets, settings, "2026-10", BudgetFilter.ALL, now)
        assertEquals(4_250_000L, budgetState.overallSpentCents)
        assertEquals(5_750_000L, budgetState.overallRemainingCents)
        assertEquals(1, budgetState.nearLimitCount) // 42.5k / 50k = 85% >= 80%

        val reports = FinanceEngine.computeReports(items, listOf(foodCat, salaryCat, transferCat), budgets, settings, "2026-10", ReportPeriod.MONTHLY, null, null, now)
        assertEquals(14_250_000L, reports.netCashFlowCents)
        assertEquals(77, reports.savingsRatePercent)
    }

    @Test
    fun `MoneyMath formatIntegerGroup matches US grouping`() {
        assertEquals("0", MoneyMath.formatIntegerGroup(0L))
        assertEquals("4,850", MoneyMath.formatIntegerGroup(4_850L))
        assertEquals("1,250,000", MoneyMath.formatIntegerGroup(1_250_000L))
    }

    @Test
    fun `FinanceEngine groupTransactionsByDay orders descending and excludes transfers from day net`() {
        val day1Cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 20, 14, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val day2Cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 19, 9, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val catExp = CategoryEntity(id = 1L, name = "Food", subtitle = "", iconKey = "restaurant", colorHex = "#10B981", type = TransactionType.EXPENSE)
        val catInc = CategoryEntity(id = 2L, name = "Salary", subtitle = "", iconKey = "payments", colorHex = "#34D399", type = TransactionType.INCOME)
        val catTrf = CategoryEntity(id = 3L, name = "Transfer", subtitle = "", iconKey = "swap_horiz", colorHex = "#38BDF8", type = TransactionType.TRANSFER)
        val cash = PaymentMethodEntity(id = 1L, name = "Cash", iconKey = "payments")

        val txs = listOf(
            TransactionEntity(id = 1L, type = TransactionType.EXPENSE, amountCents = 150_000L, categoryId = 1L, paymentMethodId = 1L, merchant = "Cafe", note = "", timestamp = day1Cal.timeInMillis),
            TransactionEntity(id = 2L, type = TransactionType.TRANSFER, amountCents = 500_000L, categoryId = 3L, paymentMethodId = 1L, toPaymentMethodId = 1L, merchant = "ATM", note = "", timestamp = day1Cal.timeInMillis - 1000L),
            TransactionEntity(id = 3L, type = TransactionType.INCOME, amountCents = 2_000_000L, categoryId = 2L, paymentMethodId = 1L, merchant = "Freelance", note = "", timestamp = day2Cal.timeInMillis),
            TransactionEntity(id = 4L, type = TransactionType.EXPENSE, amountCents = 50_000L, categoryId = 1L, paymentMethodId = 1L, merchant = "Bus", note = "", timestamp = day2Cal.timeInMillis - 1000L)
        )
        val items = FinanceEngine.mapTransactionItems(txs, listOf(catExp, catInc, catTrf), listOf(cash))
        val grouped = FinanceEngine.groupTransactionsByDay(items, nowMillis = day1Cal.timeInMillis)

        assertEquals(4, grouped.totalCount)
        assertEquals(200_000L, grouped.totalSpentCents)
        assertEquals(2_000_000L, grouped.totalEarnedCents)
        assertEquals(1_800_000L, grouped.netCashCents)
        assertEquals(2, grouped.dayGroups.size)

        // Most recent day first ("Today")
        assertEquals("Today", grouped.dayGroups[0].dayPrefix)
        assertEquals(-150_000L, grouped.dayGroups[0].dayNetCents)
        assertEquals(2, grouped.dayGroups[0].items.size)

        // Older day second ("Yesterday")
        assertEquals("Yesterday", grouped.dayGroups[1].dayPrefix)
        assertEquals(1_950_000L, grouped.dayGroups[1].dayNetCents)
        assertEquals(2, grouped.dayGroups[1].items.size)
    }

    @Test
    fun `FinanceEngine single-pass aggregations remain exact across 1000 transactions`() {
        val nowCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 20, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = nowCal.timeInMillis
        val cat1 = CategoryEntity(id = 1L, name = "Groceries", subtitle = "", iconKey = "shopping_cart", colorHex = "#10B981", type = TransactionType.EXPENSE)
        val cat2 = CategoryEntity(id = 2L, name = "Utilities", subtitle = "", iconKey = "bolt", colorHex = "#38BDF8", type = TransactionType.EXPENSE)
        val catInc = CategoryEntity(id = 3L, name = "Salary", subtitle = "", iconKey = "payments", colorHex = "#34D399", type = TransactionType.INCOME)
        val cash = PaymentMethodEntity(id = 1L, name = "Cash", iconKey = "payments")

        val txs = (0 until 1000).map { idx ->
            val dayOffset = idx % 20
            val ts = now - dayOffset * 86_400_000L
            when (idx % 10) {
                0 -> TransactionEntity(id = idx.toLong() + 1L, type = TransactionType.INCOME, amountCents = 100_000L, categoryId = 3L, paymentMethodId = 1L, merchant = "Client", note = "", timestamp = ts)
                else -> TransactionEntity(id = idx.toLong() + 1L, type = TransactionType.EXPENSE, amountCents = 10_000L, categoryId = if (idx % 2 == 0) 1L else 2L, paymentMethodId = 1L, merchant = "Shop $idx", note = "", timestamp = ts)
            }
        }
        val items = FinanceEngine.mapTransactionItems(txs, listOf(cat1, cat2, catInc), listOf(cash))
        val budgets = listOf(
            BudgetEntity(id = 1L, yearMonth = "2026-10", categoryId = null, limitCents = 15_000_000L, alertThresholdPercent = 80),
            BudgetEntity(id = 2L, yearMonth = "2026-10", categoryId = 1L, limitCents = 5_000_000L, alertThresholdPercent = 80)
        )
        val settings = AppSettings()

        // 100 income txs * 100,000 = 10,000,000; 900 expense txs * 10,000 = 9,000,000
        val dash = FinanceEngine.computeDashboard(null, items, listOf(cat1, cat2, catInc), budgets, settings, "2026-10", now)
        assertEquals(10_000_000L, dash.periodIncomeCents)
        assertEquals(9_000_000L, dash.periodExpenseCents)
        assertEquals(1_000_000L, dash.totalNetBalanceCents)

        val budgetState = FinanceEngine.computeBudgets(items, listOf(cat1, cat2, catInc), budgets, settings, "2026-10", BudgetFilter.ALL, now)
        assertEquals(9_000_000L, budgetState.overallSpentCents)
        assertEquals(6_000_000L, budgetState.overallRemainingCents)

        val reports = FinanceEngine.computeReports(items, listOf(cat1, cat2, catInc), budgets, settings, "2026-10", ReportPeriod.MONTHLY, null, null, now)
        assertEquals(10_000_000L, reports.totalInflowCents)
        assertEquals(9_000_000L, reports.totalOutflowCents)
        assertEquals(1_000_000L, reports.netCashFlowCents)
    }
}

