package com.prepstack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prepstack.domain.model.*
import com.prepstack.domain.model.UserStreak
import com.prepstack.domain.repository.UserProgressRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

/**
 * ViewModel for HomeScreen
 */
class HomeViewModel(
    private val userProgressRepository: UserProgressRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        // Check and update streak immediately on app launch
        viewModelScope.launch(Dispatchers.IO) {
            userProgressRepository.checkAndUpdateStreak()
            loadHomeData()
        }
    }
    
    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                val activitiesFlow = userProgressRepository.getRecentActivities(limit = 5)
                val performanceFlow = userProgressRepository.getUserPerformance()
                val incompleteTestsFlow = userProgressRepository.getIncompleteTests()
                val userStreakFlow = userProgressRepository.getUserStreak()
                
                activitiesFlow.combine(performanceFlow) { activities, performance ->
                    Pair(activities, performance)
                }.combine(incompleteTestsFlow) { pair, tests ->
                    Triple(pair.first, pair.second, tests)
                }.combine(userStreakFlow) { triple, streak ->
                    HomeData(
                        recentActivities = triple.first,
                        performance = triple.second,
                        incompleteTests = triple.third,
                        userStreak = streak
                    )
                }.collect { homeData -> 
                    _uiState.value = HomeUiState.Success(homeData)
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun resumeTest(testId: String) {
        // This will be handled by navigation in the UI layer
        viewModelScope.launch {
            // Optionally remove the incomplete test when resumed
            // userProgressRepository.removeIncompleteTest(testId)
        }
    }
    
    fun deleteIncompleteTest(testId: String) {
        viewModelScope.launch {
            userProgressRepository.removeIncompleteTest(testId)
        }
    }
    
    fun navigateToActivity(activity: UserActivity) {
        // This will be handled by navigation in the UI layer
    }
}

/**
 * UI State for Home Screen
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: HomeData) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

/**
 * Data class containing all home screen data
 */
data class HomeData(
    val recentActivities: List<UserActivity>,
    val performance: UserPerformance,
    val incompleteTests: List<IncompleteTest>,
    val userStreak: UserStreak? = null
)
