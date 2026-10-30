package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ArtworkEntity::class, CachedArtworkEntity::class],
    version = 3,
    exportSchema = false
)
abstract class DailyCuratorDatabase : RoomDatabase() {
    abstract fun artworkDao(): ArtworkDao
    abstract fun artworkCacheDao(): ArtworkCacheDao

    companion object {
        @Volatile
        private var INSTANCE: DailyCuratorDatabase? = null

        fun getInstance(context: Context): DailyCuratorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DailyCuratorDatabase::class.java,
                    "daily_curator.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
