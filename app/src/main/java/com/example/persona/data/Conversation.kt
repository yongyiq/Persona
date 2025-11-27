package com.example.persona.data

data class Conversation(
    val personaId: String,
    val personaName: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val lastMessageTime: String? // 后端传回来的时间字符串
)