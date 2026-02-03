package com.prepstack.voiceinterview.speech

import android.content.Context
import com.prepstack.voiceinterview.core.model.VoiceOption

/**
 * Callback interface for speech recognition results.
 */
interface SpeechRecognitionCallback {
    /**
     * Called when speech recognition returns a result.
     * @param text The recognized text
     * @param isFinal Whether this is the final result for the current recognition session
     */
    fun onResult(text: String, isFinal: Boolean)
    
    /**
     * Called when an error occurs during speech recognition.
     * @param error The error message
     */
    fun onError(error: String)
}

/**
 * Interface for managing speech recognition and text-to-speech functionality.
 */
interface SpeechManager {
    /**
     * Start listening for speech input.
     * @param callback Callback to receive recognition results
     */
    fun startListening(callback: SpeechRecognitionCallback)
    
    /**
     * Stop listening for speech input.
     */
    fun stopListening()
    
    /**
     * Check if the device is currently listening for speech input.
     * @return true if listening, false otherwise
     */
    fun isListening(): Boolean
    
    /**
     * Speak the provided text.
     * @param text Text to be spoken
     * @param onComplete Callback to be invoked when speech is complete
     */
    fun speak(text: String, onComplete: (() -> Unit)? = null)
    
    /**
     * Stop any ongoing text-to-speech output.
     */
    fun stopSpeaking()
    
    /**
     * Check if text-to-speech is currently speaking.
     * @return true if speaking, false otherwise
     */
    fun isSpeaking(): Boolean
    
    /**
     * Set the voice to be used for text-to-speech.
     * @param voice The voice option to use
     */
    fun setVoice(voice: VoiceOption)
    
    /**
     * Set the speech rate for text-to-speech.
     * @param rate The speech rate (0.1 to 2.0, 1.0 is normal)
     */
    fun setSpeechRate(rate: Float)
    
    /**
     * Release resources used by the speech manager.
     */
    fun shutdown()
    
    companion object {
        /**
         * Create a new instance of SpeechManager.
         */
        fun create(
            speechRecognition: SpeechRecognitionManager,
            textToSpeech: TextToSpeechManager
        ): SpeechManager {
            return DefaultSpeechManager(speechRecognition, textToSpeech)
        }
    }
}

/**
 * Default implementation of SpeechManager.
 */
internal class DefaultSpeechManager(
    private val speechRecognition: SpeechRecognitionManager,
    private val textToSpeech: TextToSpeechManager
) : SpeechManager {
    
    override fun startListening(callback: SpeechRecognitionCallback) {
        // Stop TTS if it's speaking to avoid conflict
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop()
        }
        
        speechRecognition.startListening(callback)
    }
    
    override fun stopListening() {
        speechRecognition.stopListening()
    }
    
    override fun isListening(): Boolean {
        return speechRecognition.isListening()
    }
    
    override fun speak(text: String, onComplete: (() -> Unit)?) {
        // Stop recognition if it's listening to avoid conflict
        if (speechRecognition.isListening()) {
            speechRecognition.stopListening()
        }
        
        textToSpeech.speak(text, onComplete)
    }
    
    override fun stopSpeaking() {
        textToSpeech.stop()
    }
    
    override fun isSpeaking(): Boolean {
        return textToSpeech.isSpeaking()
    }
    
    override fun setVoice(voice: VoiceOption) {
        textToSpeech.setVoice(voice)
    }
    
    override fun setSpeechRate(rate: Float) {
        textToSpeech.setSpeechRate(rate)
    }
    
    override fun shutdown() {
        speechRecognition.shutdown()
        textToSpeech.shutdown()
    }
}