package com.example.persona.data

data class LoginRequest(
    val username: String,
    val password: String // 真实项目中要注意加密传输，毕设可明文
)

// 用于接收登录后的用户信息
data class User(
    val id: Long,
    val username: String,
    val avatarUrl: String?
)