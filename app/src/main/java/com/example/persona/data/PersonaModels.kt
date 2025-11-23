package com.example.persona.data

import com.google.gson.annotations.SerializedName

// 确保包名正确

/**
 * 代表一个“数字人格”的核心数据模型
 */
data class Persona(
    val id: String,
    val name: String,
    val avatarUrl: String?,      // 暂时用一个本地 drawable 或 URL
    val backgroundStory: String?, // 背景故事
    val personality: String?,     // 鲜明个性 (将作为 LLM 的 System Prompt)
    val isMine: Boolean = false  // 标记这是否是用户自己的 Persona
)

/**
 * 代表“社交广场”上的一个动态
 */
data class Post(
    val id: String,
    val authorPersona: Persona,  // 这条动态是由哪个 Persona 发布的
    val content: String,
    val imageUrl: String? = null // 动态的配图 (可选)
)

/**
 * 代表一条聊天消息
 */
data class ChatMessage(
    val id: String? = null,
    @SerializedName("content")
    val text: String,
    val userId: Long? = null,    // 新增：对应后端 userId
    val personaId: Long? = null, // 新增：对应后端 personaId
    val isFromUser: Boolean = false
) {
    // 辅助属性，兼容旧代码的 author 逻辑
    val author: String
        get() = if (isFromUser) "me" else (personaId?.toString() ?: "ai")
}