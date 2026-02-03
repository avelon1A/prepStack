package com.prepstack.bookmarks.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database for bookmarks
 */
@Database(
    entities = [BookmarkEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(StringListConverter::class, QuestionTypeConverter::class, DifficultyLevelConverter::class)
abstract class BookmarkDatabase : RoomDatabase() {
    
    abstract fun bookmarkDao(): BookmarkDao
    
    companion object {
        @Volatile
        private var INSTANCE: BookmarkDatabase? = null
        
        // Migration from version 1 to version 2 - complete rebuild of the table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table with all the fields
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bookmarks_new (
                        questionId TEXT NOT NULL PRIMARY KEY,
                        topicId TEXT NOT NULL,
                        domainId TEXT NOT NULL,
                        questionText TEXT NOT NULL,
                        questionType TEXT NOT NULL,
                        options TEXT NOT NULL,
                        correctAnswer TEXT NOT NULL,
                        explanation TEXT NOT NULL,
                        codeExample TEXT,
                        imageUrl TEXT,
                        difficulty TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """
                )
                
                // Drop the old table - we can't migrate data since new table has more columns
                database.execSQL("DROP TABLE IF EXISTS bookmarks")
                
                // Rename the new table to the correct name
                database.execSQL("ALTER TABLE bookmarks_new RENAME TO bookmarks")
            }
        }
        
        fun getDatabase(context: Context): BookmarkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookmarkDatabase::class.java,
                    "bookmark_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration() // If migration fails, recreate the database
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
