package com.example.persona.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.data.MockData
import com.example.persona.data.Post
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 定义 Feed 屏幕的 UI 状态
data class FeedUiState(
    val posts: List<Post> = emptyList(), // 帖子列表
    val isPublishing: Boolean = false // 是否正在发布新帖子
)

// Feed 屏幕的 ViewModel
class FeedViewModel : ViewModel() {
    // 可变的 UI 状态，使用 MutableStateFlow
    private val _uiState = MutableStateFlow(FeedUiState())
    // 将可变的 UI 状态暴露为不可变的 StateFlow
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    // 初始化块，在 ViewModel 创建时调用
    init {
        loadFeed()
    }
    // 加载动态
    private fun loadFeed() {
        // 使用模拟数据更新 UI 状态
        _uiState.value = FeedUiState(posts = MockData.samplePosts)
    }

    // 当点击发布新帖子时调用
    fun onPublishNewPost() {
        // 如果正在发布，则直接返回，防止重复点击
        if (_uiState.value.isPublishing) return
        // 启动一个协程来处理发布逻辑
        viewModelScope.launch {
            // 更新 UI 状态为正在发布
            _uiState.update { it.copy(isPublishing = true) }
            // 模拟网络延迟
            delay(1500)
            // 获取当前用户的 Persona
            val myPersona = MockData.myPersona
            // 创建新的帖子内容
            val newContent = "(${myPersona.personality})"

            // 创建一个新的帖子对象
            val newPost = Post(
                id = "posts-${System.currentTimeMillis()}",
                authorPersona = myPersona,
                content = newContent
            )
            // 更新 UI 状态
            _uiState.update { currState ->
                // 将新帖子添加到列表的开头
                val newPostList = listOf(newPost) + currState.posts

                // 返回更新后的状态
                currState.copy(
                    posts = newPostList,
                    isPublishing = false
                )
            }
        }
    }

}

