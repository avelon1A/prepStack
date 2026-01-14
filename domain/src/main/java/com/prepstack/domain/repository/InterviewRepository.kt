package com.prepstack.domain.repository

import com.prepstack.core.util.Resource
import com.prepstack.domain.model.QuizDescriptor
import com.prepstack.domain.model.Domain
import com.prepstack.domain.model.Question
import com.prepstack.domain.model.Topic
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for interview data operations
 * This follows the dependency inversion principle of Clean Architecture
 */
interface InterviewRepository {
    
    /**
     * Load all data from JSON at app startup
     */
    suspend fun loadData(): Resource<Unit>
    
    /**
     * Get all available domains
     */
    fun getDomains(): Flow<Resource<List<Domain>>>
    
    /**
     * Get topics for a specific domain
     */
    fun getTopicsByDomain(domainId: String): Flow<Resource<List<Topic>>>
    
    /**
     * Get questions for a specific topic (all types)
     */
    fun getQuestionsByTopic(topicId: String): Flow<Resource<List<Question>>>
    
    /**
     * Get THEORY questions for learning (topic screen)
     */
    fun getTheoryQuestionsByTopic(topicId: String): Flow<Resource<List<Question>>>
    
    /**
     * Get MCQ questions for quiz (quiz screen)
     */
    fun getQuizQuestionsByTopic(topicId: String): Flow<Resource<List<Question>>>
    
    /**
     * Get a single question by ID
     */
    fun getQuestionById(questionId: String): Flow<Resource<Question>>
    
    /**
     * Get random MCQ questions for quiz
     */
    fun getRandomQuizQuestions(
        domainId: String? = null,
        topicId: String? = null,
        count: Int
    ): Flow<Resource<List<Question>>>
    
    /**
     * Get all quiz descriptors for a domain (to populate quiz list/popup)
     */
    fun getDomainQuizzes(domainId: String): List<QuizDescriptor>
}
