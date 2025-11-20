package com.example.persona.data

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LLMApiService {
    @POST("chat/completions")
    suspend fun getChatResponse(
        @Header("Authorization") apiKey: String,
        @Body request: ChatRequest
    ): ChatResponse
}