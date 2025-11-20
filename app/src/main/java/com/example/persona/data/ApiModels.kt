package com.example.persona.data

data class ChatRequest(
    val model: String,
    val messages: List<ApiMessage>,
)

data class ApiMessage(
    val role: String,
    val content: String,
)

data class ChatResponse(
    val choices: List<Choice>,
)

data class Choice(
    val message: ApiMessage,
)