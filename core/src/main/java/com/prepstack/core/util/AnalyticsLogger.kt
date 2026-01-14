package com.prepstack.core.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Helper class to handle Firebase Analytics events
 */
object AnalyticsLogger {
    private lateinit var analytics: FirebaseAnalytics
    
    /**
     * Initialize the Analytics Logger
     * Must be called before using any other methods
     */
    fun initialize(context: Context) {
        if (!::analytics.isInitialized) {
            analytics = FirebaseAnalytics.getInstance(context)
        }
    }
    
    /**
     * Event constants
     */
    object Events {
        // Screen Views
        const val SCREEN_VIEW = "screen_view"
        
        // User Interactions
        const val DOMAIN_SELECTED = "domain_selected"
        const val TOPIC_SELECTED = "topic_selected"
        const val QUIZ_STARTED = "quiz_started"
        const val QUIZ_COMPLETED = "quiz_completed"
        const val QUESTION_ANSWERED = "question_answered"
        const val BOOKMARK_ADDED = "bookmark_added"
        const val BOOKMARK_REMOVED = "bookmark_removed"
        
        // Ad related events
        const val AD_IMPRESSION = "ad_impression"
        const val REWARDED_AD_EARNED = "rewarded_ad_earned"
        
        // Content Engagement
        const val SEARCH_PERFORMED = "search_performed"
        const val FILTER_APPLIED = "filter_applied"
        const val SORT_APPLIED = "sort_applied"
        
        // App Performance
        const val APP_ERROR = "app_error"
        const val CONTENT_LOADING_TIME = "content_loading_time"
    }
    
    /**
     * Parameter constants
     */
    object Params {
        const val SCREEN_NAME = "screen_name"
        const val DOMAIN_ID = "domain_id"
        const val DOMAIN_NAME = "domain_name"
        const val TOPIC_ID = "topic_id"
        const val TOPIC_NAME = "topic_name"
        const val QUIZ_ID = "quiz_id"
        const val QUIZ_TYPE = "quiz_type"
        const val QUIZ_LENGTH = "quiz_length"
        const val QUIZ_DURATION = "quiz_duration" 
        const val QUIZ_SCORE = "quiz_score"
        const val QUESTION_ID = "question_id"
        const val QUESTION_TYPE = "question_type"
        const val QUESTION_DIFFICULTY = "question_difficulty"
        const val IS_CORRECT = "is_correct"
        const val AD_TYPE = "ad_type"
        const val AD_PLACEMENT = "ad_placement"
        const val SEARCH_QUERY = "search_query"
        const val FILTER_TYPE = "filter_type"
        const val FILTER_VALUE = "filter_value"
        const val SORT_TYPE = "sort_type"
        const val ERROR_MESSAGE = "error_message"
        const val ERROR_LOCATION = "error_location"
        const val LOADING_TIME_MS = "loading_time_ms"
    }
    
    /**
     * Log a screen view event
     */
    fun logScreenView(screenName: String) {
        val params = Bundle().apply {
            putString(Params.SCREEN_NAME, screenName)
        }
        analytics.logEvent(Events.SCREEN_VIEW, params)
    }
    
    /**
     * Log domain selection event
     */
    fun logDomainSelected(domainId: String, domainName: String) {
        val params = Bundle().apply {
            putString(Params.DOMAIN_ID, domainId)
            putString(Params.DOMAIN_NAME, domainName)
        }
        analytics.logEvent(Events.DOMAIN_SELECTED, params)
    }
    
    /**
     * Log topic selection event
     */
    fun logTopicSelected(domainId: String, topicId: String, topicName: String) {
        val params = Bundle().apply {
            putString(Params.DOMAIN_ID, domainId)
            putString(Params.TOPIC_ID, topicId)
            putString(Params.TOPIC_NAME, topicName)
        }
        analytics.logEvent(Events.TOPIC_SELECTED, params)
    }
    
    /**
     * Log quiz started event
     */
    fun logQuizStarted(quizType: String, domainId: String, topicId: String?, questionCount: Int) {
        val params = Bundle().apply {
            putString(Params.QUIZ_TYPE, quizType)
            putString(Params.DOMAIN_ID, domainId)
            topicId?.let { putString(Params.TOPIC_ID, it) }
            putInt(Params.QUIZ_LENGTH, questionCount)
        }
        analytics.logEvent(Events.QUIZ_STARTED, params)
    }
    
    /**
     * Log quiz completed event
     */
    fun logQuizCompleted(
        quizType: String, 
        domainId: String, 
        topicId: String?, 
        score: Int, 
        total: Int, 
        durationSeconds: Long
    ) {
        val params = Bundle().apply {
            putString(Params.QUIZ_TYPE, quizType)
            putString(Params.DOMAIN_ID, domainId)
            topicId?.let { putString(Params.TOPIC_ID, it) }
            putInt(Params.QUIZ_SCORE, score)
            putInt(Params.QUIZ_LENGTH, total)
            putLong(Params.QUIZ_DURATION, durationSeconds)
        }
        analytics.logEvent(Events.QUIZ_COMPLETED, params)
    }
    
    /**
     * Log question answered event
     */
    fun logQuestionAnswered(
        questionId: String,
        questionType: String,
        difficulty: String,
        isCorrect: Boolean
    ) {
        val params = Bundle().apply {
            putString(Params.QUESTION_ID, questionId)
            putString(Params.QUESTION_TYPE, questionType)
            putString(Params.QUESTION_DIFFICULTY, difficulty)
            putBoolean(Params.IS_CORRECT, isCorrect)
        }
        analytics.logEvent(Events.QUESTION_ANSWERED, params)
    }
    
    /**
     * Log bookmark added event
     */
    fun logBookmarkAdded(questionId: String, domainId: String, topicId: String) {
        val params = Bundle().apply {
            putString(Params.QUESTION_ID, questionId)
            putString(Params.DOMAIN_ID, domainId)
            putString(Params.TOPIC_ID, topicId)
        }
        analytics.logEvent(Events.BOOKMARK_ADDED, params)
    }
    
    /**
     * Log bookmark removed event
     */
    fun logBookmarkRemoved(questionId: String) {
        val params = Bundle().apply {
            putString(Params.QUESTION_ID, questionId)
        }
        analytics.logEvent(Events.BOOKMARK_REMOVED, params)
    }
    
    /**
     * Log ad impression event
     */
    fun logAdImpression(adType: String, placement: String) {
        val params = Bundle().apply {
            putString(Params.AD_TYPE, adType)
            putString(Params.AD_PLACEMENT, placement)
        }
        analytics.logEvent(Events.AD_IMPRESSION, params)
    }
    
    /**
     * Log rewarded ad earned event
     */
    fun logRewardedAdEarned(placement: String) {
        val params = Bundle().apply {
            putString(Params.AD_PLACEMENT, placement)
        }
        analytics.logEvent(Events.REWARDED_AD_EARNED, params)
    }
    
    /**
     * Log search performed event
     */
    fun logSearchPerformed(query: String) {
        val params = Bundle().apply {
            putString(Params.SEARCH_QUERY, query)
        }
        analytics.logEvent(Events.SEARCH_PERFORMED, params)
    }
    
    /**
     * Log filter applied event
     */
    fun logFilterApplied(filterType: String, filterValue: String) {
        val params = Bundle().apply {
            putString(Params.FILTER_TYPE, filterType)
            putString(Params.FILTER_VALUE, filterValue)
        }
        analytics.logEvent(Events.FILTER_APPLIED, params)
    }
    
    /**
     * Log sort applied event
     */
    fun logSortApplied(sortType: String) {
        val params = Bundle().apply {
            putString(Params.SORT_TYPE, sortType)
        }
        analytics.logEvent(Events.SORT_APPLIED, params)
    }
    
    /**
     * Log app error event
     */
    fun logAppError(message: String, location: String) {
        val params = Bundle().apply {
            putString(Params.ERROR_MESSAGE, message)
            putString(Params.ERROR_LOCATION, location)
        }
        analytics.logEvent(Events.APP_ERROR, params)
    }
    
    /**
     * Log content loading time event
     */
    fun logContentLoadingTime(feature: String, timeMs: Long) {
        val params = Bundle().apply {
            putString("feature", feature)
            putLong(Params.LOADING_TIME_MS, timeMs)
        }
        analytics.logEvent(Events.CONTENT_LOADING_TIME, params)
    }
    
    /**
     * Log custom event with parameters
     */
    fun logEvent(eventName: String, params: Bundle = Bundle()) {
        analytics.logEvent(eventName, params)
    }
    
    /**
     * Set user property
     */
    fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }
}