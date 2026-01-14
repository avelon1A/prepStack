package com.prepstack.bookmarks.repository

import com.prepstack.bookmarks.data.BookmarkDao
import com.prepstack.bookmarks.data.BookmarkEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for bookmark operations
 */
class BookmarkRepository(private val bookmarkDao: BookmarkDao) {
    
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> {
        return bookmarkDao.getAllBookmarks()
    }
    
    fun isBookmarked(questionId: String): Flow<Boolean> {
        return bookmarkDao.isBookmarked(questionId)
    }
    
    suspend fun addBookmark(questionId: String, topicId: String, domainId: String) {
        val bookmark = BookmarkEntity(
            questionId = questionId,
            topicId = topicId,
            domainId = domainId
        )
        bookmarkDao.insertBookmark(bookmark)
    }
    
    suspend fun removeBookmark(questionId: String) {
        bookmarkDao.deleteBookmarkById(questionId)
    }
    
    suspend fun toggleBookmark(questionId: String, topicId: String, domainId: String) {
        val existingBookmark = bookmarkDao.getBookmarkById(questionId)
        if (existingBookmark != null) {
            bookmarkDao.deleteBookmark(existingBookmark)
        } else {
            val bookmark = BookmarkEntity(
                questionId = questionId,
                topicId = topicId,
                domainId = domainId
            )
            bookmarkDao.insertBookmark(bookmark)
        }
    }
}
