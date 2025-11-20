package com.example.persona.features.creation

import androidx.lifecycle.ViewModel
import com.example.persona.data.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 定义 Persona 创作屏幕的 UI 状态
data class PersonaCreationUIState(
    val name: String = "", // Persona 的名字
    val backgroundStory: String = "", // Persona 的背景故事
    val personality: String = "", // Persona 的个性
    val isLoading: Boolean = false // 是否正在加载
)

// Persona 创作屏幕的 ViewModel
class PersonaCreationViewModel : ViewModel() {
    // 可变的 UI 状态，使用 MutableStateFlow
    private val _uiState = MutableStateFlow(PersonaCreationUIState())
    // 将可变的 UI 状态暴露为不可变的 StateFlow
    val uiState: StateFlow<PersonaCreationUIState> = _uiState.asStateFlow()

    // 初始化块，在 ViewModel 创建时调用
    init {
        loadMockkData()
    }

    // 加载模拟数据
    private fun loadMockkData() {
        // 更新 UI 状态
        _uiState.update { currentState ->
            currentState.copy(
                name = MockData.myPersona.name,
                backgroundStory = MockData.myPersona.backgroundStory,
                personality = MockData.myPersona.personality
            )
        }
    }

    // 当名字改变时调用
    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    // 当背景故事改变时调用
    fun onStoryChanged(newStory: String) {
        _uiState.update { it.copy(backgroundStory = newStory) }
    }

    // 当个性改变时调用
    fun onPersonalityChanged(newPersonality: String) {
        _uiState.update { it.copy(personality = newPersonality) }
    }

    // 当“AI 辅助生成”按钮被点击时调用
    fun onGeneratePersonaClicked() {
        // 更新 UI 状态为加载中
        _uiState.update { it.copy(isLoading = true) }
        // TODO
        // 在这里调用 API 来生成 Persona
        // 更新 UI 状态为非加载中
        _uiState.update { it.copy(isLoading = false) }
    }
}