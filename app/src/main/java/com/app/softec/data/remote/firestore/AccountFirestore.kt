package com.app.softec.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

@Serializable
data class AccountFirestore(
    @DocumentId val id: String = "",
    val customerId: String = "",
    val totalAmountDue: Double = 0.0,
    val amountPaid: Double = 0.0,
    val amountRemaining: Double = 0.0,
    val dueDate: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val lastFollowUpDate: Long? = null,
    val nextFollowUpDate: Long? = null,
    val status: String = "active",
    val notes: String? = null,
    val userId: String = ""
)

