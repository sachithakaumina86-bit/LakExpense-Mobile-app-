package com.example.data.model

import androidx.compose.runtime.Immutable
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.PaymentMethodEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserProfileEntity

@Immutable
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val currencyOption: CurrencyDisplayOption = CurrencyDisplayOption.RS_PREFIX,
    val dateFormat: DateFormatOption = DateFormatOption.DD_MM_YYYY,
    val cycleStartDay: Int = 1,
    val firstDayOfWeek: Int = java.util.Calendar.MONDAY,
    val pinEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val autoLockOption: AutoLockOption = AutoLockOption.IMMEDIATE,
    val pinUpdatedAt: Long = 0L,
    val hapticsEnabled: Boolean = true,
    val budgetNotificationsEnabled: Boolean = true,
    val defaultAlertThresholdPercent: Int = 80,
    val balanceHidden: Boolean = false,
    val lastBackupTimestamp: Long = 0L
)

@Immutable
data class TransactionItem(
    val entity: TransactionEntity,
    val category: CategoryEntity?,
    val paymentMethod: PaymentMethodEntity?,
    val toPaymentMethod: PaymentMethodEntity?
)

@Immutable
data class TransactionDayGroup(
    val dayStartMillis: Long,
    val dayPrefix: String,
    val dayDateStr: String,
    val dayNetCents: Long,
    val items: List<TransactionItem>
)

@Immutable
data class TransactionsListUiState(
    val totalSpentCents: Long = 0L,
    val totalEarnedCents: Long = 0L,
    val netCashCents: Long = 0L,
    val totalCount: Int = 0,
    val dayGroups: List<TransactionDayGroup> = emptyList()
)

@Immutable
data class CategorySpendSummary(
    val category: CategoryEntity,
    val totalCents: Long,
    val percentage: Int,
    val transactionCount: Int,
    val sampleMerchants: String
)

@Immutable
data class BudgetCardUi(
    val budget: BudgetEntity,
    val category: CategoryEntity,
    val spentCents: Long,
    val remainingCents: Long,
    val progressFraction: Float,
    val percentUsed: Int,
    val safeDailyPaceCents: Long,
    val isNearLimit: Boolean,
    val isOverspent: Boolean
)

@Immutable
data class MonthlyBudgetHistoryItem(
    val yearMonth: String,
    val monthLabel: String,
    val capCents: Long,
    val spentCents: Long,
    val percentUsed: Int
)

@Immutable
data class DailyChartPoint(
    val dayLabel: String,
    val timestampMillis: Long,
    val amountCents: Long,
    val cumulativeCents: Long,
    val benchmarkCents: Long,
    val topMerchant: String
)

@Immutable
data class MonthlyComparisonBar(
    val label: String,
    val incomeCents: Long,
    val expenseCents: Long
)

@Immutable
data class VaultInsight(
    val id: String,
    val title: String,
    val badgeText: String,
    val description: String,
    val isPositive: Boolean
)

@Immutable
data class BudgetAlertNotification(
    val id: String,
    val title: String,
    val message: String,
    val categoryName: String,
    val percentUsed: Int,
    val isOverspent: Boolean
)

@Immutable
data class VaultStats(
    val transactionCount: Int = 0,
    val budgetCount: Int = 0,
    val receiptCount: Int = 0,
    val databaseBytes: Long = 0L,
    val hasDemoData: Boolean = false
)

@Immutable
data class TransactionFilterState(
    val query: String = "",
    val typeFilter: TransactionType? = null,
    val useSelectedMonth: Boolean = true,
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedPaymentMethodIds: Set<Long> = emptySet(),
    val customStartMillis: Long? = null,
    val customEndMillis: Long? = null
)

@Immutable
data class DashboardUiState(
    val profile: UserProfileEntity? = null,
    val totalNetBalanceCents: Long = 0L,
    val periodIncomeCents: Long = 0L,
    val periodExpenseCents: Long = 0L,
    val availableToSpendCents: Long = 0L,
    val monthlyBudgetCapCents: Long = 0L,
    val expenseDeltaPercentVsPrevMonth: Int? = null,
    val dailyVelocityPoints: List<DailyChartPoint> = emptyList(),
    val categoryBreakdown: List<CategorySpendSummary> = emptyList(),
    val recentTransactions: List<TransactionItem> = emptyList(),
    val alerts: List<BudgetAlertNotification> = emptyList(),
    val cycleStartMillis: Long = 0L,
    val cycleEndMillis: Long = 0L
)

@Immutable
data class BudgetsUiState(
    val yearMonth: String = "",
    val cycleLabel: String = "",
    val daysRemainingInCycle: Int = 1,
    val cycleEndsText: String = "",
    val overallBudget: BudgetEntity? = null,
    val overallCapCents: Long = 0L,
    val overallSpentCents: Long = 0L,
    val overallRemainingCents: Long = 0L,
    val overallPercentUsed: Int = 0,
    val safeDailyPaceCents: Long = 0L,
    val safePaceBenchmarkPercent: Int = 70,
    val categoryBudgets: List<BudgetCardUi> = emptyList(),
    val nearLimitCount: Int = 0,
    val overspentCount: Int = 0,
    val activeFilter: BudgetFilter = BudgetFilter.ALL,
    val history: List<MonthlyBudgetHistoryItem> = emptyList(),
    val availableCategories: List<CategoryEntity> = emptyList()
)

@Immutable
data class ReportsUiState(
    val period: ReportPeriod = ReportPeriod.MONTHLY,
    val rangeLabel: String = "",
    val daysLogged: Int = 0,
    val totalDaysInRange: Int = 30,
    val totalInflowCents: Long = 0L,
    val totalOutflowCents: Long = 0L,
    val netCashFlowCents: Long = 0L,
    val savingsRatePercent: Int = 0,
    val avgDailyBurnCents: Long = 0L,
    val budgetCapCents: Long = 0L,
    val inflowDeltaVsPrevPercent: Int? = null,
    val outflowDeltaVsBudgetPercent: Int? = null,
    val dailyPoints: List<DailyChartPoint> = emptyList(),
    val categoryDistribution: List<CategorySpendSummary> = emptyList(),
    val monthlyComparisons: List<MonthlyComparisonBar> = emptyList(),
    val insights: List<VaultInsight> = emptyList(),
    val filteredTransactions: List<TransactionItem> = emptyList(),
    val customStartMillis: Long? = null,
    val customEndMillis: Long? = null
)
