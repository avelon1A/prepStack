package com.prepstack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to track quiz history
 */
@Entity(tableName = "quiz_history")
data class QuizHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val domainId: String,
    val topicId: String?,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val skippedQuestions: Int,
    val percentage: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val completed: Boolean = true
)
