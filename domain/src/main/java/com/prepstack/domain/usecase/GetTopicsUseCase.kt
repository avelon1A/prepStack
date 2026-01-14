package com.prepstack.domain.usecase

import com.prepstack.core.util.Resource
import com.prepstack.domain.model.Topic
import com.prepstack.domain.repository.InterviewRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for fetching topics by domain
 */
class GetTopicsUseCase(
    private val repository: InterviewRepository
) {
    operator fun invoke(domainId: String): Flow<Resource<List<Topic>>> {
        return repository.getTopicsByDomain(domainId)
    }
}
