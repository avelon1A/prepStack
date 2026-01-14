package com.prepstack.core.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for date/time formatting
 */
object DateUtils {
    
    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    private const val HOUR_MILLIS = 60 * 60 * 1000L
    private const val MINUTE_MILLIS = 60 * 1000L
    
    /**
     * Format timestamp to relative time string (e.g., "2 hours ago", "Yesterday")
     */
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < MINUTE_MILLIS -> "Just now"
            diff < HOUR_MILLIS -> "${diff / MINUTE_MILLIS} min ago"
            diff < DAY_MILLIS -> "${diff / HOUR_MILLIS}h ago"
            diff < 2 * DAY_MILLIS -> "Yesterday"
            diff < 7 * DAY_MILLIS -> "${diff / DAY_MILLIS}d ago"
            else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
        }
    }
    
    /**
     * Format timestamp to date string
     */
    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
    
    /**
     * Format timestamp to date and time string
     */
    fun formatDateTime(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}