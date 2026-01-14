package com.prepstack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prepstack.core.util.Resource
import com.prepstack.domain.model.Question
import com.prepstack.domain.repository.InterviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Quiz Screen
 * Manages quiz state and fetches questions from repository
 */
class QuizViewModel(
    private val repository: InterviewRepository
) : ViewModel() {
    
    private val _quizState = MutableStateFlow<QuizState>(QuizState.Loading)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()
    
    /**
     * Load quiz questions (MCQ only) by topic ID
     */
    fun loadQuizByTopic(topicId: String) {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            
            repository.getQuizQuestionsByTopic(topicId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val questions = resource.data ?: emptyList()
                        if (questions.isEmpty()) {
                            _quizState.value = QuizState.Error("No quiz questions found for this topic")
                        } else {
                            _quizState.value = QuizState.Success(questions.shuffled())
                        }
                    }
                    is Resource.Error -> {
                        _quizState.value = QuizState.Error(resource.message ?: "Failed to load questions")
                    }
                    is Resource.Loading -> {
                        _quizState.value = QuizState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Load random quiz questions by domain
     */
    fun loadQuizByDomain(domainId: String, questionCount: Int = 10) {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            
            repository.getRandomQuizQuestions(
                domainId = domainId,
                topicId = null,
                count = questionCount
            ).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val questions = resource.data ?: emptyList()
                        if (questions.isEmpty()) {
                            _quizState.value = QuizState.Error("No questions found for this domain")
                        } else {
                            _quizState.value = QuizState.Success(questions)
                        }
                    }
                    is Resource.Error -> {
                        _quizState.value = QuizState.Error(resource.message ?: "Failed to load questions")
                    }
                    is Resource.Loading -> {
                        _quizState.value = QuizState.Loading
                    }
                }
            }
        }
    }
}

/**
 * Quiz state sealed class
 */
sealed class QuizState {
    object Loading : QuizState()
    data class Success(val questions: List<Question>) : QuizState()
    data class Error(val message: String) : QuizState()
}
