package com.prepstack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to track user's login streak
 */
@Entity(tableName = "user_streak")
data class UserStreakEntity(
    @PrimaryKey
    val id: Int = 1, // Single row for the user
    val streakCount: Int = 0,
    val lastLoginDate: Long = 0 // Timestamp of the last login day
)