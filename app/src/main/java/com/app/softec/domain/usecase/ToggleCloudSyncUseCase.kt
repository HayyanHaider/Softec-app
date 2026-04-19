package com.app.softec.domain.usecase

import com.app.softec.domain.repository.SettingsRepository
import javax.inject.Inject

class ToggleCloudSyncUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(isEnabled: Boolean) {
        settingsRepository.setCloudSyncEnabled(isEnabled)
    }
}