package com.app.softec.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

@Serializable
data class FollowUpFirestore(
    @DocumentId val id: String = "",
    val accountId: String = "",
    val customerId: String = "",
    val followUpDate: Long = 0L,
    val status: String = "pending", // "pending", "contacted", "completed", "rescheduled"
    val outcome: String? = null, // "no_response", "promise_to_pay", "paid_partial", "paid_full"
    val contactMethod: String? = null, // "call", "whatsapp", "sms"
    val suggestedMessage: String? = null,
    val actualMessage: String? = null,
    val promiseDate: Long? = null,
    val nextFollowUpDate: Long? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val notes: String? = null,
    val userId: String = ""
)

