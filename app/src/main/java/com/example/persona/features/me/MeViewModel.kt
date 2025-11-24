package com.example.persona.features.me


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

data class MeUiState(
    val myPersonas: List<Persona> = emptyList(),
    val activePersonaId: String? = null,
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
                val userId = MyApplication.prefs.getUserId()

                // 1. 并行获取：后端列表 & 本地存的 Active ID
                val response = NetworkModule.backendService.getMyPersonas(userId = userId)
                val savedActiveId = MyApplication.prefs.getActivePersonaId()

                if (response.isSuccess() && response.data != null) {
                    val personas = response.data.map { it.copy(isMine = true) }

                    // 2. 确定当前的 Active ID
                    // 如果本地存过且在列表里，就用它；否则默认用第一个
                    var finalActiveId = savedActiveId
                    if (personas.isNotEmpty()) {
                        val exists = personas.any { it.id == savedActiveId }
                        if (!exists || finalActiveId == null) {
                            finalActiveId = personas.first().id
                            MyApplication.prefs.saveActivePersonaId(finalActiveId) // 修正本地存储
                        }
                    }

                    _uiState.update {
                        it.copy(
                            myPersonas = personas,
                            activePersonaId = finalActiveId,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "获取失败: ${response.message}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(error = "网络错误", isLoading = false) }
            }
        }
    }
    // --- 新增：切换身份 ---
    fun switchPersona(personaId: String) {
        viewModelScope.launch {
            // 1. 保存到 DataStore
            MyApplication.prefs.saveActivePersonaId(personaId)

            // 2. 更新 UI 状态
            _uiState.update { it.copy(activePersonaId = personaId) }
        }
    }
}