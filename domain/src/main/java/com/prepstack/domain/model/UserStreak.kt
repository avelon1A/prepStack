package com.prepstack.domain.model

/**
 * Data class representing user's login streak
 */
data class UserStreak(
    val streakCount: Int = 0,
    val lastLoginDate: Long = 0
)