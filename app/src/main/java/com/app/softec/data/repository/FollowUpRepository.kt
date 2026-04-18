package com.app.softec.data.repository

import com.app.softec.data.local.dao.FollowUpDao
import com.app.softec.data.local.entity.FollowUpEntity
import com.app.softec.data.mapper.FollowUpMappers.toEntity
import com.app.softec.data.mapper.FollowUpMappers.toDomain
import com.app.softec.data.mapper.FollowUpMappers.toFirestore
import com.app.softec.domain.model.FollowUp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowUpRepository @Inject constructor(
    private val followUpDao: FollowUpDao,
    private val firestore: FirebaseFirestore
) {
    
    // Get follow-ups by account
    fun getFollowUpsByAccount(accountId: String): Flow<List<FollowUp>> =
        followUpDao.getFollowUpsByAccount(accountId).map { entities ->
            entities.map { it.toDomain() }
        }
    
    // Get pending follow-ups
    fun getPendingFollowUps(userId: String): Flow<List<FollowUp>> =
        followUpDao.getPendingFollowUps(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    
    // Get today's follow-ups
    fun getTodayFollowUps(userId: String, today: java.util.Date): Flow<List<FollowUp>> =
        followUpDao.getTodayFollowUps(userId, today).map { entities ->
            entities.map { it.toDomain() }
        }
    
    // Get top 10 pending
    fun getTopPendingFollowUps(userId: String, today: java.util.Date): Flow<List<FollowUp>> =
        followUpDao.getTopPendingFollowUps(userId, today).map { entities ->
            entities.map { it.toDomain() }
        }
    
    // Get single follow-up
    suspend fun getFollowUpById(followUpId: String): FollowUp? =
        followUpDao.getFollowUpById(followUpId)?.toDomain()
    
    // Insert follow-up
    suspend fun insertFollowUp(followUp: FollowUp, userId: String) {
        val entity = followUp.toEntity(userId)
        followUpDao.insertFollowUp(entity)
    }
    
    // Update follow-up
    suspend fun updateFollowUp(followUp: FollowUp, userId: String) {
        val entity = followUp.toEntity(userId)
        followUpDao.updateFollowUp(entity)
    }
    
    // Delete follow-up
    suspend fun deleteFollowUp(followUp: FollowUp) {
        followUpDao.deleteFollowUp(followUp.toEntity(""))
    }
    
    // Sync unsynced follow-ups to Firestore
    suspend fun syncUnsynced(userId: String) {
        val unsyncedFollowUps = followUpDao.getUnsyncedFollowUps(userId)
        
        unsyncedFollowUps.forEach { entity ->
            try {
                firestore.collection("users").document(userId)
                    .collection("followUps").document(entity.id)
                    .set(entity.toFirestore())
                    .await()
                
                // Mark as synced
                followUpDao.updateFollowUp(entity.copy(isSynced = true))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Pull follow-ups from Firestore
    suspend fun pullFromFirestore(userId: String) {
        try {
            val documents = firestore.collection("users").document(userId)
                .collection("followUps").get().await()
            
            documents.documents.forEach { doc ->
                val entity = doc.toObject(FollowUpEntity::class.java)?.copy(
                    isSynced = true,
                    userId = userId
                )
                if (entity != null) {
                    followUpDao.insertFollowUp(entity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

