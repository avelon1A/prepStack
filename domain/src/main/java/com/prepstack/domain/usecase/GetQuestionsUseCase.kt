package com.prepstack.domain.usecase

import com.prepstack.core.util.Resource
import com.prepstack.domain.model.Question
import com.prepstack.domain.repository.InterviewRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for fetching THEORY questions by topic (for learning)
 */
class GetQuestionsUseCase(
    private val repository: InterviewRepository
) {
    operator fun invoke(topicId: String): Flow<Resource<List<Question>>> {
        return repository.getTheoryQuestionsByTopic(topicId)
    }
}
