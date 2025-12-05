package com.example.persona.features.chat

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.data.ChatMessage
import com.example.persona.data.ChatRepository
import com.example.persona.data.MockData
import com.example.persona.data.NetworkModule
import com.example.persona.data.Persona
import kotlinx.coroutines.Dispatchers
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

    // 新增状态
    val selectedImageUri = mutableStateOf<Uri?>(null)

    init {
        loadChatWith(MockData.samplePosts.first().authorPersona)
    }

    fun onImageSelected(uri: Uri?) {
        selectedImageUri.value = uri
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
            // 2. 加载历史记录

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

        viewModelScope.launch {
            val targetPersonaId = target.id.toLongOrNull()
            val currentUserId = com.example.persona.MyApplication.prefs.getUserId() // 暂时硬编码为 1，对应数据库里的 admin 用户
            // 1. 如果有选图，先上传
            var uploadedImageUrl: String? = null
            if (selectedImageUri.value != null) {
                // 上传图片拿到 URL
                uploadedImageUrl = repository.uploadImage(selectedImageUri.value!!)
                // 清空选中状态
                selectedImageUri.value = null
            }
            val displayContent = if (uploadedImageUrl != null) "$textToSend\n![image]($uploadedImageUrl)" else textToSend
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(), // 生成一个临时 ID 给 UI 用
                text = displayContent,
                userId = currentUserId,   // 谁发的
                personaId = targetPersonaId, // 发给谁
                isFromUser = true,         // 关键标志：是我发的
                isStreaming = false,
                type = 0
            )

            _uiStates.update {
                it.copy(
                    message = it.message + userMsg,
                    inputText = "",
                    isTyping = true
                )
            }

            launch(Dispatchers.IO) {
                try {
                    repository.syncToBackend(userMsg.copy(id = null)) // 封装了 backendService.syncMessage
                } catch (e: Exception) { e.printStackTrace() }
            }

            // 简单的指令判断
            val isImageRequest = textToSend.startsWith("/image") || textToSend.startsWith("画一张")

            if (isImageRequest) {
                // ===========================
                //      分支 A: 文生图流程
                // ===========================

                // A1. 创建一个临时的“正在生成”提示消息
                val aiMsgId = UUID.randomUUID().toString()
                val loadingPlaceholder = ChatMessage(
                    id = aiMsgId,
                    text = "🎨 正在挥毫泼墨中...", // 提示语
                    userId = currentUserId,
                    personaId = targetPersonaId,
                    isFromUser = false,
                    isStreaming = false,
                    type = 0 // 暂时还是文本类型
                )

                _uiStates.update { it.copy(message = it.message + loadingPlaceholder) }

                try {
                    // A2. 调用后端画图接口
                    // 复用 userMsg 的信息传给后端
                    val response = NetworkModule.backendService.generateImage(userMsg.copy(id = null))

                    if (response.isSuccess() && response.data != null) {
                        // A3. 成功！后端返回了包含 URL 和 type=1 的完整消息对象
                        val imageMsg = response.data

                        // 更新 UI：用返回的图片消息替换掉刚才的提示消息
                        _uiStates.update { state ->
                            val updatedList = state.message.map { msg ->
                                if (msg.id == aiMsgId) {
                                    // 保持 UI 上的临时 ID 不变，但内容换成图片的
                                    imageMsg.copy(id = aiMsgId)
                                } else {
                                    msg
                                }
                            }
                            state.copy(message = updatedList, isTyping = false)
                        }
                    } else {
                        // A4. 失败处理：显示错误信息
                        _uiStates.update { state ->
                            val updatedList = state.message.map { msg ->
                                if (msg.id == aiMsgId) msg.copy(text = "生成失败: ${response.message}") else msg
                            }
                            state.copy(message = updatedList, isTyping = false)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 网络异常处理
                    _uiStates.update { state ->
                        val updatedList = state.message.map { msg ->
                            if (msg.id == aiMsgId) msg.copy(text = "网络错误: ${e.message}") else msg
                        }
                        state.copy(message = updatedList, isTyping = false)
                    }
                }
            } else {
                // 2. 创建一个空的 AI 消息占位符
                val aiMsgId = UUID.randomUUID().toString()
                val aiMsgPlaceholder = ChatMessage(
                    id = aiMsgId,
                    text = "", // 初始为空
                    userId = currentUserId,
                    personaId = targetPersonaId,
                    isFromUser = false,
                    isStreaming = true
                )

                // 先把空消息加入列表，让 UI 渲染出一个空气泡
                _uiStates.update {
                    it.copy(
                        message = it.message + aiMsgPlaceholder,
                        inputText = "",
                        isTyping = true
                    )
                }

                // 3. 收集流式响应
                var fullResponse = ""

                repository.sendMessageStream(
                    persona = target,
                    messageHistory = currentState.message,
                    newUserMessage = textToSend,
                    imageToSend = uploadedImageUrl
                ).collect { delta ->
                    // 收到一个字，就拼接到总内容上
                    fullResponse += delta

                    // 实时更新 UI：找到刚才那个 AI 消息，更新它的 text
                    _uiStates.update { state ->
                        val updatedList = state.message.map { msg ->
                            if (msg.id == aiMsgId) {
                                msg.copy(text = fullResponse) // 更新文本
                            } else {
                                msg
                            }
                        }
                        state.copy(message = updatedList)
                    }
                }

                val finalAiMsg = ChatMessage(
                    id = null, // ID 交给后端生成，或者传 null
                    text = fullResponse, // 这里存的是最终拼好的完整文本
                    userId = currentUserId,
                    personaId = targetPersonaId,
                    isFromUser = false
                )
                launch(Dispatchers.IO) {
                    try {
                        repository.syncToBackend(finalAiMsg) // 此时调用 syncMessage
                    } catch (e: Exception) { e.printStackTrace() }
                }
                // 4. 流结束，同步到后端数据库
                _uiStates.update { state ->
                    val updatedList = state.message.map { msg ->
                        if (msg.id == aiMsgId) {
                            msg.copy(isStreaming = false) // 结束标记
                        } else {
                            msg
                        }
                    }
                    state.copy(message = updatedList, isTyping = false)
                }
            }
        }
    }
}