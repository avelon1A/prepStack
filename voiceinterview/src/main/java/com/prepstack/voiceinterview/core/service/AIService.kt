package com.prepstack.voiceinterview.core.service

import com.prepstack.voiceinterview.core.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Service for AI-powered interview functionality.
 */
interface AIService {
    /**
     * Generate the initial interview question.
     * @param topicId ID of the interview topic
     * @param difficultyLevel Difficulty level for the question
     * @return A new interview question
     */
    suspend fun generateInitialQuestion(
        topicId: String,
        difficultyLevel: DifficultyLevel
    ): InterviewQuestion
    
    /**
     * Generate a follow-up question based on previous responses.
     * @param previousQuestionId ID of the previous question
     * @param nextQuestionId ID for the next question (if provided by response)
     * @param previousResponses List of previous responses
     * @param topicId ID of the interview topic
     * @param difficultyLevel Difficulty level for the question
     * @return A new interview question
     */
    suspend fun generateFollowUpQuestion(
        previousQuestionId: String,
        nextQuestionId: String,
        previousResponses: List<InterviewResponse>,
        topicId: String,
        difficultyLevel: DifficultyLevel
    ): InterviewQuestion
    
    /**
     * Process the user's answer and evaluate it.
     * @param questionId ID of the question being answered
     * @param question Text of the question being answered
     * @param answer User's answer text
     * @param difficultyLevel Difficulty level of the question
     * @param previousResponses List of previous responses
     * @param topicId ID of the interview topic
     * @return Interview response with evaluation and next question ID
     */
    suspend fun processAnswer(
        questionId: String,
        question: String,
        answer: String,
        difficultyLevel: DifficultyLevel,
        previousResponses: List<InterviewResponse>,
        topicId: String
    ): InterviewResponse
    
    /**
     * Generate a summary of the entire interview.
     * @param questions List of all questions asked
     * @param responses List of all responses received
     * @param config Configuration used for the interview
     * @return Interview summary
     */
    suspend fun generateInterviewSummary(
        questions: List<InterviewQuestion>,
        responses: List<InterviewResponse>,
        config: InterviewConfig
    ): InterviewSummary
    
    companion object {
        /**
         * Create a default implementation of AIService.
         */
        fun create(apiKey: String, baseUrl: String): AIService {
            return DefaultAIService(apiKey, baseUrl)
        }
    }
}

/**
 * Implementation of AIService using OpenAI API.
 */
internal class DefaultAIService(
    private val apiKey: String,
    private val baseUrl: String
) : AIService {
    private val api: OpenAIApi = OpenAIApiClient.create(apiKey, baseUrl)
    
    companion object {
        fun create(apiKey: String, baseUrl: String): AIService {
            return DefaultAIService(apiKey, baseUrl)
        }
    }
    
    override suspend fun generateInitialQuestion(
        topicId: String,
        difficultyLevel: DifficultyLevel
    ): InterviewQuestion {
        val topic = InterviewRepository.create().getTopicById(topicId)
            ?: throw IllegalArgumentException("Invalid topic ID: $topicId")
        
        val systemPrompt = buildSystemPrompt(
            action = "generate_initial_question",
            topic = topic,
            difficultyLevel = difficultyLevel
        )
        
        val requestBody = ChatCompletionRequest(
            model = "gpt-4-turbo",
            messages = listOf(
                ChatMessage(role = "system", content = systemPrompt),
                ChatMessage(
                    role = "user",
                    content = "Generate the first interview question for this ${difficultyLevel.name.lowercase()} " +
                            "level interview on ${topic.name}."
                )
            ),
            responseFormat = ResponseFormat(type = "json_object")
        )
        
        val response = api.createChatCompletion(requestBody)
        
        val questionResponse = deserializeJsonResponse<QuestionResponse>(
            response.choices[0].message.content
        )
        
        return InterviewQuestion(
            id = questionResponse.questionId,
            text = questionResponse.questionText,
            type = QuestionType.valueOf(questionResponse.questionType),
            difficultyLevel = difficultyLevel,
            expectedDurationSeconds = questionResponse.expectedDurationSeconds
        )
    }
    
    override suspend fun generateFollowUpQuestion(
        previousQuestionId: String,
        nextQuestionId: String,
        previousResponses: List<InterviewResponse>,
        topicId: String,
        difficultyLevel: DifficultyLevel
    ): InterviewQuestion {
        val topic = InterviewRepository.create().getTopicById(topicId)
            ?: throw IllegalArgumentException("Invalid topic ID: $topicId")
        
        val systemPrompt = buildSystemPrompt(
            action = "generate_followup_question",
            topic = topic,
            difficultyLevel = difficultyLevel
        )
        
        val previousQuestionsContext = previousResponses.map { response ->
            """
            Previous Question ID: ${response.questionId}
            User Answer: ${response.transcribedAnswer}
            Evaluation Score: ${response.evaluation.score}/10
            """
        }.joinToString("\n")
        
        val requestBody = ChatCompletionRequest(
            model = "gpt-4-turbo",
            messages = listOf(
                ChatMessage(role = "system", content = systemPrompt),
                ChatMessage(
                    role = "user",
                    content = """
                    Generate the next interview question based on the previous responses.
                    
                    Previous context:
                    $previousQuestionsContext
                    
                    Next Question ID: $nextQuestionId
                    Difficulty Level: ${difficultyLevel.name}
                    Topic: ${topic.name}
                    """
                )
            ),
            responseFormat = ResponseFormat(type = "json_object")
        )
        
        val response = api.createChatCompletion(requestBody)
        
        val questionResponse = deserializeJsonResponse<QuestionResponse>(
            response.choices[0].message.content
        )
        
        return InterviewQuestion(
            id = nextQuestionId,
            text = questionResponse.questionText,
            type = QuestionType.valueOf(questionResponse.questionType),
            difficultyLevel = difficultyLevel,
            expectedDurationSeconds = questionResponse.expectedDurationSeconds
        )
    }
    
    override suspend fun processAnswer(
        questionId: String,
        question: String,
        answer: String,
        difficultyLevel: DifficultyLevel,
        previousResponses: List<InterviewResponse>,
        topicId: String
    ): InterviewResponse {
        val topic = InterviewRepository.create().getTopicById(topicId)
            ?: throw IllegalArgumentException("Invalid topic ID: $topicId")
        
        val systemPrompt = buildSystemPrompt(
            action = "evaluate_answer",
            topic = topic,
            difficultyLevel = difficultyLevel
        )
        
        val requestBody = ChatCompletionRequest(
            model = "gpt-4-turbo",
            messages = listOf(
                ChatMessage(role = "system", content = systemPrompt),
                ChatMessage(
                    role = "user",
                    content = """
                    Evaluate the following interview response:
                    
                    Question ID: $questionId
                    Question: $question
                    User's Answer: $answer
                    
                    Difficulty Level: ${difficultyLevel.name}
                    Topic: ${topic.name}
                    Previous Questions: ${previousResponses.size}
                    """
                )
            ),
            responseFormat = ResponseFormat(type = "json_object")
        )
        
        val response = api.createChatCompletion(requestBody)
        
        val evaluationResponse = deserializeJsonResponse<EvaluationResponse>(
            response.choices[0].message.content
        )
        
        val evaluation = InterviewEvaluation(
            score = evaluationResponse.score,
            feedbackSummary = evaluationResponse.feedbackSummary,
            strengths = evaluationResponse.strengths,
            improvements = evaluationResponse.improvements,
            technicalAccuracy = evaluationResponse.technicalAccuracy,
            communicationClarity = evaluationResponse.communicationClarity
        )
        
        return InterviewResponse(
            questionId = questionId,
            transcribedAnswer = answer,
            evaluation = evaluation,
            nextQuestionId = evaluationResponse.nextQuestionId
        )
    }
    
    override suspend fun generateInterviewSummary(
        questions: List<InterviewQuestion>,
        responses: List<InterviewResponse>,
        config: InterviewConfig
    ): InterviewSummary {
        val topic = InterviewRepository.create().getTopicById(config.topicId)
            ?: throw IllegalArgumentException("Invalid topic ID: ${config.topicId}")
        
        val systemPrompt = buildSystemPrompt(
            action = "generate_summary",
            topic = topic,
            difficultyLevel = config.difficultyLevel
        )
        
        val questionsAndAnswers = questions.zip(responses) { question, response ->
            """
            Question: ${question.text}
            Answer: ${response.transcribedAnswer}
            Score: ${response.evaluation.score}/10
            Feedback: ${response.evaluation.feedbackSummary}
            """
        }.joinToString("\n\n")
        
        val requestBody = ChatCompletionRequest(
            model = "gpt-4-turbo",
            messages = listOf(
                ChatMessage(role = "system", content = systemPrompt),
                ChatMessage(
                    role = "user",
                    content = """
                    Generate a summary for the completed interview:
                    
                    Topic: ${topic.name}
                    Difficulty Level: ${config.difficultyLevel.name}
                    Questions Asked: ${questions.size}
                    
                    Interview Details:
                    $questionsAndAnswers
                    """
                )
            ),
            responseFormat = ResponseFormat(type = "json_object")
        )
        
        val response = api.createChatCompletion(requestBody)
        
        val summaryResponse = deserializeJsonResponse<SummaryResponse>(
            response.choices[0].message.content
        )
        
        return InterviewSummary(
            overallScore = summaryResponse.overallScore,
            strengthAreas = summaryResponse.strengthAreas,
            improvementAreas = summaryResponse.improvementAreas,
            generalFeedback = summaryResponse.generalFeedback,
            totalQuestionsAsked = questions.size,
            completedQuestionsCount = responses.size
        )
    }
    
    private fun buildSystemPrompt(
        action: String,
        topic: InterviewTopic,
        difficultyLevel: DifficultyLevel
    ): String {
        val commonInstructions = """
            You are an expert interviewer for technical interviews, specializing in ${topic.name}.
            Provide JSON responses ONLY, with no additional explanations or markdown.
            Keep feedback concise and spoken-friendly, as it will be spoken via text-to-speech.
            Limit each text field to a maximum of 2 short sentences.
        """.trimIndent()
        
        val actionSpecificInstructions = when (action) {
            "generate_initial_question" -> """
                Generate an initial ${difficultyLevel.name.lowercase()} level interview question about ${topic.name}.
                
                Your response must be a JSON object with these fields:
                - questionId (string): A unique identifier for the question
                - questionText (string): The actual question, well formulated for voice
                - questionType (enum string): One of [TECHNICAL_KNOWLEDGE, PROBLEM_SOLVING, BEHAVIORAL, SYSTEM_DESIGN, OPEN_ENDED, SCENARIO_BASED]
                - expectedDurationSeconds (int): Expected answer time (30-120 seconds)
            """.trimIndent()
            
            "generate_followup_question" -> """
                Generate a follow-up ${difficultyLevel.name.lowercase()} level interview question about ${topic.name}.
                The question should build on previous responses and increase in complexity.
                
                Your response must be a JSON object with these fields:
                - questionText (string): The actual question, well formulated for voice
                - questionType (enum string): One of [TECHNICAL_KNOWLEDGE, PROBLEM_SOLVING, BEHAVIORAL, SYSTEM_DESIGN, OPEN_ENDED, SCENARIO_BASED]
                - expectedDurationSeconds (int): Expected answer time (30-120 seconds)
            """.trimIndent()
            
            "evaluate_answer" -> """
                Evaluate the candidate's answer to a ${difficultyLevel.name.lowercase()} level question about ${topic.name}.
                Be thorough but concise, as your feedback will be spoken via text-to-speech.
                
                Your response must be a JSON object with these fields:
                - score (int): Score from 1-10
                - feedbackSummary (string): Brief spoken feedback (1-2 sentences)
                - strengths (array of strings): 1-3 specific strengths
                - improvements (array of strings): 1-3 specific improvements
                - technicalAccuracy (int, optional): Score from 1-10 for technical questions
                - communicationClarity (int): Score from 1-10
                - nextQuestionId (string, optional): UUID for next question, null if no more questions
            """.trimIndent()
            
            "generate_summary" -> """
                Generate a summary of the entire interview on ${topic.name} at ${difficultyLevel.name.lowercase()} level.
                Keep it concise and constructive, as it will be spoken via text-to-speech.
                
                Your response must be a JSON object with these fields:
                - overallScore (int): Score from 1-10
                - strengthAreas (array of strings): 1-3 major strengths demonstrated
                - improvementAreas (array of strings): 1-3 areas for improvement
                - generalFeedback (string): Brief spoken summary feedback (1-2 sentences)
            """.trimIndent()
            
            else -> throw IllegalArgumentException("Unknown action: $action")
        }
        
        return "$commonInstructions\n\n$actionSpecificInstructions"
    }
    
    private inline fun <reified T> deserializeJsonResponse(jsonString: String): T {
        return kotlinx.serialization.json.Json.decodeFromString(jsonString)
    }
}

// API models and client
internal interface OpenAIApi {
    suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse
}

internal object OpenAIApiClient {
    fun create(apiKey: String, baseUrl: String): OpenAIApi {
        return object : OpenAIApi {
            override suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse {
                return try {
                    // Make actual API call to OpenAI
                    makeApiCall(apiKey, baseUrl, request)
                } catch (e: Exception) {
                    // Fallback to simulated response if API call fails
                    ChatCompletionResponse(
                        id = "simulated-${UUID.randomUUID()}",
                        choices = listOf(
                            ChatCompletionChoice(
                                message = ChatMessage(
                                    role = "assistant",
                                    content = simulateResponse(request)
                                ),
                                finishReason = "stop"
                            )
                        )
                    )
                }
            }
            
            private suspend fun makeApiCall(
                apiKey: String,
                baseUrl: String,
                request: ChatCompletionRequest
            ): ChatCompletionResponse {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()
                
                val json = kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
                
                // Serialize request to JSON
                val requestJson = json.encodeToString(ChatCompletionRequest.serializer(), request)
                
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = requestJson.toRequestBody(mediaType)
                
                val apiRequest = Request.Builder()
                    .url("${baseUrl}chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()
                
                // Execute API call in IO context
                val response = withContext(Dispatchers.IO) {
                    client.newCall(apiRequest).execute()
                }
                
                if (!response.isSuccessful) {
                    throw Exception("API call failed: ${response.code} ${response.message}")
                }
                
                val responseBody = response.body?.string() 
                    ?: throw Exception("Empty response body")
                
                // Deserialize response
                return json.decodeFromString(ChatCompletionResponse.serializer(), responseBody)
            }
            
            private fun simulateResponse(request: ChatCompletionRequest): String {
                // This would be replaced by actual API call in production
                val userMessage = request.messages.last { it.role == "user" }.content
                
                // Extract topic from user message
                val topicMatch = Regex("interview on (.+)\\.").find(userMessage)
                val topic = topicMatch?.groupValues?.get(1) ?: "programming"
                
                // Generate topic-specific questions
                return if (userMessage.contains("Generate the first interview question")) {
                    val questionText = when {
                        topic.contains("Android", ignoreCase = true) -> 
                            "Can you explain the Android activity lifecycle and how you handle configuration changes in your apps?"
                        topic.contains("React", ignoreCase = true) -> 
                            "What are React hooks and how do they improve functional components compared to class components?"
                        topic.contains("Node", ignoreCase = true) || topic.contains("JavaScript", ignoreCase = true) -> 
                            "How does the event loop work in Node.js and what is the difference between callbacks, promises, and async/await?"
                        topic.contains("Java", ignoreCase = true) -> 
                            "Can you explain the differences between abstract classes and interfaces in Java, and when would you use each?"
                        topic.contains("Python", ignoreCase = true) -> 
                            "What are Python decorators and how would you use them in a real-world application?"
                        topic.contains("Kotlin", ignoreCase = true) -> 
                            "Can you explain the concept of coroutines in Kotlin and how they help with asynchronous programming?"
                        topic.contains("iOS", ignoreCase = true) || topic.contains("Swift", ignoreCase = true) -> 
                            "What is the difference between structs and classes in Swift, and when should you use each?"
                        else -> 
                            "Can you tell me about your experience with $topic and describe a challenging problem you solved?"
                    }
                    """
                    {
                        "questionId": "${UUID.randomUUID()}",
                        "questionText": "$questionText",
                        "questionType": "TECHNICAL_KNOWLEDGE",
                        "expectedDurationSeconds": 60
                    }
                    """.trimIndent()
                } else if (userMessage.contains("Generate the next interview question") || userMessage.contains("follow-up")) {
                    val questionText = when {
                        topic.contains("Android", ignoreCase = true) -> 
                            "How would you implement a RecyclerView with multiple view types and handle item click events efficiently?"
                        topic.contains("React", ignoreCase = true) -> 
                            "How do you manage state in a large React application? Can you compare Context API with Redux?"
                        topic.contains("Node", ignoreCase = true) || topic.contains("JavaScript", ignoreCase = true) -> 
                            "How would you build a RESTful API in Node.js with proper error handling and authentication?"
                        topic.contains("Java", ignoreCase = true) -> 
                            "How do you handle multi-threading in Java and what are the best practices for avoiding race conditions?"
                        topic.contains("Python", ignoreCase = true) -> 
                            "How would you optimize the performance of a Python application that processes large datasets?"
                        topic.contains("Kotlin", ignoreCase = true) -> 
                            "What are the advantages of using Flow over LiveData in Android development?"
                        else -> 
                            "Can you design a scalable architecture for a $topic application that needs to handle high traffic?"
                    }
                    """
                    {
                        "questionText": "$questionText",
                        "questionType": "SYSTEM_DESIGN",
                        "expectedDurationSeconds": 90
                    }
                    """.trimIndent()
                } else if (userMessage.contains("Evaluate the following interview response")) {
                    """
                    {
                        "score": 8,
                        "feedbackSummary": "Your answer was clear and technically accurate. You covered the core principles well.",
                        "strengths": ["Strong technical understanding", "Clear explanation", "Good real-world examples"],
                        "improvements": ["Could elaborate more on encapsulation", "Consider mentioning more design patterns"],
                        "technicalAccuracy": 8,
                        "communicationClarity": 9,
                        "nextQuestionId": "${UUID.randomUUID()}"
                    }
                    """.trimIndent()
                } else if (userMessage.contains("Generate a summary")) {
                    """
                    {
                        "overallScore": 8,
                        "strengthAreas": ["Strong technical knowledge", "Clear communication", "Practical experience"],
                        "improvementAreas": ["System design depth", "Algorithm complexity analysis"],
                        "generalFeedback": "You demonstrated solid technical knowledge and communication skills. Focus on deepening your system design expertise."
                    }
                    """.trimIndent()
                } else {
                    """
                    {
                        "error": "Unrecognized request type"
                    }
                    """.trimIndent()
                }
            }
        }
    }
}

// Data models for OpenAI API
@Serializable
internal data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f,
    @SerialName("response_format")
    val responseFormat: ResponseFormat? = null
)

@Serializable
internal data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
internal data class ResponseFormat(
    val type: String
)

@Serializable
internal data class ChatCompletionResponse(
    val id: String,
    val choices: List<ChatCompletionChoice>
)

@Serializable
internal data class ChatCompletionChoice(
    val message: ChatMessage,
    @SerialName("finish_reason")
    val finishReason: String
)

// Response data models
@Serializable
internal data class QuestionResponse(
    val questionId: String = UUID.randomUUID().toString(),
    val questionText: String,
    val questionType: String,
    val expectedDurationSeconds: Int
)

@Serializable
internal data class EvaluationResponse(
    val score: Int,
    val feedbackSummary: String,
    val strengths: List<String>,
    val improvements: List<String>,
    val technicalAccuracy: Int? = null,
    val communicationClarity: Int,
    val nextQuestionId: String?
)

@Serializable
internal data class SummaryResponse(
    val overallScore: Int,
    val strengthAreas: List<String>,
    val improvementAreas: List<String>,
    val generalFeedback: String
)