package com.prepstack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database for user progress tracking
 */
@Database(
    entities = [
        UserActivityEntity::class,
        QuizHistoryEntity::class,
        IncompleteTestEntity::class,
        UserStreakEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class UserProgressDatabase : RoomDatabase() {
    
    abstract fun userProgressDao(): UserProgressDao
    
    companion object {
        // Migration from version 1 to version 2: Add user_streak table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_streak` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`streakCount` INTEGER NOT NULL, " +
                    "`lastLoginDate` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`id`))"
                )
            }
        }
        
        @Volatile
        private var INSTANCE: UserProgressDatabase? = null
        
        fun getDatabase(context: Context): UserProgressDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserProgressDatabase::class.java,
                    "user_progress_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
