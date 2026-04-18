package com.app.softec.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.softec.data.local.entity.FollowUpEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface FollowUpDao {
    @Insert
    suspend fun insertFollowUp(followUp: FollowUpEntity)

    @Update
    suspend fun updateFollowUp(followUp: FollowUpEntity)

    @Delete
    suspend fun deleteFollowUp(followUp: FollowUpEntity)

    @Query("SELECT * FROM follow_ups WHERE id = :followUpId")
    suspend fun getFollowUpById(followUpId: String): FollowUpEntity?

    @Query("SELECT * FROM follow_ups WHERE accountId = :accountId ORDER BY followUpDate DESC")
    fun getFollowUpsByAccount(accountId: String): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE userId = :userId AND status = 'pending' ORDER BY followUpDate ASC")
    fun getPendingFollowUps(userId: String): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE userId = :userId AND followUpDate <= :today AND status = 'pending' ORDER BY followUpDate ASC")
    fun getTodayFollowUps(userId: String, today: Date): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE userId = :userId AND status = 'pending' AND followUpDate <= :today ORDER BY followUpDate ASC LIMIT 10")
    fun getTopPendingFollowUps(userId: String, today: Date): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_ups WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedFollowUps(userId: String): List<FollowUpEntity>

    @Query("DELETE FROM follow_ups WHERE userId = :userId")
    suspend fun deleteAllFollowUpsByUser(userId: String)
}

