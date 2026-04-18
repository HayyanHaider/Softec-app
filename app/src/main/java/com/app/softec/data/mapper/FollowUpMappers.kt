package com.app.softec.data.mapper

import com.app.softec.data.local.entity.FollowUpEntity
import com.app.softec.data.remote.firestore.FollowUpFirestore
import com.app.softec.domain.model.FollowUp
import java.util.Date
import java.util.UUID

object FollowUpMappers {
    
    fun FollowUpEntity.toDomain(): FollowUp = FollowUp(
        id = id,
        accountId = accountId,
        customerId = customerId,
        followUpDate = followUpDate,
        status = status,
        outcome = outcome,
        contactMethod = contactMethod,
        suggestedMessage = suggestedMessage,
        actualMessage = actualMessage,
        promiseDate = promiseDate,
        nextFollowUpDate = nextFollowUpDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        notes = notes
    )

    fun FollowUp.toEntity(userId: String): FollowUpEntity = FollowUpEntity(
        id = id.ifEmpty { UUID.randomUUID().toString() },
        accountId = accountId,
        customerId = customerId,
        followUpDate = followUpDate,
        status = status,
        outcome = outcome,
        contactMethod = contactMethod,
        suggestedMessage = suggestedMessage,
        actualMessage = actualMessage,
        promiseDate = promiseDate,
        nextFollowUpDate = nextFollowUpDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        notes = notes,
        userId = userId
    )

    fun FollowUpEntity.toFirestore(): FollowUpFirestore = FollowUpFirestore(
        id = id,
        accountId = accountId,
        customerId = customerId,
        followUpDate = followUpDate.time,
        status = status,
        outcome = outcome,
        contactMethod = contactMethod,
        suggestedMessage = suggestedMessage,
        actualMessage = actualMessage,
        promiseDate = promiseDate?.time,
        nextFollowUpDate = nextFollowUpDate?.time,
        createdAt = createdAt.time,
        updatedAt = updatedAt.time,
        notes = notes,
        userId = userId
    )

    fun FollowUpFirestore.toEntity(userId: String): FollowUpEntity = FollowUpEntity(
        id = id,
        accountId = accountId,
        customerId = customerId,
        followUpDate = Date(followUpDate),
        status = status,
        outcome = outcome,
        contactMethod = contactMethod,
        suggestedMessage = suggestedMessage,
        actualMessage = actualMessage,
        promiseDate = promiseDate?.let { Date(it) },
        nextFollowUpDate = nextFollowUpDate?.let { Date(it) },
        createdAt = Date(createdAt),
        updatedAt = Date(updatedAt),
        notes = notes,
        userId = userId,
        isSynced = true
    )
}

