package com.prepstack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to track user's recent activities
 */
@Entity(tableName = "user_activities")
data class UserActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topicId: String,
    val topicName: String,
    val domainId: String,
    val questionsCompleted: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)
