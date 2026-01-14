package com.prepstack.data.mapper

import com.prepstack.data.dto.DomainDto
import com.prepstack.data.dto.QuestionDto
import com.prepstack.data.dto.TopicDto
import com.prepstack.domain.model.Domain
import com.prepstack.domain.model.Question
import com.prepstack.domain.model.Topic
import com.prepstack.domain.model.QuestionType
import com.prepstack.domain.model.DifficultyLevel

/**
 * Mappers to convert DTOs to Domain models
 */

fun DomainDto.toDomain(): Domain {
    return Domain(
        id = id,
        name = name,
        description = description,
        iconUrl = iconUrl,
        topicCount = topics.size
    )
}

fun TopicDto.toTopic(domainId: String): Topic {
    return Topic(
        id = id,
        domainId = domainId,
        name = name,
        description = description,
        iconUrl = iconUrl,
        questionCount = questions.size
    )
}

fun QuestionDto.toQuestion(topicId: String, domainId: String): Question {
    return Question(
        id = id,
        topicId = topicId,
        domainId = domainId,
        questionText = questionText,
        type = when (type.uppercase()) {
            "MCQ" -> QuestionType.MCQ
            "THEORY" -> QuestionType.THEORY
            else -> QuestionType.THEORY
        },
        options = options ?: emptyList(),
        correctAnswer = correctAnswer,
        explanation = explanation?: "",
        codeExample = codeExample,
        imageUrl = imageUrl,
        difficulty = when (difficulty?.uppercase()) {
            "EASY" -> DifficultyLevel.EASY
            "HARD" -> DifficultyLevel.HARD
            else -> DifficultyLevel.MEDIUM
        }
    )
}
