package com.funcap.tracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.funcap.tracker.model.TestType
import com.funcap.tracker.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "funcap_settings"
)

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true, // Material You dynamic palette on Pixel 9 Pro
    val remindersEnabled: Boolean = true,
    val reminderIntervalDays: Int = 30, // Default: once a month (30 days)
    val preferredReminderTest: TestType = TestType.FUNCAP27,
    val lastBackupEpochMillis: Long = 0L
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_INTERVAL_DAYS = intPreferencesKey("reminder_interval_days")
        val PREFERRED_REMINDER_TEST = stringPreferencesKey("preferred_reminder_test")
        val LAST_BACKUP_EPOCH = longPreferencesKey("last_backup_epoch")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val theme = prefs[Keys.THEME_MODE]?.let {
            runCatching { ThemeMode.valueOf(it) }.getOrNull()
        } ?: ThemeMode.SYSTEM

        val preferredTest = prefs[Keys.PREFERRED_REMINDER_TEST]?.let {
            runCatching { TestType.valueOf(it) }.getOrNull()
        } ?: TestType.FUNCAP27

        UserSettings(
            themeMode = theme,
            useDynamicColor = prefs[Keys.USE_DYNAMIC_COLOR] ?: true,
            remindersEnabled = prefs[Keys.REMINDERS_ENABLED] ?: true,
            reminderIntervalDays = (prefs[Keys.REMINDER_INTERVAL_DAYS] ?: 30).coerceIn(1, 365),
            preferredReminderTest = preferredTest,
            lastBackupEpochMillis = prefs[Keys.LAST_BACKUP_EPOCH] ?: 0L
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USE_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updateReminderConfig(
        enabled: Boolean,
        intervalDays: Int,
        preferredTest: TestType
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REMINDERS_ENABLED] = enabled
            prefs[Keys.REMINDER_INTERVAL_DAYS] = intervalDays.coerceIn(1, 365)
            prefs[Keys.PREFERRED_REMINDER_TEST] = preferredTest.name
        }
    }

    suspend fun recordBackupTimestamp(timestampMillis: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_BACKUP_EPOCH] = timestampMillis
        }
    }
}
