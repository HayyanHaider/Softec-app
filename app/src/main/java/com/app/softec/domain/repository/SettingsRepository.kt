package com.app.softec.domain.repository

import com.app.softec.domain.model.ReminderTemplates
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val isDarkModeEnabled: Flow<Boolean>
    val isCloudSyncEnabled: Flow<Boolean>
    val reminderTemplates: Flow<ReminderTemplates>
    val geminiApiKey: Flow<String?>

    suspend fun setDarkModeEnabled(isEnabled: Boolean)
    suspend fun setCloudSyncEnabled(isEnabled: Boolean)
    suspend fun setReminderTemplates(templates: ReminderTemplates)
    suspend fun setGeminiApiKey(apiKey: String)
}