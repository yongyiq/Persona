package com.example.persona.data

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

interface LLMApiService {
    @POST("chat/completions")
    suspend fun getChatResponse(
        @Header("Authorization") apiKey: String,
        @Body request: ChatRequest
    ): ChatResponse

    @Streaming // 关键：告诉 Retrofit 不要一次性读取 Response
    @POST("chat/completions")
    suspend fun streamChatResponse(
        @Header("Authorization") apiKey: String,
        @Body request: ChatRequest
    ): ResponseBody // 返回原始 ResponseBody 以便手动读取流
}