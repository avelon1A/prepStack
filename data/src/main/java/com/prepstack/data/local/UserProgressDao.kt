package com.prepstack.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.prepstack.data.local.UserStreakEntity

/**
 * DAO for user progress tracking
 */
@Dao
interface UserProgressDao {
    
    // Streak tracking
    @Query("SELECT * FROM user_streak WHERE id = 1")
    fun getUserStreak(): Flow<UserStreakEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserStreak(streak: UserStreakEntity)
    
    
    // User Activities
    @Query("SELECT * FROM user_activities ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentActivities(limit: Int = 10): Flow<List<UserActivityEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: UserActivityEntity)
    
    @Query("DELETE FROM user_activities WHERE id = :activityId")
    suspend fun deleteActivity(activityId: Long)
    
    // Quiz History
    @Query("SELECT * FROM quiz_history ORDER BY timestamp DESC")
    fun getAllQuizHistory(): Flow<List<QuizHistoryEntity>>
    
    @Query("SELECT * FROM quiz_history WHERE completed = 1 ORDER BY timestamp DESC")
    fun getCompletedQuizzes(): Flow<List<QuizHistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizHistory(quizHistory: QuizHistoryEntity)
    
    @Query("SELECT SUM(totalQuestions) FROM quiz_history WHERE completed = 1")
    fun getTotalQuestionsAnswered(): Flow<Int?>
    
    @Query("SELECT SUM(correctAnswers) FROM quiz_history WHERE completed = 1")
    fun getTotalCorrectAnswers(): Flow<Int?>
    
    // Incomplete Tests
    @Query("SELECT * FROM incomplete_tests ORDER BY timestamp DESC")
    fun getIncompleteTests(): Flow<List<IncompleteTestEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncompleteTest(test: IncompleteTestEntity)
    
    @Query("DELETE FROM incomplete_tests WHERE testId = :testId")
    suspend fun deleteIncompleteTest(testId: String)
    
    @Query("DELETE FROM incomplete_tests")
    suspend fun clearAllIncompleteTests()
}
