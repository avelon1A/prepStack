package com.prepstack.voiceinterview.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prepstack.voiceinterview.VoiceInterviewSDK
import com.prepstack.voiceinterview.core.InterviewSession
import com.prepstack.voiceinterview.core.model.*
import com.prepstack.voiceinterview.core.model.InterviewSummary as CoreInterviewSummary
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Voice Interview feature.
 */
class VoiceInterviewViewModel(
    application: Application,
    private val apiKey: String
) : AndroidViewModel(application) {
    
    private val sdk: VoiceInterviewSDK = VoiceInterviewSDK.initialize(application, apiKey)
    private var currentSession: InterviewSession? = null
    
    private val _uiState = MutableStateFlow<VoiceInterviewUiState>(VoiceInterviewUiState.Loading)
    val uiState: StateFlow<VoiceInterviewUiState> = _uiState.asStateFlow()
    
    init {
        loadTopics()
    }
    
    /**
     * Load available interview topics.
     */
    fun loadTopics() {
        viewModelScope.launch {
            _uiState.value = VoiceInterviewUiState.Loading
            
            try {
                val topics = sdk.getAvailableTopics()
                _uiState.value = VoiceInterviewUiState.TopicSelection(topics)
            } catch (e: Exception) {
                _uiState.value = VoiceInterviewUiState.Error(
                    errorMessage = e.message ?: "Failed to load topics"
                )
            }
        }
    }
    
    /**
     * Select a topic to start configuring an interview.
     */
    fun selectTopic(topic: InterviewTopic) {
        _uiState.value = VoiceInterviewUiState.ConfigureInterview(topic)
    }
    
    /**
     * Reset to topic selection screen.
     */
    fun resetTopicSelection() {
        viewModelScope.launch {
            try {
                val topics = sdk.getAvailableTopics()
                _uiState.value = VoiceInterviewUiState.TopicSelection(topics)
            } catch (e: Exception) {
                _uiState.value = VoiceInterviewUiState.Error(
                    errorMessage = e.message ?: "Failed to load topics"
                )
            }
        }
    }
    
    /**
     * Start an interview with the given configuration.
     */
    fun startInterview(config: InterviewConfig) {
        viewModelScope.launch {
            _uiState.value = VoiceInterviewUiState.Loading
            
            try {
                val session = sdk.startInterview(config)
                currentSession = session
                
                // Observe interview state
                session.state.collect { interviewState ->
                    _uiState.value = VoiceInterviewUiState.ActiveInterview(interviewState)
                    
                    // If interview is completed, show summary
                    if (interviewState.status == InterviewStatus.COMPLETED && interviewState.sessionSummary != null) {
                        _uiState.value = VoiceInterviewUiState.InterviewSummary(interviewState.sessionSummary)
                        currentSession = null
                    }
                }
            } catch (e: Exception) {
                _uiState.value = VoiceInterviewUiState.Error(
                    errorMessage = e.message ?: "Failed to start interview"
                )
            }
        }
    }
    
    /**
     * Start listening for user's answer to the current question.
     */
    fun startListening() {
        viewModelScope.launch {
            try {
                currentSession?.startListening() ?: throw IllegalStateException("No active session")
            } catch (e: Exception) {
                _uiState.value = VoiceInterviewUiState.Error(
                    errorMessage = e.message ?: "Failed to start listening"
                )
            }
        }
    }
    
    /**
     * Stop listening and process the captured audio as user's answer.
     */
    fun stopListening() {
        viewModelScope.launch {
            try {
                currentSession?.stopListening() ?: throw IllegalStateException("No active session")
            } catch (e: Exception) {
                _uiState.value = VoiceInterviewUiState.Error(
                    errorMessage = e.message ?: "Failed to stop listening"
                )
            }
        }
    }
    
    /**
     * Submit a text answer instead of using voice input.
     */
    fun submitTextAnswer(textAnswer: String) {
        viewModelScope.launch {
            try {
                currentSession?.submitTextAnswer(textAnswer) ?: throw IllegalStateException("No active session")
            } catch (e: Exception) {
                _uiState.value = VoiceInterviewUiState.Error(
                    errorMessage = e.message ?: "Failed to submit answer"
                )
            }
        }
    }
    
    /**
     * Move to the next question in the interview.
     */
    fun nextQuestion() {
        viewModelScope.launch {
            try {
                currentSession?.nextQuestion() ?: throw IllegalStateException("No active session")
            } catch (e: Exception) {
                _uiState.value = VoiceInterviewUiState.Error(
                    errorMessage = e.message ?: "Failed to move to next question"
                )
            }
        }
    }
    
    /**
     * End the current interview session.
     */
    fun endInterview() {
        viewModelScope.launch {
            try {
                android.util.Log.d("VoiceInterview", "Ending interview...")
                val summary = currentSession?.end()
                
                if (summary != null) {
                    android.util.Log.d("VoiceInterview", "Summary generated successfully")
                    _uiState.value = VoiceInterviewUiState.InterviewSummary(summary)
                    currentSession = null
                } else {
                    android.util.Log.e("VoiceInterview", "Summary is null")
                    _uiState.value = VoiceInterviewUiState.Error(
                        errorMessage = "No interview data to summarize"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("VoiceInterview", "Error ending interview", e)
                _uiState.value = VoiceInterviewUiState.Error(
                    errorMessage = e.message ?: "Failed to end interview"
                )
            }
        }
    }
    
    /**
     * Reset the interview and go back to topic selection.
     */
    fun resetInterview() {
        viewModelScope.launch {
            try {
                currentSession?.end(false)
                currentSession = null
                
                // Load topics again
                val topics = sdk.getAvailableTopics()
                _uiState.value = VoiceInterviewUiState.TopicSelection(topics)
            } catch (e: Exception) {
                _uiState.value = VoiceInterviewUiState.Error(
                    errorMessage = e.message ?: "Failed to reset interview"
                )
            }
        }
    }
    
    /**
     * Retry after an error.
     */
    fun retryAfterError() {
        // Return to topic selection
        loadTopics()
    }
    
    override fun onCleared() {
        super.onCleared()
        // Shutdown will handle cleanup of active sessions
        sdk.shutdown()
    }
}

/**
 * UI states for Voice Interview feature.
 */
sealed class VoiceInterviewUiState {
    object Loading : VoiceInterviewUiState()
    
    data class TopicSelection(val topics: List<InterviewTopic>) : VoiceInterviewUiState()
    
    data class ConfigureInterview(val topic: InterviewTopic) : VoiceInterviewUiState()
    
    data class ActiveInterview(val interviewState: InterviewState) : VoiceInterviewUiState()
    
    data class InterviewSummary(val summary: CoreInterviewSummary) : VoiceInterviewUiState()
    
    data class Error(val errorMessage: String) : VoiceInterviewUiState()
}