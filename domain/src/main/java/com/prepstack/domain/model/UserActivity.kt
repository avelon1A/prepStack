package com.prepstack.domain.model

/**
 * Domain model for user activity
 */
data class UserActivity(
    val id: Long = 0,
    val topicId: String,
    val topicName: String,
    val domainId: String,
    val questionsCompleted: Int,
    val totalQuestions: Int,
    val timestamp: Long,
    val progress: Float = if (totalQuestions > 0) questionsCompleted.toFloat() / totalQuestions else 0f
)
