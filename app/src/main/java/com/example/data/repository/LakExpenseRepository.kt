package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.DefaultCatalog
import com.example.data.local.DemoDataSeeder
import com.example.data.local.LakExpenseDatabase
import com.example.data.local.PaymentMethodEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.TransactionEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.AppSettings
import com.example.data.model.AutoLockOption
import com.example.data.model.CurrencyDisplayOption
import com.example.data.model.DateFormatOption
import com.example.data.model.ThemeMode
import com.example.data.model.TransactionType
import com.example.data.model.VaultStats
import com.example.security.VaultSecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class LakExpenseRepository(
    private val context: Context,
    private val db: LakExpenseDatabase,
    private val prefs: PreferencesManager,
    val security: VaultSecurityManager
) {
    val profileFlow: Flow<UserProfileEntity?> = db.userProfileDao().observeProfile()
    val categoriesFlow: Flow<List<CategoryEntity>> = db.categoryDao().observeAll()
    val paymentMethodsFlow: Flow<List<PaymentMethodEntity>> = db.paymentMethodDao().observeAll()
    val transactionsFlow: Flow<List<TransactionEntity>> = db.transactionDao().observeAll()
    val budgetsFlow: Flow<List<BudgetEntity>> = db.budgetDao().observeAll()
    val settingsFlow: Flow<AppSettings> = prefs.settingsFlow

    suspend fun ensureDefaultCatalogSeeded() = withContext(Dispatchers.IO) {
        if (db.categoryDao().count() == 0) {
            db.categoryDao().insertAll(DefaultCatalog.defaultCategories())
        }
        if (db.paymentMethodDao().count() == 0) {
            db.paymentMethodDao().insertAll(DefaultCatalog.defaultPaymentMethods())
        }
    }

    suspend fun completeOnboarding(
        fullName: String,
        email: String,
        location: String,
        openingBalanceCents: Long,
        includeSampleData: Boolean
    ) = withContext(Dispatchers.IO) {
        ensureDefaultCatalogSeeded()
        val existing = db.userProfileDao().getProfile()
        val now = System.currentTimeMillis()
        db.userProfileDao().upsert(
            UserProfileEntity(
                id = 1,
                fullName = fullName.trim().ifBlank { "LakExpense User" },
                email = email.trim(),
                location = location.trim().ifBlank { "Colombo, Sri Lanka" },
                openingBalanceCents = openingBalanceCents,
                avatarUri = existing?.avatarUri,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
        if (includeSampleData) {
            DemoDataSeeder.seedDemoData(db, now)
        }
        prefs.setOnboardingCompleted(true)
    }

    suspend fun updateProfile(
        fullName: String,
        email: String,
        location: String,
        openingBalanceCents: Long,
        avatarUri: String?
    ) = withContext(Dispatchers.IO) {
        val current = db.userProfileDao().getProfile()
        val now = System.currentTimeMillis()
        db.userProfileDao().upsert(
            UserProfileEntity(
                id = 1,
                fullName = fullName.trim().ifBlank { "LakExpense User" },
                email = email.trim(),
                location = location.trim().ifBlank { "Colombo, Sri Lanka" },
                openingBalanceCents = openingBalanceCents,
                avatarUri = avatarUri,
                createdAt = current?.createdAt ?: now,
                updatedAt = now
            )
        )
    }

    suspend fun copyMediaToLocalStorage(uri: Uri, subFolder: String): String? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, subFolder).apply { mkdirs() }
            val file = File(dir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveTransaction(tx: TransactionEntity): Long = withContext(Dispatchers.IO) {
        if (tx.id == 0L) {
            db.transactionDao().insert(tx)
        } else {
            db.transactionDao().update(tx)
            tx.id
        }
    }

    suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        val existing = db.transactionDao().getById(id)
        existing?.receiptPath?.let { path ->
            runCatching { File(path).delete() }
        }
        db.transactionDao().deleteById(id)
    }

    suspend fun loadSampleDemoData() = withContext(Dispatchers.IO) {
        ensureDefaultCatalogSeeded()
        DemoDataSeeder.seedDemoData(db)
    }

    suspend fun clearDemoData() = withContext(Dispatchers.IO) {
        db.transactionDao().deleteDemoTransactions()
    }

    suspend fun saveCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        if (category.id == 0L) {
            db.categoryDao().insert(category)
        } else {
            db.categoryDao().update(category)
            category.id
        }
    }

    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        db.categoryDao().delete(category)
    }

    suspend fun savePaymentMethod(method: PaymentMethodEntity): Long = withContext(Dispatchers.IO) {
        if (method.id == 0L) {
            db.paymentMethodDao().insert(method)
        } else {
            db.paymentMethodDao().update(method)
            method.id
        }
    }

    suspend fun deletePaymentMethod(method: PaymentMethodEntity) = withContext(Dispatchers.IO) {
        db.paymentMethodDao().delete(method)
    }

    suspend fun upsertBudget(
        yearMonth: String,
        categoryId: Long?,
        limitCents: Long,
        alertThresholdPercent: Int
    ) = withContext(Dispatchers.IO) {
        val existing = if (categoryId == null) {
            db.budgetDao().getOverallForMonth(yearMonth)
        } else {
            db.budgetDao().getCategoryBudget(yearMonth, categoryId)
        }
        if (existing != null) {
            db.budgetDao().update(
                existing.copy(
                    limitCents = limitCents,
                    alertThresholdPercent = alertThresholdPercent.coerceIn(50, 95)
                )
            )
        } else {
            db.budgetDao().insert(
                BudgetEntity(
                    yearMonth = yearMonth,
                    categoryId = categoryId,
                    limitCents = limitCents,
                    alertThresholdPercent = alertThresholdPercent.coerceIn(50, 95)
                )
            )
        }
    }

    suspend fun deleteBudget(budgetId: Long) = withContext(Dispatchers.IO) {
        db.budgetDao().deleteById(budgetId)
    }

    suspend fun getVaultStats(): VaultStats = withContext(Dispatchers.IO) {
        val transactions = db.transactionDao().getAll()
        val budgets = db.budgetDao().getAll()
        val receiptDir = File(context.filesDir, "receipts")
        val receiptFiles = receiptDir.listFiles()?.toList().orEmpty()
        val dbFile = context.getDatabasePath(LakExpenseDatabase.DB_NAME)
        val walFile = File(dbFile.path + "-wal")
        val totalBytes = (if (dbFile.exists()) dbFile.length() else 0L) +
            (if (walFile.exists()) walFile.length() else 0L) +
            receiptFiles.sumOf { it.length() }

        VaultStats(
            transactionCount = transactions.size,
            budgetCount = budgets.size,
            receiptCount = transactions.count { !it.receiptPath.isNullOrBlank() },
            databaseBytes = totalBytes,
            hasDemoData = transactions.any { it.isDemo }
        )
    }

    // Settings mutations
    suspend fun setThemeMode(mode: ThemeMode) = prefs.setThemeMode(mode)
    suspend fun setCurrencyOption(option: CurrencyDisplayOption) = prefs.setCurrencyOption(option)
    suspend fun setDateFormat(option: DateFormatOption) = prefs.setDateFormat(option)
    suspend fun setCycleStartDay(day: Int) = prefs.setCycleStartDay(day)
    suspend fun setFirstDayOfWeek(dayOfWeek: Int) = prefs.setFirstDayOfWeek(dayOfWeek)
    suspend fun setAutoLockOption(option: AutoLockOption) = prefs.setAutoLockOption(option)
    suspend fun setHapticsEnabled(enabled: Boolean) = prefs.setHapticsEnabled(enabled)
    suspend fun setBudgetNotificationsEnabled(enabled: Boolean) = prefs.setBudgetNotificationsEnabled(enabled)
    suspend fun setDefaultAlertThreshold(percent: Int) = prefs.setDefaultAlertThresholdPercent(percent)
    suspend fun setBalanceHidden(hidden: Boolean) = prefs.setBalanceHidden(hidden)

    suspend fun enablePin(pin: String) = withContext(Dispatchers.IO) {
        val blob = security.createPinEncryptedBlob(pin)
        prefs.setPinCredentials(blob)
    }

    suspend fun disablePin() = prefs.disablePin()

    suspend fun verifyPin(pin: String): Boolean = withContext(Dispatchers.IO) {
        val blob = prefs.getPinEncryptedBlob() ?: return@withContext false
        security.verifyPin(pin, blob)
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        prefs.setBiometricEnabled(enabled && security.isBiometricAvailable())
    }

    // Encrypted Vault Export & Restore
    suspend fun exportEncryptedBackup(password: String): String = withContext(Dispatchers.IO) {
        val profile = db.userProfileDao().getProfile()
        val categories = db.categoryDao().getAll()
        val methods = db.paymentMethodDao().getAll()
        val transactions = db.transactionDao().getAll()
        val budgets = db.budgetDao().getAll()

        val root = JSONObject().apply {
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            if (profile != null) {
                put("profile", JSONObject().apply {
                    put("fullName", profile.fullName)
                    put("email", profile.email)
                    put("location", profile.location)
                    put("openingBalanceCents", profile.openingBalanceCents)
                    put("createdAt", profile.createdAt)
                })
            }
            put("categories", JSONArray().apply {
                categories.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                        put("subtitle", c.subtitle)
                        put("iconKey", c.iconKey)
                        put("colorHex", c.colorHex)
                        put("type", c.type.name)
                        put("isCustom", c.isCustom)
                        put("sortOrder", c.sortOrder)
                    })
                }
            })
            put("paymentMethods", JSONArray().apply {
                methods.forEach { m ->
                    put(JSONObject().apply {
                        put("id", m.id)
                        put("name", m.name)
                        put("iconKey", m.iconKey)
                        put("isCustom", m.isCustom)
                        put("sortOrder", m.sortOrder)
                    })
                }
            })
            put("transactions", JSONArray().apply {
                transactions.forEach { t ->
                    put(JSONObject().apply {
                        put("id", t.id)
                        put("type", t.type.name)
                        put("amountCents", t.amountCents)
                        put("categoryId", t.categoryId)
                        put("paymentMethodId", t.paymentMethodId)
                        if (t.toPaymentMethodId != null) put("toPaymentMethodId", t.toPaymentMethodId)
                        put("merchant", t.merchant)
                        put("note", t.note)
                        put("timestamp", t.timestamp)
                        put("isDemo", t.isDemo)
                    })
                }
            })
            put("budgets", JSONArray().apply {
                budgets.forEach { b ->
                    put(JSONObject().apply {
                        put("id", b.id)
                        put("yearMonth", b.yearMonth)
                        if (b.categoryId != null) put("categoryId", b.categoryId)
                        put("limitCents", b.limitCents)
                        put("alertThresholdPercent", b.alertThresholdPercent)
                        put("createdAt", b.createdAt)
                    })
                }
            })
        }
        val encrypted = security.encryptBackupPayload(root.toString(), password)
        prefs.setLastBackupTimestamp(System.currentTimeMillis())
        encrypted
    }

    suspend fun restoreEncryptedBackup(encryptedContent: String, password: String) = withContext(Dispatchers.IO) {
        val plain = security.decryptBackupPayload(encryptedContent, password)
        val root = JSONObject(plain)

        val profileObj = root.optJSONObject("profile")
        val catArray = root.optJSONArray("categories") ?: JSONArray()
        val methodArray = root.optJSONArray("paymentMethods") ?: JSONArray()
        val txArray = root.optJSONArray("transactions") ?: JSONArray()
        val budgetArray = root.optJSONArray("budgets") ?: JSONArray()

        db.transactionDao().deleteAll()
        db.budgetDao().deleteAll()
        db.categoryDao().deleteAll()
        db.paymentMethodDao().deleteAll()
        db.userProfileDao().deleteAll()

        if (profileObj != null) {
            db.userProfileDao().upsert(
                UserProfileEntity(
                    id = 1,
                    fullName = profileObj.optString("fullName", "LakExpense User"),
                    email = profileObj.optString("email", ""),
                    location = profileObj.optString("location", "Colombo, Sri Lanka"),
                    openingBalanceCents = profileObj.optLong("openingBalanceCents", 0L),
                    createdAt = profileObj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = System.currentTimeMillis()
                )
            )
            prefs.setOnboardingCompleted(true)
        }

        val categories = buildList {
            for (i in 0 until catArray.length()) {
                val o = catArray.getJSONObject(i)
                add(
                    CategoryEntity(
                        id = o.optLong("id", 0L),
                        name = o.getString("name"),
                        subtitle = o.optString("subtitle", ""),
                        iconKey = o.optString("iconKey", "category"),
                        colorHex = o.optString("colorHex", "#10B981"),
                        type = TransactionType.entries.firstOrNull { it.name == o.optString("type") }
                            ?: TransactionType.EXPENSE,
                        isCustom = o.optBoolean("isCustom", false),
                        sortOrder = o.optInt("sortOrder", i)
                    )
                )
            }
        }
        if (categories.isNotEmpty()) db.categoryDao().insertAll(categories) else ensureDefaultCatalogSeeded()

        val methods = buildList {
            for (i in 0 until methodArray.length()) {
                val o = methodArray.getJSONObject(i)
                add(
                    PaymentMethodEntity(
                        id = o.optLong("id", 0L),
                        name = o.getString("name"),
                        iconKey = o.optString("iconKey", "payments"),
                        isCustom = o.optBoolean("isCustom", false),
                        sortOrder = o.optInt("sortOrder", i)
                    )
                )
            }
        }
        if (methods.isNotEmpty()) db.paymentMethodDao().insertAll(methods)

        val txs = buildList {
            for (i in 0 until txArray.length()) {
                val o = txArray.getJSONObject(i)
                add(
                    TransactionEntity(
                        id = o.optLong("id", 0L),
                        type = TransactionType.entries.firstOrNull { it.name == o.optString("type") }
                            ?: TransactionType.EXPENSE,
                        amountCents = o.getLong("amountCents"),
                        categoryId = o.getLong("categoryId"),
                        paymentMethodId = o.getLong("paymentMethodId"),
                        toPaymentMethodId = if (o.has("toPaymentMethodId") && !o.isNull("toPaymentMethodId")) {
                            o.getLong("toPaymentMethodId")
                        } else null,
                        merchant = o.optString("merchant", ""),
                        note = o.optString("note", ""),
                        timestamp = o.getLong("timestamp"),
                        isDemo = o.optBoolean("isDemo", false)
                    )
                )
            }
        }
        if (txs.isNotEmpty()) db.transactionDao().insertAll(txs)

        val budgets = buildList {
            for (i in 0 until budgetArray.length()) {
                val o = budgetArray.getJSONObject(i)
                add(
                    BudgetEntity(
                        id = o.optLong("id", 0L),
                        yearMonth = o.getString("yearMonth"),
                        categoryId = if (o.has("categoryId") && !o.isNull("categoryId")) {
                            o.getLong("categoryId")
                        } else null,
                        limitCents = o.getLong("limitCents"),
                        alertThresholdPercent = o.optInt("alertThresholdPercent", 80),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }
        if (budgets.isNotEmpty()) db.budgetDao().insertAll(budgets)
    }

    suspend fun resetAllVaultData() = withContext(Dispatchers.IO) {
        db.transactionDao().deleteAll()
        db.budgetDao().deleteAll()
        db.categoryDao().deleteAll()
        db.paymentMethodDao().deleteAll()
        db.userProfileDao().deleteAll()
        File(context.filesDir, "receipts").deleteRecursively()
        File(context.filesDir, "avatars").deleteRecursively()
        prefs.clearAllSettings()
        ensureDefaultCatalogSeeded()
    }
}
