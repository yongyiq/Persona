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

// 复用 LoginUiState
data class RegisterUiState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "", // 多一个确认密码
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegisterSuccess: Boolean = false
)

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChange(text: String) { _uiState.update { it.copy(username = text) } }
    fun onPasswordChange(text: String) { _uiState.update { it.copy(password = text) } }
    fun onConfirmPasswordChange(text: String) { _uiState.update { it.copy(confirmPassword = text) } }

    fun register() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "用户名或密码不能为空") }
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(error = "两次输入的密码不一致") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. 调用后端注册接口
                val request = LoginRequest(state.username, state.password)
                val response = NetworkModule.backendService.register(request)

                if (response.isSuccess() && response.data != null) {
                    // 2. 注册成功后
                    // 保存返回的用户 ID
                    MyApplication.prefs.saveUserId(response.data.id)
                    // 清除可能残留的 Active Persona
                    MyApplication.prefs.saveActivePersonaId("") 

                    _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message ?: "注册失败") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "网络错误: ${e.message}") }
            }
        }
    }
}