package com.prepstack.bookmarks.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for bookmark operations
 */
@Dao
interface BookmarkDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)
    
    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)
    
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>
    
    @Query("SELECT * FROM bookmarks WHERE questionId = :questionId")
    suspend fun getBookmarkById(questionId: String): BookmarkEntity?
    
    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE questionId = :questionId)")
    fun isBookmarked(questionId: String): Flow<Boolean>
    
    @Query("DELETE FROM bookmarks WHERE questionId = :questionId")
    suspend fun deleteBookmarkById(questionId: String)
}
