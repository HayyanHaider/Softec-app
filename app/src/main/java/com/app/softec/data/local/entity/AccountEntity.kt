package com.app.softec.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val totalAmountDue: Double,
    val amountPaid: Double = 0.0,
    val amountRemaining: Double,
    val dueDate: Date,
    val createdAt: Date,
    val updatedAt: Date,
    val lastFollowUpDate: Date? = null,
    val nextFollowUpDate: Date? = null,
    val status: String, // "active", "paid", "overdue", "partial"
    val notes: String? = null,
    val userId: String, // Firebase user ID
    val isSynced: Boolean = false
)

