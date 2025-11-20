package com.example.persona.features.creation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// Persona 创作屏幕的 Composable 函数
@Composable
fun PersonaCreationScreen(
    // 使用 viewModel() 获取 PersonaCreationViewModel 的实例
    viewModel: PersonaCreationViewModel = viewModel()
) {
    // 从 ViewModel 中收集 UI 状态
    val uiState by viewModel.uiState.collectAsState()

    // 使用 Column 在垂直方向上排列 UI 元素
    Column(
        modifier = Modifier
            .fillMaxSize() // 填充整个屏幕
            .padding(16.dp), // 在四周添加 16dp 的内边距
        horizontalAlignment = Alignment.CenterHorizontally // 水平居中对齐
    ) {
        // 显示标题
        Text("创作你的 Persona", /* ... 在这里添加 style ... */)
        // 添加一个 16dp 高的垂直间距
        Spacer(modifier = Modifier.padding(16.dp))

        // "名称" 输入框
        OutlinedTextField(
            value = uiState.backgroundStory, // 输入框的值来自 UI 状态
            onValueChange = { viewModel.onStoryChanged(it) }, // 当值改变时，调用 ViewModel 中的方法
            label = { Text("名称") }, // 输入框的标签
            modifier = Modifier.fillMaxWidth() // 填充最大宽度
        )
        // 添加一个 8dp 高的垂直间距
        Spacer(modifier = Modifier.height(8.dp))

        // "个性" 输入框
        OutlinedTextField(
            value = uiState.personality, // 输入框的值来自 UI 状态
            onValueChange = { viewModel.onPersonalityChanged(it) }, // 当值改变时，调用 ViewModel 中的方法
            label = { Text("个性") }, // 输入框的标签
            modifier = Modifier.fillMaxWidth(), // 填充最大宽度
            minLines = 3 // 最小行数为 3
        )

        // 添加一个 24dp 高的垂直间距
        Spacer(modifier = Modifier.height(24.dp))

        // "AI 辅助生成" 按钮
        Button(
            onClick = { viewModel.onGeneratePersonaClicked() }, // 点击时调用 ViewModel 中的方法
            modifier = Modifier.fillMaxWidth() // 填充最大宽度
        ) {
            Text("AI 辅助生成")
        }

        // 如果正在加载，显示一个转圈的进度条
        if (uiState.isLoading) {
            // 添加一个 16dp 高的垂直间距
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}