package com.app.softec.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.softec.data.local.converter.RoomConverters
import com.app.softec.data.local.dao.SyncItemDao
import com.app.softec.data.local.entity.SyncItemEntity

@Database(
    entities = [SyncItemEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syncItemDao(): SyncItemDao
}
