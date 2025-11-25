package com.example.persona.features.profile


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaProfileScreen(
    personaId: String,
    onBackClick: () -> Unit,
    onNavigateToChat: (String) -> Unit, // 点击按钮去聊天
    viewModel: PersonaProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(personaId) {
        viewModel.loadPersona(personaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Persona 主页") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val persona = uiState.persona
            if (persona != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. 头像
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(persona.avatarUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. 名字
                    Text(
                        text = persona.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. 性格标签
                    Text(
                        text = persona.personality ?: "性格未知",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 4. 背景故事卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "关于 TA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = persona.backgroundStory ?: "这个角色很神秘，还没有背景故事。",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // 5. 底部按钮 (核心逻辑)
                    Button(
                        onClick = { onNavigateToChat(persona.id) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        // 根据 isMine 显示不同文案
                        val btnText = if (persona.isMine) "进入共生空间" else "开始对话"
                        Text(btnText)
                    }
                }
            } else {
                // 加载失败或未找到
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("未找到该 Persona 信息")
                }
            }
        }
    }
}