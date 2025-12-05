package com.example.persona.features.feed

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.MyApplication
import com.example.persona.data.ChatRepository
import com.example.persona.data.MockData
import com.example.persona.data.NetworkModule
import com.example.persona.data.Persona
import com.example.persona.data.Post
import com.example.persona.data.PostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// 定义 Feed 屏幕的 UI 状态
data class FeedUiState(
    val posts: List<Post> = emptyList(), // 帖子列表
    val myPersona: Persona? = null, // 新增：保存当前用户的 Persona 信息
    val isLoading: Boolean = false,

    // --- 发布相关状态 ---
    val isSheetOpen: Boolean = false, // 弹窗是否打开
    val publishContent: String = "",  // 输入框里的内容
    val isGenerating: Boolean = false, // AI 是否正在写
    val selectedImageUri: Uri? = null // 新增：选中的图片 URI
)

// Feed 屏幕的 ViewModel
class FeedViewModel(application: Application) : AndroidViewModel(application) {
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

    // 1. 用户选图回调
    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }
    // 监听 Active Persona 的变化
    private fun observeActivePersona() {
        viewModelScope.launch {
            // 监听 DataStore 中的 activePersonaIdFlow
            MyApplication.prefs.activePersonaIdFlow.collect { activeId ->
                if (!activeId.isNullOrBlank()) {
                    fetchPersonaDetails(activeId)
                } else {
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
                val currentUserId = MyApplication.prefs.getUserId()
                val userId = MyApplication.prefs.getUserId()
                val response = NetworkModule.backendService.getFeed(userId)

                if (response.isSuccess() && response.data != null) {
                    val processedPosts = response.data.map { post ->
                        val author = post.authorPersona.copy(
                            isMine = (post.authorPersona.ownerId == currentUserId)
                        )
                        post.copy(authorPersona = author)
                    }
                    _uiState.update { it.copy(posts = processedPosts, isLoading = false) }
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
        val imageUri = _uiState.value.selectedImageUri
        if (content.isBlank() && imageUri == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSheetOpen = false) } // 先关弹窗
            var uploadedImageUrl: String? = null
            // A. 如果有图，先上传
            if (imageUri != null) {
                uploadedImageUrl = uploadImage(imageUri)
                if (uploadedImageUrl == null) {
                    // 上传失败处理 (Toast 提示等，这里简化为直接返回)
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }
            }
            val request = PostRequest(personaId = persona.id, content = content, imageUrl = uploadedImageUrl)
            val response = NetworkModule.backendService.publishPost(request)

            if (response.isSuccess()) {
                loadFeed() // 刷新列表
                _uiState.update { it.copy(publishContent = "", selectedImageUri = null) } // 清空缓存
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    // 新增：处理关注点击
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
    // 辅助：上传图片
    private suspend fun uploadImage(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val bytes = inputStream.readBytes()
                inputStream.close()

                val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", "upload.jpg", requestFile)

                val response = NetworkModule.backendService.uploadImage(body)
                if (response.isSuccess()) response.data else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

}

