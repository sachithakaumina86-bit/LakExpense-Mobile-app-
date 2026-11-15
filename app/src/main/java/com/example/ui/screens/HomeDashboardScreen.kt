package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.PieChartOutline
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.CategorySpendSummary
import com.example.data.model.DailyChartPoint
import com.example.data.model.DashboardUiState
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconMapper
import com.example.ui.theme.LakTheme
import com.example.util.DateUtils
import com.example.util.MoneyMath

@Composable
fun HomeDashboardScreen(
    state: DashboardUiState,
    settings: AppSettings,
    selectedYearMonth: String,
    hasDemoData: Boolean,
    onOpenMonthPicker: () -> Unit,
    onToggleBalanceVisibility: () -> Unit,
    onQuickTransfer: () -> Unit,
    onQuickBillPay: () -> Unit,
    onNavigateBudgets: () -> Unit,
    onQuickExport: () -> Unit,
    onViewAllTransactions: () -> Unit,
    onTransactionClick: (TransactionItem) -> Unit,
    onClearDemoData: () -> Unit,
    onAddFirstTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LakTheme.colors
    val firstName = remember(state.profile?.fullName) {
        state.profile?.fullName
            ?.trim()
            ?.split(" ")
            ?.firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "Friend"
    }
    val locationText = remember(state.profile?.location) {
        (state.profile?.location?.takeIf { it.isNotBlank() } ?: "Colombo, Sri Lanka").substringBefore(",")
    }
    val monthHeaderText = remember(selectedYearMonth) {
        DateUtils.formatMonthYear(selectedYearMonth)
    }
    val prevMonthShort = remember(selectedYearMonth) {
        DateUtils.formatShortMonth(DateUtils.shiftYearMonthKey(selectedYearMonth, -1))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .testTag("home_dashboard_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Greeting & System Sovereignty Status
            item(key = "greeting", contentType = "greeting") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ayubowan, $firstName \uD83D\uDC4B",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = colors.surfaceContainerHigh,
                            border = BorderStroke(1.dp, colors.cardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Local Storage Active",
                                    tint = colors.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Local Storage Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.primary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onOpenMonthPicker,
                            shape = RoundedCornerShape(10.dp),
                            color = colors.surfaceContainer,
                            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("home_month_selector")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "Month",
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = monthHeaderText,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Select month",
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = "$locationText • Safe Zone",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }

            if (hasDemoData) {
                item(key = "demo_banner", contentType = "demo_banner") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.secondaryContainer.copy(alpha = if (colors.isDark) 0.18f else 0.7f),
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sample Sri Lankan Demo Data Active",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "Marked as demo rows. Clear anytime to start fresh.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = onClearDemoData,
                                modifier = Modifier.testTag("clear_demo_data_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = colors.expense,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear", color = colors.expense)
                            }
                        }
                    }
                }
            }

            // Hero Total Net Balance Card (Faithful to Stitch card)
            item(key = "hero_balance", contentType = "hero_balance") {
                HeroBalanceCard(
                    state = state,
                    settings = settings,
                    onToggleBalanceVisibility = onToggleBalanceVisibility
                )
            }

            // Quick Actions Row
            item(key = "quick_actions", contentType = "quick_actions") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        label = "Transfer",
                        icon = Icons.Outlined.SwapHoriz,
                        iconTint = colors.primary,
                        testTag = "quick_action_transfer",
                        onClick = onQuickTransfer,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        label = "Bill Pay",
                        icon = Icons.Outlined.Receipt,
                        iconTint = colors.tertiary,
                        testTag = "quick_action_bill_pay",
                        onClick = onQuickBillPay,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        label = "Budgets",
                        icon = Icons.Outlined.PieChartOutline,
                        iconTint = colors.secondary,
                        testTag = "quick_action_budgets",
                        onClick = onNavigateBudgets,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        label = "Export",
                        icon = Icons.Outlined.PictureAsPdf,
                        iconTint = colors.onSurfaceVariant,
                        testTag = "quick_action_export",
                        onClick = onQuickExport,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Spending Velocity Card
            item(key = "velocity_chart", contentType = "velocity_chart") {
                SpendingVelocityCard(
                    points = state.dailyVelocityPoints,
                    deltaPercent = state.expenseDeltaPercentVsPrevMonth,
                    prevMonthShort = prevMonthShort,
                    currency = settings.currencyOption
                )
            }

            // Categories Breakdown (2x2 Grid)
            item(key = "categories_breakdown", contentType = "categories_breakdown") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categories breakdown",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.onSurface
                        )
                        val periodCompactText = remember(state.periodExpenseCents) {
                            "${MoneyMath.formatCompact(state.periodExpenseCents)} Total"
                        }
                        Text(
                            text = periodCompactText,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    if (state.categoryBreakdown.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surfaceContainer,
                            border = BorderStroke(1.dp, colors.cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No expenses logged for $monthHeaderText",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap + below to record your first LKR expense.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        val rows = remember(state.categoryBreakdown) {
                            state.categoryBreakdown.take(4).chunked(2)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            rows.forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    pair.forEach { summary ->
                                        CategoryBreakdownTile(
                                            summary = summary,
                                            settings = settings,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Transactions Header
            item(key = "recent_header", contentType = "section_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onViewAllTransactions)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("home_view_all_transactions"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.primary
                        )
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = "View all transactions",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (state.recentTransactions.isEmpty()) {
                item(key = "empty_recent", contentType = "empty_card") {
                    Surface(
                        onClick = onAddFirstTransaction,
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_recent_transactions_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your Ledger Is Ready",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap here or press the + button to log an expense, income, or account transfer.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    items = state.recentTransactions,
                    key = { it.entity.id },
                    contentType = { "tx_row" }
                ) { item ->
                    DashboardTransactionRow(
                        item = item,
                        settings = settings,
                        onClick = { onTransactionClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBalanceCard(
    state: DashboardUiState,
    settings: AppSettings,
    onToggleBalanceVisibility: () -> Unit
) {
    val colors = LakTheme.colors
    val hidden = settings.balanceHidden
    val balanceFormatted = remember(state.totalNetBalanceCents) {
        MoneyMath.formatNumberOnly(state.totalNetBalanceCents, includeDecimals = true)
    }
    val incomeFormatted = remember(state.periodIncomeCents, settings.currencyOption) {
        MoneyMath.formatAmount(
            state.periodIncomeCents,
            settings.currencyOption,
            isPositiveOverride = true
        )
    }
    val expenseFormatted = remember(state.periodExpenseCents, settings.currencyOption) {
        MoneyMath.formatAmount(
            state.periodExpenseCents,
            settings.currencyOption,
            isPositiveOverride = false
        )
    }
    val availableFormatted = remember(state.availableToSpendCents, settings.currencyOption) {
        MoneyMath.formatAmount(state.availableToSpendCents, settings.currencyOption)
    }
    val cardGradient = remember(colors.isDark, colors.surfaceContainerHigh, colors.surfaceContainerLow, colors.surfaceContainerLowest, colors.surfaceContainer, colors.secondaryContainer) {
        if (colors.isDark) {
            Brush.linearGradient(
                listOf(
                    colors.surfaceContainerHigh,
                    colors.surfaceContainerLow,
                    colors.surfaceContainerLowest
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    colors.surfaceContainer,
                    colors.secondaryContainer.copy(alpha = 0.55f)
                )
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.cardBorder),
        shadowElevation = if (colors.isDark) 8.dp else 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(cardGradient)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "TOTAL NET BALANCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                        )
                    }

                    Surface(
                        onClick = onToggleBalanceVisibility,
                        shape = CircleShape,
                        color = colors.surfaceContainerHighest.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("toggle_balance_visibility_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (hidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (hidden) "Show balance" else "Hide balance",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = settings.currencyOption.symbol,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = if (hidden) "••••••••" else balanceFormatted,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 34.sp
                        ),
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceContainerLowest.copy(alpha = if (colors.isDark) 0.8f else 0.95f),
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowDownward,
                                    contentDescription = "Income",
                                    tint = colors.income,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Income",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (hidden) "••••••" else incomeFormatted,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.income,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceContainerLowest.copy(alpha = if (colors.isDark) 0.8f else 0.95f),
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowUpward,
                                    contentDescription = "Expenses",
                                    tint = colors.expense,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Expenses",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (hidden) "••••••" else expenseFormatted,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.expense,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surfaceContainerLowest.copy(alpha = if (colors.isDark) 0.55f else 0.9f),
                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Savings,
                                contentDescription = "Available to spend",
                                tint = colors.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Available to spend",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (hidden) "••••••" else availableFormatted,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LakTheme.colors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SpendingVelocityCard(
    points: List<DailyChartPoint>,
    deltaPercent: Int?,
    prevMonthShort: String,
    currency: com.example.data.model.CurrencyDisplayOption
) {
    val colors = LakTheme.colors
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SPENDING VELOCITY",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        text = "Daily Rhythm",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface
                    )
                }

                val badgeColor = if ((deltaPercent ?: 0) <= 0) colors.primary else colors.expense
                val badgeText = when {
                    deltaPercent == null -> "Active Cycle"
                    deltaPercent > 0 -> "+$deltaPercent% vs $prevMonthShort"
                    else -> "$deltaPercent% vs $prevMonthShort"
                }
                Surface(
                    shape = CircleShape,
                    color = colors.surfaceContainerHigh,
                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if ((deltaPercent ?: 0) <= 0) {
                                Icons.AutoMirrored.Outlined.TrendingDown
                            } else {
                                Icons.AutoMirrored.Outlined.TrendingUp
                            },
                            contentDescription = badgeText,
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = badgeColor
                        )
                    }
                }
            }

            val chartSummary = remember(points) {
                var max = 1L
                var anySpend = false
                var peakIdx = 0
                var peakAmt = -1L
                points.forEachIndexed { idx, pt ->
                    if (pt.amountCents > 0L) anySpend = true
                    if (pt.amountCents > max) max = pt.amountCents
                    if (pt.amountCents > peakAmt) {
                        peakAmt = pt.amountCents
                        peakIdx = idx
                    }
                }
                Triple(max, anySpend, peakIdx)
            }
            val maxVal = chartSummary.first
            val hasSpend = chartSummary.second
            val peakIndex = chartSummary.third
            val peakPoint = points.getOrNull(peakIndex)
            val peakBadgeText = remember(peakPoint, currency) {
                if (peakPoint != null && peakPoint.amountCents > 0L) {
                    "Peak ${peakPoint.dayLabel}: ${MoneyMath.formatAmount(peakPoint.amountCents, currency, includeDecimals = false)}"
                } else null
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            val w = size.width
                            val h = size.height
                            val topPad = 14f
                            val bottomPad = 12f
                            val usableH = (h - topPad - bottomPad).coerceAtLeast(10f)
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            val gridY1 = topPad + usableH * 0.25f
                            val gridY2 = topPad + usableH * 0.65f

                            if (points.size >= 2 && hasSpend) {
                                val stepX = w / (points.size - 1).coerceAtLeast(1)
                                var peakOffset = Offset.Zero
                                var prevX = 0f
                                var prevY = 0f
                                val linePath = Path()
                                var lastX = 0f
                                val maxFloat = maxVal.toFloat()

                                points.forEachIndexed { idx, pt ->
                                    val ratio = pt.amountCents.toFloat() / maxFloat
                                    val x = idx * stepX
                                    val y = topPad + usableH * (1f - ratio)
                                    if (idx == 0) {
                                        linePath.moveTo(x, y)
                                    } else {
                                        val ctrlX = (prevX + x) / 2f
                                        linePath.cubicTo(ctrlX, prevY, ctrlX, y, x, y)
                                    }
                                    if (idx == peakIndex) {
                                        peakOffset = Offset(x, y)
                                    }
                                    prevX = x
                                    prevY = y
                                    lastX = x
                                }

                                val fillPath = Path().apply {
                                    addPath(linePath)
                                    lineTo(lastX, h)
                                    lineTo(0f, h)
                                    close()
                                }
                                val fillBrush = Brush.verticalGradient(
                                    listOf(
                                        colors.primary.copy(alpha = 0.32f),
                                        colors.primary.copy(alpha = 0.0f)
                                    )
                                )
                                val strokeStyle = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                val gridColor = colors.chartGrid
                                val primaryColor = colors.primary
                                val peakHalo = colors.primary.copy(alpha = 0.28f)
                                val surfaceColor = colors.surface

                                onDrawBehind {
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(0f, gridY1),
                                        end = Offset(w, gridY1),
                                        strokeWidth = 1.5f,
                                        pathEffect = dashEffect
                                    )
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(0f, gridY2),
                                        end = Offset(w, gridY2),
                                        strokeWidth = 1.5f,
                                        pathEffect = dashEffect
                                    )
                                    drawPath(path = fillPath, brush = fillBrush)
                                    drawPath(path = linePath, color = primaryColor, style = strokeStyle)
                                    drawCircle(color = peakHalo, radius = 14f, center = peakOffset)
                                    drawCircle(color = surfaceColor, radius = 8f, center = peakOffset)
                                    drawCircle(color = primaryColor, radius = 5.5f, center = peakOffset)
                                }
                            } else {
                                val baseY = topPad + usableH * 0.88f
                                val gridColor = colors.chartGrid
                                val baselineColor = colors.primary.copy(alpha = 0.45f)
                                onDrawBehind {
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(0f, gridY1),
                                        end = Offset(w, gridY1),
                                        strokeWidth = 1.5f,
                                        pathEffect = dashEffect
                                    )
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(0f, gridY2),
                                        end = Offset(w, gridY2),
                                        strokeWidth = 1.5f,
                                        pathEffect = dashEffect
                                    )
                                    drawLine(
                                        color = baselineColor,
                                        start = Offset(0f, baseY),
                                        end = Offset(w, baseY),
                                        strokeWidth = 3f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                )

                if (!hasSpend) {
                    Text(
                        text = "No daily spend curve yet • Log expenses to track velocity",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (peakBadgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.surfaceContainerHigh,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = peakBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (points.size >= 4) {
                val labels = remember(points) {
                    listOf(
                        points.first().dayLabel,
                        points[points.size / 3].dayLabel,
                        points[(points.size * 2) / 3].dayLabel,
                        points.last().dayLabel
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEachIndexed { idx, label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (idx == labels.lastIndex) colors.primary else colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownTile(
    summary: CategorySpendSummary,
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    val colors = LakTheme.colors
    val accent = remember(summary.category.colorHex, colors.primary) {
        CategoryIconMapper.parseColor(summary.category.colorHex, colors.primary)
    }
    val categoryIcon = remember(summary.category.iconKey) {
        CategoryIconMapper.iconForKey(summary.category.iconKey)
    }
    val formattedTotal = remember(summary.totalCents, settings.currencyOption) {
        MoneyMath.formatAmount(summary.totalCents, settings.currencyOption, includeDecimals = false)
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = summary.category.name,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "${summary.percentage}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = accent
                )
            }

            Column {
                Text(
                    text = summary.category.name,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formattedTotal,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceContainerHigh)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((summary.percentage / 100f).coerceIn(0.05f, 1f))
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
            }
        }
    }
}

@Composable
fun DashboardTransactionRow(
    item: TransactionItem,
    settings: AppSettings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LakTheme.colors
    val tx = item.entity
    val iconColor = remember(tx.type, item.category?.colorHex, colors.income, colors.transfer, colors.primary) {
        when (tx.type) {
            TransactionType.INCOME -> colors.income
            TransactionType.TRANSFER -> colors.transfer
            TransactionType.EXPENSE -> CategoryIconMapper.parseColor(item.category?.colorHex, colors.primary)
        }
    }
    val categoryIcon = remember(item.category?.iconKey) {
        CategoryIconMapper.iconForKey(item.category?.iconKey)
    }
    val amountColor = when (tx.type) {
        TransactionType.INCOME -> colors.income
        TransactionType.TRANSFER -> colors.transfer
        TransactionType.EXPENSE -> colors.onSurface
    }
    val amountText = remember(tx.type, tx.amountCents, settings.currencyOption) {
        when (tx.type) {
            TransactionType.INCOME -> MoneyMath.formatAmount(tx.amountCents, settings.currencyOption, isPositiveOverride = true)
            TransactionType.EXPENSE -> MoneyMath.formatAmount(tx.amountCents, settings.currencyOption, isPositiveOverride = false)
            TransactionType.TRANSFER -> MoneyMath.formatAmount(tx.amountCents, settings.currencyOption)
        }
    }
    val title = remember(tx.merchant, item.category?.name, tx.type) {
        tx.merchant.ifBlank { item.category?.name ?: tx.type.label }
    }
    val methodBadge = remember(tx.type, item.paymentMethod?.name, item.toPaymentMethod?.name) {
        if (tx.type == TransactionType.TRANSFER && item.toPaymentMethod != null) {
            "${item.paymentMethod?.name ?: "From"} → ${item.toPaymentMethod.name}"
        } else {
            item.paymentMethod?.name ?: "Cash"
        }
    }
    val formattedTime = remember(tx.timestamp) {
        DateUtils.formatShortTimestamp(tx.timestamp)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("tx_row_${tx.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(end = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceContainerHigh)
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text("•", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.surfaceContainerLowest
                        ) {
                            Text(
                                text = methodBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (settings.balanceHidden) "••••••" else amountText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = amountColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.category?.name ?: tx.type.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (tx.type == TransactionType.INCOME) colors.income else colors.onSurfaceVariant
                )
            }
        }
    }
}
