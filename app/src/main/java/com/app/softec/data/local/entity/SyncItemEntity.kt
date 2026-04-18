package com.app.softec.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sync_items")
data class SyncItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String?,
    val tags: List<String>,
    val updatedAt: Date,
    val isSynced: Boolean = true
)
