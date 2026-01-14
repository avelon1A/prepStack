package com.prepstack.data.source

import android.content.Context
import com.google.gson.Gson
import com.prepstack.core.util.Constants
import com.prepstack.core.util.JsonReader
import com.prepstack.data.dto.InterviewDataDto
import com.prepstack.data.dto.DomainsDto
import com.prepstack.data.dto.DomainTopicsDto
import com.prepstack.data.dto.TopicQuestionsDto
import com.prepstack.data.dto.TopicQuizDto
import com.prepstack.data.dto.QuizDescriptor
import com.prepstack.data.mapper.toDomain
import com.prepstack.data.mapper.toQuestion
import com.prepstack.data.mapper.toTopic
import com.prepstack.domain.model.Domain
import com.prepstack.domain.model.Question
import com.prepstack.domain.model.Topic

/**
 * Local data source that loads and caches data from JSON
 */
class LocalDataSource(private val context: Context) {
    
    private val gson = Gson()
    
    // In-memory cache
    private var domains: List<Domain> = emptyList()
    private var topics: List<Topic> = emptyList()
    private var questions: List<Question> = emptyList()
    
    private var isDataLoaded = false
    
    /**
     * Load data from modular JSON structure and cache in memory
     */
    suspend fun loadData(): Result<Unit> {
        return try {
            if (isDataLoaded) return Result.success(Unit)

            // 1. Load domains.json
            val domainsJson = JsonReader.readJsonFromAssets(context, Constants.JSON_FILE_NAME)
                ?: return Result.failure(Exception("Failed to read domains.json"))
            val domainsDto = gson.fromJson(domainsJson, DomainsDto::class.java)

            // Prepare aggregates
            val domainList = mutableListOf<Domain>()
            val topicList = mutableListOf<Topic>()
            val questionList = mutableListOf<Question>()

            for (domain in domainsDto.domains) {
                domainList.add(Domain(
                    id = domain.id,
                    name = domain.name,
                    description = "", // For now, description is not loaded from file
                    iconUrl = domain.iconUrl,
                    topicCount = 0 // will fill below if needed
                ))
                val topicsFileStr = domain.topicsFile
                val topicsJson = JsonReader.readJsonFromAssets(context, topicsFileStr) ?: continue
                val domainTopics = gson.fromJson(topicsJson, DomainTopicsDto::class.java)
                val topics = domainTopics.topics ?: continue

                // Set topicCount for this domain
                val topicCountForDomain = topics.size
                domainList[domainList.lastIndex] = domainList.last().copy(topicCount = topicCountForDomain)

                for (topic in topics) {
                    // Questions (THEORY)
                    val qPath = topic.questionsFile
                    val questionsJson = JsonReader.readJsonFromAssets(context, qPath)
                    val topicQuestionsDto = questionsJson?.let {
                        gson.fromJson(it, TopicQuestionsDto::class.java)
                    }
                    val theoryCount = topicQuestionsDto?.questions?.size ?: 0
                    topicQuestionsDto?.questions?.forEach { questionDto ->
                        questionList.add(questionDto.toQuestion(topic.id, domain.id))
                    }

                    // Quiz Questions (MCQ), optional
                    val quizPath = topic.quizFile
                    var quizCount = 0
                    if (!quizPath.isNullOrBlank()) {
                        val quizJson = JsonReader.readJsonFromAssets(context, quizPath)
                        val topicQuizDto = quizJson?.let {
                            gson.fromJson(it, TopicQuizDto::class.java)
                        }
                        quizCount = topicQuizDto?.quizQuestions?.size ?: 0
                        topicQuizDto?.quizQuestions?.forEach { quizDto ->
                            questionList.add(quizDto.toQuestion(topic.id, domain.id))
                        }
                    }
                    val totalQuestions = theoryCount + quizCount
                    topicList.add(Topic(
                        id = topic.id,
                        domainId = domain.id,
                        name = topic.name,
                        description = topic.description,
                        iconUrl = topic.iconUrl ?: "",
                        questionCount = totalQuestions
                    ))
                }
            }
            // Optionally recalc topicCount/questionCount if needed
            isDataLoaded = true
            domains = domainList
            topics = topicList
            questions = questionList
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getDomains(): List<Domain> = domains
    
    fun getTopicsByDomain(domainId: String): List<Topic> {
        return topics.filter { it.domainId == domainId }
    }
    
    fun getQuestionsByTopic(topicId: String): List<Question> {
        return questions.filter { it.topicId == topicId }
    }
    
    /**
     * Get learning questions (THEORY) for topic screen
     */
    fun getTheoryQuestionsByTopic(topicId: String): List<Question> {
        return questions.filter { 
            it.topicId == topicId && it.type == com.prepstack.domain.model.QuestionType.THEORY 
        }
    }
    
    /**
     * Get quiz questions (MCQ) for quiz screen
     */
    fun getQuizQuestionsByTopic(topicId: String): List<Question> {
        // First try to get cached MCQ questions
        val cachedQuestions = questions.filter { 
            it.topicId == topicId && it.type == com.prepstack.domain.model.QuestionType.MCQ 
        }
        if (cachedQuestions.isNotEmpty()) return cachedQuestions

        // If not found, try loading from domain-level quizzes (latest JSON structure)
        // Find domain containing this topic
        val domain = topics.find { it.id == topicId }?.domainId ?: return emptyList()
        val quizzes = getDomainQuizzes(domain)
        val quizDescriptor = quizzes.find { it.topicId == topicId } ?: return emptyList()
        val quizJson = JsonReader.readJsonFromAssets(context, quizDescriptor.file) ?: return emptyList()
        val topicQuizDto = gson.fromJson(quizJson, com.prepstack.data.dto.TopicQuizDto::class.java)
        return topicQuizDto.quizQuestions.map { it.toQuestion(topicId, domain) }
    }
    
    fun getQuestionById(questionId: String): Question? {
        return questions.find { it.id == questionId }
    }
    
    fun getRandomQuestions(
        domainId: String? = null,
        topicId: String? = null,
        count: Int
    ): List<Question> {
        val filtered = when {
            topicId != null -> questions.filter { it.topicId == topicId && it.type == com.prepstack.domain.model.QuestionType.MCQ }
            domainId != null -> questions.filter { it.domainId == domainId && it.type == com.prepstack.domain.model.QuestionType.MCQ }
            else -> questions.filter { it.type == com.prepstack.domain.model.QuestionType.MCQ }
        }
        
        return filtered.shuffled().take(count)
    }

    fun getDomainQuizzes(domainId: String): List<QuizDescriptor> {
        // Load domains.json from assets
        val domainsJson = JsonReader.readJsonFromAssets(context, com.prepstack.core.util.Constants.JSON_FILE_NAME)
            ?: return emptyList()
        val domainsDto = gson.fromJson(domainsJson, com.prepstack.data.dto.DomainsDto::class.java)
        val domain = domainsDto.domains.find { it.id == domainId } ?: return emptyList()
        val topicsFile = domain.topicsFile
        // Load domain-specific topics file, e.g., domains/android.json
        val domainTopicsJson = JsonReader.readJsonFromAssets(context, topicsFile) ?: return emptyList()
        val domainTopics = gson.fromJson(domainTopicsJson, com.prepstack.data.dto.DomainTopicsDto::class.java)
        return domainTopics.quizzes ?: emptyList()
    }
}
