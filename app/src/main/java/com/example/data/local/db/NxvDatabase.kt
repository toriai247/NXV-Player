package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VideoEntity::class,
        FolderEntity::class,
        FavoriteEntity::class,
        PlaybackHistoryEntity::class,
        StreamUrlEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class NxvDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun streamUrlDao(): StreamUrlDao

    companion object {
        @Volatile
        private var INSTANCE: NxvDatabase? = null

        fun getDatabase(context: Context): NxvDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NxvDatabase::class.java,
                    "nxv_player_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
