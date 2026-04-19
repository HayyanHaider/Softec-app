package com.app.softec.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.softec.data.local.converter.RoomConverters
import com.app.softec.data.local.dao.AccountDao
import com.app.softec.data.local.dao.CustomerDao
import com.app.softec.data.local.dao.FollowUpDao
import com.app.softec.data.local.dao.PaymentHistoryDao
import com.app.softec.data.local.dao.SyncItemDao
import com.app.softec.data.local.entity.AccountEntity
import com.app.softec.data.local.entity.CustomerEntity
import com.app.softec.data.local.entity.FollowUpEntity
import com.app.softec.data.local.entity.PaymentHistoryEntity
import com.app.softec.data.local.entity.SyncItemEntity

@Database(
    entities = [
        SyncItemEntity::class,
        CustomerEntity::class,
        AccountEntity::class,
        FollowUpEntity::class,
        PaymentHistoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syncItemDao(): SyncItemDao
    abstract fun customerDao(): CustomerDao
    abstract fun accountDao(): AccountDao
    abstract fun followUpDao(): FollowUpDao
    abstract fun paymentHistoryDao(): PaymentHistoryDao
}
