package com.example.persona.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.persona.BuildConfig

class ChatRepository {
    private val apiService = NetworkModule.apiService

    suspend fun sendMessage(
        persona: Persona,
        messageHistory: List<ChatMessage>,
        newUserMessage: String
    ): String {
        return withContext(Dispatchers.IO) {
            // 1. System Message: 注入灵魂
            // 这是让 AI 扮演角色的关键
            val systemPrompt = """
                你现在是 ${persona.name}。
                你的背景故事是：${persona.backgroundStory}。
                你的性格特征是：${persona.personality}。
                请完全沉浸在这个角色中与我对话。不要暴露你是 AI。
                回复要简短、口语化，符合你的性格。
            """.trimIndent()

            val apiMessages = mutableListOf<ApiMessage>()

            apiMessages.add(ApiMessage(role = "system", content = systemPrompt))

            messageHistory.takeLast(10).forEach { chatMsg ->
                val role = if (chatMsg.isFromUser) "user" else "assistant"
                apiMessages.add(ApiMessage(role = role, content = chatMsg.text))
            }

            apiMessages.add(ApiMessage(role = "user", content = newUserMessage))

            val request = ChatRequest(
                model = "qwen-plus",
                messages = apiMessages
            )

            try {
                val response = apiService.getChatResponse(
                    apiKey = "Bearer ${BuildConfig.QWEN_API_KEY}",
                    request = request
                )
                response.choices.firstOrNull()?.message?.content ?: "(似乎在发呆)"
            } catch (e: Exception) {
                e.printStackTrace()
                "连接断开了。。。（${e.message}）"
            }
        }
    }
}