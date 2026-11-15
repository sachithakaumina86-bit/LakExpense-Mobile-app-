package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.CategoryEntity
import com.example.data.local.PaymentMethodEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.AppSettings
import com.example.data.model.AutoLockOption
import com.example.data.model.CurrencyDisplayOption
import com.example.data.model.DateFormatOption
import com.example.data.model.ThemeMode
import com.example.data.model.TransactionType
import com.example.data.model.VaultStats
import com.example.ui.components.CategoryIconMapper
import com.example.ui.components.LakTopHeader
import com.example.ui.theme.LakTheme
import com.example.util.BudgetNotificationHelper
import com.example.util.DateUtils
import com.example.util.MoneyMath
import java.io.File
import java.math.BigDecimal
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    profile: UserProfileEntity?,
    settings: AppSettings,
    vaultStats: VaultStats,
    categories: List<CategoryEntity>,
    paymentMethods: List<PaymentMethodEntity>,
    biometricAvailable: Boolean,
    onBack: () -> Unit,
    onPickAvatarUri: (android.net.Uri) -> Unit,
    onUpdateProfile: (name: String, email: String, location: String, openingBalanceCents: Long, avatarUri: String?) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetCurrencyOption: (CurrencyDisplayOption) -> Unit,
    onSetDateFormat: (DateFormatOption) -> Unit,
    onSetCycleStartDay: (Int) -> Unit,
    onSetFirstDayOfWeek: (Int) -> Unit,
    onEnablePin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onSetBiometricEnabled: (Boolean) -> Unit,
    onSetAutoLockOption: (AutoLockOption) -> Unit,
    onSetHapticsEnabled: (Boolean) -> Unit,
    onSetBudgetNotificationsEnabled: (Boolean) -> Unit,
    onSetDefaultAlertThreshold: (Int) -> Unit,
    onBackupVaultWithPassword: (android.net.Uri, String) -> Unit,
    onRestoreVaultWithPassword: (android.net.Uri, String) -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onSaveCustomCategory: (name: String, subtitle: String, iconKey: String, colorHex: String, type: TransactionType) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onSavePaymentMethod: (String) -> Unit,
    onDeletePaymentMethod: (PaymentMethodEntity) -> Unit,
    onLoadDemoData: () -> Unit,
    onClearDemoData: () -> Unit,
    onResetVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val colors = LakTheme.colors
    val context = LocalContext.current

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }
    var showCycleDayDialog by remember { mutableStateOf(false) }
    var showFirstWeekDayDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showAutoLockDialog by remember { mutableStateOf(false) }
    var showThresholdDialog by remember { mutableStateOf(false) }
    var showCategoriesSheet by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showExportChoiceDialog by remember { mutableStateOf(false) }
    var showResetConfirm1 by remember { mutableStateOf(false) }
    var showResetConfirm2 by remember { mutableStateOf(false) }

    var pendingBackupUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onPickAvatarUri(uri)
    }

    val backupCreator = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) pendingBackupUri = uri
    }

    val backupOpener = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) pendingRestoreUri = uri
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onSetBudgetNotificationsEnabled(granted)
    }

    val initials = remember(profile?.fullName) {
        val parts = (profile?.fullName ?: "LakExpense User").trim().split(" ").filter { it.isNotBlank() }
        parts.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "LE" }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        LakTopHeader(
            subtitle = "Profile & Settings",
            showBackButton = true,
            onBackClick = onBack,
            trailingExtra = {
                IconButton(
                    onClick = { showHelpDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("settings_help_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = "Offline vault help",
                        tint = colors.onSurfaceVariant
                    )
                }
            }
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .testTag("settings_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Profile Hero Card
                item(key = "profile_hero", contentType = "profile_hero") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clickable {
                                        avatarPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .testTag("profile_avatar_picker"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!profile?.avatarUri.isNullOrBlank()) {
                                    val avatarRequest = remember(profile?.avatarUri, context) {
                                        ImageRequest.Builder(context)
                                            .data(File(profile!!.avatarUri!!))
                                            .size(256)
                                            .crossfade(true)
                                            .build()
                                    }
                                    AsyncImage(
                                        model = avatarRequest,
                                        contentDescription = "Profile photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, colors.primary, CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(colors.primaryContainer.copy(alpha = 0.2f))
                                            .border(2.dp, colors.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.primary
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(colors.primaryContainer)
                                        .border(2.dp, colors.surfaceContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.PhotoCamera,
                                        contentDescription = "Change profile photo",
                                        tint = colors.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Text(
                                text = profile?.fullName ?: "LakExpense User",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                            Text(
                                text = listOfNotNull(
                                    profile?.email?.takeIf { it.isNotBlank() },
                                    profile?.location?.takeIf { it.isNotBlank() } ?: "Colombo, Sri Lanka"
                                ).joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = colors.primary.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Outlined.Shield, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Offline SQLite Vault: Active & Local",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.primary
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (settings.lastBackupTimestamp > 0L) {
                                                "Last backup: ${DateUtils.formatDate(settings.lastBackupTimestamp, settings.dateFormat)}"
                                            } else {
                                                "No vault backup created yet"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.onSurfaceVariant
                                        )
                                        Text(
                                            text = "AES-GCM-256",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.primary
                                        )
                                    }
                                }
                            }

                            Surface(
                                onClick = { showEditProfileDialog = true },
                                shape = CircleShape,
                                color = colors.surfaceContainerHigh,
                                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.testTag("edit_profile_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                    Text("Edit Profile", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.onSurface)
                                }
                            }
                        }
                    }
                }

                // REGIONAL & ACCOUNTING
                item(key = "regional_accounting", contentType = "settings_section") {
                    SettingsSectionTitle("REGIONAL & ACCOUNTING")
                    SettingsGroupCard {
                        SettingsRowItem(
                            icon = Icons.Outlined.Payments,
                            title = "Primary Currency",
                            subtitle = settings.currencyOption.label,
                            trailingBadge = settings.currencyOption.symbol,
                            testTag = "setting_currency",
                            onClick = { showCurrencyDialog = true }
                        )
                        SettingsRowDivider()
                        SettingsRowItem(
                            icon = Icons.Outlined.CalendarMonth,
                            title = "Date Format",
                            subtitle = "${settings.dateFormat.displayLabel} (e.g. ${DateUtils.formatDate(System.currentTimeMillis(), settings.dateFormat)})",
                            testTag = "setting_date_format",
                            onClick = { showDateFormatDialog = true }
                        )
                        SettingsRowDivider()
                        SettingsRowItem(
                            icon = Icons.Outlined.EventRepeat,
                            title = "Financial Cycle Start",
                            subtitle = "${ordinalDay(settings.cycleStartDay)} of every month",
                            testTag = "setting_cycle_start",
                            onClick = { showCycleDayDialog = true }
                        )
                        SettingsRowDivider()
                        SettingsRowItem(
                            icon = Icons.Outlined.CalendarMonth,
                            title = "First Day of Week",
                            subtitle = dayOfWeekName(settings.firstDayOfWeek),
                            testTag = "setting_first_day",
                            onClick = { showFirstWeekDayDialog = true }
                        )
                    }
                }

                // VAULT SECURITY & ACCESS
                item(key = "vault_security", contentType = "settings_section") {
                    SettingsSectionTitle("VAULT SECURITY & ACCESS", badge = "Hardware Keystore")
                    SettingsGroupCard {
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Password,
                            title = "4-Digit Master PIN",
                            subtitle = if (settings.pinEnabled) "Locks vault upon app backgrounding" else "Set a 4-digit PIN to protect local vault",
                            checked = settings.pinEnabled,
                            testTag = "setting_pin_switch",
                            onCheckedChange = { wantEnable ->
                                if (wantEnable) showPinSetupDialog = true else onDisablePin()
                            }
                        )
                        SettingsRowDivider()
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Fingerprint,
                            title = "Biometric Unlock",
                            subtitle = when {
                                !biometricAvailable -> "Unavailable: No biometric sensor/fingerprint enrolled"
                                !settings.pinEnabled -> "Enable Master PIN first to pair biometrics"
                                else -> "Fingerprint / Android BiometricPrompt"
                            },
                            checked = settings.biometricEnabled && biometricAvailable && settings.pinEnabled,
                            enabled = biometricAvailable && settings.pinEnabled,
                            testTag = "setting_biometric_switch",
                            onCheckedChange = { onSetBiometricEnabled(it) }
                        )
                        SettingsRowDivider()
                        SettingsRowItem(
                            icon = Icons.Outlined.Lock,
                            title = if (settings.pinEnabled) "Change Master PIN" else "Set Master PIN",
                            subtitle = if (settings.pinUpdatedAt > 0L) {
                                "Updated ${DateUtils.formatDate(settings.pinUpdatedAt, settings.dateFormat)}"
                            } else {
                                "Not configured yet"
                            },
                            testTag = "setting_change_pin",
                            onClick = { showPinSetupDialog = true }
                        )
                        SettingsRowDivider()
                        SettingsRowItem(
                            icon = Icons.Outlined.AccessTime,
                            title = "Auto-Lock Delay",
                            subtitle = settings.autoLockOption.label,
                            trailingText = when (settings.autoLockOption) {
                                AutoLockOption.IMMEDIATE -> "Immediate"
                                AutoLockOption.ONE_MIN -> "1 min"
                                AutoLockOption.FIVE_MIN -> "5 min"
                            },
                            testTag = "setting_auto_lock",
                            onClick = { showAutoLockDialog = true }
                        )
                    }
                }

                // NOTIFICATIONS
                item(key = "notifications", contentType = "settings_section") {
                    SettingsSectionTitle("NOTIFICATIONS")
                    SettingsGroupCard {
                        SettingsSwitchItem(
                            icon = Icons.Outlined.NotificationsActive,
                            title = "Budget Threshold Alerts",
                            subtitle = if (BudgetNotificationHelper.hasNotificationPermission(context)) {
                                "Alert when category or cap crosses ${settings.defaultAlertThresholdPercent}%"
                            } else {
                                "Tap to grant Android notification permission"
                            },
                            checked = settings.budgetNotificationsEnabled,
                            testTag = "setting_notifications_switch",
                            onCheckedChange = { enable ->
                                if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !BudgetNotificationHelper.hasNotificationPermission(context)
                                ) {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onSetBudgetNotificationsEnabled(enable)
                                }
                            }
                        )
                        SettingsRowDivider()
                        SettingsRowItem(
                            icon = Icons.Outlined.WarningAmber,
                            title = "Default Warning Threshold",
                            subtitle = "Trigger Near Limit alerts at this percentage",
                            trailingBadge = "${settings.defaultAlertThresholdPercent}%",
                            testTag = "setting_alert_threshold",
                            onClick = { showThresholdDialog = true }
                        )
                    }
                }

                // OFFLINE VAULT & BACKUPS
                item(key = "offline_vault", contentType = "settings_section") {
                    SettingsSectionTitle("OFFLINE VAULT & BACKUPS")
                    SettingsGroupCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Outlined.Storage, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                                    Text("Local Vault Size", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                                }
                                Text(
                                    text = MoneyMath.formatBytes(vaultStats.databaseBytes),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
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
                                        .fillMaxWidth(0.65f)
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(colors.primary)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${vaultStats.transactionCount} transactions", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                Text("${vaultStats.budgetCount} budgets", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                Text("${vaultStats.receiptCount} receipts", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                        }

                        SettingsRowDivider()
                        SettingsRowItem(
                            icon = Icons.Outlined.Backup,
                            title = "Encrypted Vault Backup",
                            subtitle = "Generate password-protected AES-256 export",
                            testTag = "setting_backup_vault",
                            onClick = {
                                val stamp = DateUtils.currentYearMonthKey()
                                backupCreator.launch("LakExpense_Vault_$stamp.lakvault")
                            }
                        )
                        SettingsRowDivider()
                        SettingsRowItem(
                            icon = Icons.Outlined.Restore,
                            title = "Restore Data from File",
                            subtitle = "Import existing .lakvault encrypted backup",
                            testTag = "setting_restore_vault",
                            onClick = { backupOpener.launch(arrayOf("*/*")) }
                        )
                        SettingsRowDivider()
                        SettingsRowItem(
                            icon = Icons.Outlined.Description,
                            title = "Export Financial Statements",
                            subtitle = "PDF Tax Summary or Bank-Reconciled CSV",
                            testTag = "setting_export_statements",
                            onClick = { showExportChoiceDialog = true }
                        )
                        SettingsRowDivider()
                        SettingsRowItem(
                            icon = Icons.Outlined.AccountBalanceWallet,
                            title = if (vaultStats.hasDemoData) "Clear Sample Demo Data" else "Load Sample Sri Lankan Demo Data",
                            subtitle = if (vaultStats.hasDemoData) {
                                "Remove sample demo rows while keeping your real records"
                            } else {
                                "Add optional Keells, CEB & PickMe sample records to explore"
                            },
                            testTag = "setting_demo_data_action",
                            onClick = { if (vaultStats.hasDemoData) onClearDemoData() else onLoadDemoData() }
                        )
                    }
                }

                // APPEARANCE & CATEGORIES
                item(key = "appearance_categories", contentType = "settings_section") {
                    SettingsSectionTitle("APPEARANCE & CATEGORIES")
                    SettingsGroupCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Display Theme",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        Triple(ThemeMode.DARK, "Dark", Icons.Outlined.DarkMode),
                                        Triple(ThemeMode.LIGHT, "Light", Icons.Outlined.LightMode),
                                        Triple(ThemeMode.SYSTEM, "System", Icons.Outlined.PhoneAndroid)
                                    ).forEach { (mode, label, icon) ->
                                        val selected = settings.themeMode == mode
                                        Surface(
                                            onClick = { onSetThemeMode(mode) },
                                            shape = RoundedCornerShape(9.dp),
                                            color = if (selected) colors.primaryContainer else Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .testTag("theme_mode_${mode.name.lowercase()}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = if (selected) colors.onPrimary else colors.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = if (selected) colors.onPrimary else colors.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        SettingsRowDivider()
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Vibration,
                            title = "Keypad Haptic Feedback",
                            subtitle = "Tactile click when typing amounts & PIN",
                            checked = settings.hapticsEnabled,
                            testTag = "setting_haptics_switch",
                            onCheckedChange = onSetHapticsEnabled
                        )
                        SettingsRowDivider()
                        SettingsRowItem(
                            icon = Icons.Outlined.Category,
                            title = "Manage Categories & Accounts",
                            subtitle = "Customize Sri Lankan categories & payment methods",
                            trailingBadge = "${categories.size} Active",
                            testTag = "setting_manage_categories",
                            onClick = { showCategoriesSheet = true }
                        )
                    }
                }

                // DANGER ZONE
                item(key = "danger_zone", contentType = "danger_zone") {
                    Text(
                        text = "DANGER ZONE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = colors.expense
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.expense.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.expenseContainer.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = colors.expense)
                                }
                                Column {
                                    Text(
                                        text = "Clear All Local Vault Data",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = "Permanently purges the local SQLite database, receipts, and security keys. Cannot be undone unless you have an encrypted backup.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { showResetConfirm1 = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFB91C1C),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("reset_vault_database_button")
                            ) {
                                Icon(Icons.Outlined.WarningAmber, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reset Vault Database", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Footer
                item(key = "footer", contentType = "footer") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.Shield, contentDescription = null, tint = colors.primary, modifier = Modifier.size(15.dp))
                            Text("100% Offline & Private", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = colors.primary)
                        }
                        Text("LakExpense Android v1.0.0 • Every Rupee. Under Control.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        Text("Zero Cloud Trackers • Encrypted on Device", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }

    // Dialogs & Sheets
    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(profile?.fullName ?: "") }
        var emailInput by remember { mutableStateOf(profile?.email ?: "") }
        var locationInput by remember { mutableStateOf(profile?.location ?: "Colombo, Sri Lanka") }
        var balanceInput by remember {
            mutableStateOf(
                BigDecimal.valueOf(profile?.openingBalanceCents ?: 0L)
                    .divide(BigDecimal(100))
                    .stripTrailingZeros()
                    .toPlainString()
            )
        }
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = colors.surfaceContainer,
            title = { Text("Edit Local Profile", color = colors.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Full Name") }, singleLine = true)
                    OutlinedTextField(value = emailInput, onValueChange = { emailInput = it }, label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(value = locationInput, onValueChange = { locationInput = it }, label = { Text("Location") }, singleLine = true)
                    OutlinedTextField(
                        value = balanceInput,
                        onValueChange = { balanceInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Opening Balance (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cents = MoneyMath.parseToCents(balanceInput) ?: 0L
                    onUpdateProfile(nameInput, emailInput, locationInput, cents, profile?.avatarUri)
                    showEditProfileDialog = false
                }) {
                    Text("Save", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancel", color = colors.onSurfaceVariant) }
            }
        )
    }

    if (showCurrencyDialog) {
        SingleChoiceDialog(
            title = "Currency Display",
            options = CurrencyDisplayOption.entries.map { it to it.label },
            selected = settings.currencyOption,
            onSelect = { onSetCurrencyOption(it); showCurrencyDialog = false },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showDateFormatDialog) {
        SingleChoiceDialog(
            title = "Date Format",
            options = DateFormatOption.entries.map { it to it.displayLabel },
            selected = settings.dateFormat,
            onSelect = { onSetDateFormat(it); showDateFormatDialog = false },
            onDismiss = { showDateFormatDialog = false }
        )
    }

    if (showCycleDayDialog) {
        SingleChoiceDialog(
            title = "Financial Cycle Start Day",
            options = listOf(1, 5, 10, 15, 20, 25, 28).map { it to "${ordinalDay(it)} of every month" },
            selected = settings.cycleStartDay,
            onSelect = { onSetCycleStartDay(it); showCycleDayDialog = false },
            onDismiss = { showCycleDayDialog = false }
        )
    }

    if (showFirstWeekDayDialog) {
        SingleChoiceDialog(
            title = "First Day of Week",
            options = listOf(
                Calendar.MONDAY to "Monday",
                Calendar.SUNDAY to "Sunday",
                Calendar.SATURDAY to "Saturday"
            ),
            selected = settings.firstDayOfWeek,
            onSelect = { onSetFirstDayOfWeek(it); showFirstWeekDayDialog = false },
            onDismiss = { showFirstWeekDayDialog = false }
        )
    }

    if (showAutoLockDialog) {
        SingleChoiceDialog(
            title = "Auto-Lock Delay",
            options = AutoLockOption.entries.map { it to it.label },
            selected = settings.autoLockOption,
            onSelect = { onSetAutoLockOption(it); showAutoLockDialog = false },
            onDismiss = { showAutoLockDialog = false }
        )
    }

    if (showThresholdDialog) {
        SingleChoiceDialog(
            title = "Default Alert Threshold",
            options = listOf(70, 75, 80, 85, 90).map { it to "$it% of limit" },
            selected = settings.defaultAlertThresholdPercent,
            onSelect = { onSetDefaultAlertThreshold(it); showThresholdDialog = false },
            onDismiss = { showThresholdDialog = false }
        )
    }

    if (showPinSetupDialog) {
        var pin1 by remember { mutableStateOf("") }
        var pin2 by remember { mutableStateOf("") }
        var pinErr by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showPinSetupDialog = false },
            containerColor = colors.surfaceContainer,
            title = { Text("Set 4-Digit Master PIN", color = colors.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = pin1,
                        onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { pin1 = it; pinErr = null } },
                        label = { Text("Enter 4-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.testTag("pin_setup_input_1")
                    )
                    OutlinedTextField(
                        value = pin2,
                        onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { pin2 = it; pinErr = null } },
                        label = { Text("Confirm 4-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        isError = pinErr != null,
                        supportingText = pinErr?.let { msg -> { Text(msg, color = colors.expense) } },
                        modifier = Modifier.testTag("pin_setup_input_2")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pin1.length != 4) {
                            pinErr = "PIN must be exactly 4 digits"
                            return@TextButton
                        }
                        if (pin1 != pin2) {
                            pinErr = "PIN entries do not match"
                            return@TextButton
                        }
                        onEnablePin(pin1)
                        showPinSetupDialog = false
                    },
                    modifier = Modifier.testTag("pin_setup_confirm")
                ) {
                    Text("Enable PIN", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinSetupDialog = false }) { Text("Cancel", color = colors.onSurfaceVariant) }
            }
        )
    }

    pendingBackupUri?.let { uri ->
        PasswordPromptDialog(
            title = "Encrypt Vault Backup",
            subtitle = "Choose a strong passphrase to protect your AES-256 backup file.",
            confirmLabel = "Encrypt & Save",
            onDismiss = { pendingBackupUri = null },
            onConfirm = { password ->
                onBackupVaultWithPassword(uri, password)
                pendingBackupUri = null
            }
        )
    }

    pendingRestoreUri?.let { uri ->
        PasswordPromptDialog(
            title = "Decrypt & Restore Vault",
            subtitle = "Enter the passphrase used when creating this .lakvault file.",
            confirmLabel = "Restore Vault",
            onDismiss = { pendingRestoreUri = null },
            onConfirm = { password ->
                onRestoreVaultWithPassword(uri, password)
                pendingRestoreUri = null
            }
        )
    }

    if (showExportChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showExportChoiceDialog = false },
            containerColor = colors.surfaceContainer,
            title = { Text("Export Financial Statement", color = colors.onSurface) },
            text = { Text("Generate a real statement from your local Room ledger.", color = colors.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    showExportChoiceDialog = false
                    onExportPdf()
                }) {
                    Text("PDF Summary", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportChoiceDialog = false
                    onExportCsv()
                }) {
                    Text("CSV Ledger", color = colors.primary)
                }
            }
        )
    }

    if (showCategoriesSheet) {
        var newMethodText by remember { mutableStateOf("") }
        ModalBottomSheet(
            onDismissRequest = { showCategoriesSheet = false },
            containerColor = colors.surfaceContainerLow
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "categories_header", contentType = "sheet_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Categories (${categories.size})", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
                        TextButton(onClick = { showAddCategoryDialog = true }) {
                            Icon(Icons.Outlined.Add, contentDescription = null, tint = colors.primary)
                            Text("Add Category", color = colors.primary)
                        }
                    }
                }
                items(
                    items = categories,
                    key = { "cat_${it.id}" },
                    contentType = { "category_row" }
                ) { cat ->
                    val catColor = remember(cat.colorHex, colors.primary) {
                        CategoryIconMapper.parseColor(cat.colorHex, colors.primary)
                    }
                    val catIcon = remember(cat.iconKey) {
                        CategoryIconMapper.iconForKey(cat.iconKey)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(
                                    imageVector = catIcon,
                                    contentDescription = cat.name,
                                    tint = catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(cat.name, style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                                    Text("${cat.type.label} • ${cat.subtitle}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                            }
                            if (cat.isCustom) {
                                IconButton(onClick = { onDeleteCategory(cat) }) {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete category", tint = colors.expense)
                                }
                            }
                        }
                    }
                }
                item(key = "payment_methods_header", contentType = "sheet_header") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Payment Methods (${paymentMethods.size})", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMethodText,
                            onValueChange = { newMethodText = it },
                            label = { Text("New Bank / Wallet") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (newMethodText.isNotBlank()) {
                                    onSavePaymentMethod(newMethodText)
                                    newMethodText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimary)
                        ) {
                            Text("Add")
                        }
                    }
                }
                items(
                    items = paymentMethods,
                    key = { "pm_${it.id}" },
                    contentType = { "payment_method_row" }
                ) { method ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(method.name, style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                            if (method.isCustom) {
                                IconButton(onClick = { onDeletePaymentMethod(method) }) {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete method", tint = colors.expense)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        CreateCategoryDialog(
            type = TransactionType.EXPENSE,
            onDismiss = { showAddCategoryDialog = false },
            onCreate = { name, subtitle, iconKey, colorHex ->
                onSaveCustomCategory(name, subtitle, iconKey, colorHex, TransactionType.EXPENSE)
                showAddCategoryDialog = false
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = colors.surfaceContainer,
            title = { Text("LakExpense Local Vault", color = colors.onSurface) },
            text = {
                Text(
                    "LakExpense operates 100% offline. Your LKR ledger, category budgets, receipts, and backups stay strictly on your device. Master PIN credentials are sealed using Android Keystore AES-256-GCM, and .lakvault backups use PBKDF2-SHA256 + AES-GCM-256.",
                    color = colors.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("Got It", color = colors.primary) }
            }
        )
    }

    if (showResetConfirm1) {
        AlertDialog(
            onDismissRequest = { showResetConfirm1 = false },
            containerColor = colors.surfaceContainer,
            title = { Text("Reset Local Vault?", color = colors.onSurface) },
            text = { Text("All transactions, budgets, receipts, profile settings, and PIN locks will be deleted.", color = colors.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirm1 = false
                    showResetConfirm2 = true
                }) {
                    Text("Continue", color = colors.expense, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm1 = false }) { Text("Cancel", color = colors.onSurfaceVariant) }
            }
        )
    }

    if (showResetConfirm2) {
        AlertDialog(
            onDismissRequest = { showResetConfirm2 = false },
            containerColor = colors.surfaceContainer,
            title = { Text("Confirm Permanent Purge", color = colors.expense) },
            text = { Text("This cannot be undone. Are you sure you want to wipe the SQLite database?", color = colors.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirm2 = false
                        onResetVault()
                    },
                    modifier = Modifier.testTag("confirm_reset_vault_final")
                ) {
                    Text("Purge Vault", color = colors.expense, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm2 = false }) { Text("Keep Data", color = colors.primary) }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String, badge: String? = null) {
    val colors = LakTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
            color = colors.onSurfaceVariant
        )
        if (badge != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = colors.primary, modifier = Modifier.size(12.dp))
                Text(text = badge, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.primary)
            }
        }
    }
}

@Composable
private fun SettingsGroupCard(content: @Composable () -> Unit) {
    val colors = LakTheme.colors
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsRowDivider() {
    HorizontalDivider(
        color = LakTheme.colors.outlineVariant.copy(alpha = 0.35f),
        thickness = 0.7.dp,
        modifier = Modifier.padding(horizontal = 14.dp)
    )
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    trailingBadge: String? = null,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    val colors = LakTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = colors.primary, modifier = Modifier.size(19.dp))
            }
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = colors.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (trailingBadge != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = colors.primary.copy(alpha = 0.14f)) {
                    Text(
                        text = trailingBadge,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            if (trailingText != null) {
                Text(trailingText, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.primary)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LakTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f).padding(end = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (enabled) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = colors.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun <T> SingleChoiceDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LakTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceContainer,
        title = { Text(title, color = colors.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { (value, label) ->
                    val isSel = value == selected
                    Surface(
                        onClick = { onSelect(value) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) colors.primary.copy(alpha = 0.16f) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSel) colors.primary else colors.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = colors.primary) }
        }
    )
}

@Composable
private fun PasswordPromptDialog(
    title: String,
    subtitle: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val colors = LakTheme.colors
    var password by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceContainer,
        title = { Text(title, color = colors.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; err = null },
                    label = { Text("Vault Passphrase") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = err != null,
                    supportingText = err?.let { msg -> { Text(msg, color = colors.expense) } }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (password.length < 4) {
                    err = "Enter at least 4 characters"
                    return@TextButton
                }
                onConfirm(password)
            }) {
                Text(confirmLabel, color = colors.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.onSurfaceVariant) }
        }
    )
}

private fun ordinalDay(day: Int): String {
    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$day$suffix"
}

private fun dayOfWeekName(day: Int): String = when (day) {
    Calendar.SUNDAY -> "Sunday"
    Calendar.SATURDAY -> "Saturday"
    else -> "Monday"
}
