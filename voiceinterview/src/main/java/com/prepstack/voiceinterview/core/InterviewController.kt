package com.prepstack.voiceinterview.core

import android.content.Context
import com.prepstack.voiceinterview.core.model.InterviewConfig
import com.prepstack.voiceinterview.core.model.InterviewState
import com.prepstack.voiceinterview.core.model.InterviewTopic
import com.prepstack.voiceinterview.core.service.AIService
import com.prepstack.voiceinterview.core.service.DefaultAIService
import com.prepstack.voiceinterview.core.service.InterviewRepository
import com.prepstack.voiceinterview.speech.SpeechManager
import com.prepstack.voiceinterview.speech.SpeechRecognitionManager
import com.prepstack.voiceinterview.speech.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

/**
 * Core controller that coordinates all components of the Voice Interview system.
 */
interface InterviewController {
    /**
     * Current state of an active interview, or null if no interview is in progress.
     */
    val currentInterviewState: StateFlow<InterviewState>?
    
    /**
     * Retrieves available interview topics.
     */
    suspend fun getAvailableTopics(): List<InterviewTopic>
    
    /**
     * Starts a new interview with the given configuration.
     */
    suspend fun startInterview(config: InterviewConfig): InterviewSession
    
    /**
     * Shuts down the controller and releases resources.
     */
    fun shutdown()
    
    companion object {
        /**
         * Creates a default implementation of the InterviewController.
         */
        fun create(
            context: Context,
            apiKey: String,
            baseUrl: String
        ): InterviewController {
            val speechManager = SpeechManager.create(
                speechRecognition = SpeechRecognitionManager.create(context),
                textToSpeech = TextToSpeechManager.create(context)
            )
            
            val aiService = DefaultAIService.create(
                apiKey = apiKey,
                baseUrl = baseUrl
            )
            
            val interviewRepository = InterviewRepository.create()
            
            return DefaultInterviewController(
                context = context,
                speechManager = speechManager,
                aiService = aiService,
                interviewRepository = interviewRepository
            )
        }
    }
}

/**
 * Default implementation of InterviewController.
 */
internal class DefaultInterviewController(
    private val context: Context,
    private val speechManager: SpeechManager,
    private val aiService: AIService,
    private val interviewRepository: InterviewRepository
) : InterviewController {
    private var _currentSession: InterviewSessionImpl? = null
    private var _currentInterviewState: MutableStateFlow<InterviewState>? = null
    
    override val currentInterviewState: StateFlow<InterviewState>?
        get() = _currentInterviewState
        
    override suspend fun getAvailableTopics(): List<InterviewTopic> {
        return interviewRepository.getAvailableTopics()
    }
    
    override suspend fun startInterview(config: InterviewConfig): InterviewSession {
        // End any existing session
        _currentSession?.end()
        
        // Create a new session
        val sessionId = java.util.UUID.randomUUID().toString()
        val initialState = InterviewState(
            sessionId = sessionId,
            status = com.prepstack.voiceinterview.core.model.InterviewStatus.INITIALIZING
        )
        
        val stateFlow = MutableStateFlow(initialState)
        _currentInterviewState = stateFlow
        
        val session = InterviewSessionImpl(
            sessionId = sessionId,
            config = config,
            stateFlow = stateFlow,
            speechManager = speechManager,
            aiService = aiService,
            interviewRepository = interviewRepository
        )
        
        _currentSession = session
        session.initialize()
        return session
    }
    
    override fun shutdown() {
        runBlocking {
            _currentSession?.end()
        }
        _currentSession = null
        _currentInterviewState = null
        speechManager.shutdown()
    }
}