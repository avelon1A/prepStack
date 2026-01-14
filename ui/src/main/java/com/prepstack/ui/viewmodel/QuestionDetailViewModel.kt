package com.prepstack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prepstack.bookmarks.repository.BookmarkRepository
import com.prepstack.core.util.Resource
import com.prepstack.domain.model.Question
import com.prepstack.domain.repository.InterviewRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Question Detail Screen
 */
class QuestionDetailViewModel(
    private val repository: InterviewRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<QuestionDetailUiState>(QuestionDetailUiState.Loading)
    val uiState: StateFlow<QuestionDetailUiState> = _uiState.asStateFlow()
    
    private val _selectedAnswer = MutableStateFlow<String?>(null)
    val selectedAnswer: StateFlow<String?> = _selectedAnswer.asStateFlow()
    
    private val _showAnswer = MutableStateFlow(false)
    val showAnswer: StateFlow<Boolean> = _showAnswer.asStateFlow()
    
    private val _showExplanation = MutableStateFlow(false)
    val showExplanation: StateFlow<Boolean> = _showExplanation.asStateFlow()
    
    fun loadQuestion(questionId: String) {
        viewModelScope.launch {
            repository.getQuestionById(questionId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> QuestionDetailUiState.Loading
                    is Resource.Success -> {
                        resource.data?.let {
                            QuestionDetailUiState.Success(it)
                        } ?: QuestionDetailUiState.Error("Question not found")
                    }
                    is Resource.Error -> QuestionDetailUiState.Error(
                        resource.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }
    
    fun selectAnswer(answer: String) {
        _selectedAnswer.value = answer
        _showAnswer.value = true
    }
    
    fun revealAnswer() {
        _showAnswer.value = true
    }
    
    fun revealExplanation() {
        _showExplanation.value = true
    }
    
    fun toggleBookmark(questionId: String, topicId: String, domainId: String) {
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(questionId, topicId, domainId)
        }
    }
    
    fun isBookmarked(questionId: String): Flow<Boolean> {
        return bookmarkRepository.isBookmarked(questionId)
    }
    
    fun resetAnswer() {
        _selectedAnswer.value = null
        _showAnswer.value = false
        _showExplanation.value = false
    }
}

sealed class QuestionDetailUiState {
    data object Loading : QuestionDetailUiState()
    data class Success(val question: Question) : QuestionDetailUiState()
    data class Error(val message: String) : QuestionDetailUiState()
}
