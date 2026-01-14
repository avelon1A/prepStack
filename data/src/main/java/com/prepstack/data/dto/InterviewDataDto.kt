package com.prepstack.data.dto

import com.google.gson.annotations.SerializedName

// Entry point for domains.json
data class DomainsDto(
    @SerializedName("version") val version: Int = 1,
    @SerializedName("domains") val domains: List<DomainOverviewDto>
)

// Descriptor for quizzes from new domain-level quizzes arrays
data class QuizDescriptor(
    @SerializedName("id") val id: String,
    @SerializedName("topicId") val topicId: String,
    @SerializedName("title") val title: String,
    @SerializedName("file") val file: String
)

data class DomainOverviewDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("iconUrl") val iconUrl: String,
    @SerializedName("topicsFile") val topicsFile: String,
    @SerializedName("quizzes") val quizzes: List<QuizDescriptor>? = null
)

// For: domains/<domain>.json
data class DomainTopicsDto(
    @SerializedName("domainId") val domainId: String,
    @SerializedName("quizzes") val quizzes: List<QuizDescriptor>?,
    @SerializedName("topics") val topics: List<TopicOverviewDto>
)

data class TopicOverviewDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("iconUrl") val iconUrl: String? = null,
    @SerializedName("questionsFile") val questionsFile: String,
    @SerializedName("quizFile") val quizFile: String? = null
)

// For: questions/android/<topic>_questions.json
// and similar
data class TopicQuestionsDto(
    @SerializedName("topicId") val topicId: String,
    @SerializedName("questions") val questions: List<QuestionDto>
)
// For: questions/quizzes/<topic>_quiz.json
data class TopicQuizDto(
    @SerializedName("topicId") val topicId: String,
    @SerializedName("quizQuestions") val quizQuestions: List<QuestionDto>
)

// QuestionDto unchanged (shared for both questions and quizQuestions)
data class QuestionDto(
    @SerializedName("id") val id: String,
    @SerializedName("questionText") val questionText: String,
    @SerializedName("type") val type: String,
    @SerializedName("options") val options: List<String>? = null,
    @SerializedName("correctAnswer") val correctAnswer: String,
    @SerializedName("explanation") val explanation: String? = null,
    @SerializedName("codeExample") val codeExample: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("difficulty") val difficulty: String? = null
)

// Old DTOs for migration/read-compat
data class InterviewDataDto(
    @SerializedName("domains")
    val domains: List<DomainDto>
)

data class DomainDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("iconUrl")
    val iconUrl: String,
    @SerializedName("topics")
    val topics: List<TopicDto>
)

data class TopicDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("iconUrl")
    val iconUrl: String,
    @SerializedName("questions")
    val questions: List<QuestionDto>,
    @SerializedName("quizQuestions")
    val quizQuestions: List<QuestionDto> = emptyList()
)
