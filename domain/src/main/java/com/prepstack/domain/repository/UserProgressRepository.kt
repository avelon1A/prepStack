package com.prepstack.domain.repository

import com.prepstack.domain.model.IncompleteTest
import com.prepstack.domain.model.QuizHistory
import com.prepstack.domain.model.UserActivity
import com.prepstack.domain.model.UserPerformance
import com.prepstack.domain.model.UserStreak
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user progress operations
 */
interface UserProgressRepository {
    
    // User Activities
    fun getRecentActivities(limit: Int = 10): Flow<List<UserActivity>>
    suspend fun recordActivity(activity: UserActivity)
    
    // Quiz History
    fun getAllQuizHistory(): Flow<List<QuizHistory>>
    fun getCompletedQuizzes(): Flow<List<QuizHistory>>
    suspend fun recordQuizResult(quizHistory: QuizHistory)
    
    // Performance Statistics
    fun getUserPerformance(): Flow<UserPerformance>
    
    // Incomplete Tests
    fun getIncompleteTests(): Flow<List<IncompleteTest>>
    suspend fun saveIncompleteTest(test: IncompleteTest)
    suspend fun removeIncompleteTest(testId: String)
    suspend fun clearAllIncompleteTests()
    
    // Streak tracking
    fun getUserStreak(): Flow<UserStreak>
    suspend fun updateUserStreak(userStreak: UserStreak)
    suspend fun checkAndUpdateStreak()
}
