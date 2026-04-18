package com.app.softec.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val isCloudSyncEnabled: Flow<Boolean>
    suspend fun setCloudSyncEnabled(isEnabled: Boolean)
    
    // Add other settings here later, like:
    // val defaultReminderTemplate: Flow<String>
}