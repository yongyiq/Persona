package com.example.persona.features.follow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.MyApplication
import com.example.persona.data.NetworkModule
import com.example.persona.data.Persona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FollowListUiState(
    val followList: List<Persona> = emptyList(),
    val isLoading: Boolean = false
)

class FollowListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FollowListUiState())
    val uiState: StateFlow<FollowListUiState> = _uiState.asStateFlow()

    init {
        loadFollowList()
    }

    fun loadFollowList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userId = MyApplication.prefs.getUserId()
                val response = NetworkModule.backendService.getFollowList(userId)
                if (response.isSuccess() && response.data != null) {
                    _uiState.update { it.copy(followList = response.data, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}