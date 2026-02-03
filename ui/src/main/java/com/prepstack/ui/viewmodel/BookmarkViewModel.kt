package com.prepstack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prepstack.bookmarks.repository.BookmarkRepository
import com.prepstack.domain.model.Question
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Bookmark Screen
 * Manages bookmarked questions state
 */
class BookmarkViewModel(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {
    
    // Private mutable state - only ViewModel can modify
    private val _uiState = MutableStateFlow<BookmarkUiState>(BookmarkUiState.Loading)
    
    // Public immutable state - UI observes this
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()
    
    init {
        observeBookmarks()
    }
    
    /**
     * Continuously observe bookmarked questions directly from Room
     * This will automatically update when bookmarks are added/removed
     */
    private fun observeBookmarks() {
        viewModelScope.launch {
            bookmarkRepository.getAllBookmarkedQuestions()
                .catch { e ->
                    _uiState.value = BookmarkUiState.Error(
                        e.message ?: "Failed to load bookmarks"
                    )
                }
                .collect { questions ->
                    if (questions.isEmpty()) {
                        _uiState.value = BookmarkUiState.Empty
                    } else {
                        _uiState.value = BookmarkUiState.Success(questions)
                    }
                }
        }
    }
    
    /**
     * Remove a question from bookmarks
     * The UI will automatically update due to reactive Flow observation from Room
     */
    fun removeBookmark(questionId: String) {
        viewModelScope.launch {
            try {
                bookmarkRepository.removeBookmark(questionId)
                // No need to reload - Room Flow will automatically update
            } catch (e: Exception) {
                _uiState.value = BookmarkUiState.Error(
                    e.message ?: "Failed to remove bookmark"
                )
            }
        }
    }
    
    /**
     * Refresh bookmarks
     * Forces a reload of bookmarked questions
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = BookmarkUiState.Loading
            
            try {
                // Get latest data from repository
                val questions = bookmarkRepository.getAllBookmarkedQuestions().first()
                
                if (questions.isEmpty()) {
                    _uiState.value = BookmarkUiState.Empty
                } else {
                    _uiState.value = BookmarkUiState.Success(questions)
                }
            } catch (e: Exception) {
                _uiState.value = BookmarkUiState.Error(
                    e.message ?: "Failed to refresh bookmarks"
                )
            }
        }
    }
}

/**
 * Sealed class for type-safe UI states
 */
sealed class BookmarkUiState {
    data object Loading : BookmarkUiState()
    data object Empty : BookmarkUiState()
    data class Success(val questions: List<Question>) : BookmarkUiState()
    data class Error(val message: String) : BookmarkUiState()
}
