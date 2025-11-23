package com.example.persona.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/"

    private const val BACKEND_BASE_URL = "http://10.0.2.2:8080/"
    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    // --- 新增: Backend 服务 (连接 Spring Boot) ---
    private val backendRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL) // 指向 Spring Boot
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: LLMApiService by lazy {
        retrofit.create(LLMApiService::class.java)
    }

    // 公开这个新的 Service
    val backendService: BackendApiService by lazy {
        backendRetrofit.create(BackendApiService::class.java)
    }

}