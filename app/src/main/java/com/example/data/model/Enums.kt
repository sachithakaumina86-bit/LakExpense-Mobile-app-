package com.example.data.model

enum class TransactionType(val label: String) {
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer")
}

enum class ThemeMode(val label: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System")
}

enum class DateFormatOption(val pattern: String, val displayLabel: String) {
    DD_MM_YYYY("dd/MM/yyyy", "DD/MM/YYYY"),
    MM_DD_YYYY("MM/dd/yyyy", "MM/DD/YYYY"),
    YYYY_MM_DD("yyyy-MM-dd", "YYYY-MM-DD")
}

enum class ReportPeriod(val label: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    YEARLY("Yearly"),
    CUSTOM("Custom")
}

enum class CurrencyDisplayOption(val symbol: String, val label: String) {
    RS_PREFIX("Rs.", "Sri Lankan Rupee (LKR - Rs.)"),
    LKR_PREFIX("LKR", "Sri Lankan Rupee (LKR)")
}

enum class AutoLockOption(val label: String, val timeoutMs: Long) {
    IMMEDIATE("Immediately upon exit", 0L),
    ONE_MIN("After 1 minute", 60_000L),
    FIVE_MIN("After 5 minutes", 300_000L)
}

enum class BudgetFilter(val label: String) {
    ALL("All Budgets"),
    ACTIVE("Active"),
    NEAR_LIMIT("Near Limit"),
    OVERSPENT("Overspent")
}
