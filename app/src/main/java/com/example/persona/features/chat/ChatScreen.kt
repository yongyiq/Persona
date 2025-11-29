package com.example.persona.features.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class) // TopAppBar 需要这个注解
@Composable
fun ChatScreen(
    personaId: String?,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiStates.collectAsState()


    // 列表滚动状态，用于自动滚动到底部
    val listState = rememberLazyListState()

    // --- 自动滚动逻辑 ---
    // 当 messages 数量发生变化时，自动滚动到最后一行
    LaunchedEffect(uiState.message.size) {
        if (uiState.message.isNotEmpty()) {
            listState.animateScrollToItem(uiState.message.size - 1)
        }
    }
    LaunchedEffect(personaId) {
        if (personaId != null) {
            viewModel.loadChatByPersonaId(personaId)
        }
    }

    // 1. 🔥 新增：图片选择器启动器
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )
    Scaffold(
        // 1. 顶部标题栏
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 小头像
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(uiState.targetPersona?.avatarUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // 名字
                        Text(text = uiState.targetPersona?.name ?: "Chat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        // 2. 底部输入框 (作为 Scaffold 的 bottomBar)
        bottomBar = {
            ChatInputArea(
                text = uiState.inputText,
                onTextChanged = { viewModel.onInputTextChange(it) },
                onSendClick = { viewModel.sendMessage() },
                isTyping = uiState.isTyping,
                // 🔥 新增：传入图片相关参数
                selectedImageUri = viewModel.selectedImageUri.value, // 需要在 ViewModel 中定义这个 State
                onImageSelect = {
                    // 启动相册
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onClearImage = { viewModel.onImageSelected(null) } // 清除选中的图片
            )
        }
    ) { innerPadding ->
        // 3. 消息列表
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // 避开 TopBar 和 BottomBar
                // 关键：imePadding 让列表在键盘弹出时自动上移，不被遮挡
                .imePadding()
        ) {
            LazyColumn(
                state = listState, // 绑定滚动状态
                modifier = Modifier.weight(1f) // 占据剩余所有空间
            ) {
                items(uiState.message) { msg ->
                    MessageBubble(msg)
                }
            }
        }
    }
}

// --- 抽离出来的底部输入区域组件 ---
@Composable
fun ChatInputArea(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    selectedImageUri: android.net.Uri? = null,
    onImageSelect: () -> Unit = {},
    onClearImage: () -> Unit = {},
    isTyping: Boolean
) {
    // 外层改为 Column，以便在输入框上方显示图片预览
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // 🔥 1. 图片预览区 (如果有选图才显示)
        if (selectedImageUri != null) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp) // 预览图大小
            ) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                // 删除图片的按钮
                IconButton(
                    onClick = onClearImage,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Image",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🔥 新增：图片选择按钮 (+)
            IconButton(
                onClick = onImageSelect,
                enabled = !isTyping
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Add Image",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // 输入框
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = {
                    if (isTyping) Text("对方正在输入...") else Text("输入消息...")
                },
                modifier = Modifier.weight(1f), // 占据剩余宽度
                maxLines = 3,
                enabled = !isTyping // AI回复时禁止输入(防止乱序，可选)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 发送按钮
            IconButton(
                onClick = onSendClick,
                // 逻辑优化：有文字 OR 有图片 都可以发送
                enabled = (text.isNotBlank() || selectedImageUri != null) && !isTyping
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send, // 注意：新版 Compose 这里可能有变化
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}