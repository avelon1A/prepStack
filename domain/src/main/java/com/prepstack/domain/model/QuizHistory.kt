package com.prepstack.domain.model

/**
 * Domain model for quiz history
 */
data class QuizHistory(
    val id: Long = 0,
    val domainId: String,
    val topicId: String?,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val skippedQuestions: Int,
    val percentage: Float,
    val timestamp: Long,
    val completed: Boolean = true
)
