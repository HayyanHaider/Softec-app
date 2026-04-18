package com.app.softec.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "payment_history",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PaymentHistoryEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val customerId: String,
    val amount: Double,
    val paymentDate: Date,
    val paymentMethod: String? = null, // "cash", "check", "transfer", "card"
    val transactionId: String? = null,
    val notes: String? = null,
    val createdAt: Date,
    val userId: String, // Firebase user ID
    val isSynced: Boolean = false
)

