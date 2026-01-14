package com.prepstack.domain.model

/**
 * Quiz result entity
 */
data class QuizResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val skippedQuestions: Int,
    val percentage: Float,
    val timeTaken: Long // in seconds
) {
    val passed: Boolean
        get() = percentage >= 60f
}
