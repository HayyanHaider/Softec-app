package com.app.softec.data.repository

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.softec.domain.model.ReminderTemplates
import com.app.softec.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings_preferences"
)

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    override val isDarkModeEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[DARK_MODE_ENABLED] ?: defaultDarkModeEnabled()
    }

    override val isCloudSyncEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[CLOUD_SYNC_ENABLED] ?: true
    }

    override val reminderTemplates: Flow<ReminderTemplates> = context.settingsDataStore.data.map { prefs ->
        ReminderTemplates(
            friendly = prefs[FRIENDLY_TEMPLATE] ?: ReminderTemplates.DEFAULT_FRIENDLY,
            standard = prefs[STANDARD_TEMPLATE] ?: ReminderTemplates.DEFAULT_STANDARD,
            urgent = prefs[URGENT_TEMPLATE] ?: ReminderTemplates.DEFAULT_URGENT
        )
    }

    override suspend fun setDarkModeEnabled(isEnabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[DARK_MODE_ENABLED] = isEnabled
        }
    }

    override suspend fun setCloudSyncEnabled(isEnabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[CLOUD_SYNC_ENABLED] = isEnabled
        }
    }

    override suspend fun setReminderTemplates(templates: ReminderTemplates) {
        context.settingsDataStore.edit { prefs ->
            prefs[FRIENDLY_TEMPLATE] = templates.friendly
            prefs[STANDARD_TEMPLATE] = templates.standard
            prefs[URGENT_TEMPLATE] = templates.urgent
        }
    }

    private fun defaultDarkModeEnabled(): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    private companion object {
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
        val FRIENDLY_TEMPLATE = stringPreferencesKey("friendly_reminder_template")
        val STANDARD_TEMPLATE = stringPreferencesKey("standard_reminder_template")
        val URGENT_TEMPLATE = stringPreferencesKey("urgent_reminder_template")
    }
}