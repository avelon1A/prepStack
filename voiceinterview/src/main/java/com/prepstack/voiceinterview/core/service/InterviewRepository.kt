package com.prepstack.voiceinterview.core.service

import com.prepstack.voiceinterview.core.model.DifficultyLevel
import com.prepstack.voiceinterview.core.model.InterviewCategory
import com.prepstack.voiceinterview.core.model.InterviewTopic

/**
 * Repository for interview topics and configurations.
 */
interface InterviewRepository {
    /**
     * Get all available interview topics.
     * @return List of interview topics
     */
    suspend fun getAvailableTopics(): List<InterviewTopic>
    
    /**
     * Get an interview topic by ID.
     * @param topicId ID of the topic to retrieve
     * @return Interview topic or null if not found
     */
    suspend fun getTopicById(topicId: String): InterviewTopic?
    
    companion object {
        /**
         * Create a default implementation of InterviewRepository.
         */
        fun create(): InterviewRepository {
            return InMemoryInterviewRepository()
        }
    }
}

/**
 * In-memory implementation of InterviewRepository.
 * In a real application, this would likely fetch from a database or API.
 */
internal class InMemoryInterviewRepository : InterviewRepository {
    private val topics: List<InterviewTopic> = createDefaultTopics()
    
    override suspend fun getAvailableTopics(): List<InterviewTopic> {
        return topics
    }
    
    override suspend fun getTopicById(topicId: String): InterviewTopic? {
        return topics.find { it.id == topicId }
    }
    
    private fun createDefaultTopics(): List<InterviewTopic> {
        return listOf(
            InterviewTopic(
                id = "android-dev",
                name = "Android Development",
                description = "Android platform, app architecture, UI/UX, and performance optimization",
                category = InterviewCategory.TECHNICAL,
                difficultyLevels = listOf(DifficultyLevel.BEGINNER, DifficultyLevel.INTERMEDIATE, DifficultyLevel.ADVANCED),
                estimatedTimeMinutes = 15,
                iconUrl = "https://img.icons8.com/color/96/android-os.png"
            ),
            InterviewTopic(
                id = "kotlin",
                name = "Kotlin Programming",
                description = "Kotlin language features, coroutines, functional programming, and best practices",
                category = InterviewCategory.TECHNICAL,
                difficultyLevels = listOf(DifficultyLevel.BEGINNER, DifficultyLevel.INTERMEDIATE, DifficultyLevel.ADVANCED),
                estimatedTimeMinutes = 15,
                iconUrl = "https://img.icons8.com/color/96/kotlin.png"
            ),
            InterviewTopic(
                id = "java",
                name = "Java Programming",
                description = "Java language features, object-oriented programming, concurrency, and JVM",
                category = InterviewCategory.TECHNICAL,
                difficultyLevels = listOf(DifficultyLevel.BEGINNER, DifficultyLevel.INTERMEDIATE, DifficultyLevel.ADVANCED),
                estimatedTimeMinutes = 15,
                iconUrl = "https://img.icons8.com/color/96/java-coffee-cup-logo.png"
            ),
            InterviewTopic(
                id = "system-design",
                name = "System Design",
                description = "Architecture patterns, scalability, distributed systems, and database design",
                category = InterviewCategory.SYSTEM_DESIGN,
                difficultyLevels = listOf(DifficultyLevel.INTERMEDIATE, DifficultyLevel.ADVANCED, DifficultyLevel.EXPERT),
                estimatedTimeMinutes = 20,
                iconUrl = "https://img.icons8.com/color/96/server.png"
            ),
            InterviewTopic(
                id = "data-structures",
                name = "Data Structures & Algorithms",
                description = "Common algorithms, time/space complexity, problem-solving strategies",
                category = InterviewCategory.TECHNICAL,
                difficultyLevels = listOf(DifficultyLevel.BEGINNER, DifficultyLevel.INTERMEDIATE, DifficultyLevel.ADVANCED, DifficultyLevel.EXPERT),
                estimatedTimeMinutes = 15,
                iconUrl = "https://img.icons8.com/color/96/tree-structure.png"
            ),
            InterviewTopic(
                id = "behavioral",
                name = "Behavioral Interview",
                description = "Teamwork, conflict resolution, leadership, and career goals",
                category = InterviewCategory.BEHAVIORAL,
                difficultyLevels = listOf(DifficultyLevel.BEGINNER, DifficultyLevel.INTERMEDIATE, DifficultyLevel.ADVANCED),
                estimatedTimeMinutes = 15,
                iconUrl = "https://img.icons8.com/color/96/communication.png"
            ),
            InterviewTopic(
                id = "leadership",
                name = "Leadership & Management",
                description = "Team management, project leadership, decision-making, and mentorship",
                category = InterviewCategory.LEADERSHIP,
                difficultyLevels = listOf(DifficultyLevel.INTERMEDIATE, DifficultyLevel.ADVANCED, DifficultyLevel.EXPERT),
                estimatedTimeMinutes = 15,
                iconUrl = "https://img.icons8.com/color/96/leadership.png"
            )
        )
    }
}