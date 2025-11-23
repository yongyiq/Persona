package com.example.persona

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.persona.data.NetworkModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackendApiTest {

    @Test
    fun testConnectToSpringBoot() {
        runBlocking {
            Log.d("BackendTest", "开始连接 Spring Boot...")

            try {
                // 调用 getMyPersonas，假设 userId = 1 (我们在数据库里初始化的那个)
                val response = NetworkModule.backendService.getMyPersonas(userId = 1)

                Log.d("BackendTest", "响应 Code: ${response.code}")
                Log.d("BackendTest", "响应 Data: ${response.data}")

                // 断言
                assertEquals("状态码应该是200", 200, response.code)
                assertTrue("数据列表不应为空", response.data?.isNotEmpty() == true)

                // 验证第一条数据是不是 Kira
                val firstPersona = response.data?.first()
                Log.d("BackendTest", "第一个 Persona: ${firstPersona?.name}")
                assertEquals("Kira", firstPersona?.name)

                Log.i("BackendTest", "✅ 成功连上本地 Spring Boot！")

            } catch (e: Exception) {
                Log.e("BackendTest", "❌ 连接失败: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }
}