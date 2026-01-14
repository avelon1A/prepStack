package com.prepstack.domain.model

/**
 * Domain entity representing a subject domain (e.g., Android, Kotlin, DSA)
 */
data class Domain(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val topicCount: Int = 0
)
