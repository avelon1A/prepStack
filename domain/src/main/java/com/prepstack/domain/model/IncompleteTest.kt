package com.prepstack.domain.model

/**
 * Domain model for incomplete test
 */
data class IncompleteTest(
    val testId: String,
    val domainId: String,
    val domainName: String,
    val topicId: String?,
    val topicName: String?,
    val questionsCompleted: Int,
    val totalQuestions: Int,
    val timestamp: Long,
    val progress: Float = if (totalQuestions > 0) questionsCompleted.toFloat() / totalQuestions else 0f
)
