package com.example.persona.data


// 对应 Spring Boot 的 Result<T>
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
) {
    // 辅助方法：判断是否成功
    fun isSuccess(): Boolean = code == 200
}