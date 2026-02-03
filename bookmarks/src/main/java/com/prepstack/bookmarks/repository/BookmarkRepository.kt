package com.prepstack.bookmarks.repository

import com.prepstack.bookmarks.data.BookmarkDao
import com.prepstack.bookmarks.data.BookmarkEntity
import com.prepstack.bookmarks.data.DifficultyLevel as RoomDifficultyLevel
import com.prepstack.bookmarks.data.QuestionType as RoomQuestionType
import com.prepstack.domain.model.Question
import com.prepstack.domain.model.DifficultyLevel
import com.prepstack.domain.model.QuestionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for bookmark operations
 */
class BookmarkRepository(private val bookmarkDao: BookmarkDao) {
    
    /**
     * Get all bookmarked questions as BookmarkEntity
     */
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> {
        return bookmarkDao.getAllBookmarks()
    }
    
    /**
     * Get all bookmarked questions as domain Question objects
     */
    fun getAllBookmarkedQuestions(): Flow<List<Question>> {
        return bookmarkDao.getAllBookmarks().map { bookmarks ->
            bookmarks.map { entity ->
                mapEntityToQuestion(entity)
            }
        }
    }
    
    /**
     * Check if a question is bookmarked
     */
    fun isBookmarked(questionId: String): Flow<Boolean> {
        return bookmarkDao.isBookmarked(questionId)
    }
    
    /**
     * Add a question to bookmarks
     */
    suspend fun addBookmark(question: Question) {
        val entity = mapQuestionToEntity(question)
        bookmarkDao.insertBookmark(entity)
    }
    
    /**
     * Remove a question from bookmarks
     */
    suspend fun removeBookmark(questionId: String) {
        bookmarkDao.deleteBookmarkById(questionId)
    }
    
    /**
     * Toggle bookmark status for a question
     */
    suspend fun toggleBookmark(question: Question) {
        val existingBookmark = bookmarkDao.getBookmarkById(question.id)
        if (existingBookmark != null) {
            bookmarkDao.deleteBookmark(existingBookmark)
        } else {
            val entity = mapQuestionToEntity(question)
            bookmarkDao.insertBookmark(entity)
        }
    }
    
    /**
     * Get the count of bookmarked questions
     */
    fun getBookmarkCount(): Flow<Int> {
        return bookmarkDao.getBookmarkCount()
    }
    
    /**
     * Map BookmarkEntity to domain Question
     */
    private fun mapEntityToQuestion(entity: BookmarkEntity): Question {
        return Question(
            id = entity.questionId,
            topicId = entity.topicId,
            domainId = entity.domainId,
            questionText = entity.questionText,
            type = when (entity.questionType) {
                RoomQuestionType.MCQ -> QuestionType.MCQ
                RoomQuestionType.THEORY -> QuestionType.THEORY
            },
            options = entity.options,
            correctAnswer = entity.correctAnswer,
            explanation = entity.explanation,
            codeExample = entity.codeExample,
            imageUrl = entity.imageUrl,
            difficulty = when (entity.difficulty) {
                RoomDifficultyLevel.EASY -> DifficultyLevel.EASY
                RoomDifficultyLevel.MEDIUM -> DifficultyLevel.MEDIUM
                RoomDifficultyLevel.HARD -> DifficultyLevel.HARD
            },
            isBookmarked = true // Always true for bookmarked questions
        )
    }
    
    /**
     * Map domain Question to BookmarkEntity
     */
    private fun mapQuestionToEntity(question: Question): BookmarkEntity {
        return BookmarkEntity(
            questionId = question.id,
            topicId = question.topicId,
            domainId = question.domainId,
            questionText = question.questionText,
            questionType = when (question.type) {
                QuestionType.MCQ -> RoomQuestionType.MCQ
                QuestionType.THEORY -> RoomQuestionType.THEORY
            },
            options = question.options,
            correctAnswer = question.correctAnswer,
            explanation = question.explanation,
            codeExample = question.codeExample,
            imageUrl = question.imageUrl,
            difficulty = when (question.difficulty) {
                DifficultyLevel.EASY -> RoomDifficultyLevel.EASY
                DifficultyLevel.MEDIUM -> RoomDifficultyLevel.MEDIUM
                DifficultyLevel.HARD -> RoomDifficultyLevel.HARD
            },
            timestamp = System.currentTimeMillis()
        )
    }
}
