package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppSettings
import com.example.data.model.AutoLockOption
import com.example.data.model.CurrencyDisplayOption
import com.example.data.model.DateFormatOption
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lakexpense_settings")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY_OPTION = stringPreferencesKey("currency_option")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val CYCLE_START_DAY = intPreferencesKey("cycle_start_day")
        val FIRST_DAY_OF_WEEK = intPreferencesKey("first_day_of_week")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val PIN_ENCRYPTED_BLOB = stringPreferencesKey("pin_encrypted_blob")
        val PIN_UPDATED_AT = longPreferencesKey("pin_updated_at")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val AUTO_LOCK_OPTION = stringPreferencesKey("auto_lock_option")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val BUDGET_NOTIFICATIONS_ENABLED = booleanPreferencesKey("budget_notifications_enabled")
        val DEFAULT_ALERT_THRESHOLD = intPreferencesKey("default_alert_threshold")
        val BALANCE_HIDDEN = booleanPreferencesKey("balance_hidden")
        val LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            themeMode = prefs[Keys.THEME_MODE]?.let { name ->
                ThemeMode.entries.firstOrNull { it.name == name }
            } ?: ThemeMode.DARK,
            currencyOption = prefs[Keys.CURRENCY_OPTION]?.let { name ->
                CurrencyDisplayOption.entries.firstOrNull { it.name == name }
            } ?: CurrencyDisplayOption.RS_PREFIX,
            dateFormat = prefs[Keys.DATE_FORMAT]?.let { name ->
                DateFormatOption.entries.firstOrNull { it.name == name }
            } ?: DateFormatOption.DD_MM_YYYY,
            cycleStartDay = (prefs[Keys.CYCLE_START_DAY] ?: 1).coerceIn(1, 28),
            firstDayOfWeek = prefs[Keys.FIRST_DAY_OF_WEEK] ?: Calendar.MONDAY,
            pinEnabled = (prefs[Keys.PIN_ENABLED] ?: false) && !prefs[Keys.PIN_ENCRYPTED_BLOB].isNullOrEmpty(),
            biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
            autoLockOption = prefs[Keys.AUTO_LOCK_OPTION]?.let { name ->
                AutoLockOption.entries.firstOrNull { it.name == name }
            } ?: AutoLockOption.IMMEDIATE,
            pinUpdatedAt = prefs[Keys.PIN_UPDATED_AT] ?: 0L,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            budgetNotificationsEnabled = prefs[Keys.BUDGET_NOTIFICATIONS_ENABLED] ?: true,
            defaultAlertThresholdPercent = (prefs[Keys.DEFAULT_ALERT_THRESHOLD] ?: 80).coerceIn(50, 95),
            balanceHidden = prefs[Keys.BALANCE_HIDDEN] ?: false,
            lastBackupTimestamp = prefs[Keys.LAST_BACKUP_TIMESTAMP] ?: 0L
        )
    }

    suspend fun getPinEncryptedBlob(): String? {
        return context.dataStore.data.first()[Keys.PIN_ENCRYPTED_BLOB]
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setCurrencyOption(option: CurrencyDisplayOption) {
        context.dataStore.edit { it[Keys.CURRENCY_OPTION] = option.name }
    }

    suspend fun setDateFormat(option: DateFormatOption) {
        context.dataStore.edit { it[Keys.DATE_FORMAT] = option.name }
    }

    suspend fun setCycleStartDay(day: Int) {
        context.dataStore.edit { it[Keys.CYCLE_START_DAY] = day.coerceIn(1, 28) }
    }

    suspend fun setFirstDayOfWeek(dayOfWeek: Int) {
        context.dataStore.edit { it[Keys.FIRST_DAY_OF_WEEK] = dayOfWeek }
    }

    suspend fun setPinCredentials(encryptedBlob: String) {
        context.dataStore.edit {
            it[Keys.PIN_ENCRYPTED_BLOB] = encryptedBlob
            it[Keys.PIN_ENABLED] = true
            it[Keys.PIN_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    suspend fun disablePin() {
        context.dataStore.edit {
            it[Keys.PIN_ENABLED] = false
            it.remove(Keys.PIN_ENCRYPTED_BLOB)
            it[Keys.BIOMETRIC_ENABLED] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setAutoLockOption(option: AutoLockOption) {
        context.dataStore.edit { it[Keys.AUTO_LOCK_OPTION] = option.name }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setBudgetNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BUDGET_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setDefaultAlertThresholdPercent(percent: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_ALERT_THRESHOLD] = percent.coerceIn(50, 95) }
    }

    suspend fun setBalanceHidden(hidden: Boolean) {
        context.dataStore.edit { it[Keys.BALANCE_HIDDEN] = hidden }
    }

    suspend fun setLastBackupTimestamp(timestamp: Long) {
        context.dataStore.edit { it[Keys.LAST_BACKUP_TIMESTAMP] = timestamp }
    }

    suspend fun clearAllSettings() {
        context.dataStore.edit { it.clear() }
    }
}
