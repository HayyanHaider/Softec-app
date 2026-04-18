package com.app.softec.data.repository

import com.app.softec.core.result.Resource
import com.app.softec.data.local.entity.SyncItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface SyncItemRepository {
    fun observeItems(): Flow<Resource<List<SyncItemEntity>>>
    fun bindRealtimeSync(scope: CoroutineScope)
    suspend fun refreshFromRemote(): Resource<Unit>
    suspend fun upsert(item: SyncItemEntity): Resource<Unit>
    suspend fun delete(item: SyncItemEntity): Resource<Unit>
}
