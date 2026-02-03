package com.prepstack.voiceinterview.core

import com.prepstack.voiceinterview.core.model.InterviewConfig
import com.prepstack.voiceinterview.core.model.InterviewEvaluation
import com.prepstack.voiceinterview.core.model.InterviewQuestion
import com.prepstack.voiceinterview.core.model.InterviewResponse
import com.prepstack.voiceinterview.core.model.InterviewState
import com.prepstack.voiceinterview.core.model.InterviewStatus
import com.prepstack.voiceinterview.core.model.InterviewSummary
import com.prepstack.voiceinterview.core.service.AIService
import com.prepstack.voiceinterview.core.service.InterviewRepository
import com.prepstack.voiceinterview.speech.SpeechManager
import com.prepstack.voiceinterview.speech.SpeechRecognitionCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Represents an ongoing interview session with methods to control it.
 */
interface InterviewSession {
    /**
     * Unique identifier for this session.
     */
    val sessionId: String
    
    /**
     * The configuration used for this interview.
     */
    val config: InterviewConfig
    
    /**
     * Current state of the interview session.
     */
    val state: StateFlow<InterviewState>
    
    /**
     * Start listening for user's answer to the current question.
     * @return true if listening started successfully
     */
    suspend fun startListening(): Boolean
    
    /**
     * Stop listening and process the captured audio as user's answer.
     * @return true if stopped successfully
     */
    suspend fun stopListening(): Boolean
    
    /**
     * Submit a text answer instead of using voice input.
     * @param textAnswer The text answer to submit
     * @return true if the answer was submitted successfully
     */
    suspend fun submitTextAnswer(textAnswer: String): Boolean
    
    /**
     * Move to the next question in the interview.
     * @return true if there is a next question and it was prepared successfully
     */
    suspend fun nextQuestion(): Boolean
    
    /**
     * End the current interview session.
     * @param generateSummary Whether to generate a summary of the interview
     * @return Summary of the interview if requested, null otherwise
     */
    suspend fun end(generateSummary: Boolean = true): InterviewSummary?
    
    /**
     * Pause the current interview session.
     * @return true if paused successfully
     */
    suspend fun pause(): Boolean
    
    /**
     * Resume a paused interview session.
     * @return true if resumed successfully
     */
    suspend fun resume(): Boolean
}

/**
 * Implementation of InterviewSession.
 */
internal class InterviewSessionImpl(
    override val sessionId: String,
    override val config: InterviewConfig,
    private val stateFlow: MutableStateFlow<InterviewState>,
    private val speechManager: SpeechManager,
    private val aiService: AIService,
    private val interviewRepository: InterviewRepository
) : InterviewSession {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentQuestionIndex = -1
    private val questions = mutableListOf<InterviewQuestion>()
    private val responses = mutableListOf<InterviewResponse>()
    private var isActive = false
    
    override val state: StateFlow<InterviewState>
        get() = stateFlow
        
    fun initialize() {
        isActive = true
        scope.launch {
            try {
                updateState { it.copy(status = InterviewStatus.WAITING_FOR_QUESTION) }
                
                // Get first question
                val firstQuestion = aiService.generateInitialQuestion(
                    topicId = config.topicId,
                    difficultyLevel = config.difficultyLevel
                )
                
                questions.add(firstQuestion)
                currentQuestionIndex = 0
                
                // Prepare to present question
                updateState { 
                    it.copy(
                        status = InterviewStatus.PRESENTING_QUESTION,
                        currentQuestion = firstQuestion
                    ) 
                }
                
                // Speak the question
                speechManager.speak(firstQuestion.text) {
                    // After speaking, update state to waiting for response
                    scope.launch {
                        // Add a small delay to ensure speech recognizer is ready
                        kotlinx.coroutines.delay(500) // Give 500ms for initialization
                        updateState { it.copy(status = InterviewStatus.LISTENING) }
                        startListening()
                    }
                }
            } catch (e: Exception) {
                updateState { 
                    it.copy(
                        status = InterviewStatus.ERROR,
                        error = e.message ?: "Failed to initialize interview session"
                    ) 
                }
            }
        }
    }
    
    override suspend fun startListening(): Boolean {
        return try {
            val currentQuestion = getCurrentQuestion() ?: return false
            
            updateState { it.copy(status = InterviewStatus.LISTENING) }
            
            speechManager.startListening(object : SpeechRecognitionCallback {
                override fun onResult(text: String, isFinal: Boolean) {
                    if (isFinal) {
                        scope.launch {
                            submitTextAnswer(text)
                        }
                    }
                }
                
                override fun onError(error: String) {
                    scope.launch {
                        // Differentiate between recoverable and critical errors
                        val isRecoverableError = error.contains("No speech input", ignoreCase = true) ||
                                error.contains("timeout", ignoreCase = true) ||
                                error.contains("No match", ignoreCase = true)
                        
                        if (isRecoverableError) {
                            // For recoverable errors, stay in listening state but show message
                            // This allows the user to try speaking again
                            updateState { 
                                it.copy(
                                    status = InterviewStatus.LISTENING,
                                    error = error // Store error but don't change status
                                ) 
                            }
                            // Auto-restart listening after brief delay
                            kotlinx.coroutines.delay(1000)
                            updateState { it.copy(error = null) } // Clear error message
                            startListening() // Restart listening
                        } else {
                            // For critical errors (permission, audio), go to error state
                            updateState { 
                                it.copy(
                                    status = InterviewStatus.ERROR,
                                    error = "Speech recognition failed: $error"
                                ) 
                            }
                        }
                    }
                }
            })
            true
        } catch (e: Exception) {
            updateState { 
                it.copy(
                    status = InterviewStatus.ERROR,
                    error = "Failed to start listening: ${e.message}"
                ) 
            }
            false
        }
    }
    
    override suspend fun stopListening(): Boolean {
        return try {
            speechManager.stopListening()
            true
        } catch (e: Exception) {
            updateState { 
                it.copy(
                    status = InterviewStatus.ERROR,
                    error = "Failed to stop listening: ${e.message}"
                ) 
            }
            false
        }
    }
    
    override suspend fun submitTextAnswer(textAnswer: String): Boolean {
        val currentQuestion = getCurrentQuestion() ?: return false
        
        return try {
            updateState { it.copy(status = InterviewStatus.PROCESSING_RESPONSE) }
            
            // Process answer with AI
            val response = aiService.processAnswer(
                questionId = currentQuestion.id,
                question = currentQuestion.text,
                answer = textAnswer,
                difficultyLevel = config.difficultyLevel,
                previousResponses = responses,
                topicId = config.topicId
            )
            
            responses.add(response)
            
            // Update state with evaluation
            updateState { 
                it.copy(
                    status = InterviewStatus.PRESENTING_FEEDBACK,
                    currentResponse = response,
                    previousQuestions = it.previousQuestions + currentQuestion,
                    currentQuestion = null
                ) 
            }
            
            // Speak feedback
            speechManager.speak(response.evaluation.feedbackSummary) {
                // If there's a next question, proceed to it
                if (response.nextQuestionId != null) {
                    scope.launch {
                        nextQuestion()
                    }
                } else {
                    // No more questions, end the interview
                    scope.launch {
                        end()
                    }
                }
            }
            
            true
        } catch (e: Exception) {
            updateState { 
                it.copy(
                    status = InterviewStatus.ERROR,
                    error = "Failed to process answer: ${e.message}"
                ) 
            }
            false
        }
    }
    
    override suspend fun nextQuestion(): Boolean {
        val currentState = state.value
        val nextQuestionId = currentState.currentResponse?.nextQuestionId ?: return false
        
        try {
            updateState { it.copy(status = InterviewStatus.WAITING_FOR_QUESTION) }
            
            // Fetch next question
            val nextQuestion = aiService.generateFollowUpQuestion(
                previousQuestionId = currentState.currentResponse.questionId,
                nextQuestionId = nextQuestionId,
                previousResponses = responses,
                topicId = config.topicId,
                difficultyLevel = config.difficultyLevel
            )
            
            questions.add(nextQuestion)
            currentQuestionIndex = questions.size - 1
            
            // Prepare to present question
            updateState { 
                it.copy(
                    status = InterviewStatus.PRESENTING_QUESTION,
                    currentQuestion = nextQuestion,
                    currentResponse = null
                ) 
            }
            
            // Speak the question
            speechManager.speak(nextQuestion.text) {
                // After speaking, update state to waiting for response
                scope.launch {
                    // Stop any previous listening session and add a small delay
                    // to ensure speech recognizer is ready
                    try {
                        speechManager.stopListening()
                    } catch (e: Exception) {
                        // Ignore errors if nothing was listening
                    }
                    kotlinx.coroutines.delay(500) // Give 500ms for cleanup
                    updateState { it.copy(status = InterviewStatus.LISTENING) }
                    startListening()
                }
            }
            
            return true
        } catch (e: Exception) {
            updateState { 
                it.copy(
                    status = InterviewStatus.ERROR,
                    error = "Failed to fetch next question: ${e.message}"
                ) 
            }
            return false
        }
    }
    
    override suspend fun end(generateSummary: Boolean): InterviewSummary? {
        android.util.Log.d("InterviewSession", "end() called, isActive=$isActive, responses=${responses.size}")
        
        if (!isActive) {
            android.util.Log.w("InterviewSession", "Session not active, returning null")
            return null
        }
        isActive = false
        
        return try {
            // Stop any ongoing speech or listening
            speechManager.stopSpeaking()
            speechManager.stopListening()
            
            android.util.Log.d("InterviewSession", "generateSummary=$generateSummary, responses.size=${responses.size}")
            
            if (generateSummary) {
                if (responses.isNotEmpty()) {
                    // Generate AI summary
                    android.util.Log.d("InterviewSession", "Generating AI summary...")
                    val summary = aiService.generateInterviewSummary(
                        questions = questions,
                        responses = responses,
                        config = config
                    )
                    
                    android.util.Log.d("InterviewSession", "Summary generated: $summary")
                    
                    updateState { 
                        it.copy(
                            status = InterviewStatus.COMPLETED,
                            sessionSummary = summary
                        ) 
                    }
                    
                    summary
                } else {
                    // No responses, create a basic summary
                    android.util.Log.d("InterviewSession", "No responses, creating basic summary")
                    val basicSummary = InterviewSummary(
                        overallScore = 0,
                        strengthAreas = emptyList(),
                        improvementAreas = listOf("Try completing the interview next time"),
                        generalFeedback = "Interview was ended without answering any questions.",
                        totalQuestionsAsked = questions.size,
                        completedQuestionsCount = 0
                    )
                    
                    updateState { 
                        it.copy(
                            status = InterviewStatus.COMPLETED,
                            sessionSummary = basicSummary
                        ) 
                    }
                    
                    basicSummary
                }
            } else {
                updateState { it.copy(status = InterviewStatus.COMPLETED) }
                null
            }
        } catch (e: Exception) {
            updateState { 
                it.copy(
                    status = InterviewStatus.ERROR,
                    error = "Failed to end interview: ${e.message}"
                ) 
            }
            null
        }
    }
    
    override suspend fun pause(): Boolean {
        return try {
            speechManager.stopListening()
            speechManager.stopSpeaking()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun resume(): Boolean {
        val currentState = state.value
        return when (currentState.status) {
            InterviewStatus.PRESENTING_QUESTION -> {
                val currentQuestion = getCurrentQuestion() ?: return false
                speechManager.speak(currentQuestion.text) {
                    scope.launch {
                        updateState { it.copy(status = InterviewStatus.LISTENING) }
                        startListening()
                    }
                }
                true
            }
            InterviewStatus.PRESENTING_FEEDBACK -> {
                val currentResponse = currentState.currentResponse ?: return false
                speechManager.speak(currentResponse.evaluation.feedbackSummary) {
                    // If there's a next question, proceed to it
                    if (currentResponse.nextQuestionId != null) {
                        scope.launch {
                            nextQuestion()
                        }
                    } else {
                        // No more questions, end the interview
                        scope.launch {
                            end()
                        }
                    }
                }
                true
            }
            else -> false
        }
    }
    
    private fun getCurrentQuestion(): InterviewQuestion? {
        return if (currentQuestionIndex in questions.indices) {
            questions[currentQuestionIndex]
        } else {
            null
        }
    }
    
    private suspend fun updateState(update: (InterviewState) -> InterviewState) {
        withContext(Dispatchers.Main) {
            stateFlow.value = update(stateFlow.value)
        }
    }
}