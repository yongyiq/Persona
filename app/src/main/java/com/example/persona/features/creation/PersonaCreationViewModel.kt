package com.example.persona.features.creation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.MyApplication
import com.example.persona.data.ChatRepository
import com.example.persona.data.MockData
import com.example.persona.data.NetworkModule
import com.example.persona.data.Persona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PersonaCreationUiState(
    val topic: String = "赛博朋克黑客", // 默认主题
    val name: String = "",
    val backgroundStory: String = "",
    val personality: String = "",
    val avatarUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMsg: String? = null
)

class PersonaCreationViewModel : ViewModel() {

    private val repository = ChatRepository()
    private val _uiState = MutableStateFlow(PersonaCreationUiState())
    val uiState: StateFlow<PersonaCreationUiState> = _uiState.asStateFlow()

    // 用户输入主题
    fun onTopicChanged(newTopic: String) {
        _uiState.update { it.copy(topic = newTopic) }
    }

    // 手动修改生成的内容
    fun onNameChanged(v: String) { _uiState.update { it.copy(name = v) } }
    fun onStoryChanged(v: String) { _uiState.update { it.copy(backgroundStory = v) } }
    fun onPersonalityChanged(v: String) { _uiState.update { it.copy(personality = v) } }

    // 2. 新增：处理头像选择
    fun onAvatarSelected(uri: Uri?) {
        _uiState.update { it.copy(avatarUri = uri) }
    }

    // --- 核心：点击 AI 生成 ---
    fun onGeneratePersonaClicked() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }

            // 调用 Repository
            val generatedPersona = repository.generatePersonaProfile(_uiState.value.topic)

            if (generatedPersona != null) {
                _uiState.update {
                    it.copy(
                        // 如果 generatedPersona.name 是 null，就用 "" (空字符串) 代替
                        name = generatedPersona.name,
                        backgroundStory = generatedPersona.backgroundStory ?: "",
                        personality = generatedPersona.personality ?: "",
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMsg = "生成失败，请重试")
                }
            }
        }
    }

    // --- 核心：保存并进入 App ---
    fun onCompleteCreation() {
        val state = _uiState.value
        if (state.name.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            var finalAvatarUrl = "https://api.dicebear.com/9.x/bottts/png?seed=${state.name}" // 默认用 DiceBear
            // 如果用户选了图，先上传
            if (state.avatarUri != null) {
                val uploadedUrl = repository.uploadImage(state.avatarUri)
                if (uploadedUrl != null) {
                    finalAvatarUrl = uploadedUrl
                } else {
                    // 上传失败处理：可以选择报错，或者降级使用默认头像
                    // 这里我们选择不做中断，继续使用默认头像，但可以给个提示（可选）
                }
            }
            val currentUserId = MyApplication.prefs.getUserId()
            // 1. 创建最终的 Persona 对象
            val newPersona = Persona(
                id = "",
                name = state.name,
                avatarUrl = finalAvatarUrl, // 依然为空，或者你可以给个默认头像
                backgroundStory = state.backgroundStory,
                personality = state.personality,
                ownerId = currentUserId,
                isMine = true
            )

            try {
                // 2. 调用后端 API
                val response = NetworkModule.backendService.createPersona(newPersona)

                if (response.isSuccess()) {
                    // 保存成功！
                    // MeScreen 会重新拉取最新的列表
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _uiState.update { it.copy(errorMsg = "保存失败: ${response.message}", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMsg = "网络错误", isLoading = false) }
            }
        }
    }
}