package com.prepstack.voiceinterview.core.model

import kotlinx.serialization.Serializable

/**
 * Represents an interview topic with its configuration options.
 */
@Serializable
data class InterviewTopic(
    val id: String,
    val name: String,
    val description: String,
    val category: InterviewCategory,
    val difficultyLevels: List<DifficultyLevel>,
    val estimatedTimeMinutes: Int,
    val iconUrl: String? = null
)

/**
 * Categories of interview types.
 */
enum class InterviewCategory {
    TECHNICAL,
    BEHAVIORAL,
    SYSTEM_DESIGN,
    LEADERSHIP,
    GENERAL
}

/**
 * Interview difficulty levels.
 */
enum class DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}

/**
 * Configuration for starting an interview.
 */
@Serializable
data class InterviewConfig(
    val topicId: String,
    val difficultyLevel: DifficultyLevel,
    val durationMinutes: Int = 15,
    val questionCount: Int = 5,
    val adaptiveFeedback: Boolean = true,
    val userProfile: UserProfile? = null
)

/**
 * Optional user profile to tailor the interview.
 */
@Serializable
data class UserProfile(
    val yearsOfExperience: Int? = null,
    val currentRole: String? = null,
    val targetRole: String? = null,
    val focusAreas: List<String>? = null
)

/**
 * Interview state representing the current status of an ongoing interview.
 */
@Serializable
data class InterviewState(
    val sessionId: String,
    val status: InterviewStatus,
    val currentQuestion: InterviewQuestion? = null,
    val previousQuestions: List<InterviewQuestion> = emptyList(),
    val currentResponse: InterviewResponse? = null,
    val sessionSummary: InterviewSummary? = null,
    val error: String? = null
)

/**
 * Status of the interview.
 */
enum class InterviewStatus {
    INITIALIZING,
    WAITING_FOR_QUESTION,
    PRESENTING_QUESTION,
    LISTENING,
    PROCESSING_RESPONSE,
    PRESENTING_FEEDBACK,
    COMPLETED,
    ERROR
}

/**
 * Represents an interview question.
 */
@Serializable
data class InterviewQuestion(
    val id: String,
    val text: String,
    val type: QuestionType,
    val difficultyLevel: DifficultyLevel,
    val expectedDurationSeconds: Int
)

/**
 * Types of interview questions.
 */
enum class QuestionType {
    TECHNICAL_KNOWLEDGE,
    PROBLEM_SOLVING,
    BEHAVIORAL,
    SYSTEM_DESIGN,
    OPEN_ENDED,
    SCENARIO_BASED
}

/**
 * Response to an interview question.
 */
@Serializable
data class InterviewResponse(
    val questionId: String,
    val transcribedAnswer: String,
    val evaluation: InterviewEvaluation,
    val nextQuestionId: String?
)

/**
 * Evaluation of an interview response.
 */
@Serializable
data class InterviewEvaluation(
    val score: Int, // 1-10
    val feedbackSummary: String,
    val strengths: List<String>,
    val improvements: List<String>,
    val technicalAccuracy: Int? = null, // 1-10, for technical questions only
    val communicationClarity: Int // 1-10
)

/**
 * Summary of the entire interview session.
 */
@Serializable
data class InterviewSummary(
    val overallScore: Int, // 1-10
    val strengthAreas: List<String>,
    val improvementAreas: List<String>,
    val generalFeedback: String,
    val totalQuestionsAsked: Int,
    val completedQuestionsCount: Int
)

/**
 * Voice options for text-to-speech.
 */
enum class VoiceOption {
    MALE_NATURAL,
    FEMALE_NATURAL,
    MALE_SYNTHETIC,
    FEMALE_SYNTHETIC
}