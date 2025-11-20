package com.example.persona.features.feed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.persona.data.MockData

// “广场”屏幕的 Composable 函数
@Composable
fun FeedScreen(
    // 使用 viewModel() 获取 FeedViewModel 的实例
    viewModel: FeedViewModel = viewModel(),
    onNavigateToChat: (String) -> Unit = {}
) {
    // 从 ViewModel 中收集 UI 状态
    val uiState by viewModel.uiState.collectAsState()

    // 使用 LazyColumn 来显示一个可滚动的列表
    LazyColumn(
        modifier = Modifier.fillMaxSize() // 填充整个屏幕
    ) {
        // 列表的第一项是发布卡片
        item {
            PublishCard(
                persona = MockData.myPersona, // 总是显示我们自己的 Persona
                isPublished = uiState.isPublishing, // 是否正在发布
                onPublishClick = { // 点击发布按钮时调用
                    viewModel.onPublishNewPost()
                }
            )
        }

        // 遍历帖子列表，为每个帖子创建一个 PostCard
        items(uiState.posts) { post ->
            PostCard(
                post = post,
                onAvatarClick = { personaId ->
                    onNavigateToChat(personaId)
                }
            )
        }
    }
}
