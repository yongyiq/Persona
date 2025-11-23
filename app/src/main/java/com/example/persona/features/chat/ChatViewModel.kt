package com.example.persona.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.data.ChatMessage
import com.example.persona.data.ChatRepository
import com.example.persona.data.MockData
import com.example.persona.data.Persona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val targetPersona: Persona? = null,
    val message: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isTyping: Boolean = false
)

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _uiStates = MutableStateFlow(ChatUiState())
    val uiStates: StateFlow<ChatUiState> = _uiStates.asStateFlow()

    init {
        loadChatWith(MockData.samplePosts.first().authorPersona)
    }

    fun loadChatWith(persona: Persona) {
        _uiStates.update {
            it.copy(
                targetPersona = persona,
                message = emptyList()
            )
        }
    }

    fun loadChatByPersonaId(personaId: String) {

        viewModelScope.launch {

            // 1. 先清空状态，避免显示上一个人的数据
            _uiStates.update {
                it.copy(targetPersona = null, message = emptyList(), isTyping = false)
            }
            val target = repository.getPersonaById(personaId)
            val finalTarget = target ?: MockData.myPersona
            // 2. 加载历史记录 (这是新增的！)

            // 3. 更新 UI 显示目标名字/头像
            _uiStates.update { it.copy(targetPersona = finalTarget) }
            // 调用 Repository 从后端拉取历史
            val history = repository.getHistoryFromBackend(personaId)

            _uiStates.update {
                it.copy(message = history)
            }
        }
    }


    fun onInputTextChange(newText: String) {
        _uiStates.update { it.copy(inputText = newText) }
    }

    fun sendMessage() {
        val currentState = _uiStates.value
        val textToSend = currentState.inputText.trim()
        val target = currentState.targetPersona ?: return

        if (textToSend.isBlank()) return
        if (currentState.isTyping) return

        val targetPersonaId = target.id.toLongOrNull()
        val currentUserId = 1L // 暂时硬编码为 1，对应数据库里的 admin 用户

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(), // 生成一个临时 ID 给 UI 用
            text = textToSend,
            userId = currentUserId,   // 谁发的
            personaId = targetPersonaId, // 发给谁
            isFromUser = true         // 关键标志：是我发的
        )

        _uiStates.update {
            it.copy(
                message = it.message + userMsg,
                inputText = "",
                isTyping = true
            )
        }

        viewModelScope.launch {
            val aiResponse = repository.sendMessageWithSync(
                persona = target,
                messageHistory = currentState.message,
                newUserMessage = textToSend
            )

            val aiMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = aiResponse,
                userId = currentUserId,
                personaId = targetPersonaId,
                isFromUser = false // 关键标志：是 AI 发的
            )

            _uiStates.update {
                it.copy(
                    message = it.message + aiMsg,
                    isTyping = false
                )
            }
        }
    }

}