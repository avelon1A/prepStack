package com.prepstack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prepstack.core.util.Resource
import com.prepstack.domain.model.Domain
import com.prepstack.domain.usecase.GetDomainsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Domain Screen
 * Follows MVVM pattern with StateFlow
 */
class DomainViewModel(
    private val getDomainsUseCase: GetDomainsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DomainUiState>(DomainUiState.Loading)
    val uiState: StateFlow<DomainUiState> = _uiState.asStateFlow()
    
    init {
        loadDomains()
    }
    
    fun loadDomains() {
        viewModelScope.launch {
            getDomainsUseCase().collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> DomainUiState.Loading
                    is Resource.Success -> {
                        resource.data?.let {
                            DomainUiState.Success(it)
                        } ?: DomainUiState.Error("No data available")
                    }
                    is Resource.Error -> DomainUiState.Error(
                        resource.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }
}

/**
 * UI State for Domain Screen
 */
sealed class DomainUiState {
    data object Loading : DomainUiState()
    data class Success(val domains: List<Domain>) : DomainUiState()
    data class Error(val message: String) : DomainUiState()
}
