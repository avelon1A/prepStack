package com.prepstack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prepstack.core.util.Resource
import com.prepstack.domain.model.QuizDescriptor
import com.prepstack.domain.model.Topic
import com.prepstack.domain.repository.InterviewRepository
import com.prepstack.domain.usecase.GetTopicsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TopicViewModel(
    private val getTopicsUseCase: GetTopicsUseCase,
    private val interviewRepository: InterviewRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TopicUiState>(TopicUiState.Loading)
    val uiState: StateFlow<TopicUiState> = _uiState.asStateFlow()
    
    // Public property for quiz descriptors
    var quizzes: List<QuizDescriptor> = emptyList()
        private set

    fun loadTopics(domainId: String) {
        viewModelScope.launch {
            getTopicsUseCase(domainId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> TopicUiState.Loading
                    is Resource.Success -> {
                        resource.data?.let {
                            TopicUiState.Success(it)
                        } ?: TopicUiState.Error("No topics available")
                    }
                    is Resource.Error -> TopicUiState.Error(
                        resource.message ?: "An unexpected error occurred"
                    )
                }
            }
            quizzes = interviewRepository.getDomainQuizzes(domainId).map { repoQuiz ->
                QuizDescriptor(
                    id = repoQuiz.id,
                    topicId = repoQuiz.topicId,
                    title = repoQuiz.title,
                    file = repoQuiz.file
                )
            }
        }
    }
}

sealed class TopicUiState {
    data object Loading : TopicUiState()
    data class Success(val topics: List<Topic>) : TopicUiState()
    data class Error(val message: String) : TopicUiState()
}
