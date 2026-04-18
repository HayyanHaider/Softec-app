package com.app.softec.data.mapper

import com.app.softec.data.local.entity.PaymentHistoryEntity
import com.app.softec.data.remote.firestore.PaymentHistoryFirestore
import com.app.softec.domain.model.PaymentHistory
import java.util.Date
import java.util.UUID

object PaymentHistoryMappers {
    
    fun PaymentHistoryEntity.toDomain(): PaymentHistory = PaymentHistory(
        id = id,
        accountId = accountId,
        customerId = customerId,
        amount = amount,
        paymentDate = paymentDate,
        paymentMethod = paymentMethod,
        transactionId = transactionId,
        notes = notes,
        createdAt = createdAt
    )

    fun PaymentHistory.toEntity(userId: String): PaymentHistoryEntity = PaymentHistoryEntity(
        id = id.ifEmpty { UUID.randomUUID().toString() },
        accountId = accountId,
        customerId = customerId,
        amount = amount,
        paymentDate = paymentDate,
        paymentMethod = paymentMethod,
        transactionId = transactionId,
        notes = notes,
        createdAt = createdAt,
        userId = userId
    )

    fun PaymentHistoryEntity.toFirestore(): PaymentHistoryFirestore = PaymentHistoryFirestore(
        id = id,
        accountId = accountId,
        customerId = customerId,
        amount = amount,
        paymentDate = paymentDate.time,
        paymentMethod = paymentMethod,
        transactionId = transactionId,
        notes = notes,
        createdAt = createdAt.time,
        userId = userId
    )

    fun PaymentHistoryFirestore.toEntity(userId: String): PaymentHistoryEntity = PaymentHistoryEntity(
        id = id,
        accountId = accountId,
        customerId = customerId,
        amount = amount,
        paymentDate = Date(paymentDate),
        paymentMethod = paymentMethod,
        transactionId = transactionId,
        notes = notes,
        createdAt = Date(createdAt),
        userId = userId,
        isSynced = true
    )
}

