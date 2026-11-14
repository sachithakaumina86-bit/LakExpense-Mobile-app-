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
}
