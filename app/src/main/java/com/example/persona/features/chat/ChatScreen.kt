package com.example.persona.features.chat

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
                isTyping = uiState.isTyping
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
    isTyping: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            enabled = text.isNotBlank() && !isTyping
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send, // 注意：新版 Compose 这里可能有变化
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}