package com.prepstack.bookmarks.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Room entity for bookmarked questions - stores complete question data
 */
@Entity(tableName = "bookmarks")
@TypeConverters(StringListConverter::class, QuestionTypeConverter::class, DifficultyLevelConverter::class)
data class BookmarkEntity(
    @PrimaryKey
    val questionId: String,
    val topicId: String,
    val domainId: String,
    val questionText: String,
    val questionType: QuestionType,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val codeExample: String?,
    val imageUrl: String?,
    val difficulty: DifficultyLevel,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Question type enum for Room storage
 */
enum class QuestionType {
    MCQ,
    THEORY
}

/**
 * Difficulty level enum for Room storage
 */
enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
}

/**
 * Type converter for List<String> in Room
 */
class StringListConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromList(list: List<String>): String {
        return gson.toJson(list)
    }
}

/**
 * Type converter for QuestionType in Room
 */
class QuestionTypeConverter {
    @TypeConverter
    fun toQuestionType(value: String) = enumValueOf<QuestionType>(value)
    
    @TypeConverter
    fun fromQuestionType(value: QuestionType) = value.name
}

/**
 * Type converter for DifficultyLevel in Room
 */
class DifficultyLevelConverter {
    @TypeConverter
    fun toDifficultyLevel(value: String) = enumValueOf<DifficultyLevel>(value)
    
    @TypeConverter
    fun fromDifficultyLevel(value: DifficultyLevel) = value.name
}
