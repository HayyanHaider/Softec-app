package com.app.softec.domain.usecase

import com.app.softec.domain.model.Account
import com.app.softec.domain.model.Customer
import javax.inject.Inject

/**
 * Use case for automatically generating professional reminder messages for overdue accounts.
 * Leverages the separated Customer and Account models.
 */
class GenerateReminderMessageUseCase @Inject constructor() {

    /**
     * Automatically generates a message based on account overdue severity. [cite: 6]
     *
     * @param account The overdue account/invoice.
     * @param customer The customer associated with the account.
     * @return A formatted message string.
     */
    operator fun invoke(account: Account, customer: Customer): String {
        return generateMessageBySeverity(account, customer)
    }

    /**
     * Generates a message using a specific template type.
     *
     * @param account The overdue account/invoice details.
     * @param customer The customer's personal details.
     * @param templateType The specific tone to use.
     * @return A formatted message string.
     */
    fun generateWithTemplate(
        account: Account,
        customer: Customer,
        templateType: TemplateType
    ): String {
        val amountStr = formatAmount(account.amountRemaining) // Uses amountRemaining from Account model

        return when (templateType) {
            TemplateType.FRIENDLY -> {
                "Hi ${customer.customerName}, this is a friendly reminder that your account has an outstanding balance of $amountStr. Please let us know if you have any questions. Thank you!"
            }
            TemplateType.STANDARD -> {
                "Dear ${customer.customerName}, our records indicate an overdue balance of $amountStr on your account. Please process this payment at your earliest convenience or contact our billing department."
            }
            TemplateType.URGENT -> {
                "URGENT NOTICE: Dear ${customer.customerName}, your account is significantly past due with a balance of $amountStr. Immediate payment is required to avoid further action."
            }
        }
    }

    private fun generateMessageBySeverity(account: Account, customer: Customer): String {
        // Uses daysOverdue logic from the Account model
        return when {
            account.daysOverdue <= 7 -> generateWithTemplate(account, customer, TemplateType.FRIENDLY)
            account.daysOverdue in 8..30 -> generateWithTemplate(account, customer, TemplateType.STANDARD)
            else -> generateWithTemplate(account, customer, TemplateType.URGENT)
        }
    }

    private fun formatAmount(amount: Double): String {
        return String.format("$%.2f", amount)
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