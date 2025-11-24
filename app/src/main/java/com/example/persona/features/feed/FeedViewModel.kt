package com.example.persona.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.MyApplication
import com.example.persona.data.ChatRepository
import com.example.persona.data.MockData
import com.example.persona.data.NetworkModule
import com.example.persona.data.Persona
import com.example.persona.data.Post
import com.example.persona.data.PostRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 定义 Feed 屏幕的 UI 状态
data class FeedUiState(
    val posts: List<Post> = emptyList(), // 帖子列表
    val myPersona: Persona? = null, // 新增：保存当前用户的 Persona 信息
    val isLoading: Boolean = false,

    // --- 发布相关状态 ---
    val isSheetOpen: Boolean = false, // 弹窗是否打开
    val publishContent: String = "",  // 输入框里的内容
    val isGenerating: Boolean = false // AI 是否正在写
)

// Feed 屏幕的 ViewModel
class FeedViewModel : ViewModel() {
    // 可变的 UI 状态，使用 MutableStateFlow
    private val _uiState = MutableStateFlow(FeedUiState())
    // 将可变的 UI 状态暴露为不可变的 StateFlow
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
    private val chatRepository = ChatRepository()

    // 初始化块，在 ViewModel 创建时调用
    init {
        loadFeed()
        observeActivePersona() // 初始化时加载我的信息
    }
    // 监听 Active Persona 的变化
    private fun observeActivePersona() {
        viewModelScope.launch {
            // 监听 DataStore 中的 activePersonaIdFlow
            MyApplication.prefs.activePersonaIdFlow.collect { activeId ->
                if (activeId != null) {
                    // 如果 ID 变了，就去后端拉取这个 Persona 的详情
                    fetchPersonaDetails(activeId)
                } else {
                    // 如果没 ID，尝试初始化一个 (fallback)
                    loadDefaultPersona()
                }
            }
        }
    }
    private fun loadDefaultPersona() {
        // (旧逻辑) 作为保底
        viewModelScope.launch {
            val userId = MyApplication.prefs.getUserId()
            val response = NetworkModule.backendService.getMyPersonas(userId = userId)
            if (response.isSuccess() && !response.data.isNullOrEmpty()) {
                val first = response.data.first()
                _uiState.update { it.copy(myPersona = first) }
                // 顺便存一下
                MyApplication.prefs.saveActivePersonaId(first.id)
            }
        }
    }
    private suspend fun fetchPersonaDetails(personaId: String) {
        try {
            // 假设后端有 getPersonaDetail 接口 (我们之前加了)
            val pId = personaId.toLongOrNull() ?: return
            val response = NetworkModule.backendService.getPersonaDetail(pId)
            if (response.isSuccess() && response.data != null) {
                _uiState.update { it.copy(myPersona = response.data) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // 1. 加载我的 Persona 信息 (用于显示在发布栏头部)
//    private fun loadMyPersonaInfo() {
//        viewModelScope.launch {
//            // 暂时硬编码 ID=1，对应数据库的 Kira
//            val userId = com.example.persona.MyApplication.prefs.getUserId()
//            val response = NetworkModule.backendService.getMyPersonas(userId = userId)
//            if (response.isSuccess() && !response.data.isNullOrEmpty()) {
//                _uiState.update { it.copy(myPersona = response.data.first()) }
//            }
//        }
//    }
    // 2. 控制弹窗开关
    fun openPublishSheet() { _uiState.update { it.copy(isSheetOpen = true) } }
    fun closePublishSheet() { _uiState.update { it.copy(isSheetOpen = false, publishContent = "") } }

    // 3. 输入框内容变化
    fun onContentChanged(text: String) { _uiState.update { it.copy(publishContent = text) } }

    // 4. AI 帮我写 (Magic Button)
    fun onAiGenerateClick() {
        val persona = _uiState.value.myPersona ?: return
        if (_uiState.value.isGenerating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            // 调用 Repository 生成文案
            val content = chatRepository.generatePostContent(persona)
            _uiState.update {
                it.copy(
                    publishContent = content, // 自动填入输入框
                    isGenerating = false
                )
            }
        }
    }
    // 加载动态
    private fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userId = MyApplication.prefs.getUserId()
                val response = NetworkModule.backendService.getFeed(userId)

                if (response.isSuccess() && response.data != null) {
                    _uiState.update { it.copy(posts = response.data, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // 5. 确认发布
    fun publishPost() {
        val content = _uiState.value.publishContent
        val persona = _uiState.value.myPersona ?: return
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSheetOpen = false) } // 先关弹窗

            val request = PostRequest(personaId = persona.id, content = content)
            val response = NetworkModule.backendService.publishPost(request)

            if (response.isSuccess()) {
                loadFeed() // 刷新列表
                _uiState.update { it.copy(publishContent = "") } // 清空缓存
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    // 🔥 新增：处理关注点击
    fun toggleFollow(post: Post) {
        viewModelScope.launch {
            val currentUserId = MyApplication.prefs.getUserId()
            // 1. 乐观更新 UI (立刻变色，不用等网络)
            // 我们需要更新列表中该作者发的所有帖子的关注状态
            val updatedList = _uiState.value.posts.map {
                if (it.authorPersona.id == post.authorPersona.id) {
                    it.copy(isFollowing = !it.isFollowing)
                } else {
                    it
                }
            }
            _uiState.update { it.copy(posts = updatedList) }

            // 2. 发起网络请求
            try {
                val targetId = post.authorPersona.id.toLongOrNull() ?: return@launch
                NetworkModule.backendService.toggleFollow(currentUserId, targetId)
            } catch (e: Exception) {
                // 如果失败，可以在这里回滚 UI (可选)
                e.printStackTrace()
            }
        }
    }

}

