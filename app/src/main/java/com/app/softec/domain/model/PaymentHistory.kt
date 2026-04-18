package com.app.softec.domain.model

import java.util.Date

data class PaymentHistory(
    val id: String,
    val accountId: String,
    val customerId: String,
    val amount: Double,
    val paymentDate: Date,
    val paymentMethod: String? = null,
    val transactionId: String? = null,
    val notes: String? = null,
    val createdAt: Date
)

