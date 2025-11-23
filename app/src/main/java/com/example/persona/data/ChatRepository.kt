package com.example.persona.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.persona.BuildConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatRepository {
    private val apiService = NetworkModule.apiService
    private val backendService = NetworkModule.backendService

    private val currentUserId = 1L

    // 新增：根据 ID 获取 Persona 对象
    suspend fun getPersonaById(id: String): Persona? {
        return withContext(Dispatchers.IO) {
            try {
                // 尝试转成 Long
                val longId = id.toLongOrNull() ?: return@withContext null

                val response = backendService.getPersonaDetail(longId)
                if (response.isSuccess()) {
                    response.data
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // 1. 获取历史记录 (从后端)
    suspend fun getHistoryFromBackend(personaId: String): List<ChatMessage> {
        return withContext(Dispatchers.IO) {
            try {
                // personaId String -> Long
                val pId = personaId.toLongOrNull() ?: return@withContext emptyList()

                val response = backendService.getChatHistory(currentUserId, pId)
                if (response.isSuccess()) {
                    response.data ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    suspend fun generatePersonaProfile(theme: String): Persona? {
        return withContext(Dispatchers.IO) {
            val systemPrompt = """
                你是一个专业的创意写作助手。请根据用户的主题，设计一个独特的虚拟角色(Persona)。
                
                必须严格按照以下 JSON 格式返回，不要包含任何 markdown 标记或额外文字：
                {
                  "name": "角色的名字",
                  "backgroundStory": "200字以内的背景故事",
                  "personality": "角色的核心性格关键词和描述"
                }
            """.trimIndent()
            val userMessage = "主题是：$theme"

            val apiMessage = listOf(
                ApiMessage(role = "system", content = systemPrompt),
                ApiMessage(role = "user", content = userMessage)
            )
            val request = ChatRequest(
                model = "qwen-plus",
                messages = apiMessage
            )
            try {
                val response = apiService.getChatResponse("Bearer ${BuildConfig.QWEN_API_KEY}", request)
                val content = response.choices.firstOrNull()?.message?.content ?:return@withContext null

                val cleanJson = content
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()
                // 3. 解析 JSON
                val type = object : TypeToken<Map<String, String>>() {}.type
                val map: Map<String, String> = Gson().fromJson(cleanJson, type)

                // 4. 转换为 Persona 对象 (ID 随机生成)
                Persona(
                    id = "my-persona-${System.currentTimeMillis()}",
                    name = map["name"] ?: "Unknown",
                    avatarUrl = "", // 暂时留空
                    backgroundStory = map["backgroundStory"] ?: "",
                    personality = map["personality"] ?: "",
                    isMine = true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    }
    // 2. 发送消息 (保存用户消息 -> 调LLM -> 保存AI消息 -> 返回AI回复)
    suspend fun sendMessageWithSync(
        persona: Persona,
        messageHistory: List<ChatMessage>,
        newUserMessage: String
    ): String {
        return withContext(Dispatchers.IO) {
            val pId = persona.id.toLongOrNull() ?: 0L

            // --- A. 异步保存用户的消息到后端 ---
            // 我们不等待它保存成功才发请求，而是“发后即忘”或异步处理，提高速度
            val userMsgObj = ChatMessage(
                text = newUserMessage,
                userId = currentUserId,
                personaId = pId,
                isFromUser = true
            )
            try {
                backendService.syncMessage(userMsgObj)
            } catch (e: Exception) { e.printStackTrace() }

            // --- B. 调用 LLM (逻辑保持不变) ---
            val systemPrompt = if (persona.isMine) {
                // 共生模式 Prompt ... (复制之前的代码)
                """
                你现在是用户创造的数字人格 "${persona.name}"...
                """.trimIndent()
            } else {
                // 普通模式 Prompt ...
                """
                你现在是 ${persona.name}...
                """.trimIndent()
            }

            val apiMessages = mutableListOf<ApiMessage>()
            apiMessages.add(ApiMessage(role = "system", content = systemPrompt))

            // 转换历史记录格式
            messageHistory.takeLast(10).forEach { chatMsg ->
                val role = if (chatMsg.isFromUser) "user" else "assistant"
                apiMessages.add(ApiMessage(role = role, content = chatMsg.text))
            }
            apiMessages.add(ApiMessage(role = "user", content = newUserMessage))

            val request = ChatRequest(model = "qwen-plus", messages = apiMessages)

            // 发起 LLM 请求
            var aiContent = "..."
            try {
                val response = apiService.getChatResponse("Bearer ${BuildConfig.QWEN_API_KEY}", request)
                aiContent = response.choices.firstOrNull()?.message?.content ?: "(无回复)"
            } catch (e: Exception) {
                e.printStackTrace()
                aiContent = "连接断开了... (${e.message})"
            }

            // --- C. 异步保存 AI 的消息到后端 ---
            val aiMsgObj = ChatMessage(
                text = aiContent,
                userId = currentUserId,
                personaId = pId,
                isFromUser = false
            )
            try {
                backendService.syncMessage(aiMsgObj)
            } catch (e: Exception) { e.printStackTrace() }

            // 返回给 ViewModel 显示
            aiContent
        }
    }
    // 新增：让 AI 基于人设生成一条社交动态
    suspend fun generatePostContent(persona: Persona): String {
        return withContext(Dispatchers.IO) {
            val systemPrompt = """
                你现在是 "${persona.name}"。
                性格：${persona.personality}。
                
                任务：请写一条简短的社交网络动态（类似微博/Twitter）。
                要求：
                1. 必须完全符合你的性格口吻。
                2. 长度控制在 30-80 字之间。
                3. 不要加引号，直接输出内容。
            """.trimIndent()

            val request = ChatRequest(
                model = "qwen-plus",
                messages = listOf(ApiMessage(role = "system", content = systemPrompt))
            )

            try {
                val response = apiService.getChatResponse("Bearer ${BuildConfig.QWEN_API_KEY}", request)
                response.choices.firstOrNull()?.message?.content ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
}