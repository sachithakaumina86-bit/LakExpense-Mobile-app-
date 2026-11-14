package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BudgetAlertNotification
import com.example.ui.theme.LakTheme
import com.example.util.DateUtils

object AppRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOCK = "lock"
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val ADD_TRANSACTION = "add_transaction"
    const val EDIT_TRANSACTION = "add_transaction?txId={txId}&initialType={initialType}"
    const val BUDGETS = "budgets"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"

    fun addRoute(txId: Long = 0L, initialType: String = "EXPENSE"): String =
        "add_transaction?txId=$txId&initialType=$initialType"
}

@Composable
fun LakLogoMark(modifier: Modifier = Modifier) {
    val colors = LakTheme.colors
    val resId = if (colors.isDark) R.drawable.ic_lakexpense_logo_dark else R.drawable.ic_lakexpense_logo_light
    Image(
        painter = painterResource(id = resId),
        contentDescription = "LakExpense logo",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
fun LakTopHeader(
    subtitle: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    monthChipText: String? = null,
    onMonthChipClick: (() -> Unit)? = null,
    alertCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    trailingExtra: (@Composable () -> Unit)? = null
) {
    val colors = LakTheme.colors
    Surface(
        color = colors.headerBackground,
        tonalElevation = 0.dp,
        shadowElevation = if (colors.isDark) 4.dp else 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (showBackButton && onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("header_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceContainerHigh)
                            .border(1.dp, colors.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LakLogoMark(modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "LAKEXPENSE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.9.sp
                            ),
                            color = colors.primary
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (monthChipText != null && onMonthChipClick != null) {
                        Surface(
                            onClick = onMonthChipClick,
                            shape = RoundedCornerShape(12.dp),
                            color = colors.surfaceContainerHigh,
                            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("header_month_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "Selected month",
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = monthChipText,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Change month",
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (trailingExtra != null) {
                        trailingExtra()
                    }

                    if (onNotificationsClick != null) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("notifications_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceContainerHigh.copy(alpha = 0.7f))
                                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.45f), CircleShape)
                                    .clickable(onClick = onNotificationsClick),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Budget alerts and notifications",
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            if (alertCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-6).dp, y = 6.dp)
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary)
                                )
                            }
                        }
                    }

                    if (onProfileClick != null) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("profile_settings_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryContainer.copy(alpha = 0.18f))
                                    .border(1.dp, colors.primary.copy(alpha = 0.45f), CircleShape)
                                    .clickable(onClick = onProfileClick),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Profile and Settings",
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f), thickness = 0.7.dp)
        }
    }
}

@Composable
fun LakBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LakTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = colors.navBarBackground,
            tonalElevation = 0.dp,
            shadowElevation = if (colors.isDark) 16.dp else 8.dp,
            border = BorderStroke(0.7.dp, colors.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BottomNavItem(
                    label = "Home",
                    icon = Icons.Outlined.Home,
                    selected = currentRoute == AppRoutes.HOME,
                    testTag = "nav_home",
                    onClick = { onNavigate(AppRoutes.HOME) }
                )
                BottomNavItem(
                    label = "Trans.",
                    icon = Icons.Outlined.ReceiptLong,
                    selected = currentRoute == AppRoutes.TRANSACTIONS,
                    testTag = "nav_transactions",
                    onClick = { onNavigate(AppRoutes.TRANSACTIONS) }
                )
                Spacer(modifier = Modifier.width(64.dp))
                BottomNavItem(
                    label = "Budgets",
                    icon = Icons.Outlined.AccountBalanceWallet,
                    selected = currentRoute == AppRoutes.BUDGETS,
                    testTag = "nav_budgets",
                    onClick = { onNavigate(AppRoutes.BUDGETS) }
                )
                BottomNavItem(
                    label = "Reports",
                    icon = Icons.Outlined.BarChart,
                    selected = currentRoute == AppRoutes.REPORTS,
                    testTag = "nav_reports",
                    onClick = { onNavigate(AppRoutes.REPORTS) }
                )
            }
        }

        // Elevated Emerald Center Quick-Add FAB matching Stitch bottom bar
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
                .size(58.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    ambientColor = colors.primaryContainer,
                    spotColor = colors.primaryContainer
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(colors.primaryContainer, colors.primary)
                    )
                )
                .clickable(onClick = onAddClick)
                .testTag("nav_quick_add_fab"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add Transaction",
                tint = colors.onPrimary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val colors = LakTheme.colors
    val tint = if (selected) colors.primary else colors.onSurfaceVariant
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = tint
        )
    }
}

@Composable
fun LakNavigationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LakTheme.colors
    NavigationRail(
        containerColor = colors.navBarBackground,
        contentColor = colors.onSurface,
        modifier = modifier,
        header = {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer)
                    .clickable(onClick = onAddClick)
                    .testTag("rail_quick_add_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add Transaction",
                    tint = colors.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    ) {
        val navItems = listOf(
            Triple(AppRoutes.HOME, "Home", Icons.Outlined.Home),
            Triple(AppRoutes.TRANSACTIONS, "Trans.", Icons.Outlined.ReceiptLong),
            Triple(AppRoutes.BUDGETS, "Budgets", Icons.Outlined.AccountBalanceWallet),
            Triple(AppRoutes.REPORTS, "Reports", Icons.Outlined.BarChart)
        )
        navItems.forEach { (route, label, icon) ->
            NavigationRailItem(
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant,
                    indicatorColor = colors.primary.copy(alpha = 0.14f)
                )
            )
        }
    }
}

@Composable
fun MonthSelectorDialog(
    selectedYearMonth: String,
    onSelectYearMonth: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LakTheme.colors
    val currentYm = DateUtils.currentYearMonthKey()
    val monthOptions = (-11..2).map { offset ->
        DateUtils.shiftYearMonthKey(currentYm, offset)
    }.reversed()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceContainer,
        titleContentColor = colors.onSurface,
        title = { Text("Select Financial Month", style = MaterialTheme.typography.headlineSmall) },
        text = {
            LazyColumn(modifier = Modifier.height(300.dp)) {
                items(monthOptions) { ym ->
                    val isSelected = ym == selectedYearMonth
                    Surface(
                        onClick = {
                            onSelectYearMonth(ym)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) colors.primary.copy(alpha = 0.16f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = DateUtils.formatMonthYear(ym),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) colors.primary else colors.onSurface
                            )
                            if (ym == currentYm) {
                                Text(
                                    text = "Current",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = colors.primary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    alerts: List<BudgetAlertNotification>,
    onNavigateToBudgets: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LakTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceContainerLow,
        contentColor = colors.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vault Alerts & Pace",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface
                    )
                    Text(
                        text = "Computed locally from your budget thresholds",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                TextButton(onClick = {
                    onDismiss()
                    onNavigateToBudgets()
                }) {
                    Text("View Budgets", color = colors.primary)
                }
            }

            if (alerts.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "All Budgets Under Control",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No category or monthly limits have reached their warning thresholds for this cycle.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            } else {
                alerts.forEach { alert ->
                    val accent = if (alert.isOverspent) colors.expense else colors.warning
                    val bg = if (alert.isOverspent) colors.expenseContainer.copy(alpha = 0.25f) else colors.warningContainer
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = bg,
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WarningAmber,
                                contentDescription = alert.title,
                                tint = accent,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = alert.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${alert.percentUsed}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = accent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthNavigatorHeader(
    monthLabel: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    trailingAction: @Composable () -> Unit
) {
    val colors = LakTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = onPrevMonth,
                shape = CircleShape,
                color = colors.surfaceContainerHigh,
                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("prev_month_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronLeft,
                        contentDescription = "Previous month",
                        tint = colors.onSurface
                    )
                }
            }
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface
            )
            Surface(
                onClick = onNextMonth,
                shape = CircleShape,
                color = colors.surfaceContainerHigh,
                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("next_month_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Next month",
                        tint = colors.onSurface
                    )
                }
            }
        }
        trailingAction()
    }
}
