package com.prepstack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prepstack.bookmarks.repository.BookmarkRepository
import com.prepstack.core.util.Resource
import com.prepstack.domain.model.Question
import com.prepstack.domain.usecase.GetQuestionsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuestionListViewModel(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<QuestionListUiState>(QuestionListUiState.Loading)
    val uiState: StateFlow<QuestionListUiState> = _uiState.asStateFlow()
    
    fun loadQuestions(topicId: String) {
        viewModelScope.launch {
            getQuestionsUseCase(topicId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> QuestionListUiState.Loading
                    is Resource.Success -> {
                        resource.data?.let {
                            QuestionListUiState.Success(it)
                        } ?: QuestionListUiState.Error("No questions available")
                    }
                    is Resource.Error -> QuestionListUiState.Error(
                        resource.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }
    
    fun toggleBookmark(questionId: String, topicId: String, domainId: String) {
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(questionId, topicId, domainId)
        }
    }
    
    fun isBookmarked(questionId: String): Flow<Boolean> {
        return bookmarkRepository.isBookmarked(questionId)
    }
}

sealed class QuestionListUiState {
    data object Loading : QuestionListUiState()
    data class Success(val questions: List<Question>) : QuestionListUiState()
    data class Error(val message: String) : QuestionListUiState()
}
