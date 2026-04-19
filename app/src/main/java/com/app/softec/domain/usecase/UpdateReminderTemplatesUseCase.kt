package com.app.softec.domain.usecase

import com.app.softec.domain.model.ReminderTemplates
import com.app.softec.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateReminderTemplatesUseCase @Inject constructor(
	private val settingsRepository: SettingsRepository
) {
	suspend operator fun invoke(templates: ReminderTemplates) {
		settingsRepository.setReminderTemplates(templates)
	}
}
