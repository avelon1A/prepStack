package com.prepstack.bookmarks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for bookmarked questions
 */
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val questionId: String,
    val topicId: String,
    val domainId: String,
    val timestamp: Long = System.currentTimeMillis()
)
