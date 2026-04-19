package com.app.softec.domain.model

data class ReminderTemplates(
    val friendly: String = DEFAULT_FRIENDLY,
    val standard: String = DEFAULT_STANDARD,
    val urgent: String = DEFAULT_URGENT
) {
    companion object {
        const val DEFAULT_FRIENDLY =
            "Hi {name}, this is a friendly reminder that your account has an outstanding balance of {amount}. Please let us know if you have any questions. Thank you!"
        const val DEFAULT_STANDARD =
            "Dear {name}, our records indicate an overdue balance of {amount} on your account. Please process this payment at your earliest convenience or contact our billing department."
        const val DEFAULT_URGENT =
            "URGENT NOTICE: Dear {name}, your account is significantly past due with a balance of {amount}. Immediate payment is required to avoid further action."
    }
}