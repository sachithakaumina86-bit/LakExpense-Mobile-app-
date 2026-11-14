package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CategoryEntity
import com.example.data.local.PaymentMethodEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.AppSettings
import com.example.data.model.AutoLockOption
import com.example.data.model.BudgetFilter
import com.example.data.model.BudgetsUiState
import com.example.data.model.CurrencyDisplayOption
import com.example.data.model.DashboardUiState
import com.example.data.model.DateFormatOption
import com.example.data.model.ReportPeriod
import com.example.data.model.ReportsUiState
import com.example.data.model.ThemeMode
import com.example.data.model.TransactionFilterState
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.data.model.VaultStats
import com.example.data.repository.LakExpenseRepository
import com.example.domain.FinanceEngine
import com.example.util.BudgetNotificationHelper
import com.example.util.DateUtils
import com.example.util.ExportManager
import com.example.util.MoneyMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val appContext: Context,
    val repository: LakExpenseRepository,
    val exportManager: ExportManager
) : ViewModel() {

    private val _isSplashFinished = MutableStateFlow(false)
    val isSplashFinished: StateFlow<Boolean> = _isSplashFinished.asStateFlow()

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _selectedYearMonth = MutableStateFlow(DateUtils.currentYearMonthKey())
    val selectedYearMonth: StateFlow<String> = _selectedYearMonth.asStateFlow()

    private val _filterState = MutableStateFlow(TransactionFilterState())
    val filterState: StateFlow<TransactionFilterState> = _filterState.asStateFlow()

    private val _budgetFilter = MutableStateFlow(BudgetFilter.ALL)
    val budgetFilter: StateFlow<BudgetFilter> = _budgetFilter.asStateFlow()

    private val _reportPeriod = MutableStateFlow(ReportPeriod.MONTHLY)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    private val _reportCustomRange = MutableStateFlow<Pair<Long?, Long?>>(null to null)

    private val _vaultStats = MutableStateFlow(VaultStats())
    val vaultStats: StateFlow<VaultStats> = _vaultStats.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    private var backgroundedAtMillis: Long = 0L

    val settings: StateFlow<AppSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val categories: StateFlow<List<CategoryEntity>> = repository.categoriesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val paymentMethods: StateFlow<List<PaymentMethodEntity>> = repository.paymentMethodsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allTransactionItems: StateFlow<List<TransactionItem>> = combine(
        repository.transactionsFlow,
        repository.categoriesFlow,
        repository.paymentMethodsFlow
    ) { txs, cats, methods ->
        FinanceEngine.mapTransactionItems(txs, cats, methods)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filteredTransactions: StateFlow<List<TransactionItem>> = combine(
        allTransactionItems,
        _filterState,
        _selectedYearMonth,
        settings
    ) { items, filter, ym, cfg ->
        FinanceEngine.filterTransactions(items, filter, ym, cfg.cycleStartDay)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dashboardState: StateFlow<DashboardUiState> = combine(
        repository.profileFlow,
        allTransactionItems,
        categories,
        repository.budgetsFlow,
        settings,
        _selectedYearMonth
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val profile = args[0] as com.example.data.local.UserProfileEntity?
        @Suppress("UNCHECKED_CAST")
        val items = args[1] as List<TransactionItem>
        @Suppress("UNCHECKED_CAST")
        val cats = args[2] as List<CategoryEntity>
        @Suppress("UNCHECKED_CAST")
        val budgets = args[3] as List<com.example.data.local.BudgetEntity>
        val cfg = args[4] as AppSettings
        val ym = args[5] as String
        FinanceEngine.computeDashboard(profile, items, cats, budgets, cfg, ym)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    val budgetsState: StateFlow<BudgetsUiState> = combine(
        allTransactionItems,
        categories,
        repository.budgetsFlow,
        settings,
        _selectedYearMonth,
        _budgetFilter
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val items = args[0] as List<TransactionItem>
        @Suppress("UNCHECKED_CAST")
        val cats = args[1] as List<CategoryEntity>
        @Suppress("UNCHECKED_CAST")
        val budgets = args[2] as List<com.example.data.local.BudgetEntity>
        val cfg = args[3] as AppSettings
        val ym = args[4] as String
        val filter = args[5] as BudgetFilter
        FinanceEngine.computeBudgets(items, cats, budgets, cfg, ym, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetsUiState())

    val reportsState: StateFlow<ReportsUiState> = combine(
        allTransactionItems,
        categories,
        repository.budgetsFlow,
        settings,
        _selectedYearMonth,
        _reportPeriod,
        _reportCustomRange
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val items = args[0] as List<TransactionItem>
        @Suppress("UNCHECKED_CAST")
        val cats = args[1] as List<CategoryEntity>
        @Suppress("UNCHECKED_CAST")
        val budgets = args[2] as List<com.example.data.local.BudgetEntity>
        val cfg = args[3] as AppSettings
        val ym = args[4] as String
        val period = args[5] as ReportPeriod
        @Suppress("UNCHECKED_CAST")
        val customRange = args[6] as Pair<Long?, Long?>
        FinanceEngine.computeReports(
            items = items,
            categories = cats,
            budgets = budgets,
            settings = cfg,
            selectedYearMonth = ym,
            period = period,
            customStartMillis = customRange.first,
            customEndMillis = customRange.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    init {
        viewModelScope.launch {
            repository.ensureDefaultCatalogSeeded()
            val currentSettings = repository.settingsFlow.first()
            _isVaultUnlocked.value = !currentSettings.pinEnabled
            refreshVaultStats()
        }
    }

    fun markSplashFinished() {
        _isSplashFinished.value = true
    }

    fun onAppBackgrounded() {
        backgroundedAtMillis = System.currentTimeMillis()
        if (settings.value.pinEnabled && settings.value.autoLockOption == AutoLockOption.IMMEDIATE) {
            _isVaultUnlocked.value = false
        }
    }

    fun onAppForegrounded() {
        val cfg = settings.value
        if (!cfg.pinEnabled) {
            _isVaultUnlocked.value = true
            return
        }
        if (backgroundedAtMillis > 0L) {
            val elapsed = System.currentTimeMillis() - backgroundedAtMillis
            if (elapsed >= cfg.autoLockOption.timeoutMs) {
                _isVaultUnlocked.value = false
            }
        }
    }

    suspend fun attemptPinUnlock(pin: String): Boolean {
        val ok = repository.verifyPin(pin)
        if (ok) _isVaultUnlocked.value = true
        return ok
    }

    fun unlockViaBiometric() {
        _isVaultUnlocked.value = true
    }

    fun refreshVaultStats() {
        viewModelScope.launch {
            _vaultStats.value = repository.getVaultStats()
        }
    }

    fun selectMonth(yearMonth: String) {
        _selectedYearMonth.value = yearMonth
    }

    fun shiftMonth(delta: Int) {
        _selectedYearMonth.value = DateUtils.shiftYearMonthKey(_selectedYearMonth.value, delta)
    }

    fun updateFilter(transform: (TransactionFilterState) -> TransactionFilterState) {
        _filterState.value = transform(_filterState.value)
    }

    fun resetFilters() {
        _filterState.value = TransactionFilterState()
    }

    fun setBudgetFilter(filter: BudgetFilter) {
        _budgetFilter.value = filter
    }

    fun setReportPeriod(period: ReportPeriod) {
        _reportPeriod.value = period
    }

    fun setCustomReportRange(startMillis: Long, endMillis: Long) {
        _reportCustomRange.value = startMillis to endMillis
        _reportPeriod.value = ReportPeriod.CUSTOM
    }

    fun completeOnboarding(
        fullName: String,
        email: String,
        location: String,
        openingBalanceCents: Long,
        includeSampleData: Boolean,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.completeOnboarding(
                fullName = fullName,
                email = email,
                location = location,
                openingBalanceCents = openingBalanceCents,
                includeSampleData = includeSampleData
            )
            _isVaultUnlocked.value = true
            refreshVaultStats()
            onComplete()
        }
    }

    fun saveTransaction(
        txId: Long,
        type: TransactionType,
        amountCents: Long,
        categoryId: Long,
        paymentMethodId: Long,
        toPaymentMethodId: Long?,
        merchant: String,
        note: String,
        receiptPath: String?,
        timestamp: Long,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = txId,
                type = type,
                amountCents = amountCents,
                categoryId = categoryId,
                paymentMethodId = paymentMethodId,
                toPaymentMethodId = if (type == TransactionType.TRANSFER) toPaymentMethodId else null,
                merchant = merchant.trim(),
                note = note.trim(),
                receiptPath = receiptPath,
                timestamp = timestamp,
                isDemo = false
            )
            repository.saveTransaction(entity)
            refreshVaultStats()
            if (type == TransactionType.EXPENSE && settings.value.budgetNotificationsEnabled) {
                evaluateAndNotifyBudgetThresholds(categoryId, timestamp)
            }
            _snackbarEvents.emit(if (txId == 0L) "Transaction saved to local vault" else "Transaction updated")
            onSaved()
        }
    }

    private suspend fun evaluateAndNotifyBudgetThresholds(categoryId: Long, timestamp: Long) {
        val cfg = settings.value
        val ym = DateUtils.currentYearMonthKey(timestamp)
        val items = allTransactionItems.value
        val cats = categories.value
        val budgets = repository.budgetsFlow.first()
        val state = FinanceEngine.computeBudgets(items, cats, budgets, cfg, ym, BudgetFilter.ALL)

        state.categoryBudgets.firstOrNull { it.category.id == categoryId }?.let { card ->
            if (card.isOverspent) {
                BudgetNotificationHelper.postBudgetAlert(
                    appContext,
                    notificationId = card.budget.id.toInt() + 100,
                    title = "${card.category.name} Budget Exceeded",
                    body = "Spent ${MoneyMath.formatAmount(card.spentCents, cfg.currencyOption)} against ${MoneyMath.formatAmount(card.budget.limitCents, cfg.currencyOption)} cap (${card.percentUsed}%)."
                )
            } else if (card.isNearLimit) {
                BudgetNotificationHelper.postBudgetAlert(
                    appContext,
                    notificationId = card.budget.id.toInt() + 100,
                    title = "${card.category.name} Approaching Limit",
                    body = "${card.percentUsed}% of your ${card.category.name} budget used (${MoneyMath.formatAmount(card.remainingCents, cfg.currencyOption)} remaining)."
                )
            }
        }
        if (state.overallCapCents > 0L && state.overallPercentUsed >= cfg.defaultAlertThresholdPercent) {
            BudgetNotificationHelper.postBudgetAlert(
                appContext,
                notificationId = 999,
                title = if (state.overallSpentCents > state.overallCapCents) "Monthly Cap Exceeded" else "Monthly Cap Alert",
                body = "Overall spend reached ${state.overallPercentUsed}% of ${MoneyMath.formatAmount(state.overallCapCents, cfg.currencyOption)}."
            )
        }
    }

    fun deleteTransaction(id: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            refreshVaultStats()
            _snackbarEvents.emit("Transaction removed")
            onDeleted()
        }
    }

    fun saveCustomCategory(
        id: Long = 0L,
        name: String,
        subtitle: String,
        iconKey: String,
        colorHex: String,
        type: TransactionType
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.saveCategory(
                CategoryEntity(
                    id = id,
                    name = name.trim(),
                    subtitle = subtitle.trim().ifBlank { "Custom category" },
                    iconKey = iconKey,
                    colorHex = colorHex,
                    type = type,
                    isCustom = true,
                    sortOrder = categories.value.size + 1
                )
            )
            _snackbarEvents.emit("Category saved")
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            _snackbarEvents.emit("Category deleted")
        }
    }

    fun savePaymentMethod(name: String, iconKey: String = "account_balance_wallet") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.savePaymentMethod(
                PaymentMethodEntity(
                    name = name.trim(),
                    iconKey = iconKey,
                    isCustom = true,
                    sortOrder = paymentMethods.value.size + 1
                )
            )
            _snackbarEvents.emit("Payment method added")
        }
    }

    fun deletePaymentMethod(method: PaymentMethodEntity) {
        viewModelScope.launch {
            repository.deletePaymentMethod(method)
        }
    }

    fun upsertBudget(categoryId: Long?, limitCents: Long, thresholdPercent: Int) {
        viewModelScope.launch {
            repository.upsertBudget(
                yearMonth = _selectedYearMonth.value,
                categoryId = categoryId,
                limitCents = limitCents,
                alertThresholdPercent = thresholdPercent
            )
            refreshVaultStats()
            _snackbarEvents.emit("Budget saved for ${DateUtils.formatMonthYear(_selectedYearMonth.value)}")
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            repository.deleteBudget(budgetId)
            refreshVaultStats()
            _snackbarEvents.emit("Budget deleted")
        }
    }

    fun updateProfile(
        fullName: String,
        email: String,
        location: String,
        openingBalanceCents: Long,
        avatarUri: String?
    ) {
        viewModelScope.launch {
            repository.updateProfile(fullName, email, location, openingBalanceCents, avatarUri)
            _snackbarEvents.emit("Profile updated")
        }
    }

    fun copyReceiptFromPicker(uri: Uri, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            onResult(repository.copyMediaToLocalStorage(uri, "receipts"))
        }
    }

    fun copyAvatarFromPicker(uri: Uri) {
        viewModelScope.launch {
            val path = repository.copyMediaToLocalStorage(uri, "avatars") ?: return@launch
            val current = repository.profileFlow.first()
            if (current != null) {
                repository.updateProfile(
                    current.fullName,
                    current.email,
                    current.location,
                    current.openingBalanceCents,
                    path
                )
                _snackbarEvents.emit("Profile photo updated")
            }
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadSampleDemoData()
            refreshVaultStats()
            _snackbarEvents.emit("Sample Sri Lankan ledger data loaded")
        }
    }

    fun clearDemoData() {
        viewModelScope.launch {
            repository.clearDemoData()
            refreshVaultStats()
            _snackbarEvents.emit("Sample demo transactions cleared")
        }
    }

    // Settings updates
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }
    fun setCurrencyOption(option: CurrencyDisplayOption) = viewModelScope.launch { repository.setCurrencyOption(option) }
    fun setDateFormat(option: DateFormatOption) = viewModelScope.launch { repository.setDateFormat(option) }
    fun setCycleStartDay(day: Int) = viewModelScope.launch { repository.setCycleStartDay(day) }
    fun setFirstDayOfWeek(day: Int) = viewModelScope.launch { repository.setFirstDayOfWeek(day) }
    fun setAutoLockOption(option: AutoLockOption) = viewModelScope.launch { repository.setAutoLockOption(option) }
    fun setHapticsEnabled(enabled: Boolean) = viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    fun setBudgetNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { repository.setBudgetNotificationsEnabled(enabled) }
    fun setDefaultAlertThreshold(percent: Int) = viewModelScope.launch { repository.setDefaultAlertThreshold(percent) }
    fun setBalanceHidden(hidden: Boolean) = viewModelScope.launch { repository.setBalanceHidden(hidden) }

    fun enablePin(pin: String) = viewModelScope.launch {
        repository.enablePin(pin)
        _isVaultUnlocked.value = true
        _snackbarEvents.emit("4-Digit Master PIN enabled")
    }

    fun disablePin() = viewModelScope.launch {
        repository.disablePin()
        _isVaultUnlocked.value = true
        _snackbarEvents.emit("Master PIN disabled")
    }

    fun setBiometricEnabled(enabled: Boolean) = viewModelScope.launch {
        repository.setBiometricEnabled(enabled)
    }

    fun writeEncryptedBackupToUri(uri: Uri, password: String) {
        viewModelScope.launch {
            runCatching {
                val encrypted = repository.exportEncryptedBackup(password)
                withContext(Dispatchers.IO) {
                    appContext.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(encrypted.toByteArray(Charsets.UTF_8))
                    } ?: error("Cannot open destination file")
                }
            }.onSuccess {
                _snackbarEvents.emit("Encrypted AES-256 vault backup saved")
            }.onFailure { err ->
                _snackbarEvents.emit("Backup failed: ${err.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun restoreEncryptedBackupFromUri(uri: Uri, password: String) {
        viewModelScope.launch {
            runCatching {
                val content = withContext(Dispatchers.IO) {
                    appContext.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Cannot read backup file")
                }
                repository.restoreEncryptedBackup(content, password)
                refreshVaultStats()
            }.onSuccess {
                _snackbarEvents.emit("Vault restored successfully from encrypted backup")
            }.onFailure { err ->
                _snackbarEvents.emit("Restore failed: Check your password or backup file")
            }
        }
    }

    fun exportCsvToUri(uri: Uri, items: List<TransactionItem>) {
        viewModelScope.launch {
            runCatching {
                val cfg = settings.value
                withContext(Dispatchers.IO) {
                    appContext.contentResolver.openOutputStream(uri)?.use { out ->
                        exportManager.writeCsv(out, items, cfg.dateFormat, cfg.currencyOption)
                    } ?: error("Cannot open destination URI")
                }
            }.onSuccess {
                _snackbarEvents.emit("CSV financial ledger exported")
            }.onFailure {
                _snackbarEvents.emit("CSV export failed")
            }
        }
    }

    fun exportPdfToUri(uri: Uri, report: ReportsUiState) {
        viewModelScope.launch {
            runCatching {
                val cfg = settings.value
                val owner = dashboardState.value.profile?.fullName ?: "LakExpense User"
                withContext(Dispatchers.IO) {
                    appContext.contentResolver.openOutputStream(uri)?.use { out ->
                        exportManager.writePdfStatement(
                            outputStream = out,
                            ownerName = owner,
                            periodLabel = report.rangeLabel,
                            totalInflowCents = report.totalInflowCents,
                            totalOutflowCents = report.totalOutflowCents,
                            netCashFlowCents = report.netCashFlowCents,
                            categoryBreakdown = report.categoryDistribution,
                            items = report.filteredTransactions,
                            dateFormat = cfg.dateFormat,
                            currency = cfg.currencyOption
                        )
                    } ?: error("Cannot open destination URI")
                }
            }.onSuccess {
                _snackbarEvents.emit("PDF financial statement exported")
            }.onFailure {
                _snackbarEvents.emit("PDF export failed")
            }
        }
    }

    fun buildSharePdfIntent(report: ReportsUiState): Intent {
        val cfg = settings.value
        val owner = dashboardState.value.profile?.fullName ?: "LakExpense User"
        return exportManager.createShareableExportIntent(
            fileName = "LakExpense_Statement_${_selectedYearMonth.value}.pdf",
            mimeType = "application/pdf"
        ) { out ->
            exportManager.writePdfStatement(
                outputStream = out,
                ownerName = owner,
                periodLabel = report.rangeLabel,
                totalInflowCents = report.totalInflowCents,
                totalOutflowCents = report.totalOutflowCents,
                netCashFlowCents = report.netCashFlowCents,
                categoryBreakdown = report.categoryDistribution,
                items = report.filteredTransactions,
                dateFormat = cfg.dateFormat,
                currency = cfg.currencyOption
            )
        }
    }

    fun buildShareCsvIntent(items: List<TransactionItem>): Intent {
        val cfg = settings.value
        return exportManager.createShareableExportIntent(
            fileName = "LakExpense_Ledger_${_selectedYearMonth.value}.csv",
            mimeType = "text/csv"
        ) { out ->
            exportManager.writeCsv(out, items, cfg.dateFormat, cfg.currencyOption)
        }
    }

    fun resetAllVaultData(onResetComplete: () -> Unit) {
        viewModelScope.launch {
            repository.resetAllVaultData()
            _selectedYearMonth.value = DateUtils.currentYearMonthKey()
            _filterState.value = TransactionFilterState()
            _isVaultUnlocked.value = true
            refreshVaultStats()
            _snackbarEvents.emit("Local vault purged cleanly")
            onResetComplete()
        }
    }
}
