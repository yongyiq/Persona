package com.example.persona.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.data.ChatRepository
import com.example.persona.data.MockData
import com.example.persona.data.Persona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val persona: Persona? = null,
    val isLoading: Boolean = false
)

class PersonaProfileViewModel : ViewModel() {
    private val repository = ChatRepository()
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadPersona(personaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            var target = repository.getPersonaById(personaId)

            if (target == null) {
                target = MockData.samplePosts.find { it.authorPersona.id == personaId }?.authorPersona
                // 或者就是我自己
                if (target == null && personaId == MockData.myPersona.id) {
                    target = MockData.myPersona
                }
            }
            _uiState.update { it.copy(persona = target, isLoading = false) }
        }
    }


}