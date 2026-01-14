package com.prepstack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to track incomplete tests/quizzes
 */
@Entity(tableName = "incomplete_tests")
data class IncompleteTestEntity(
    @PrimaryKey
    val testId: String, // Unique ID for the test session
    val domainId: String,
    val domainName: String,
    val topicId: String?,
    val topicName: String?,
    val questionsCompleted: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)
