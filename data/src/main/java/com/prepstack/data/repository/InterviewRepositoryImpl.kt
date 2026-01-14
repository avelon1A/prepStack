package com.prepstack.data.repository

import com.prepstack.core.util.Resource
import com.prepstack.data.dto.QuizDescriptor as QuizDescriptorDto
import com.prepstack.data.source.LocalDataSource
import com.prepstack.domain.model.Domain
import com.prepstack.domain.model.Question
import com.prepstack.domain.model.Topic
import com.prepstack.domain.model.QuizDescriptor
import com.prepstack.domain.repository.InterviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of InterviewRepository
 */
class InterviewRepositoryImpl(
    private val localDataSource: LocalDataSource
) : InterviewRepository {
    
    override suspend fun loadData(): Resource<Unit> {
        return try {
            val result = localDataSource.loadData()
            if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
    
    override fun getDomains(): Flow<Resource<List<Domain>>> = flow {
        try {
            emit(Resource.Loading())
            val domains = localDataSource.getDomains()
            emit(Resource.Success(domains))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
    
    override fun getTopicsByDomain(domainId: String): Flow<Resource<List<Topic>>> = flow {
        try {
            emit(Resource.Loading())
            val topics = localDataSource.getTopicsByDomain(domainId)
            emit(Resource.Success(topics))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
    
    override fun getQuestionsByTopic(topicId: String): Flow<Resource<List<Question>>> = flow {
        try {
            emit(Resource.Loading())
            val questions = localDataSource.getQuestionsByTopic(topicId)
            emit(Resource.Success(questions))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
    
    override fun getTheoryQuestionsByTopic(topicId: String): Flow<Resource<List<Question>>> = flow {
        try {
            emit(Resource.Loading())
            val questions = localDataSource.getTheoryQuestionsByTopic(topicId)
            emit(Resource.Success(questions))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
    
    override fun getQuizQuestionsByTopic(topicId: String): Flow<Resource<List<Question>>> = flow {
        try {
            emit(Resource.Loading())
            val questions = localDataSource.getQuizQuestionsByTopic(topicId)
            emit(Resource.Success(questions))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
    
    override fun getQuestionById(questionId: String): Flow<Resource<Question>> = flow {
        try {
            emit(Resource.Loading())
            val question = localDataSource.getQuestionById(questionId)
            if (question != null) {
                emit(Resource.Success(question))
            } else {
                emit(Resource.Error("Question not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
    
    override fun getRandomQuizQuestions(
        domainId: String?,
        topicId: String?,
        count: Int
    ): Flow<Resource<List<Question>>> = flow {
        try {
            emit(Resource.Loading())
            val questions = localDataSource.getRandomQuestions(domainId, topicId, count)
            emit(Resource.Success(questions))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

   override fun getDomainQuizzes(domainId: String): List<QuizDescriptor> {
        return localDataSource.getDomainQuizzes(domainId).map {
            QuizDescriptor(
                id = it.id,
                topicId = it.topicId,
                title = it.title,
                file = it.file
            )
        }
    }
}
