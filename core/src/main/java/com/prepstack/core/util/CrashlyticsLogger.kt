package com.prepstack.core.util

import android.util.Log

/**
 * Helper class to handle Firebase Crashlytics logging
 * 
 * This class provides a safe API for Crashlytics that gracefully degrades
 * if Crashlytics is not available or disabled.
 */
object CrashlyticsLogger {
    private const val TAG = "CrashlyticsLogger"
    
    // Dynamically access Crashlytics to avoid direct dependency
    private val crashlytics by lazy {
        try {
            val crashlyticsClass = Class.forName("com.google.firebase.crashlytics.FirebaseCrashlytics")
            val getInstanceMethod = crashlyticsClass.getMethod("getInstance")
            getInstanceMethod.invoke(null)
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Crashlytics not available: ${e.message}")
            null
        }
    }
    
    /**
     * Log a non-fatal exception to Crashlytics
     * 
     * @param throwable The exception to log
     * @param message Optional message to include with the exception
     */
    fun logException(throwable: Throwable, message: String? = null) {
        message?.let {
            Log.e(TAG, it, throwable)
            logInternal("$it: ${throwable.message}")
        } ?: run {
            Log.e(TAG, throwable.message ?: "Unknown error", throwable)
        }
        
        try {
            crashlytics?.javaClass?.getMethod("recordException", Throwable::class.java)
                ?.invoke(crashlytics, throwable)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to record exception: ${e.message}")
        }
    }
    
    /**
     * Log a custom event or error to Crashlytics
     * 
     * @param message The message to log
     * @param level The log level (e.g., "ERROR", "WARNING", "INFO")
     */
    fun log(message: String, level: String = "INFO") {
        val logMessage = "[$level] $message"
        Log.d(TAG, logMessage)
        logInternal(logMessage)
    }
    
    private fun logInternal(message: String) {
        try {
            crashlytics?.javaClass?.getMethod("log", String::class.java)
                ?.invoke(crashlytics, message)
        } catch (e: Exception) {
            Log.v(TAG, "Failed to log to Crashlytics: ${e.message}")
        }
    }
    
    /**
     * Set custom keys that will be included with crash reports
     * 
     * @param key The custom key name
     * @param value The value for the custom key
     */
    fun setCustomKey(key: String, value: String) {
        try {
            crashlytics?.javaClass?.getMethod("setCustomKey", String::class.java, String::class.java)
                ?.invoke(crashlytics, key, value)
        } catch (e: Exception) {
            Log.v(TAG, "Failed to set custom key: ${e.message}")
        }
    }
    
    /**
     * Set custom keys that will be included with crash reports
     * 
     * @param key The custom key name
     * @param value The numeric value for the custom key
     */
    fun setCustomKey(key: String, value: Int) {
        try {
            crashlytics?.javaClass?.getMethod("setCustomKey", String::class.java, Int::class.java)
                ?.invoke(crashlytics, key, value)
        } catch (e: Exception) {
            Log.v(TAG, "Failed to set custom key: ${e.message}")
        }
    }
    
    /**
     * Set custom keys that will be included with crash reports
     * 
     * @param key The custom key name
     * @param value The boolean value for the custom key
     */
    fun setCustomKey(key: String, value: Boolean) {
        try {
            crashlytics?.javaClass?.getMethod("setCustomKey", String::class.java, Boolean::class.java)
                ?.invoke(crashlytics, key, value)
        } catch (e: Exception) {
            Log.v(TAG, "Failed to set custom key: ${e.message}")
        }
    }
    
    /**
     * Set the user ID for crash reports
     * Note: Do not use personally identifiable information for user IDs
     * 
     * @param userId A unique identifier for the user
     */
    fun setUserId(userId: String) {
        try {
            crashlytics?.javaClass?.getMethod("setUserId", String::class.java)
                ?.invoke(crashlytics, userId)
        } catch (e: Exception) {
            Log.v(TAG, "Failed to set user ID: ${e.message}")
        }
    }
}