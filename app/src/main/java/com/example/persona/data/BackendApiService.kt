package com.example.persona.data

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
// 为了发布方便，定义一个简单的请求对象（因为发布时我们只传 personaId，不传整个对象）
data class PostRequest(
    val personaId: String, // 后端是 Long，Android 传 String 也没事
    val content: String,
    val imageUrl: String? = null
)
interface BackendApiService {

    // 1. 获取"我的" Persona 列表
    // 对应后端: GET /api/personas/mine?userId={userId}
    @GET("api/personas/mine")
    suspend fun getMyPersonas(
        @Query("userId") userId: Long
    ): ApiResponse<List<Persona>>

    // 2. 创建新的 Persona
    // 对应后端: POST /api/personas
    @POST("api/personas")
    suspend fun createPersona(
        @Body persona: Persona
    ): ApiResponse<Boolean>

    // 新增：获取单个 Persona 详情
    @GET("api/personas/{id}")
    suspend fun getPersonaDetail(
        @Path("id") id: Long
    ): ApiResponse<Persona>
    // 获取广场列表
// 修改：getFeed 现在需要传 userId 了，以便后端判断关注状态
    @GET("api/feed")
    suspend fun getFeed(
        @Query("userId") userId: Long
    ): ApiResponse<List<Post>>

    // 新增：关注/取消关注
    @POST("api/follow/toggle")
    suspend fun toggleFollow(
        @Query("userId") userId: Long,
        @Query("personaId") personaId: Long
    ): ApiResponse<Boolean>

    @GET("api/follow/list")
    suspend fun getFollowList(
        @Query("userId") userId: Long
    ): ApiResponse<List<Persona>>

    // 发布动态
    @POST("api/feed")
    suspend fun publishPost(@Body post: PostRequest): ApiResponse<Boolean>

    // 获取聊天记录
    @GET("api/chat/history")
    suspend fun getChatHistory(
        @Query("userId") userId: Long,
        @Query("personaId") personaId: Long
    ): ApiResponse<List<ChatMessage>>

    // 同步消息
    @POST("api/chat")
    suspend fun syncMessage(@Body message: ChatMessage): ApiResponse<Boolean>
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<User>

    @POST("api/auth/register")
    suspend fun register(@Body request: LoginRequest): ApiResponse<User>

    // 请求生成图片
    @POST("api/chat/image")
    suspend fun generateImage(@Body request: ChatMessage): ApiResponse<ChatMessage>

    // 上传图片接口
    @POST("api/upload")
    @Multipart // 关键注解
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): ApiResponse<String> // 返回图片 URL

    // 获取会话列表
    @GET("api/chat/conversations")
    suspend fun getConversations(
        @Query("userId") userId: Long
    ): ApiResponse<List<Conversation>>
}