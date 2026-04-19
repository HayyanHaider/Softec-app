package com.app.softec.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.softec.domain.model.ReminderTemplates
import com.app.softec.domain.repository.SettingsRepository
import com.app.softec.domain.usecase.ToggleCloudSyncUseCase
import com.app.softec.domain.usecase.UpdateReminderTemplatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkModeEnabled: Boolean = false,
    val isCloudSyncEnabled: Boolean = true,
    val reminderTemplates: ReminderTemplates = ReminderTemplates()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val toggleCloudSyncUseCase: ToggleCloudSyncUseCase,
    private val updateReminderTemplatesUseCase: UpdateReminderTemplatesUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.isDarkModeEnabled,
        settingsRepository.isCloudSyncEnabled,
        settingsRepository.reminderTemplates
    ) { isDarkModeEnabled, isCloudSyncEnabled, reminderTemplates ->
        SettingsUiState(
            isDarkModeEnabled = isDarkModeEnabled,
            isCloudSyncEnabled = isCloudSyncEnabled,
            reminderTemplates = reminderTemplates
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setDarkModeEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkModeEnabled(isEnabled)
        }
    }

    fun setCloudSyncEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            toggleCloudSyncUseCase(isEnabled)
        }
    }

    fun updateReminderTemplates(templates: ReminderTemplates) {
        val cleaned = ReminderTemplates(
            friendly = templates.friendly.trim(),
            standard = templates.standard.trim(),
            urgent = templates.urgent.trim()
        )

        if (cleaned.friendly.isBlank() || cleaned.standard.isBlank() || cleaned.urgent.isBlank()) {
            return
        }

        viewModelScope.launch {
            updateReminderTemplatesUseCase(cleaned)
        }
    }
}