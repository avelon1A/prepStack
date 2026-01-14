package com.prepstack.data.repository

import com.prepstack.data.local.*
import com.prepstack.domain.model.*
import com.prepstack.domain.model.UserStreak
import com.prepstack.domain.repository.UserProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Implementation of UserProgressRepository
 */
class UserProgressRepositoryImpl(
    private val dao: UserProgressDao
) : UserProgressRepository {
    
    override fun getRecentActivities(limit: Int): Flow<List<UserActivity>> {
        return dao.getRecentActivities(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun recordActivity(activity: UserActivity) {
        dao.insertActivity(activity.toEntity())
    }
    
    override fun getAllQuizHistory(): Flow<List<QuizHistory>> {
        return dao.getAllQuizHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getCompletedQuizzes(): Flow<List<QuizHistory>> {
        return dao.getCompletedQuizzes().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun recordQuizResult(quizHistory: QuizHistory) {
        dao.insertQuizHistory(quizHistory.toEntity())
    }
    
    override fun getUserPerformance(): Flow<UserPerformance> {
        return combine(
            dao.getTotalQuestionsAnswered(),
            dao.getTotalCorrectAnswers()
        ) { totalQuestions, correctAnswers ->
            val total = totalQuestions ?: 0
            val correct = correctAnswers ?: 0
            UserPerformance(
                totalQuestionsAnswered = total,
                totalCorrectAnswers = correct,
                accuracy = if (total > 0) (correct.toFloat() / total * 100) else 0f
            )
        }
    }
    
    override fun getIncompleteTests(): Flow<List<IncompleteTest>> {
        return dao.getIncompleteTests().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun saveIncompleteTest(test: IncompleteTest) {
        dao.insertIncompleteTest(test.toEntity())
    }
    
    override suspend fun removeIncompleteTest(testId: String) {
        dao.deleteIncompleteTest(testId)
    }
    
    override suspend fun clearAllIncompleteTests() {
        dao.clearAllIncompleteTests()
    }
    
    override fun getUserStreak(): Flow<UserStreak> {
        return dao.getUserStreak().map { entity ->
            entity?.toDomain() ?: UserStreak()
        }
    }
    
    override suspend fun updateUserStreak(userStreak: UserStreak) {
        dao.updateUserStreak(userStreak.toEntity())
    }
    
    override suspend fun checkAndUpdateStreak() {
        val currentStreak = getUserStreak().first()
        val currentTime = System.currentTimeMillis()
        
        // Get today's date with time set to 00:00:00
        val today = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        // Convert lastLoginDate to 00:00:00 format for comparison
        val lastLoginDay = if (currentStreak.lastLoginDate > 0) {
            Calendar.getInstance().apply {
                timeInMillis = currentStreak.lastLoginDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } else 0L
        
        val updatedStreak = when {
            // First time user - start streak at 1
            currentStreak.lastLoginDate == 0L -> UserStreak(
                streakCount = 1,
                lastLoginDate = currentTime
            )
            
            // Already logged in today - no change
            lastLoginDay == today -> currentStreak
            
            // Logged in yesterday - increase streak
            isYesterday(lastLoginDay, today) -> UserStreak(
                streakCount = currentStreak.streakCount + 1,
                lastLoginDate = currentTime
            )
            
            // Missed a day or more - reset streak
            else -> UserStreak(
                streakCount = 1, 
                lastLoginDate = currentTime
            )
        }
        
        updateUserStreak(updatedStreak)
    }
    
    private fun isYesterday(lastLoginTimestamp: Long, todayTimestamp: Long): Boolean {
        val oneDayInMillis = TimeUnit.DAYS.toMillis(1)
        return (todayTimestamp - lastLoginTimestamp) <= oneDayInMillis
    }
}

// Mapper extensions
private fun UserActivityEntity.toDomain() = UserActivity(
    id = id,
    topicId = topicId,
    topicName = topicName,
    domainId = domainId,
    questionsCompleted = questionsCompleted,
    totalQuestions = totalQuestions,
    timestamp = timestamp
)

private fun UserActivity.toEntity() = UserActivityEntity(
    id = id,
    topicId = topicId,
    topicName = topicName,
    domainId = domainId,
    questionsCompleted = questionsCompleted,
    totalQuestions = totalQuestions,
    timestamp = timestamp
)

private fun QuizHistoryEntity.toDomain() = QuizHistory(
    id = id,
    domainId = domainId,
    topicId = topicId,
    totalQuestions = totalQuestions,
    correctAnswers = correctAnswers,
    incorrectAnswers = incorrectAnswers,
    skippedQuestions = skippedQuestions,
    percentage = percentage,
    timestamp = timestamp,
    completed = completed
)

private fun QuizHistory.toEntity() = QuizHistoryEntity(
    id = id,
    domainId = domainId,
    topicId = topicId,
    totalQuestions = totalQuestions,
    correctAnswers = correctAnswers,
    incorrectAnswers = incorrectAnswers,
    skippedQuestions = skippedQuestions,
    percentage = percentage,
    timestamp = timestamp,
    completed = completed
)

private fun IncompleteTestEntity.toDomain() = IncompleteTest(
    testId = testId,
    domainId = domainId,
    domainName = domainName,
    topicId = topicId,
    topicName = topicName,
    questionsCompleted = questionsCompleted,
    totalQuestions = totalQuestions,
    timestamp = timestamp
)

private fun IncompleteTest.toEntity() = IncompleteTestEntity(
    testId = testId,
    domainId = domainId,
    domainName = domainName,
    topicId = topicId,
    topicName = topicName,
    questionsCompleted = questionsCompleted,
    totalQuestions = totalQuestions,
    timestamp = timestamp
)

private fun UserStreakEntity.toDomain() = UserStreak(
    streakCount = streakCount,
    lastLoginDate = lastLoginDate
)

private fun UserStreak.toEntity() = UserStreakEntity(
    streakCount = streakCount,
    lastLoginDate = lastLoginDate
)
