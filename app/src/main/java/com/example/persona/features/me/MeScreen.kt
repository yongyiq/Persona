package com.example.persona.features.me

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.persona.data.Persona

// ... (保留原有的 imports)

@Composable
fun MeScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onLogout: () -> Unit,
    viewModel: MeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activePersona = uiState.myPersonas.find { it.id == uiState.activePersonaId }
        ?: uiState.myPersonas.firstOrNull()

    LaunchedEffect(Unit) {
        viewModel.loadMyPersonas()
    }

    // 移除了最外层的 Box，直接使用 Column 作为主容器
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. 顶部：角色切换栏 (保持不变) ---
        Text(
            text = "切换当前身份",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(uiState.myPersonas) { persona ->
                PersonaAvatarItem(
                    persona = persona,
                    isActive = persona.id == uiState.activePersonaId,
                    onClick = { viewModel.switchPersona(persona.id) }
                )
            }
            item {
                IconButton(
                    onClick = onNavigateToCreate,
                    modifier = Modifier
                        .size(50.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()

        // --- 2. 中间内容区域 (使用 weight(1f) 占据剩余空间) ---
        Box(
            modifier = Modifier
                .weight(1f) // 关键：让这个区域占据除去顶部和底部按钮外的所有空间
                .fillMaxWidth(),
            contentAlignment = Alignment.Center // 让空状态文字居中
        ) {
            if (activePersona != null) {
                // 有 Persona 时显示详情 (注意：这里移除了原来的 Spacer(weight) 和 退出按钮)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(activePersona.avatarUrl)
                            .placeholder(android.R.drawable.ic_menu_myplaces)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = activePersona.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "当前活跃身份 (Active)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = { onNavigateToChat(activePersona.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("进入共生空间 (Evolve Chat)")
                    }
                }
            } else {
                // 空状态时显示提示
                Text("还没有角色，点击上方 + 号创建")
            }
        }

        // --- 3. 底部：退出登录按钮 (始终显示) ---
        // 将按钮移到了 if/else 之外，无论是否有 Persona 都会显示
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("退出登录")
            }
        }
    }
}

// --- 小组件：圆形头像项 ---
@Composable
fun PersonaAvatarItem(
    persona: Persona,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderDb = if (isActive) 2.dp else 0.dp

    Box(contentAlignment = Alignment.BottomEnd) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(persona.avatarUrl)
                .placeholder(android.R.drawable.ic_menu_myplaces)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(borderDb, borderColor, CircleShape)
                .clickable { onClick() }
        )

        // 如果选中，右下角显示一个小勾勾
        if (isActive) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp).offset(x = 2.dp, y = 2.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }
}