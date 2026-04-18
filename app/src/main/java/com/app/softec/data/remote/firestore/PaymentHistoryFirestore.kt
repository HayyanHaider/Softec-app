package com.app.softec.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

@Serializable
data class PaymentHistoryFirestore(
    @DocumentId val id: String = "",
    val accountId: String = "",
    val customerId: String = "",
    val amount: Double = 0.0,
    val paymentDate: Long = 0L,
    val paymentMethod: String? = null, // "cash", "check", "transfer", "card"
    val transactionId: String? = null,
    val notes: String? = null,
    val createdAt: Long = 0L,
    val userId: String = ""
)

