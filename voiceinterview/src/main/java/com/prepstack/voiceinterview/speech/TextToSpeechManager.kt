package com.prepstack.voiceinterview.speech

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.prepstack.voiceinterview.core.model.VoiceOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Manager for text-to-speech functionality.
 */
interface TextToSpeechManager {
    /**
     * Speak the provided text.
     * @param text Text to be spoken
     * @param onComplete Callback to be invoked when speech is complete
     */
    fun speak(text: String, onComplete: (() -> Unit)? = null)
    
    /**
     * Stop any ongoing text-to-speech output.
     */
    fun stop()
    
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
     * Set the pitch for text-to-speech.
     * @param pitch The pitch (0.5 to 2.0, 1.0 is normal)
     */
    fun setPitch(pitch: Float)
    
    /**
     * Get available voices on the device.
     * @return List of available voice names
     */
    fun getAvailableVoices(): List<String>
    
    /**
     * Release resources used by the text-to-speech manager.
     */
    fun shutdown()
    
    companion object {
        /**
         * Create a new instance of TextToSpeechManager.
         */
        fun create(context: Context): TextToSpeechManager {
            return AndroidTextToSpeechManager(context)
        }
    }
}

/**
 * Implementation of TextToSpeechManager using Android's TextToSpeech.
 */
internal class AndroidTextToSpeechManager(
    private val context: Context
) : TextToSpeechManager {
    private var tts: TextToSpeech? = null
    private var initialized = false
    private val utteranceCallbacks = ConcurrentHashMap<String, () -> Unit>()
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            initialized = status == TextToSpeech.SUCCESS
            
            if (initialized) {
                tts?.language = Locale.US
                
                // Configure for more natural speech
                // Slightly slower for better clarity and natural feel
                tts?.setSpeechRate(0.92f) // Slightly slower than normal
                
                // Slightly higher pitch for more engaging voice
                tts?.setPitch(1.05f) // Slightly higher than normal
                
                // Try to select the best available voice
                selectBestVoice()
                
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId != null) {
                            utteranceCallbacks[utteranceId]?.invoke()
                            utteranceCallbacks.remove(utteranceId)
                        }
                    }
                    
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (utteranceId != null) {
                            utteranceCallbacks.remove(utteranceId)
                        }
                    }
                })
            }
        }
    }
    
    /**
     * Automatically select the best available voice for natural speech.
     */
    private fun selectBestVoice() {
        val availableVoices = tts?.voices ?: return
        
        // Priority order for selecting natural-sounding voices:
        // 1. High-quality local voices (Quality > 400)
        // 2. Enhanced network voices with "enhanced" in name
        // 3. Voices with "natural" or "premium" in name
        // 4. Any local voice
        // 5. Fallback to default
        
        val bestVoice = availableVoices
            .filter { it.locale == Locale.US || it.locale.language == "en" }
            .sortedWith(compareByDescending<Voice> { voice ->
                // Prioritize high quality
                voice.quality
            }.thenByDescending { voice ->
                // Prefer voices with "enhanced", "natural", or "premium"
                val name = voice.name.lowercase()
                when {
                    name.contains("enhanced") -> 3
                    name.contains("natural") -> 2
                    name.contains("premium") -> 2
                    !voice.isNetworkConnectionRequired -> 1 // Prefer offline voices
                    else -> 0
                }
            })
            .firstOrNull()
        
        if (bestVoice != null) {
            tts?.voice = bestVoice
            android.util.Log.d("TTS", "Selected voice: ${bestVoice.name} (Quality: ${bestVoice.quality})")
        }
    }
    
    override fun speak(text: String, onComplete: (() -> Unit)?) {
        if (!initialized) {
            onComplete?.invoke()
            return
        }
        
        val utteranceId = UUID.randomUUID().toString()
        
        if (onComplete != null) {
            utteranceCallbacks[utteranceId] = onComplete
        }
        
        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            Bundle(),
            utteranceId
        )
    }
    
    override fun stop() {
        tts?.stop()
        
        // Clear callbacks for any pending utterances
        utteranceCallbacks.clear()
    }
    
    override fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }
    
    override fun setVoice(voice: VoiceOption) {
        if (!initialized) return
        
        val availableVoices = tts?.voices ?: return
        
        val targetVoice = when (voice) {
            VoiceOption.FEMALE_NATURAL -> {
                // Try to find high-quality female voice
                availableVoices
                    .filter { 
                        val name = it.name.lowercase()
                        (name.contains("female") || name.contains("woman")) &&
                        (it.locale == Locale.US || it.locale.language == "en")
                    }
                    .sortedByDescending { it.quality }
                    .firstOrNull()
            }
            VoiceOption.MALE_NATURAL -> {
                // Try to find high-quality male voice
                availableVoices
                    .filter { 
                        val name = it.name.lowercase()
                        (name.contains("male") || name.contains("man")) &&
                        (it.locale == Locale.US || it.locale.language == "en")
                    }
                    .sortedByDescending { it.quality }
                    .firstOrNull()
            }
            VoiceOption.FEMALE_SYNTHETIC -> {
                availableVoices.firstOrNull { 
                    it.name.contains("female", ignoreCase = true)
                }
            }
            VoiceOption.MALE_SYNTHETIC -> {
                availableVoices.firstOrNull { 
                    it.name.contains("male", ignoreCase = true)
                }
            }
        }
        
        if (targetVoice != null) {
            tts?.voice = targetVoice
            android.util.Log.d("TTS", "Voice changed to: ${targetVoice.name} (Quality: ${targetVoice.quality})")
        }
    }
    
    override fun setSpeechRate(rate: Float) {
        if (!initialized) return
        
        tts?.setSpeechRate(rate.coerceIn(0.1f, 2.0f))
    }
    
    override fun setPitch(pitch: Float) {
        if (!initialized) return
        
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }
    
    override fun getAvailableVoices(): List<String> {
        if (!initialized) return emptyList()
        
        return tts?.voices?.map { voice ->
            "${voice.name} (${voice.locale}, Quality: ${voice.quality})"
        } ?: emptyList()
    }
    
    override fun shutdown() {
        utteranceCallbacks.clear()
        
        tts?.stop()
        tts?.shutdown()
        tts = null
        initialized = false
    }
}