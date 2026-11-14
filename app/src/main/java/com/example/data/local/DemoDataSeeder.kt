package com.example.data.local

import com.example.data.model.TransactionType
import com.example.util.DateUtils
import java.util.Calendar

/**
 * Optional, explicitly opt-in Sri Lankan demo dataset.
 * Every inserted transaction is tagged `isDemo = true` so users can remove sample records
 * cleanly with one tap at any time without disturbing their own entries.
 */
object DemoDataSeeder {
    suspend fun seedDemoData(db: LakExpenseDatabase, nowMillis: Long = System.currentTimeMillis()) {
        val categories = db.categoryDao().getAll()
        val methods = db.paymentMethodDao().getAll()
        if (categories.isEmpty() || methods.isEmpty()) return

        fun catId(name: String, type: TransactionType): Long =
            categories.firstOrNull { it.name == name && it.type == type }?.id
                ?: categories.firstOrNull { it.type == type }?.id
                ?: categories.first().id

        fun methodId(name: String): Long =
            methods.firstOrNull { it.name == name }?.id ?: methods.first().id

        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        fun daysAgo(days: Int, hour: Int, minute: Int): Long {
            return (cal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, -days)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        val currentYm = DateUtils.currentYearMonthKey(nowMillis)
        val prevYm = DateUtils.shiftYearMonthKey(currentYm, -1)

        val demoTransactions = listOf(
            // Current month transactions
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountCents = 425_000L, // Rs. 4,250.00
                categoryId = catId("Groceries", TransactionType.EXPENSE),
                paymentMethodId = methodId("Cash"),
                merchant = "Keells Super Colombo 03",
                note = "Weekly fresh produce & pantry stock",
                timestamp = daysAgo(0, 16, 15),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountCents = 289_000L, // Rs. 2,890.00
                categoryId = catId("CEB / Water", TransactionType.EXPENSE),
                paymentMethodId = methodId("Sampath Bank"),
                merchant = "Dialog Axiata PLC",
                note = "Home Fibre & mobile post-paid bill",
                timestamp = daysAgo(0, 11, 30),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountCents = 85_000L, // Rs. 850.00
                categoryId = catId("Fuel & Ride", TransactionType.EXPENSE),
                paymentMethodId = methodId("FriMi / Genie"),
                merchant = "PickMe Ride",
                note = "Galle Rd → Bambalapitiya",
                timestamp = daysAgo(1, 18, 15),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.INCOME,
                amountCents = 4_500_000L, // Rs. 45,000.00
                categoryId = catId("Freelance", TransactionType.INCOME),
                paymentMethodId = methodId("Sampath Bank"),
                merchant = "Client UX Retainer",
                note = "Direct bank transfer",
                timestamp = daysAgo(1, 14, 0),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountCents = 500_000L, // Rs. 5,000.00
                categoryId = catId("Fuel & Ride", TransactionType.EXPENSE),
                paymentMethodId = methodId("Commercial Bank"),
                merchant = "Ceypetco Fuel Station",
                note = "92 Octane full tank",
                timestamp = daysAgo(3, 8, 30),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountCents = 890_000L, // Rs. 8,900.00
                categoryId = catId("Shopping", TransactionType.EXPENSE),
                paymentMethodId = methodId("Credit Card"),
                merchant = "Odel Colombo",
                note = "Apparel & essentials",
                timestamp = daysAgo(3, 19, 45),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountCents = 645_000L, // Rs. 6,450.00
                categoryId = catId("CEB / Water", TransactionType.EXPENSE),
                paymentMethodId = methodId("FriMi / Genie"),
                merchant = "CEB Electricity Bill",
                note = "Monthly domestic electricity settlement",
                timestamp = daysAgo(5, 10, 15),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountCents = 485_000L, // Rs. 4,850.00
                categoryId = catId("Food & Dining", TransactionType.EXPENSE),
                paymentMethodId = methodId("Commercial Bank"),
                merchant = "Dutch Hospital Colombo",
                note = "Dinner with family",
                timestamp = daysAgo(6, 20, 10),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountCents = 1_840_000L, // Rs. 18,400.00
                categoryId = catId("Groceries", TransactionType.EXPENSE),
                paymentMethodId = methodId("Commercial Bank"),
                merchant = "Cargills Food City",
                note = "Monthly household bulk groceries",
                timestamp = daysAgo(9, 17, 20),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.INCOME,
                amountCents = 18_500_000L, // Rs. 185,000.00
                categoryId = catId("Salary", TransactionType.INCOME),
                paymentMethodId = methodId("Commercial Bank"),
                merchant = "Monthly Salary Credit",
                note = "Primary payroll deposit",
                timestamp = daysAgo(12, 9, 0),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountCents = 430_000L, // Rs. 4,300.00
                categoryId = catId("Medical", TransactionType.EXPENSE),
                paymentMethodId = methodId("Cash"),
                merchant = "Union Chemist Pharmacy",
                note = "Vitamins & prescription",
                timestamp = daysAgo(14, 15, 40),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.TRANSFER,
                amountCents = 2_000_000L, // Rs. 20,000.00
                categoryId = catId("Account Transfer", TransactionType.TRANSFER),
                paymentMethodId = methodId("Commercial Bank"),
                toPaymentMethodId = methodId("Cash"),
                merchant = "ATM Cash Withdrawal",
                note = "Monthly wallet top-up",
                timestamp = daysAgo(11, 12, 10),
                isDemo = true
            ),
            // Previous month transactions for real benchmark comparison
            TransactionEntity(
                type = TransactionType.INCOME,
                amountCents = 18_500_000L,
                categoryId = catId("Salary", TransactionType.INCOME),
                paymentMethodId = methodId("Commercial Bank"),
                merchant = "Monthly Salary Credit",
                note = "Previous cycle payroll",
                timestamp = daysAgo(35, 9, 0),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountCents = 4_950_000L,
                categoryId = catId("Groceries", TransactionType.EXPENSE),
                paymentMethodId = methodId("Commercial Bank"),
                merchant = "Keells & Cargills",
                note = "Previous month groceries",
                timestamp = daysAgo(33, 18, 0),
                isDemo = true
            ),
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amountCents = 1_120_000L,
                categoryId = catId("CEB / Water", TransactionType.EXPENSE),
                paymentMethodId = methodId("FriMi / Genie"),
                merchant = "CEB & Water Board",
                note = "Previous month utilities",
                timestamp = daysAgo(31, 11, 0),
                isDemo = true
            )
        )
        db.transactionDao().insertAll(demoTransactions)

        // Seed realistic budgets for current & previous month if empty
        if (db.budgetDao().getForMonth(currentYm).isEmpty()) {
            val budgets = listOf(
                BudgetEntity(yearMonth = currentYm, categoryId = null, limitCents = 15_000_000L, alertThresholdPercent = 80),
                BudgetEntity(yearMonth = currentYm, categoryId = catId("Groceries", TransactionType.EXPENSE), limitCents = 3_500_000L, alertThresholdPercent = 80),
                BudgetEntity(yearMonth = currentYm, categoryId = catId("Food & Dining", TransactionType.EXPENSE), limitCents = 2_000_000L, alertThresholdPercent = 80),
                BudgetEntity(yearMonth = currentYm, categoryId = catId("CEB / Water", TransactionType.EXPENSE), limitCents = 1_500_000L, alertThresholdPercent = 85),
                BudgetEntity(yearMonth = currentYm, categoryId = catId("Fuel & Ride", TransactionType.EXPENSE), limitCents = 2_000_000L, alertThresholdPercent = 80),
                BudgetEntity(yearMonth = prevYm, categoryId = null, limitCents = 14_000_000L, alertThresholdPercent = 80)
            )
            db.budgetDao().insertAll(budgets)
        }
    }
}
