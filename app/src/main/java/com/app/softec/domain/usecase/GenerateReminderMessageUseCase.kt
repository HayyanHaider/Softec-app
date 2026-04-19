package com.app.softec.domain.usecase

import com.app.softec.domain.model.Account
import com.app.softec.domain.model.Customer
import com.app.softec.domain.model.ReminderTemplates
import com.app.softec.domain.repository.SettingsRepository
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Use case for automatically generating professional reminder messages for overdue accounts.
 * Leverages the separated Customer and Account models.
 */
class GenerateReminderMessageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    /**
     * Automatically generates a message based on account overdue severity. [cite: 6]
     *
     * @param account The overdue account/invoice.
     * @param customer The customer associated with the account.
     * @return A formatted message string.
     */
    suspend operator fun invoke(account: Account, customer: Customer): String {
        val templates = settingsRepository.reminderTemplates.first()
        return generateMessageBySeverity(account, customer, templates)
    }

    /**
     * Generates a message using a specific template type.
     *
     * @param account The overdue account/invoice details.
     * @param customer The customer's personal details.
     * @param templateType The specific tone to use.
     * @return A formatted message string.
     */
    suspend fun generateWithTemplate(
        account: Account,
        customer: Customer,
        templateType: TemplateType
    ): String {
        val templates = settingsRepository.reminderTemplates.first()
        return generateWithTemplate(account, customer, templateType, templates)
    }

    private fun generateWithTemplate(
        account: Account,
        customer: Customer,
        templateType: TemplateType,
        templates: ReminderTemplates,
        currencyPrefix: String
    ): String {
        val amountStr = formatAmount(account.amountRemaining, currencyPrefix)
        val rawTemplate = when (templateType) {
            TemplateType.FRIENDLY -> templates.friendly
            TemplateType.STANDARD -> templates.standard
            TemplateType.URGENT -> templates.urgent
        }

        return rawTemplate
            .replace("{name}", customer.customerName)
            .replace("{amount}", amountStr)
    }

    private suspend fun generateMessageBySeverity(
        account: Account,
        customer: Customer,
        templates: ReminderTemplates
    ): String {
        val currencyPrefix = settingsRepository.currencyPrefix.first()
        // Uses daysOverdue logic from the Account model
        return when {
            account.daysOverdue <= 7 -> {
                generateWithTemplate(account, customer, TemplateType.FRIENDLY, templates, currencyPrefix)
            }
            account.daysOverdue in 8..30 -> {
                generateWithTemplate(account, customer, TemplateType.STANDARD, templates, currencyPrefix)
            }
            else -> {
                generateWithTemplate(account, customer, TemplateType.URGENT, templates, currencyPrefix)
            }
        }
    }

    private suspend fun generateWithTemplate(
        account: Account,
        customer: Customer,
        templateType: TemplateType,
        templates: ReminderTemplates
    ): String {
        val currencyPrefix = settingsRepository.currencyPrefix.first()
        return generateWithTemplate(account, customer, templateType, templates, currencyPrefix)
    }

    private fun formatAmount(amount: Double, currencyPrefix: String): String {
        val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.getDefault()))
        val sanitizedPrefix = currencyPrefix.trim().take(3)
        return if (sanitizedPrefix.isBlank()) {
            formatter.format(amount)
        } else {
            "$sanitizedPrefix ${formatter.format(amount)}"
        }
    }

    /**
     * Defines the available message templates based on urgency.
     */
    enum class TemplateType {
        FRIENDLY,  // For initial reminders (< 7 days overdue)
        STANDARD,  // For moderate delays (8 - 30 days overdue)
        URGENT     // For severe delays (> 30 days overdue)
    }
}