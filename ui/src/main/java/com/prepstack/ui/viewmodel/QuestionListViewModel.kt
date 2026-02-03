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
    
    private var currentTopicId: String? = null
    
    fun loadQuestions(topicId: String) {
        // Avoid reloading if same topic
        if (currentTopicId == topicId) return
        currentTopicId = topicId
        
        viewModelScope.launch {
            getQuestionsUseCase(topicId).collect { questionResource ->
                when (questionResource) {
                    is Resource.Loading -> {
                        _uiState.value = QuestionListUiState.Loading
                    }
                    is Resource.Success -> {
                        questionResource.data?.let { questions ->
                            // Load initial bookmark status
                            val bookmarks = bookmarkRepository.getAllBookmarks().first()
                            val bookmarkedIds = bookmarks.map { it.questionId }.toSet()
                            
                            val enrichedQuestions = questions.map { question ->
                                question.copy(isBookmarked = bookmarkedIds.contains(question.id))
                            }
                            
                            _uiState.value = QuestionListUiState.Success(enrichedQuestions)
                        } ?: run {
                            _uiState.value = QuestionListUiState.Error("No questions available")
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = QuestionListUiState.Error(
                            questionResource.message ?: "An unexpected error occurred"
                        )
                    }
                }
            }
        }
    }
    
    fun toggleBookmark(questionId: String, topicId: String, domainId: String) {
        viewModelScope.launch {
            // Find the question to toggle
            val currentState = _uiState.value
            if (currentState is QuestionListUiState.Success) {
                val question = currentState.questions.find { it.id == questionId }
                if (question != null) {
                    // Toggle the bookmark in database with full question data
                    bookmarkRepository.toggleBookmark(question)
                    
                    // Immediately update the UI state to reflect the change
                    val updatedQuestions = currentState.questions.map { q ->
                        if (q.id == questionId) {
                            q.copy(isBookmarked = !q.isBookmarked)
                        } else {
                            q
                        }
                    }
                    _uiState.value = QuestionListUiState.Success(updatedQuestions)
                }
            }
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
