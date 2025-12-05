package com.example.persona

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.persona.data.ChatMessage
import com.example.persona.data.NetworkModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageGenerationApiTest {

    private val TAG = "ImageGenTest"

    @Test
    fun testGenerateImageApi() {
        runBlocking {
            Log.d(TAG, "🖼️ 开始测试文生图 API...")

            // 1. 构造请求消息
            // 注意：这里直接模拟 ViewModel 传给后端的对象
            // 后端会直接取 text 字段作为 Prompt，所以我们可以不加 "/image" 前缀，直接写提示词
            val prompt = "A cyberpunk cat with neon lights, high quality"
            val requestMsg = ChatMessage(
                text = prompt,
                userId = 1L,      // 假设用户 ID 为 1
                personaId = 1L,   // 假设 Persona ID 为 1
                isFromUser = true,
                type = 0          // 发送请求时是文本类型
            )

            try {
                // 2. 调用后端接口
                // 确保你的 BackendApiService 中已经定义了 generateImage 方法
                val response = NetworkModule.backendService.generateImage(requestMsg)

                Log.d(TAG, "收到响应: Code=${response.code}, Msg=${response.message}")

                // 3. 验证基础响应
                assertEquals("状态码应该是200", 200, response.code)
                assertTrue("响应消息应该是 success", response.isSuccess())
                assertNotNull("返回的数据 data 不应为空", response.data)

                // 4. 验证返回的图片消息
                val imageMsg = response.data!!
                Log.d(TAG, "图片消息内容: ${imageMsg.text}")
                Log.d(TAG, "消息类型: ${imageMsg.type}")

                // 验证类型是否为 1 (图片)
                assertEquals("返回的消息类型应该是 1 (图片)", 1, imageMsg.type)
                
                // 验证内容是否为 URL
                assertTrue("返回的内容应该是 URL", imageMsg.text.startsWith("http"))
                
                // 验证发送者是否为 AI (isFromUser = false)
                assertEquals("发送者应该是 AI", false, imageMsg.isFromUser)

                Log.i(TAG, "文生图 API 测试通过！图片 URL: ${imageMsg.text}")

            } catch (e: Exception) {
                Log.e(TAG, "测试失败: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }
}