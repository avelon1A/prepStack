package com.prepstack.domain.usecase

import com.prepstack.core.util.Resource
import com.prepstack.domain.model.Question
import com.prepstack.domain.repository.InterviewRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for fetching random quiz questions
 */
class GetRandomQuizQuestionsUseCase(
    private val repository: InterviewRepository
) {
    operator fun invoke(
        domainId: String? = null,
        topicId: String? = null,
        count: Int = 10
    ): Flow<Resource<List<Question>>> {
        return repository.getRandomQuizQuestions(domainId, topicId, count)
    }
}
