package com.example.persona.features.me


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.data.NetworkModule
import com.example.persona.data.Persona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeUiState(
    val myPersonas: List<Persona> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MeUiState())
    val uiState: StateFlow<MeUiState> = _uiState.asStateFlow()

    // 初始化时加载
    init {
        loadMyPersonas()
    }

    fun loadMyPersonas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 暂时硬编码 userId = 1 (对应数据库里的 admin)
                // 真实项目中这里应该从 SharedPreferences 读取当前登录用户的 ID
                val response = NetworkModule.backendService.getMyPersonas(userId = 1)

                if (response.isSuccess() && response.data != null) {
                    // 拿到数据后，把 isMine 标记为 true
                    val personas = response.data.map { it.copy(isMine = true) }

                    _uiState.update {
                        it.copy(myPersonas = personas, isLoading = false)
                    }
                } else {
                    _uiState.update { it.copy(error = "获取失败: ${response.message}", isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(error = "网络错误", isLoading = false) }
            }
        }
    }
}