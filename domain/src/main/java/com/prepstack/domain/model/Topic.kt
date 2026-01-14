package com.prepstack.domain.model

/**
 * Topic entity representing a specific topic within a domain
 */
data class Topic(
    val id: String,
    val domainId: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val questionCount: Int = 0
)
