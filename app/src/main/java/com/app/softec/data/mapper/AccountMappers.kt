package com.app.softec.data.mapper

import com.app.softec.data.local.entity.AccountEntity
import com.app.softec.data.remote.firestore.AccountFirestore
import com.app.softec.domain.model.Account
import java.util.Date
import java.util.UUID

object AccountMappers {
    
    fun AccountEntity.toDomain(): Account = Account(
        id = id,
        customerId = customerId,
        totalAmountDue = totalAmountDue,
        amountPaid = amountPaid,
        amountRemaining = amountRemaining,
        dueDate = dueDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastFollowUpDate = lastFollowUpDate,
        nextFollowUpDate = nextFollowUpDate,
        status = status,
        notes = notes,
        daysOverdue = calculateDaysOverdue(dueDate)
    )

    fun Account.toEntity(userId: String): AccountEntity = AccountEntity(
        id = id.ifEmpty { UUID.randomUUID().toString() },
        customerId = customerId,
        totalAmountDue = totalAmountDue,
        amountPaid = amountPaid,
        amountRemaining = amountRemaining,
        dueDate = dueDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastFollowUpDate = lastFollowUpDate,
        nextFollowUpDate = nextFollowUpDate,
        status = status,
        notes = notes,
        userId = userId
    )

    fun AccountEntity.toFirestore(): AccountFirestore = AccountFirestore(
        id = id,
        customerId = customerId,
        totalAmountDue = totalAmountDue,
        amountPaid = amountPaid,
        amountRemaining = amountRemaining,
        dueDate = dueDate.time,
        createdAt = createdAt.time,
        updatedAt = updatedAt.time,
        lastFollowUpDate = lastFollowUpDate?.time,
        nextFollowUpDate = nextFollowUpDate?.time,
        status = status,
        notes = notes,
        userId = userId
    )

    fun AccountFirestore.toEntity(userId: String): AccountEntity = AccountEntity(
        id = id,
        customerId = customerId,
        totalAmountDue = totalAmountDue,
        amountPaid = amountPaid,
        amountRemaining = amountRemaining,
        dueDate = Date(dueDate),
        createdAt = Date(createdAt),
        updatedAt = Date(updatedAt),
        lastFollowUpDate = lastFollowUpDate?.let { Date(it) },
        nextFollowUpDate = nextFollowUpDate?.let { Date(it) },
        status = status,
        notes = notes,
        userId = userId,
        isSynced = true
    )

    private fun calculateDaysOverdue(dueDate: Date): Int {
        val today = Date()
        if (today <= dueDate) return 0
        return ((today.time - dueDate.time) / (1000 * 60 * 60 * 24)).toInt()
    }
}

