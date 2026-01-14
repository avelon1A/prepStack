package com.prepstack.domain.model

/**
 * Domain model for user performance statistics
 */
data class UserPerformance(
    val totalQuestionsAnswered: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val accuracy: Float = if (totalQuestionsAnswered > 0) 
        (totalCorrectAnswers.toFloat() / totalQuestionsAnswered * 100) else 0f
)
