package com.app.softec.domain.model

import java.util.Date

data class FollowUp(
    val id: String,
    val accountId: String,
    val customerId: String,
    val followUpDate: Date,
    val status: String, // "pending", "contacted", "completed", "rescheduled"
    val outcome: String? = null, // "no_response", "promise_to_pay", "paid_partial", "paid_full"
    val contactMethod: String? = null,        // "call", "whatsapp", "sms"
    val suggestedMessage: String? = null,
    val actualMessage: String? = null,
    val promiseDate: Date? = null,
    val nextFollowUpDate: Date? = null,
    val createdAt: Date,
    val updatedAt: Date,
    val notes: String? = null
)

