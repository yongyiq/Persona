package com.example.persona

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.persona.ui.theme.PersonaTheme
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            val currentId = MyApplication.prefs.getUserId()
            // 这里的逻辑是：如果读取回来是默认值(1L)，或者确实需要重置，这一步保证了ID肯定存在
            // 也可以写得更严谨：if (userIdFlow.first() == null) saveUserId(1L)
            // 但由于 getUserId() 有默认值 1L，我们其实主要依赖它。
            // 为了保险，显式存一次：
            if (currentId == 0L) { // 假设0是不合法的
                MyApplication.prefs.saveUserId(1L)
            }
        }
        enableEdgeToEdge()
        setContent {
            PersonaTheme {
                MainScreen()
            }
        }
    }
}
