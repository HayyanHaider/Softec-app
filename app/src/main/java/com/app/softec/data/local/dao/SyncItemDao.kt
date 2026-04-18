package com.app.softec.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.app.softec.data.local.entity.SyncItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncItemDao : BaseDao<SyncItemEntity> {
    @Query("SELECT * FROM sync_items ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SyncItemEntity>>

    @Query("SELECT * FROM sync_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SyncItemEntity?

    @Query("DELETE FROM sync_items")
    suspend fun clearAll()
}
