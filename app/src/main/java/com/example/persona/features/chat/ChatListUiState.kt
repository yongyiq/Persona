package com.example.persona.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.MyApplication
import com.example.persona.data.Conversation
import com.example.persona.data.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatListUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false
)

class ChatListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    // 每次进入页面都刷新
    fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userId = MyApplication.prefs.getUserId()
                val response = NetworkModule.backendService.getConversations(userId)
                
                if (response.isSuccess() && response.data != null) {
                    _uiState.update { it.copy(conversations = response.data, isLoading = false) }
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