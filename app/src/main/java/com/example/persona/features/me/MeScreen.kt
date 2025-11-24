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

@Composable
fun MeScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: MeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 找到当前激活的 Persona 对象
    val activePersona = uiState.myPersonas.find { it.id == uiState.activePersonaId }
        ?: uiState.myPersonas.firstOrNull()

    LaunchedEffect(Unit) {
        viewModel.loadMyPersonas()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading && uiState.myPersonas.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. 顶部：角色切换栏 ---
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
                    // 显示所有 Persona 头像
                    items(uiState.myPersonas) { persona ->
                        PersonaAvatarItem(
                            persona = persona,
                            isActive = persona.id == uiState.activePersonaId,
                            onClick = { viewModel.switchPersona(persona.id) }
                        )
                    }

                    // 末尾加一个“创建”按钮
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

                // --- 2. 下方：当前角色的详细信息 ---
                if (activePersona != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // 大头像
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

                        // 名字
                        Text(
                            text = activePersona.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 状态
                        Text(
                            text = "当前活跃身份 (Active)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        // 共生按钮 (传入当前 active ID)
                        Button(
                            onClick = { onNavigateToChat(activePersona.id) },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("进入共生空间 (Evolve Chat)")
                        }
                    }
                } else {
                    // 空状态
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("还没有角色，点击上方 + 号创建")
                    }
                }
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