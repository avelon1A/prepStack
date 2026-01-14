package com.prepstack.domain.model

/**
 * Question entity representing interview questions
 * Supports both MCQ and Theory type questions
 */
data class Question(
    val id: String,
    val topicId: String,
    val domainId: String,
    val questionText: String,
    val type: QuestionType,
    val options: List<String> = emptyList(), // For MCQ
    val correctAnswer: String, // For MCQ: option text, For Theory: answer text
    val explanation: String,
    val codeExample: String? = null, // Code example for better understanding
    val imageUrl: String? = null, // Image/diagram URL for visual learning
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val isBookmarked: Boolean = false
)

enum class QuestionType {
    MCQ,
    THEORY
}

enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
}
