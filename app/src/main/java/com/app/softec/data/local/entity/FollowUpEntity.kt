package com.app.softec.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "follow_ups",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FollowUpEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val customerId: String,
    val followUpDate: Date,
    val status: String, // "pending", "contacted", "completed", "rescheduled", "not_reachable"
    val outcome: String? = null, // "no_response", "promise_to_pay", "paid_partial", "paid_full", "declined"
    val contactMethod: String? = null, // "call", "whatsapp", "sms", "in_person"
    val suggestedMessage: String? = null,
    val actualMessage: String? = null,
    val promiseDate: Date? = null,
    val nextFollowUpDate: Date? = null,
    val createdAt: Date,
    val updatedAt: Date,
    val notes: String? = null,
    val userId: String, // Firebase user ID
    val isSynced: Boolean = false
)

