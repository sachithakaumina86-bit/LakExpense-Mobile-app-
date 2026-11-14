package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CategoryEntity
import com.example.data.model.AppSettings
import com.example.data.model.BudgetCardUi
import com.example.data.model.BudgetFilter
import com.example.data.model.BudgetsUiState
import com.example.ui.components.CategoryIconMapper
import com.example.ui.components.MonthNavigatorHeader
import com.example.ui.theme.LakTheme
import com.example.util.MoneyMath
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    state: BudgetsUiState,
    settings: AppSettings,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectFilter: (BudgetFilter) -> Unit,
    onUpsertBudget: (categoryId: Long?, limitCents: Long, thresholdPercent: Int) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LakTheme.colors
    var showOverallCapDialog by remember { mutableStateOf(false) }
    var showCategoryBudgetDialog by remember { mutableStateOf<BudgetCardUi?>(null) }
    var showAddNewBudgetDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }

    val visibleCategoryCards = remember(state.categoryBudgets, state.activeFilter) {
        when (state.activeFilter) {
            BudgetFilter.ALL -> state.categoryBudgets
            BudgetFilter.ACTIVE -> state.categoryBudgets.filter { it.budget.limitCents > 0L }
            BudgetFilter.NEAR_LIMIT -> state.categoryBudgets.filter { it.isNearLimit }
            BudgetFilter.OVERSPENT -> state.categoryBudgets.filter { it.isOverspent }
        }
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
                .testTag("budgets_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Month Navigator + "+ New Budget" pill
            item {
                MonthNavigatorHeader(
                    monthLabel = "${state.cycleLabel} •",
                    onPrevMonth = onPrevMonth,
                    onNextMonth = onNextMonth,
                    trailingAction = {
                        Surface(
                            onClick = { showAddNewBudgetDialog = true },
                            shape = CircleShape,
                            color = colors.primaryContainer,
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("new_budget_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = "New budget",
                                    tint = colors.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "New Budget",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onPrimary
                                )
                            }
                        }
                    }
                )
            }

            // Cycle Countdown Banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = "Cycle countdown",
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${state.daysRemainingInCycle} days remaining in cycle",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurface
                            )
                        }
                        Text(
                            text = state.cycleEndsText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                    }
                }
            }

            // Hero Total Monthly Budget Card
            item {
                val isOver = state.overallCapCents > 0L && state.overallSpentCents > state.overallCapCents
                val isNear = !isOver && state.overallPercentUsed >= settings.defaultAlertThresholdPercent
                val statusLabel = when {
                    state.overallCapCents <= 0L -> "Set Monthly Cap"
                    isOver -> "Overspent"
                    isNear -> "Near Limit"
                    else -> "On Track"
                }
                val statusColor = when {
                    isOver -> colors.expense
                    isNear -> colors.warning
                    else -> colors.primary
                }
                val progress = if (state.overallCapCents > 0L) {
                    (state.overallSpentCents.toFloat() / state.overallCapCents.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }

                Surface(
                    onClick = { showOverallCapDialog = true },
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("overall_budget_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = "Total monthly budget",
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Total Monthly Budget",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = statusColor.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isOver || isNear) Icons.Outlined.WarningAmber else Icons.Outlined.CheckCircleOutline,
                                        contentDescription = statusLabel,
                                        tint = statusColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = statusLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = statusColor
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = if (state.overallCapCents > 0L) {
                                    MoneyMath.formatAmount(state.overallCapCents, settings.currencyOption, includeDecimals = false)
                                } else {
                                    "Tap to set cap"
                                },
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.onSurface
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Spent so far",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                                Text(
                                    text = "${MoneyMath.formatAmount(state.overallSpentCents, settings.currencyOption, includeDecimals = false)} (${state.overallPercentUsed}%)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                            }
                        }

                        // Progress bar with Safe Pace Benchmark Marker
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceContainerHigh)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress)
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(state.safePaceBenchmarkPercent / 100f)
                                        .fillMaxHeight()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .width(2.dp)
                                            .fillMaxHeight()
                                            .background(colors.onSurface)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${settings.currencyOption.symbol} 0",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                                Text(
                                    text = "Safe pace limit (${state.safePaceBenchmarkPercent}%)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.primary
                                )
                                Text(
                                    text = MoneyMath.formatAmount(state.overallCapCents, settings.currencyOption, includeDecimals = false),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }

                        // Inner metrics row: Left to Spend | Safe Daily Pace
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surfaceContainerLow,
                                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = if (state.overallRemainingCents >= 0L) "Left to Spend" else "Over Limit By",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurfaceVariant
                                    )
                                    Text(
                                        text = MoneyMath.formatAmount(abs(state.overallRemainingCents), settings.currencyOption, includeDecimals = false),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (state.overallRemainingCents >= 0L) colors.primary else colors.expense
                                    )
                                    val remPct = (100 - state.overallPercentUsed).coerceAtLeast(0)
                                    Text(
                                        text = if (state.overallRemainingCents >= 0L) "$remPct% remaining" else "Exceeded",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (state.overallRemainingCents >= 0L) colors.primary else colors.expense
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surfaceContainerLow,
                                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "Safe Daily Pace",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${MoneyMath.formatAmount(state.safeDailyPaceCents, settings.currencyOption, includeDecimals = false)} /day",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = "Next ${state.daysRemainingInCycle} days",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dynamic Category Pace Warning Banner (only shown when real warnings exist)
            val topWarning = state.categoryBudgets.firstOrNull { it.isOverspent || it.isNearLimit }
            if (topWarning != null) {
                item {
                    val accent = if (topWarning.isOverspent) colors.expense else colors.warning
                    val bg = if (topWarning.isOverspent) colors.expenseContainer.copy(alpha = 0.25f) else colors.warningContainer
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = bg,
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WarningAmber,
                                contentDescription = "Budget pace warning",
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Category Pace Warning",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "${topWarning.category.name} reached ${topWarning.percentUsed}% limit. ${state.nearLimitCount + state.overspentCount} category budget(s) need attention.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips + History Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BudgetFilterChip(
                        label = "All Budgets (${state.categoryBudgets.size})",
                        selected = state.activeFilter == BudgetFilter.ALL,
                        testTag = "budget_filter_all",
                        onClick = { onSelectFilter(BudgetFilter.ALL) }
                    )
                    BudgetFilterChip(
                        label = "Active (${state.categoryBudgets.size})",
                        selected = state.activeFilter == BudgetFilter.ACTIVE,
                        testTag = "budget_filter_active",
                        onClick = { onSelectFilter(BudgetFilter.ACTIVE) }
                    )
                    BudgetFilterChip(
                        label = "Near Limit (${state.nearLimitCount})",
                        selected = state.activeFilter == BudgetFilter.NEAR_LIMIT,
                        dotColor = colors.warning,
                        testTag = "budget_filter_near",
                        onClick = { onSelectFilter(BudgetFilter.NEAR_LIMIT) }
                    )
                    BudgetFilterChip(
                        label = "Overspent (${state.overspentCount})",
                        selected = state.activeFilter == BudgetFilter.OVERSPENT,
                        dotColor = colors.expense,
                        testTag = "budget_filter_over",
                        onClick = { onSelectFilter(BudgetFilter.OVERSPENT) }
                    )
                    Surface(
                        onClick = { showHistorySheet = true },
                        shape = CircleShape,
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("budget_history_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = "Monthly budget history",
                                tint = colors.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text("History", style = MaterialTheme.typography.labelMedium, color = colors.onSurface)
                        }
                    }
                }
            }

            // Category Breakdown Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Breakdown",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface
                    )
                    Text(
                        text = "${visibleCategoryCards.size} tracked categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            if (visibleCategoryCards.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "No Category Budgets In This View",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onSurface
                            )
                            Text(
                                text = "Create category spending limits below to monitor groceries, CEB utilities, fuel, and more.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(visibleCategoryCards, key = { it.budget.id }) { card ->
                    CategoryBudgetCard(
                        card = card,
                        daysRemaining = state.daysRemainingInCycle,
                        settings = settings,
                        onClick = { showCategoryBudgetDialog = card }
                    )
                }
            }

            // Add Category Budget CTA button
            item {
                Surface(
                    onClick = { showAddNewBudgetDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceContainerLow,
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("add_category_budget_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddCircleOutline,
                            contentDescription = "Add category budget",
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Category Budget",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SQLite Encrypted Vault • Real-time expense sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showOverallCapDialog) {
        BudgetEditorDialog(
            title = "Monthly Budget Cap (${state.cycleLabel})",
            categories = emptyList(),
            selectedCategory = null,
            initialLimitCents = state.overallBudget?.limitCents ?: state.overallCapCents,
            initialThreshold = state.overallBudget?.alertThresholdPercent ?: settings.defaultAlertThresholdPercent,
            canDelete = state.overallBudget != null,
            onDismiss = { showOverallCapDialog = false },
            onSave = { _, limitCents, threshold ->
                onUpsertBudget(null, limitCents, threshold)
                showOverallCapDialog = false
            },
            onDelete = {
                state.overallBudget?.let { onDeleteBudget(it.id) }
                showOverallCapDialog = false
            }
        )
    }

    showCategoryBudgetDialog?.let { editingCard ->
        BudgetEditorDialog(
            title = "Edit ${editingCard.category.name} Budget",
            categories = listOf(editingCard.category),
            selectedCategory = editingCard.category,
            initialLimitCents = editingCard.budget.limitCents,
            initialThreshold = editingCard.budget.alertThresholdPercent,
            canDelete = true,
            onDismiss = { showCategoryBudgetDialog = null },
            onSave = { _, limitCents, threshold ->
                onUpsertBudget(editingCard.category.id, limitCents, threshold)
                showCategoryBudgetDialog = null
            },
            onDelete = {
                onDeleteBudget(editingCard.budget.id)
                showCategoryBudgetDialog = null
            }
        )
    }

    if (showAddNewBudgetDialog) {
        BudgetEditorDialog(
            title = "New Budget (${state.cycleLabel})",
            categories = state.availableCategories,
            selectedCategory = state.availableCategories.firstOrNull(),
            initialLimitCents = 0L,
            initialThreshold = settings.defaultAlertThresholdPercent,
            canDelete = false,
            onDismiss = { showAddNewBudgetDialog = false },
            onSave = { categoryId, limitCents, threshold ->
                onUpsertBudget(categoryId, limitCents, threshold)
                showAddNewBudgetDialog = false
            },
            onDelete = {}
        )
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = colors.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Monthly Budget History",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface
                )
                state.history.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.monthLabel, style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                                Text(
                                    text = "Spent ${MoneyMath.formatAmount(item.spentCents, settings.currencyOption, includeDecimals = false)} of ${MoneyMath.formatAmount(item.capCents, settings.currencyOption, includeDecimals = false)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${item.percentUsed}%",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (item.percentUsed > 100) colors.expense else colors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    card: BudgetCardUi,
    daysRemaining: Int,
    settings: AppSettings,
    onClick: () -> Unit
) {
    val colors = LakTheme.colors
    val catColor = CategoryIconMapper.parseColor(card.category.colorHex, colors.primary)
    val statusColor = when {
        card.isOverspent -> colors.expense
        card.isNearLimit -> colors.warning
        else -> colors.primary
    }
    val statusText = when {
        card.isOverspent -> "Overspent (${card.percentUsed}%)"
        card.isNearLimit -> "Near Limit (${card.percentUsed}%)"
        card.percentUsed <= 50 -> "Under Budget"
        else -> "Good Pace (${card.percentUsed}%)"
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_card_${card.category.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryIconMapper.iconForKey(card.category.iconKey),
                            contentDescription = card.category.name,
                            tint = catColor,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = card.category.name,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = card.category.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${MoneyMath.formatAmount(card.spentCents, settings.currencyOption, includeDecimals = false)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onSurface
                )
                Text(
                    text = if (card.remainingCents >= 0L) {
                        "Remaining: ${MoneyMath.formatAmount(card.remainingCents, settings.currencyOption, includeDecimals = false)}"
                    } else {
                        "Over: ${MoneyMath.formatAmount(abs(card.remainingCents), settings.currencyOption, includeDecimals = false)}"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (card.isOverspent || card.isNearLimit) statusColor else colors.primary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceContainerHigh)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(card.progressFraction)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cap: ${MoneyMath.formatAmount(card.budget.limitCents, settings.currencyOption, includeDecimals = false)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Text(
                    text = if (card.remainingCents > 0L && daysRemaining > 0) {
                        "${MoneyMath.formatAmount(card.safeDailyPaceCents, settings.currencyOption, includeDecimals = false)}/day left"
                    } else {
                        "${card.percentUsed}% spent"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (card.isNearLimit || card.isOverspent) statusColor else colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BudgetFilterChip(
    label: String,
    selected: Boolean,
    testTag: String,
    dotColor: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit
) {
    val colors = LakTheme.colors
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) colors.primaryContainer else colors.surfaceContainer,
        border = BorderStroke(
            1.dp,
            if (selected) colors.primary else colors.outlineVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .height(36.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (dotColor != null && !selected) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) colors.onPrimary else colors.onSurface
            )
        }
    }
}

@Composable
private fun BudgetEditorDialog(
    title: String,
    categories: List<CategoryEntity>,
    selectedCategory: CategoryEntity?,
    initialLimitCents: Long,
    initialThreshold: Int,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onSave: (categoryId: Long?, limitCents: Long, threshold: Int) -> Unit,
    onDelete: () -> Unit
) {
    val colors = LakTheme.colors
    var pickedCategory by remember { mutableStateOf(selectedCategory) }
    var isOverallCap by remember { mutableStateOf(selectedCategory == null) }
    var amountText by remember {
        mutableStateOf(
            if (initialLimitCents > 0L) (initialLimitCents / 100L).toString() else ""
        )
    }
    var threshold by remember { mutableFloatStateOf(initialThreshold.toFloat()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceContainer,
        title = { Text(title, color = colors.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (categories.size > 1 || (categories.isNotEmpty() && selectedCategory == null)) {
                    Text("Target", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (selectedCategory == null) {
                            Surface(
                                onClick = { isOverallCap = true },
                                shape = CircleShape,
                                color = if (isOverallCap) colors.primaryContainer else colors.surfaceContainerHigh
                            ) {
                                Text(
                                    text = "Overall Monthly",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOverallCap) colors.onPrimary else colors.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                        categories.forEach { cat ->
                            val active = !isOverallCap && pickedCategory?.id == cat.id
                            Surface(
                                onClick = {
                                    isOverallCap = false
                                    pickedCategory = cat
                                },
                                shape = CircleShape,
                                color = if (active) colors.primaryContainer else colors.surfaceContainerHigh
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) colors.onPrimary else colors.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        errorText = null
                        amountText = it.filter { ch -> ch.isDigit() || ch == '.' }
                    },
                    label = { Text("Budget Limit (Rs.)") },
                    placeholder = { Text("e.g. 50000") },
                    prefix = { Text("Rs. ", color = colors.primary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = errorText?.let { msg -> { Text(msg, color = colors.expense) } },
                    modifier = Modifier.testTag("budget_limit_input")
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Alert Threshold",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                        Text(
                            text = "${threshold.roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                    }
                    Slider(
                        value = threshold,
                        onValueChange = { threshold = it },
                        valueRange = 50f..95f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cents = MoneyMath.parseToCents(amountText)
                    if (cents == null || cents <= 0L) {
                        errorText = "Enter a valid limit above Rs. 0"
                        return@TextButton
                    }
                    onSave(
                        if (isOverallCap) null else pickedCategory?.id,
                        cents,
                        threshold.roundToInt()
                    )
                },
                modifier = Modifier.testTag("budget_save_button")
            ) {
                Text("Save", color = colors.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (canDelete) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = colors.expense)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = colors.onSurfaceVariant)
                }
            }
        }
    )
}
