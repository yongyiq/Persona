package com.example.persona

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.persona.data.ApiMessage
import com.example.persona.data.ChatRequest
import com.example.persona.data.NetworkModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LlmApiTest {

    private val TAG = "LlmApiTest"

    @Test
    fun testChatApiConnection() { // <--- 修改了这里：去掉了 = 号
        runBlocking {
            // 1. 准备
            val apiKey = "Bearer ${BuildConfig.QWEN_API_KEY}"

            Log.d(TAG, "开始测试 API 调用...")
            Log.d(TAG, "使用的 Key (前5位): ${BuildConfig.QWEN_API_KEY.take(5)}...")

            val request = ChatRequest(
                model = "qwen-plus",
                messages = listOf(
                    ApiMessage(role = "user", content = "你好，请回复'测试成功'这四个字。")
                )
            )

            try {
                // 2. 执行
                val response = NetworkModule.apiService.getChatResponse(
                    apiKey = apiKey,
                    request = request
                )

                // 3. 验证
                Log.d(TAG, "收到响应: $response")
                assertNotNull("响应不能为空", response)
                assertFalse("Choices 列表不能为空", response.choices.isEmpty())

                val content = response.choices.first().message.content
                Log.d(TAG, "AI 回复内容: $content")
                assertNotNull("回复内容不能为 null", content)

                Log.i(TAG, "✅ 测试通过！API 连接正常。")

            } catch (e: Exception) {
                Log.e(TAG, "❌ 测试失败: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }
}