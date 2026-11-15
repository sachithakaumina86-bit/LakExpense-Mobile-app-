package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.DailyChartPoint
import com.example.data.model.MonthlyComparisonBar
import com.example.data.model.ReportPeriod
import com.example.data.model.ReportsUiState
import com.example.ui.components.CategoryIconMapper
import com.example.ui.theme.LakTheme
import com.example.util.MoneyMath
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    state: ReportsUiState,
    settings: AppSettings,
    onSelectPeriod: (ReportPeriod) -> Unit,
    onSelectCustomRange: (Long, Long) -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LakTheme.colors
    var cumulativeMode by rememberSaveable { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }

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
                .testTag("reports_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector Pills: Weekly | Monthly | Quarterly | Yearly | Calendar Custom
            item(key = "period_selector", contentType = "period_selector") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        ReportPeriod.WEEKLY,
                        ReportPeriod.MONTHLY,
                        ReportPeriod.QUARTERLY,
                        ReportPeriod.YEARLY
                    ).forEach { period ->
                        val selected = state.period == period
                        Surface(
                            onClick = { onSelectPeriod(period) },
                            shape = CircleShape,
                            color = if (selected) colors.primaryContainer else colors.surfaceContainer,
                            border = BorderStroke(
                                1.dp,
                                if (selected) colors.primary else colors.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("report_period_${period.name.lowercase()}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = period.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (selected) colors.onPrimary else colors.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = { showDateRangeDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = if (state.period == ReportPeriod.CUSTOM) colors.primaryContainer else colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("report_custom_date_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Custom date range",
                                tint = if (state.period == ReportPeriod.CUSTOM) colors.onPrimary else colors.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Date Range + Offline Encrypted Status Row
            item(key = "date_status", contentType = "date_status") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                        )
                        Text(
                            text = "${state.rangeLabel} • ${state.daysLogged} days logged",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurface
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = colors.primary.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Offline Encrypted",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.primary
                            )
                        }
                    }
                }
            }

            // Net Cash Flow Hero Card
            item(key = "net_cash_flow", contentType = "net_cash_flow") {
                val netPositive = state.netCashFlowCents >= 0L
                val netCashFormatted = remember(state.netCashFlowCents, settings.currencyOption) {
                    MoneyMath.formatAmount(
                        state.netCashFlowCents,
                        settings.currencyOption,
                        includeDecimals = false,
                        showSign = true
                    )
                }
                val inflowFormatted = remember(state.totalInflowCents, settings.currencyOption) {
                    MoneyMath.formatAmount(state.totalInflowCents, settings.currencyOption, includeDecimals = false)
                }
                val outflowFormatted = remember(state.totalOutflowCents, settings.currencyOption) {
                    MoneyMath.formatAmount(state.totalOutflowCents, settings.currencyOption, includeDecimals = false)
                }
                val burnFormatted = remember(state.avgDailyBurnCents, settings.currencyOption) {
                    "Burn: ${MoneyMath.formatAmount(state.avgDailyBurnCents, settings.currencyOption, includeDecimals = false)}/day"
                }
                val capFormatted = remember(state.budgetCapCents, settings.currencyOption) {
                    if (state.budgetCapCents > 0L) {
                        "Cap: ${MoneyMath.formatAmount(state.budgetCapCents, settings.currencyOption, includeDecimals = false)}"
                    } else {
                        "Cap: Not set"
                    }
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
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
                            Text(
                                text = "NET CASH FLOW",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = colors.onSurfaceVariant
                            )
                            Surface(
                                shape = CircleShape,
                                color = (if (netPositive) colors.income else colors.expense).copy(alpha = 0.14f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                        contentDescription = null,
                                        tint = if (netPositive) colors.income else colors.expense,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (netPositive) "${state.savingsRatePercent}% Saved" else "Net Outflow",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (netPositive) colors.income else colors.expense
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = if (settings.balanceHidden) "••••••••" else netCashFormatted,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (netPositive) colors.income else colors.expense
                            )
                            Text(
                                text = if (netPositive) "Surplus retained in local accounts" else "Spending exceeded inflows for this range",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }

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
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Outlined.ArrowDownward, contentDescription = null, tint = colors.income, modifier = Modifier.size(14.dp))
                                        Text("TOTAL INFLOW", style = MaterialTheme.typography.labelSmall, color = colors.income)
                                    }
                                    Text(
                                        text = inflowFormatted,
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = state.inflowDeltaVsPrevPercent?.let {
                                            if (it >= 0) "+$it% vs prior" else "$it% vs prior"
                                        } ?: "No prior cycle",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.income
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surfaceContainerLow,
                                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Outlined.ArrowUpward, contentDescription = null, tint = colors.expense, modifier = Modifier.size(14.dp))
                                        Text("TOTAL OUTFLOW", style = MaterialTheme.typography.labelSmall, color = colors.expense)
                                    }
                                    Text(
                                        text = outflowFormatted,
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = state.outflowDeltaVsBudgetPercent?.let {
                                            if (it >= 0) "+$it% vs cap/prior" else "$it% vs cap/prior"
                                        } ?: "Logged expenses",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.primary
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null, tint = colors.warning, modifier = Modifier.size(15.dp))
                                Text(
                                    text = burnFormatted,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Outlined.Shield, contentDescription = null, tint = colors.primary, modifier = Modifier.size(15.dp))
                                Text(
                                    text = capFormatted,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Spending Trajectory Interactive Spline Chart
            item(key = "spending_trajectory", contentType = "spending_trajectory") {
                SpendingTrajectoryCard(
                    points = state.dailyPoints,
                    cumulativeMode = cumulativeMode,
                    onToggleCumulative = { cumulativeMode = it },
                    budgetCapCents = state.budgetCapCents,
                    totalOutflowCents = state.totalOutflowCents,
                    settings = settings
                )
            }

            // Category Distribution Card
            item(key = "category_distribution", contentType = "category_distribution") {
                Surface(
                    shape = RoundedCornerShape(18.dp),
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
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Category Distribution",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "${state.categoryDistribution.size} distinct spending categories",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Text(
                                text = remember(state.totalOutflowCents, settings.currencyOption) {
                                    MoneyMath.formatAmount(state.totalOutflowCents, settings.currencyOption, includeDecimals = false)
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                        }

                        if (state.categoryDistribution.isEmpty()) {
                            Text(
                                text = "No categorized expenses recorded during this period.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        } else {
                            // Multi-color horizontal segmented bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceContainerHigh)
                            ) {
                                state.categoryDistribution.forEach { catSummary ->
                                    val w = (catSummary.percentage / 100f).coerceAtLeast(0.03f)
                                    val segColor = remember(catSummary.category.colorHex, colors.primary) {
                                        CategoryIconMapper.parseColor(catSummary.category.colorHex, colors.primary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(w)
                                            .fillMaxHeight()
                                            .background(segColor)
                                    )
                                }
                            }

                            state.categoryDistribution.forEach { summary ->
                                val accent = remember(summary.category.colorHex, colors.primary) {
                                    CategoryIconMapper.parseColor(summary.category.colorHex, colors.primary)
                                }
                                val catIcon = remember(summary.category.iconKey) {
                                    CategoryIconMapper.iconForKey(summary.category.iconKey)
                                }
                                val formattedAmount = remember(summary.totalCents, settings.currencyOption) {
                                    MoneyMath.formatAmount(summary.totalCents, settings.currencyOption, includeDecimals = false)
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f).padding(end = 10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(accent.copy(alpha = 0.14f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = catIcon,
                                                contentDescription = summary.category.name,
                                                tint = accent,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = summary.category.name,
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                                color = colors.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = summary.sampleMerchants,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colors.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formattedAmount,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = colors.onSurface
                                        )
                                        Text(
                                            text = "${summary.percentage}%",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = accent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6-Month Income vs Expense Comparison Bar Chart
            item(key = "income_expense_comparison", contentType = "income_expense_comparison") {
                IncomeVsExpenseComparisonCard(
                    bars = state.monthlyComparisons,
                    settings = settings
                )
            }

            // Vault Intelligence Card
            item(key = "vault_intelligence", contentType = "vault_intelligence") {
                Surface(
                    shape = RoundedCornerShape(18.dp),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Psychology,
                                    contentDescription = "Vault Intelligence",
                                    tint = colors.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Vault Intelligence",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = colors.primary.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "${state.insights.size} Insights",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (state.insights.isEmpty()) {
                            Text(
                                text = "Record transactions across days to unlock on-device spending anomaly and retention intelligence.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        } else {
                            state.insights.forEach { insight ->
                                val accent = if (insight.isPositive) colors.income else colors.warning
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = colors.surfaceContainerLow,
                                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(accent.copy(alpha = 0.14f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (insight.isPositive) Icons.Outlined.CheckCircleOutline else Icons.AutoMirrored.Outlined.TrendingUp,
                                                contentDescription = insight.title,
                                                tint = accent,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = insight.title,
                                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = colors.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = insight.badgeText,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = accent
                                                )
                                            }
                                            Text(
                                                text = insight.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colors.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Computed entirely on-device via SQLite engine • Zero cloud tracking",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }

            // Reports & Audit Exports
            item(key = "audit_exports", contentType = "audit_exports") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "REPORTS & AUDIT EXPORTS",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = colors.onSurfaceVariant
                    )

                    Surface(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_pdf_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.expenseContainer.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = "PDF statement", tint = colors.expense)
                                }
                                Column {
                                    Text("Tax & Financial Summary (PDF)", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                                    Text("IRD-ready LKR breakdown with transaction ledger", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Outlined.Download, contentDescription = "Download PDF", tint = colors.primary)
                        }
                    }

                    Surface(
                        onClick = onExportCsv,
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_csv_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.primary.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.TableChart, contentDescription = "CSV ledger", tint = colors.primary)
                                }
                                Column {
                                    Text("Bank-Reconciled Data (CSV / Excel)", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                                    Text("Compatible with Commercial Bank, Sampath & Excel", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Outlined.Download, contentDescription = "Download CSV", tint = colors.primary)
                        }
                    }
                }
            }

            // Bottom assurance pill
            item(key = "privacy_footer", contentType = "privacy_footer") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceContainerLow,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SQLite Local Vault • AES-GCM-256 • Sri Lanka Data Privacy Assured",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showDateRangeDialog) {
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = state.customStartMillis,
            initialSelectedEndDateMillis = state.customEndMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDateRangeDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val s = pickerState.selectedStartDateMillis
                    val e = pickerState.selectedEndDateMillis ?: s
                    if (s != null && e != null) {
                        onSelectCustomRange(s, e)
                    }
                    showDateRangeDialog = false
                }) {
                    Text("Apply Range", color = colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangeDialog = false }) {
                    Text("Cancel", color = colors.onSurfaceVariant)
                }
            }
        ) {
            DateRangePicker(state = pickerState, modifier = Modifier.height(420.dp))
        }
    }
}

@Composable
private fun SpendingTrajectoryCard(
    points: List<DailyChartPoint>,
    cumulativeMode: Boolean,
    onToggleCumulative: (Boolean) -> Unit,
    budgetCapCents: Long,
    totalOutflowCents: Long,
    settings: AppSettings
) {
    val colors = LakTheme.colors
    var selectedIdx by remember(points, cumulativeMode) {
        val peak = if (cumulativeMode) points.lastIndex else points.indices.maxByOrNull { points[it].amountCents } ?: 0
        mutableIntStateOf(peak.coerceAtLeast(0))
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
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
                        text = "Spending Trajectory",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface
                    )
                    Text(
                        text = "Actual spend pace vs benchmark",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surfaceContainerHigh
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        listOf(false to "Daily", true to "Cumulative").forEach { (isCum, label) ->
                            val active = cumulativeMode == isCum
                            Surface(
                                onClick = { onToggleCumulative(isCum) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (active) colors.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                                modifier = Modifier.testTag("trajectory_mode_${label.lowercase()}")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) colors.onPrimary else colors.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            val selectedPoint = points.getOrNull(selectedIdx)
            val tooltipText = remember(selectedPoint, cumulativeMode, settings.currencyOption) {
                selectedPoint?.let { pt ->
                    val valCents = if (cumulativeMode) pt.cumulativeCents else pt.amountCents
                    val merchantSuffix = if (!cumulativeMode && pt.topMerchant.isNotBlank()) {
                        " • ${pt.topMerchant}"
                    } else ""
                    "${pt.dayLabel}: ${MoneyMath.formatAmount(valCents, settings.currencyOption, includeDecimals = false)}$merchantSuffix"
                }
            }
            if (tooltipText != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surfaceContainerHighest,
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(colors.primary))
                        Text(
                            text = tooltipText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.onSurface
                        )
                    }
                }
            }

            val seriesData = remember(points, cumulativeMode) {
                val mainVals = LongArray(points.size)
                val benchVals = LongArray(points.size)
                var cumBench = 0L
                var max = 1L
                points.forEachIndexed { idx, pt ->
                    val mainV = if (cumulativeMode) pt.cumulativeCents else pt.amountCents
                    cumBench += pt.benchmarkCents
                    val benchV = if (cumulativeMode) cumBench else pt.benchmarkCents
                    mainVals[idx] = mainV
                    benchVals[idx] = benchV
                    if (mainV > max) max = mainV
                    if (benchV > max) max = benchV
                }
                Triple(mainVals, benchVals, max)
            }
            val seriesValues = seriesData.first
            val benchmarkValues = seriesData.second
            val maxVal = seriesData.third

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            if (points.size >= 2) {
                                val step = size.width.toFloat() / (points.size - 1)
                                val idx = (offset.x / step).roundToInt().coerceIn(0, points.lastIndex)
                                selectedIdx = idx
                            }
                        }
                    }
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            val w = size.width
                            val h = size.height
                            val topPad = 14f
                            val botPad = 12f
                            val usableH = (h - topPad - botPad).coerceAtLeast(10f)
                            val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                            val gridY0 = topPad + usableH * 0.25f
                            val gridY1 = topPad + usableH * 0.55f
                            val gridY2 = topPad + usableH * 0.85f
                            val gridColor = colors.chartGrid
                            val benchColor = colors.chartBenchmark
                            val primaryColor = colors.primary
                            val surfaceColor = colors.surface
                            val selLineColor = colors.primary.copy(alpha = 0.45f)
                            val selHaloColor = colors.primary.copy(alpha = 0.3f)
                            val benchStroke = Stroke(width = 3f, pathEffect = dash)
                            val mainStroke = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)

                            if (points.size >= 2) {
                                val stepX = w / (points.size - 1)
                                val maxFloat = maxVal.toFloat()

                                val benchPath = Path()
                                var bPrevX = 0f
                                var bPrevY = 0f
                                for (idx in benchmarkValues.indices) {
                                    val x = idx * stepX
                                    val y = topPad + usableH * (1f - (benchmarkValues[idx].toFloat() / maxFloat))
                                    if (idx == 0) {
                                        benchPath.moveTo(x, y)
                                    } else {
                                        val cx = (bPrevX + x) / 2f
                                        benchPath.cubicTo(cx, bPrevY, cx, y, x, y)
                                    }
                                    bPrevX = x
                                    bPrevY = y
                                }

                                val mainPath = Path()
                                val mainCoords = Array(seriesValues.size) { Offset.Zero }
                                var mPrevX = 0f
                                var mPrevY = 0f
                                for (idx in seriesValues.indices) {
                                    val x = idx * stepX
                                    val y = topPad + usableH * (1f - (seriesValues[idx].toFloat() / maxFloat))
                                    mainCoords[idx] = Offset(x, y)
                                    if (idx == 0) {
                                        mainPath.moveTo(x, y)
                                    } else {
                                        val cx = (mPrevX + x) / 2f
                                        mainPath.cubicTo(cx, mPrevY, cx, y, x, y)
                                    }
                                    mPrevX = x
                                    mPrevY = y
                                }

                                val fillPath = Path().apply {
                                    addPath(mainPath)
                                    lineTo(mainCoords.last().x, h)
                                    lineTo(mainCoords.first().x, h)
                                    close()
                                }
                                val fillBrush = Brush.verticalGradient(
                                    listOf(primaryColor.copy(alpha = 0.28f), primaryColor.copy(alpha = 0f))
                                )

                                onDrawBehind {
                                    drawLine(color = gridColor, start = Offset(0f, gridY0), end = Offset(w, gridY0), strokeWidth = 1.2f, pathEffect = dash)
                                    drawLine(color = gridColor, start = Offset(0f, gridY1), end = Offset(w, gridY1), strokeWidth = 1.2f, pathEffect = dash)
                                    drawLine(color = gridColor, start = Offset(0f, gridY2), end = Offset(w, gridY2), strokeWidth = 1.2f, pathEffect = dash)

                                    drawPath(path = benchPath, color = benchColor, style = benchStroke)
                                    drawPath(path = fillPath, brush = fillBrush)
                                    drawPath(path = mainPath, color = primaryColor, style = mainStroke)

                                    val sel = mainCoords.getOrNull(selectedIdx)
                                    if (sel != null) {
                                        drawLine(
                                            color = selLineColor,
                                            start = Offset(sel.x, sel.y),
                                            end = Offset(sel.x, h),
                                            strokeWidth = 2f,
                                            pathEffect = dash
                                        )
                                        drawCircle(color = selHaloColor, radius = 14f, center = sel)
                                        drawCircle(color = surfaceColor, radius = 8f, center = sel)
                                        drawCircle(color = primaryColor, radius = 5.5f, center = sel)
                                    }
                                }
                            } else {
                                onDrawBehind {
                                    drawLine(color = gridColor, start = Offset(0f, gridY0), end = Offset(w, gridY0), strokeWidth = 1.2f, pathEffect = dash)
                                    drawLine(color = gridColor, start = Offset(0f, gridY1), end = Offset(w, gridY1), strokeWidth = 1.2f, pathEffect = dash)
                                    drawLine(color = gridColor, start = Offset(0f, gridY2), end = Offset(w, gridY2), strokeWidth = 1.2f, pathEffect = dash)
                                }
                            }
                        }
                )
            }

            if (points.size >= 4) {
                val xLabels = remember(points) {
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
                    xLabels.forEachIndexed { idx, label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (idx == 2) colors.primary else colors.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.primary))
                        Text("This Period", style = MaterialTheme.typography.labelSmall, color = colors.onSurface)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(modifier = Modifier.width(12.dp).height(2.dp).background(colors.chartBenchmark))
                        Text("Prior Benchmark", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                }
                if (budgetCapCents > 0L) {
                    val pctUnder = 100 - MoneyMath.percentage(totalOutflowCents, budgetCapCents)
                    Surface(shape = CircleShape, color = colors.primary.copy(alpha = 0.14f)) {
                        Text(
                            text = if (pctUnder >= 0) "$pctUnder% under cap" else "${-pctUnder}% over cap",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (pctUnder >= 0) colors.primary else colors.expense,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomeVsExpenseComparisonCard(
    bars: List<MonthlyComparisonBar>,
    settings: AppSettings
) {
    val colors = LakTheme.colors
    val maxBarCents = remember(bars) {
        (bars.maxOfOrNull { maxOf(it.incomeCents, it.expenseCents) } ?: 0L).coerceAtLeast(1L)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
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
                        text = "Income vs Expense",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface
                    )
                    Text(
                        text = "6-month cash flow comparison",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.income))
                        Text("In", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.expense))
                        Text("Out", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .drawWithCache {
                        val incColor = colors.income
                        val expColor = colors.expense
                        val corner = CornerRadius(6f, 6f)
                        if (bars.isEmpty()) {
                            onDrawBehind { }
                        } else {
                            val groupWidth = size.width / bars.size
                            val barW = (groupWidth * 0.28f).coerceIn(10f, 26f)
                            val gap = 6f
                            val usableH = size.height - 8f
                            val maxFloat = maxBarCents.toFloat()
                            val incRects = Array(bars.size) { index ->
                                val item = bars[index]
                                val centerX = (index + 0.5f) * groupWidth
                                val incH = ((item.incomeCents.toFloat() / maxFloat) * usableH)
                                    .coerceAtLeast(if (item.incomeCents > 0L) 6f else 2f)
                                Offset(centerX - barW - gap / 2f, size.height - incH) to Size(barW, incH)
                            }
                            val expRects = Array(bars.size) { index ->
                                val item = bars[index]
                                val centerX = (index + 0.5f) * groupWidth
                                val expH = ((item.expenseCents.toFloat() / maxFloat) * usableH)
                                    .coerceAtLeast(if (item.expenseCents > 0L) 6f else 2f)
                                Offset(centerX + gap / 2f, size.height - expH) to Size(barW, expH)
                            }
                            onDrawBehind {
                                for (i in incRects.indices) {
                                    val (incPos, incSize) = incRects[i]
                                    drawRoundRect(color = incColor, topLeft = incPos, size = incSize, cornerRadius = corner)
                                    val (expPos, expSize) = expRects[i]
                                    drawRoundRect(color = expColor, topLeft = expPos, size = expSize, cornerRadius = corner)
                                }
                            }
                        }
                    }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                bars.forEach { bar ->
                    val netCompact = remember(bar.incomeCents, bar.expenseCents) {
                        if (bar.incomeCents > 0L || bar.expenseCents > 0L) {
                            MoneyMath.formatCompact(bar.incomeCents - bar.expenseCents)
                        } else null
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = bar.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.onSurfaceVariant
                        )
                        if (netCompact != null) {
                            Text(
                                text = netCompact,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (bar.incomeCents >= bar.expenseCents) colors.income else colors.expense
                            )
                        }
                    }
                }
            }
        }
    }
}
