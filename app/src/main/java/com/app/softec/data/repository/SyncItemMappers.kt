package com.app.softec.data.repository

import com.app.softec.data.local.entity.SyncItemEntity
import com.app.softec.data.remote.firestore.SyncItemRemote

fun SyncItemEntity.toRemote(): SyncItemRemote {
    return SyncItemRemote(
        id = id,
        title = title,
        notes = notes,
        tags = tags,
        updatedAt = updatedAt
    )
}

fun SyncItemRemote.toEntity(): SyncItemEntity {
    return SyncItemEntity(
        id = id,
        title = title,
        notes = notes,
        tags = tags,
        updatedAt = updatedAt,
        isSynced = true
    )
}
