package com.app.softec.data.repository

import com.app.softec.core.result.Resource
import com.app.softec.data.local.dao.SyncItemDao
import com.app.softec.data.local.entity.SyncItemEntity
import com.app.softec.data.remote.firestore.SyncItemRemote
import com.app.softec.data.remote.firestore.save
import com.app.softec.data.remote.firestore.snapshotAsFlow
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSyncItemRepository @Inject constructor(
    private val syncItemDao: SyncItemDao,
    private val firestore: FirebaseFirestore
) : SyncItemRepository {

    private val collection by lazy { firestore.collection(COLLECTION_PATH) }
    private var syncJob: Job? = null

    override fun observeItems(): Flow<Resource<List<SyncItemEntity>>> {
        return syncItemDao.observeAll()
            .map { items: List<SyncItemEntity> ->
                Resource.Success(items) as Resource<List<SyncItemEntity>>
            }
            .onStart { emit(Resource.Loading) }
            .catch { throwable ->
                emit(Resource.Error("Failed to observe local data", throwable))
            }
    }

    override fun bindRealtimeSync(scope: CoroutineScope) {
        if (syncJob?.isActive == true) return

        syncJob = scope.launch(Dispatchers.IO) {
            collection.snapshotAsFlow<SyncItemRemote>()
                .catch { throwable ->
                    if (throwable is FirebaseFirestoreException && throwable.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        return@catch
                    }
                    throw throwable
                }
                .collectLatest { remoteItems ->
                    syncItemDao.clearAll()
                    syncItemDao.insertAll(remoteItems.map { it.toEntity() })
                }
        }
    }

    override suspend fun refreshFromRemote(): Resource<Unit> {
        return try {
            val snapshot = collection.get().await()
            val items = snapshot.documents
                .mapNotNull { it.toObject(SyncItemRemote::class.java) }
                .map { it.toEntity() }
            syncItemDao.clearAll()
            syncItemDao.insertAll(items)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Remote refresh failed", throwable = e)
        }
    }

    override suspend fun upsert(item: SyncItemEntity): Resource<Unit> {
        return try {
            syncItemDao.insert(item)
            collection.document(item.id).save(item.toRemote())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Save failed", throwable = e)
        }
    }

    override suspend fun delete(item: SyncItemEntity): Resource<Unit> {
        return try {
            syncItemDao.delete(item)
            collection.document(item.id).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Delete failed", throwable = e)
        }
    }

    private companion object {
        const val COLLECTION_PATH = "sync_items"
    }
}
