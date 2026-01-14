package com.prepstack.domain.usecase

import com.prepstack.core.util.Resource
import com.prepstack.domain.model.Domain
import com.prepstack.domain.repository.InterviewRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for fetching all domains
 */
class GetDomainsUseCase(
    private val repository: InterviewRepository
) {
    operator fun invoke(): Flow<Resource<List<Domain>>> {
        return repository.getDomains()
    }
}
