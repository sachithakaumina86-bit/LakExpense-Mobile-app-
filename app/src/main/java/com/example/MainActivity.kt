package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.ReportDrawnWhen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.TransactionType
import com.example.ui.components.AppRoutes
import com.example.ui.components.LakBottomBar
import com.example.ui.components.LakNavigationRail
import com.example.ui.components.LakTopHeader
import com.example.ui.components.MonthSelectorDialog
import com.example.ui.components.NotificationsBottomSheet
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.LakExpenseTheme
import com.example.ui.theme.LakTheme
import com.example.ui.viewmodel.AppContainer
import com.example.ui.viewmodel.LakViewModelFactory
import com.example.ui.viewmodel.MainViewModel
import com.example.util.BudgetNotificationHelper
import com.example.util.DateUtils

class MainActivity : FragmentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        BudgetNotificationHelper.ensureChannel(this)

        val container = AppContainer(applicationContext)
        viewModel = ViewModelProvider(
            this,
            LakViewModelFactory(applicationContext, container)
        )[MainViewModel::class.java]

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                viewModel.onAppBackgrounded()
            }

            override fun onStart(owner: LifecycleOwner) {
                viewModel.onAppForegrounded()
            }
        })

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            LakExpenseTheme(themeMode = settings.themeMode) {
                LakExpenseApp(
                    viewModel = viewModel,
                    onRequestBiometricPrompt = {
                        viewModel.repository.security.authenticateBiometric(
                            activity = this,
                            onSuccess = { viewModel.unlockViaBiometric() },
                            onError = { /* User falls back to PIN keypad */ }
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LakExpenseApp(
    viewModel: MainViewModel,
    onRequestBiometricPrompt: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isSplashFinished by viewModel.isSplashFinished.collectAsStateWithLifecycle()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsStateWithLifecycle()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val alertCount by viewModel.budgetAlertCount.collectAsStateWithLifecycle()

    var showMonthPicker by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }

    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.exportCurrentPdfToUri(uri)
        }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportCurrentCsvToUri(uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.SPLASH

    // Enforce Master PIN lock screen whenever vault locks after splash & onboarding
    LaunchedEffect(isSplashFinished, settings.onboardingCompleted, settings.pinEnabled, isVaultUnlocked) {
        if (isSplashFinished && settings.onboardingCompleted && settings.pinEnabled && !isVaultUnlocked) {
            if (currentRoute != AppRoutes.LOCK) {
                navController.navigate(AppRoutes.LOCK) {
                    launchSingleTop = true
                }
            }
        }
    }

    val navigateTopTab = remember(navController) {
        { route: String ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val topLevelRoutes = remember {
        setOf(
            AppRoutes.HOME,
            AppRoutes.TRANSACTIONS,
            AppRoutes.BUDGETS,
            AppRoutes.REPORTS
        )
    }
    val isTopLevel = currentRoute in topLevelRoutes

    val headerSubtitle = when (currentRoute) {
        AppRoutes.HOME -> "Home"
        AppRoutes.TRANSACTIONS -> "Transactions"
        AppRoutes.BUDGETS -> "Budgets"
        AppRoutes.REPORTS -> "Reports"
        else -> ""
    }
    val monthChipText = remember(currentRoute, selectedYearMonth) {
        if (currentRoute == AppRoutes.BUDGETS || currentRoute == AppRoutes.REPORTS) {
            DateUtils.formatMonthYear(selectedYearMonth, shortMonth = true)
        } else null
    }

    ReportDrawnWhen {
        currentRoute == AppRoutes.HOME ||
            currentRoute == AppRoutes.ONBOARDING ||
            currentRoute == AppRoutes.LOCK
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true }
    ) {
        val useNavigationRail = maxWidth >= 840.dp

        Scaffold(
            containerColor = LakTheme.colors.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                if (isTopLevel) {
                    LakTopHeader(
                        subtitle = headerSubtitle,
                        monthChipText = monthChipText,
                        onMonthChipClick = { showMonthPicker = true },
                        alertCount = alertCount,
                        onNotificationsClick = { showNotificationsSheet = true },
                        onProfileClick = { navController.navigate(AppRoutes.SETTINGS) }
                    )
                }
            },
            bottomBar = {
                if (isTopLevel && !useNavigationRail) {
                    LakBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = navigateTopTab,
                        onAddClick = { navController.navigate(AppRoutes.addRoute()) }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTopLevel && useNavigationRail) {
                    LakNavigationRail(
                        currentRoute = currentRoute,
                        onNavigate = navigateTopTab,
                        onAddClick = { navController.navigate(AppRoutes.addRoute()) }
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.SPLASH
                    ) {
                        composable(AppRoutes.SPLASH) {
                            SplashScreen(
                                onSplashComplete = {
                                    viewModel.markSplashFinished()
                                    val dest = when {
                                        !settings.onboardingCompleted -> AppRoutes.ONBOARDING
                                        settings.pinEnabled && !isVaultUnlocked -> AppRoutes.LOCK
                                        else -> AppRoutes.HOME
                                    }
                                    navController.navigate(dest) {
                                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(AppRoutes.ONBOARDING) {
                            OnboardingScreen(
                                onCompleteOnboarding = { name, email, location, openingCents, includeDemo ->
                                    viewModel.completeOnboarding(
                                        fullName = name,
                                        email = email,
                                        location = location,
                                        openingBalanceCents = openingCents,
                                        includeSampleData = includeDemo
                                    ) {
                                        navController.navigate(AppRoutes.HOME) {
                                            popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable(AppRoutes.LOCK) {
                            val biometricAvailable = remember(viewModel) {
                                viewModel.repository.security.isBiometricAvailable()
                            }
                            LockScreen(
                                biometricEnabled = settings.biometricEnabled && biometricAvailable,
                                hapticsEnabled = settings.hapticsEnabled,
                                onAttemptPinUnlock = { pin ->
                                    val ok = viewModel.attemptPinUnlock(pin)
                                    if (ok) {
                                        if (navController.previousBackStackEntry != null) {
                                            navController.popBackStack()
                                        } else {
                                            navController.navigate(AppRoutes.HOME) {
                                                popUpTo(AppRoutes.LOCK) { inclusive = true }
                                            }
                                        }
                                    }
                                    ok
                                },
                                onRequestBiometricPrompt = onRequestBiometricPrompt
                            )
                            LaunchedEffect(isVaultUnlocked) {
                                if (isVaultUnlocked) {
                                    if (navController.previousBackStackEntry != null) {
                                        navController.popBackStack()
                                    } else {
                                        navController.navigate(AppRoutes.HOME) {
                                            popUpTo(AppRoutes.LOCK) { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }

                        composable(AppRoutes.HOME) {
                            val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
                            val vaultStats by viewModel.vaultStats.collectAsStateWithLifecycle()
                            HomeDashboardScreen(
                                state = dashboardState,
                                settings = settings,
                                selectedYearMonth = selectedYearMonth,
                                hasDemoData = vaultStats.hasDemoData,
                                onOpenMonthPicker = { showMonthPicker = true },
                                onToggleBalanceVisibility = {
                                    viewModel.setBalanceHidden(!settings.balanceHidden)
                                },
                                onQuickTransfer = {
                                    navController.navigate(AppRoutes.addRoute(initialType = TransactionType.TRANSFER.name))
                                },
                                onQuickBillPay = {
                                    navController.navigate(AppRoutes.addRoute(initialType = TransactionType.EXPENSE.name))
                                },
                                onNavigateBudgets = { navigateTopTab(AppRoutes.BUDGETS) },
                                onQuickExport = { showExportSheet = true },
                                onViewAllTransactions = { navigateTopTab(AppRoutes.TRANSACTIONS) },
                                onTransactionClick = { item ->
                                    navController.navigate(AppRoutes.addRoute(txId = item.entity.id))
                                },
                                onClearDemoData = viewModel::clearDemoData,
                                onAddFirstTransaction = { navController.navigate(AppRoutes.addRoute()) }
                            )
                        }

                        composable(AppRoutes.TRANSACTIONS) {
                            val transactionsListState by viewModel.transactionsListState.collectAsStateWithLifecycle()
                            val filterState by viewModel.filterState.collectAsStateWithLifecycle()
                            val categories by viewModel.categories.collectAsStateWithLifecycle()
                            val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()
                            TransactionsScreen(
                                listState = transactionsListState,
                                filterState = filterState,
                                selectedYearMonth = selectedYearMonth,
                                categories = categories,
                                paymentMethods = paymentMethods,
                                settings = settings,
                                onUpdateFilter = viewModel::updateFilter,
                                onResetFilters = viewModel::resetFilters,
                                onEditTransaction = { item ->
                                    navController.navigate(AppRoutes.addRoute(txId = item.entity.id))
                                },
                                onDeleteTransaction = { id -> viewModel.deleteTransaction(id) },
                                onAddTransactionClick = { navController.navigate(AppRoutes.addRoute()) }
                            )
                        }

                        composable(
                            route = AppRoutes.EDIT_TRANSACTION,
                            arguments = listOf(
                                navArgument("txId") {
                                    type = NavType.LongType
                                    defaultValue = 0L
                                },
                                navArgument("initialType") {
                                    type = NavType.StringType
                                    defaultValue = TransactionType.EXPENSE.name
                                }
                            )
                        ) { entry ->
                            val allItems by viewModel.allTransactionItems.collectAsStateWithLifecycle()
                            val categories by viewModel.categories.collectAsStateWithLifecycle()
                            val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()
                            val txId = entry.arguments?.getLong("txId") ?: 0L
                            val typeArg = entry.arguments?.getString("initialType") ?: TransactionType.EXPENSE.name
                            val initType = TransactionType.entries.firstOrNull { it.name == typeArg }
                                ?: TransactionType.EXPENSE
                            val existing = remember(txId, allItems) {
                                if (txId > 0L) allItems.firstOrNull { it.entity.id == txId }?.entity else null
                            }

                            AddTransactionScreen(
                                existingTransaction = existing,
                                initialType = initType,
                                categories = categories,
                                paymentMethods = paymentMethods,
                                settings = settings,
                                onClose = { navController.popBackStack() },
                                onNavigateProfile = { navController.navigate(AppRoutes.SETTINGS) },
                                onCopyReceiptUri = viewModel::copyReceiptFromPicker,
                                onSaveCustomCategory = { n, sub, icon, hex, t ->
                                    viewModel.saveCustomCategory(name = n, subtitle = sub, iconKey = icon, colorHex = hex, type = t)
                                },
                                onSavePaymentMethod = { name -> viewModel.savePaymentMethod(name) },
                                onSaveTransaction = { id, type, amount, catId, payId, toPayId, merch, note, receipt, ts ->
                                    viewModel.saveTransaction(
                                        txId = id,
                                        type = type,
                                        amountCents = amount,
                                        categoryId = catId,
                                        paymentMethodId = payId,
                                        toPaymentMethodId = toPayId,
                                        merchant = merch,
                                        note = note,
                                        receiptPath = receipt,
                                        timestamp = ts
                                    ) {
                                        navController.popBackStack()
                                    }
                                },
                                onDeleteTransaction = { id ->
                                    viewModel.deleteTransaction(id) {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }

                        composable(AppRoutes.BUDGETS) {
                            val budgetsState by viewModel.budgetsState.collectAsStateWithLifecycle()
                            BudgetsScreen(
                                state = budgetsState,
                                settings = settings,
                                onPrevMonth = { viewModel.shiftMonth(-1) },
                                onNextMonth = { viewModel.shiftMonth(1) },
                                onSelectFilter = viewModel::setBudgetFilter,
                                onUpsertBudget = viewModel::upsertBudget,
                                onDeleteBudget = viewModel::deleteBudget
                            )
                        }

                        composable(AppRoutes.REPORTS) {
                            val reportsState by viewModel.reportsState.collectAsStateWithLifecycle()
                            ReportsScreen(
                                state = reportsState,
                                settings = settings,
                                onSelectPeriod = viewModel::setReportPeriod,
                                onSelectCustomRange = viewModel::setCustomReportRange,
                                onExportPdf = {
                                    pdfExportLauncher.launch("LakExpense_Statement_$selectedYearMonth.pdf")
                                },
                                onExportCsv = {
                                    csvExportLauncher.launch("LakExpense_Ledger_$selectedYearMonth.csv")
                                }
                            )
                        }

                        composable(AppRoutes.SETTINGS) {
                            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
                            val vaultStats by viewModel.vaultStats.collectAsStateWithLifecycle()
                            val categories by viewModel.categories.collectAsStateWithLifecycle()
                            val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()
                            val biometricAvailable = remember(viewModel) {
                                viewModel.repository.security.isBiometricAvailable()
                            }
                            ProfileSettingsScreen(
                                profile = userProfile,
                                settings = settings,
                                vaultStats = vaultStats,
                                categories = categories,
                                paymentMethods = paymentMethods,
                                biometricAvailable = biometricAvailable,
                                onBack = { navController.popBackStack() },
                                onPickAvatarUri = viewModel::copyAvatarFromPicker,
                                onUpdateProfile = viewModel::updateProfile,
                                onSetThemeMode = viewModel::setThemeMode,
                                onSetCurrencyOption = viewModel::setCurrencyOption,
                                onSetDateFormat = viewModel::setDateFormat,
                                onSetCycleStartDay = viewModel::setCycleStartDay,
                                onSetFirstDayOfWeek = viewModel::setFirstDayOfWeek,
                                onEnablePin = viewModel::enablePin,
                                onDisablePin = viewModel::disablePin,
                                onSetBiometricEnabled = viewModel::setBiometricEnabled,
                                onSetAutoLockOption = viewModel::setAutoLockOption,
                                onSetHapticsEnabled = viewModel::setHapticsEnabled,
                                onSetBudgetNotificationsEnabled = viewModel::setBudgetNotificationsEnabled,
                                onSetDefaultAlertThreshold = viewModel::setDefaultAlertThreshold,
                                onBackupVaultWithPassword = viewModel::writeEncryptedBackupToUri,
                                onRestoreVaultWithPassword = viewModel::restoreEncryptedBackupFromUri,
                                onExportPdf = {
                                    pdfExportLauncher.launch("LakExpense_Statement_$selectedYearMonth.pdf")
                                },
                                onExportCsv = {
                                    csvExportLauncher.launch("LakExpense_Ledger_$selectedYearMonth.csv")
                                },
                                onSaveCustomCategory = { n, sub, icon, hex, t ->
                                    viewModel.saveCustomCategory(name = n, subtitle = sub, iconKey = icon, colorHex = hex, type = t)
                                },
                                onDeleteCategory = viewModel::deleteCategory,
                                onSavePaymentMethod = { viewModel.savePaymentMethod(it) },
                                onDeletePaymentMethod = viewModel::deletePaymentMethod,
                                onLoadDemoData = viewModel::loadDemoData,
                                onClearDemoData = viewModel::clearDemoData,
                                onResetVault = {
                                    viewModel.resetAllVaultData {
                                        navController.navigate(AppRoutes.ONBOARDING) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMonthPicker) {
        MonthSelectorDialog(
            selectedYearMonth = selectedYearMonth,
            onSelectYearMonth = viewModel::selectMonth,
            onDismiss = { showMonthPicker = false }
        )
    }

    if (showNotificationsSheet) {
        val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
        NotificationsBottomSheet(
            alerts = dashboardState.alerts,
            onNavigateToBudgets = { navigateTopTab(AppRoutes.BUDGETS) },
            onDismiss = { showNotificationsSheet = false }
        )
    }

    if (showExportSheet) {
        val reportsState by viewModel.reportsState.collectAsStateWithLifecycle()
        val colors = LakTheme.colors
        ModalBottomSheet(
            onDismissRequest = { showExportSheet = false },
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
                    text = "Export Statement (${reportsState.rangeLabel})",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface
                )
                Text(
                    text = "Save locally via Android file picker or share with another app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )

                Surface(
                    onClick = {
                        showExportSheet = false
                        pdfExportLauncher.launch("LakExpense_Statement_$selectedYearMonth.pdf")
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, tint = colors.expense)
                        Text("Save PDF Summary", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                    }
                }

                Surface(
                    onClick = {
                        showExportSheet = false
                        csvExportLauncher.launch("LakExpense_Ledger_$selectedYearMonth.csv")
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Outlined.TableChart, contentDescription = null, tint = colors.primary)
                        Text("Save CSV Ledger", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                    }
                }

                Surface(
                    onClick = {
                        showExportSheet = false
                        val shareIntent = viewModel.buildSharePdfIntent(reportsState)
                        context.startActivity(Intent.createChooser(shareIntent, "Share PDF Statement"))
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, tint = colors.primary)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Share PDF Statement", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                    }
                }
            }
        }
    }
}
