package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
            item {
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
            item {
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
            item {
                val netPositive = state.netCashFlowCents >= 0L
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
                                text = if (settings.balanceHidden) "••••••••" else MoneyMath.formatAmount(
                                    state.netCashFlowCents,
                                    settings.currencyOption,
                                    includeDecimals = false,
                                    showSign = true
                                ),
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
                                        text = MoneyMath.formatAmount(state.totalInflowCents, settings.currencyOption, includeDecimals = false),
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
                                        text = MoneyMath.formatAmount(state.totalOutflowCents, settings.currencyOption, includeDecimals = false),
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
                                    text = "Burn: ${MoneyMath.formatAmount(state.avgDailyBurnCents, settings.currencyOption, includeDecimals = false)}/day",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Outlined.Shield, contentDescription = null, tint = colors.primary, modifier = Modifier.size(15.dp))
                                Text(
                                    text = if (state.budgetCapCents > 0L) {
                                        "Cap: ${MoneyMath.formatAmount(state.budgetCapCents, settings.currencyOption, includeDecimals = false)}"
                                    } else {
                                        "Cap: Not set"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Spending Trajectory Interactive Spline Chart
            item {
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
            item {
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
                                text = MoneyMath.formatAmount(state.totalOutflowCents, settings.currencyOption, includeDecimals = false),
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
                                    Box(
                                        modifier = Modifier
                                            .weight(w)
                                            .fillMaxHeight()
                                            .background(CategoryIconMapper.parseColor(catSummary.category.colorHex, colors.primary))
                                    )
                                }
                            }

                            state.categoryDistribution.forEach { summary ->
                                val accent = CategoryIconMapper.parseColor(summary.category.colorHex, colors.primary)
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
                                                imageVector = CategoryIconMapper.iconForKey(summary.category.iconKey),
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
                                            text = MoneyMath.formatAmount(summary.totalCents, settings.currencyOption, includeDecimals = false),
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
            item {
                IncomeVsExpenseComparisonCard(
                    bars = state.monthlyComparisons,
                    settings = settings
                )
            }

            // Vault Intelligence Card
            item {
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
            item {
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
            item {
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
            if (selectedPoint != null) {
                val valCents = if (cumulativeMode) selectedPoint.cumulativeCents else selectedPoint.amountCents
                val merchantSuffix = if (!cumulativeMode && selectedPoint.topMerchant.isNotBlank()) {
                    " • ${selectedPoint.topMerchant}"
                } else ""
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
                            text = "${selectedPoint.dayLabel}: ${MoneyMath.formatAmount(valCents, settings.currencyOption, includeDecimals = false)}$merchantSuffix",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.onSurface
                        )
                    }
                }
            }

            val seriesValues = points.map { if (cumulativeMode) it.cumulativeCents else it.amountCents }
            val benchmarkValues = remember(points, cumulativeMode) {
                var cum = 0L
                points.map { pt ->
                    cum += pt.benchmarkCents
                    if (cumulativeMode) cum else pt.benchmarkCents
                }
            }
            val maxVal = maxOf(
                seriesValues.maxOrNull() ?: 0L,
                benchmarkValues.maxOrNull() ?: 0L,
                1L
            )

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
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val topPad = 14f
                    val botPad = 12f
                    val usableH = (h - topPad - botPad).coerceAtLeast(10f)
                    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)

                    listOf(0.25f, 0.55f, 0.85f).forEach { frac ->
                        drawLine(
                            color = colors.chartGrid,
                            start = Offset(0f, topPad + usableH * frac),
                            end = Offset(w, topPad + usableH * frac),
                            strokeWidth = 1.2f,
                            pathEffect = dash
                        )
                    }

                    if (points.size >= 2) {
                        val stepX = w / (points.size - 1)
                        val benchCoords = benchmarkValues.mapIndexed { idx, v ->
                            Offset(idx * stepX, topPad + usableH * (1f - (v.toFloat() / maxVal.toFloat())))
                        }
                        val benchPath = Path().apply {
                            moveTo(benchCoords.first().x, benchCoords.first().y)
                            for (i in 0 until benchCoords.lastIndex) {
                                val c = benchCoords[i]
                                val n = benchCoords[i + 1]
                                val cx = (c.x + n.x) / 2f
                                cubicTo(cx, c.y, cx, n.y, n.x, n.y)
                            }
                        }
                        drawPath(
                            path = benchPath,
                            color = colors.chartBenchmark,
                            style = Stroke(width = 3f, pathEffect = dash)
                        )

                        val mainCoords = seriesValues.mapIndexed { idx, v ->
                            Offset(idx * stepX, topPad + usableH * (1f - (v.toFloat() / maxVal.toFloat())))
                        }
                        val mainPath = Path().apply {
                            moveTo(mainCoords.first().x, mainCoords.first().y)
                            for (i in 0 until mainCoords.lastIndex) {
                                val c = mainCoords[i]
                                val n = mainCoords[i + 1]
                                val cx = (c.x + n.x) / 2f
                                cubicTo(cx, c.y, cx, n.y, n.x, n.y)
                            }
                        }
                        val fillPath = Path().apply {
                            addPath(mainPath)
                            lineTo(mainCoords.last().x, h)
                            lineTo(mainCoords.first().x, h)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                listOf(colors.primary.copy(alpha = 0.28f), colors.primary.copy(alpha = 0f))
                            )
                        )
                        drawPath(
                            path = mainPath,
                            color = colors.primary,
                            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        val sel = mainCoords.getOrNull(selectedIdx)
                        if (sel != null) {
                            drawLine(
                                color = colors.primary.copy(alpha = 0.45f),
                                start = Offset(sel.x, sel.y),
                                end = Offset(sel.x, h),
                                strokeWidth = 2f,
                                pathEffect = dash
                            )
                            drawCircle(color = colors.primary.copy(alpha = 0.3f), radius = 14f, center = sel)
                            drawCircle(color = colors.surface, radius = 8f, center = sel)
                            drawCircle(color = colors.primary, radius = 5.5f, center = sel)
                        }
                    }
                }
            }

            if (points.size >= 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        points.first().dayLabel,
                        points[points.size / 3].dayLabel,
                        points[(points.size * 2) / 3].dayLabel,
                        points.last().dayLabel
                    ).forEachIndexed { idx, label ->
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
    val maxBarCents = (bars.maxOfOrNull { maxOf(it.incomeCents, it.expenseCents) } ?: 0L).coerceAtLeast(1L)

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

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                if (bars.isEmpty()) return@Canvas
                val groupWidth = size.width / bars.size
                val barW = (groupWidth * 0.28f).coerceIn(10f, 26f)
                val gap = 6f
                val usableH = size.height - 8f

                bars.forEachIndexed { index, item ->
                    val centerX = (index + 0.5f) * groupWidth
                    val incH = ((item.incomeCents.toFloat() / maxBarCents.toFloat()) * usableH).coerceAtLeast(if (item.incomeCents > 0L) 6f else 2f)
                    val expH = ((item.expenseCents.toFloat() / maxBarCents.toFloat()) * usableH).coerceAtLeast(if (item.expenseCents > 0L) 6f else 2f)

                    drawRoundRect(
                        color = colors.income,
                        topLeft = Offset(centerX - barW - gap / 2f, size.height - incH),
                        size = Size(barW, incH),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = colors.expense,
                        topLeft = Offset(centerX + gap / 2f, size.height - expH),
                        size = Size(barW, expH),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                bars.forEach { bar ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = bar.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.onSurfaceVariant
                        )
                        if (bar.incomeCents > 0L || bar.expenseCents > 0L) {
                            Text(
                                text = MoneyMath.formatCompact(bar.incomeCents - bar.expenseCents),
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
