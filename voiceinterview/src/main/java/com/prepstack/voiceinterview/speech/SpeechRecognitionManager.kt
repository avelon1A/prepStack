package com.prepstack.voiceinterview.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*

/**
 * Manager for speech recognition functionality.
 */
interface SpeechRecognitionManager {
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
     * Check if the manager is currently listening for speech input.
     * @return true if listening, false otherwise
     */
    fun isListening(): Boolean
    
    /**
     * Release resources used by the speech recognition manager.
     */
    fun shutdown()
    
    companion object {
        /**
         * Create a new instance of SpeechRecognitionManager.
         */
        fun create(context: Context): SpeechRecognitionManager {
            return AndroidSpeechRecognitionManager(context)
        }
    }
}

/**
 * Implementation of SpeechRecognitionManager using Android's SpeechRecognizer.
 */
internal class AndroidSpeechRecognitionManager(
    private val context: Context
) : SpeechRecognitionManager {
    private var speechRecognizer: SpeechRecognizer? = null
    private var callback: SpeechRecognitionCallback? = null
    private var listening = false
    private var timeoutJob: Job? = null
    
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun startListening(callback: SpeechRecognitionCallback) {
        // If already listening, stop first and clear state
        if (listening) {
            stopListening()
        }
        
        // Check permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val errorMessage = "Microphone permission is required for voice input"
            Toast.makeText(
                context,
                "🎤 $errorMessage\nPlease grant microphone permission in Settings",
                Toast.LENGTH_LONG
            ).show()
            callback.onError("RECORD_AUDIO permission not granted")
            return
        }

        // Initialize speech recognizer if needed
        if (speechRecognizer == null) {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                val errorMessage = "Speech recognition is not available on this device"
                Toast.makeText(
                    context,
                    "⚠️ $errorMessage\nPlease ensure Google app is installed",
                    Toast.LENGTH_LONG
                ).show()
                callback.onError(errorMessage)
                return
            }

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }

        this.callback = callback
        
        val recognitionListener = createRecognitionListener()
        speechRecognizer?.setRecognitionListener(recognitionListener)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
        }
        
        speechRecognizer?.startListening(intent)
        listening = true
        
        // Set a timeout to stop listening after 60 seconds if no results
        timeoutJob?.cancel()
        timeoutJob = coroutineScope.launch {
            delay(60000) // 60 seconds
            if (listening) {
                stopListening()
                callback.onError("Speech recognition timed out")
            }
        }
    }
    
    override fun stopListening() {
        timeoutJob?.cancel()
        
        if (listening) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                // Ignore errors during stop
            }
            listening = false
            callback = null // Clear callback to prevent memory leaks
        }
    }
    
    override fun isListening(): Boolean {
        return listening
    }
    
    override fun shutdown() {
        timeoutJob?.cancel()
        
        if (listening) {
            speechRecognizer?.stopListening()
            listening = false
        }
        
        speechRecognizer?.destroy()
        speechRecognizer = null
        callback = null
        coroutineScope.cancel()
    }
    
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            
            override fun onBeginningOfSpeech() {}
            
            override fun onRmsChanged(rmsdB: Float) {}
            
            override fun onBufferReceived(buffer: ByteArray?) {}
            
            override fun onEndOfSpeech() {
                listening = false
            }
            
            override fun onError(error: Int) {
                listening = false
                
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech input"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service is busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error: $error"
                }
                
                // Show Toast for critical errors (not recoverable ones)
                when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> {
                        Toast.makeText(
                            context,
                            "⚠️ Microphone error. Please check your audio settings",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        Toast.makeText(
                            context,
                            "🎤 Microphone permission denied. Please enable it in Settings",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        Toast.makeText(
                            context,
                            "⚠️ Speech recognition is busy. Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                        Toast.makeText(
                            context,
                            "📡 Network error. Please check your connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Don't show Toast for recoverable errors (NO_MATCH, TIMEOUT)
                    // These are handled by auto-retry logic
                }
                
                callback?.onError(errorMessage)
            }
            
            override fun onResults(results: Bundle?) {
                listening = false
                timeoutJob?.cancel()
                
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    callback?.onResult(matches[0], true)
                } else {
                    callback?.onError("No speech recognized")
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    callback?.onResult(matches[0], false)
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}