package com.example.persona.data

data class ChatRequest(
    val model: String,
    val messages: List<ApiMessage>,
    val stream: Boolean = false
)

data class ApiMessage(
    val role: String,
    val content: Any,
)

data class ContentItem(
    val type: String? = null, // "text" 或 "image_url" (OpenAI格式) 或 Qwen特定格式
    val text: String? = null,
    val image: String? = null // Qwen-VL 使用 "image" 字段放 URL
)
data class ChatResponse(
    val choices: List<Choice>,
)

data class Choice(
    val message: ApiMessage,
)

data class StreamChatResponse(
    val choices: List<StreamChoice>
)

data class StreamChoice(
    val delta: StreamDelta // 注意：流式里叫 delta
)

data class StreamDelta(
    val content: String? // 内容可能是 null（比如结束时）
)