package com.example.persona.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.MyApplication
import com.example.persona.data.LoginRequest
import com.example.persona.data.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false // 登录成功标志
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(text: String) { _uiState.update { it.copy(username = text) } }
    fun onPasswordChange(text: String) { _uiState.update { it.copy(password = text) } }

    fun login() {
        val username = _uiState.value.username
        val password = _uiState.value.password
        if (username.isBlank() || password.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 调用后端登录
                val request = LoginRequest(username, password)
                val response = NetworkModule.backendService.login(request)

                if (response.isSuccess() && response.data != null) {
                    // 🔥 核心：保存用户 ID！
                    // 这样整个 App 的后续操作（MeScreen, ChatScreen）都会基于这个 ID
                    MyApplication.prefs.saveUserId(response.data.id)
                    
                    // 清除旧的 Active Persona，防止串号
                    MyApplication.prefs.saveActivePersonaId("") 

                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message ?: "登录失败") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "网络错误: ${e.message}") }
            }
        }
    }
    
    // 你也可以加一个 register() 方法，逻辑类似
}