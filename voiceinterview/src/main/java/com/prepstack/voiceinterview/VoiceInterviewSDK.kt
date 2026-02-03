package com.prepstack.voiceinterview

import android.content.Context
import com.prepstack.voiceinterview.core.InterviewController
import com.prepstack.voiceinterview.core.InterviewSession
import com.prepstack.voiceinterview.core.model.InterviewConfig
import com.prepstack.voiceinterview.core.model.InterviewState
import com.prepstack.voiceinterview.core.model.InterviewTopic
import com.prepstack.voiceinterview.speech.SpeechManager
import kotlinx.coroutines.flow.StateFlow

/**
 * Main entry point for the Voice Interview SDK.
 * This class provides a clean, high-level API for starting and conducting voice interviews.
 */
class VoiceInterviewSDK private constructor(
    private val context: Context,
    private val apiKey: String,
    private val baseUrl: String = DEFAULT_API_URL
) {
    private val interviewController: InterviewController by lazy {
        InterviewController.create(context, apiKey, baseUrl)
    }

    companion object {
        private const val DEFAULT_API_URL = "https://api.openai.com/v1/"
        
        /**
         * Initialize the Voice Interview SDK.
         * @param context Application context
         * @param apiKey OpenAI API key
         * @param baseUrl Optional custom API URL
         */
        @JvmStatic
        fun initialize(
            context: Context, 
            apiKey: String,
            baseUrl: String = DEFAULT_API_URL
        ): VoiceInterviewSDK {
            return VoiceInterviewSDK(context.applicationContext, apiKey, baseUrl)
        }
    }

    /**
     * Get the list of available interview topics.
     * @return List of interview topics with their configurations
     */
    suspend fun getAvailableTopics(): List<InterviewTopic> {
        return interviewController.getAvailableTopics()
    }

    /**
     * Start a new interview session with the specified configuration.
     * @param config Interview configuration
     * @return New interview session
     */
    suspend fun startInterview(config: InterviewConfig): InterviewSession {
        return interviewController.startInterview(config)
    }

    /**
     * Get the current interview state if one is active.
     * @return StateFlow of the current interview state or null if no interview is active
     */
    fun getCurrentInterview(): StateFlow<InterviewState>? {
        return interviewController.currentInterviewState
    }

    /**
     * Stop any ongoing interviews and release resources.
     */
    fun shutdown() {
        interviewController.shutdown()
    }
}