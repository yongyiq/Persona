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

data class ChaatUiState(
    val targetPersona: Persona? = null,
    val message: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isTyping: Boolean = false
)

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _uiStates = MutableStateFlow(ChaatUiState())
    val uiStates: StateFlow<ChaatUiState> = _uiStates.asStateFlow()

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
        val target = MockData.samplePosts.find { it.authorPersona.id ==  personaId }?.authorPersona

            ?: if (personaId == MockData.myPersona.id) MockData.myPersona else null

        if (target != null) {
            if (_uiStates.value.targetPersona?.id != target.id) {
                _uiStates.update {
                    it.copy(
                        targetPersona = target,
                        message = emptyList()
                    )
                }
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


        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = textToSend,
            author = "me",
            isFromUser = true
        )

        _uiStates.update {
            it.copy(
                message = it.message + userMsg,
                inputText = "",
                isTyping = true
            )
        }

        viewModelScope.launch {
            val aiResponse = repository.sendMessage(
                persona = target,
                messageHistory = currentState.message,
                newUserMessage = textToSend
            )

            val aiMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = aiResponse,
                author = target.name,
                isFromUser = false
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