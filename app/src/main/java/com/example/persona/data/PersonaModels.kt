package com.example.persona.data // 确保包名正确

/**
 * 代表一个“数字人格”的核心数据模型
 */
data class Persona(
    val id: String,
    val name: String,
    val avatarUrl: String,      // 暂时用一个本地 drawable 或 URL
    val backgroundStory: String, // 背景故事
    val personality: String,     // 鲜明个性 (将作为 LLM 的 System Prompt)
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
    val id: String,
    val text: String,
    val author: String, // 可以是 "user" 或 Persona 的 id
    val isFromUser: Boolean // 方便 UI 判断是显示在左边还是右边
)