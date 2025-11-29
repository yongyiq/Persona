package com.example.persona

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.persona.data.ChatRepository
import com.example.persona.data.MockData
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MultiModalChatTest {

    private val TAG = "MultiModalTest"

    @Test
    fun testImageUnderstanding() {
        runBlocking {
            Log.d(TAG, "🖼️ 开始测试多模态（图文）对话...")

            // 1. 准备测试数据
            val repository = ChatRepository()
            
            // 使用一张阿里云 DashScope 官方文档中的示例图片（确保 AI 能访问到）
            // 这是一个女孩和狗的图片
            val testImageUrl = "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg"
            val prompt = "请详细描述一下这张图片的内容。"
            
            // 使用 MockData 里的默认 Persona
            val persona = MockData.myPersona

            Log.d(TAG, "测试图片: $testImageUrl")
            Log.d(TAG, "测试问题: $prompt")

            try {
                // 2. 调用 Repository 的流式方法
                // 注意：这里我们跳过了 uploadImage 步骤，直接传 URL，专注于测试 LLM 的视觉能力
                val responseFlow = repository.sendMessageStream(
                    persona = persona,
                    messageHistory = emptyList(), // 不带历史记录
                    newUserMessage = prompt,
                    imageToSend = testImageUrl // 🔥 传入图片 URL
                )

                // 3. 收集流式响应
                val fullResponseBuilder = StringBuilder()
                
                // toList() 会收集 Flow 直到结束
                responseFlow.collect { delta ->
                    fullResponseBuilder.append(delta)
                    // 打印每个片段，模拟流式效果
                    // Log.d(TAG, "收到片段: $delta") 
                }

                val fullResponse = fullResponseBuilder.toString()
                
                Log.d(TAG, "🤖 AI 回复完整内容: \n$fullResponse")

                // 4. 断言验证
                assertTrue("回复内容不应为空", fullResponse.isNotBlank())
                
                // 验证 AI 是否真的看懂了图 (关键词匹配)
                // 示例图里有女孩和狗，AI 的回复里应该包含这些词
                val isRelevant = fullResponse.contains("女孩") || 
                                 fullResponse.contains("狗") || 
                                 fullResponse.contains("海边") ||
                                 fullResponse.contains("沙滩")
                                 
                assertTrue("AI 应该能识别出图中的'女孩'或'狗'", isRelevant)

                Log.i(TAG, "✅ 多模态测试通过！Qwen-VL 模型工作正常。")

            } catch (e: Exception) {
                Log.e(TAG, "❌ 测试失败: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }
}