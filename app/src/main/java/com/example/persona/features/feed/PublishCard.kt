package com.example.persona.features.feed

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.persona.data.Persona

// 发布新帖子的卡片 Composable 函数
@Composable
fun PublishCard(
    persona: Persona, // 当前用户的 Persona
    isPublished: Boolean, // 是否正在发布
    onPublishClick: () -> Unit // 点击发布按钮的回调
) {
    // 使用 Card 作为容器
    Card(
        modifier = Modifier
            .fillMaxWidth() // 填充最大宽度
            .padding(horizontal = 16.dp, vertical = 8.dp) // 设置水平和垂直内边距
    ) {
        // 使用 Row 在水平方向上排列 UI 元素
        Row(
            modifier = Modifier
                .fillMaxWidth() // 填充最大宽度
                .padding(16.dp), // 设置内边距
            verticalAlignment = Alignment.CenterVertically // 垂直居中对齐
        ) {
            // 1. 显示我们自己的头像
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(persona.avatarUrl) // 图片 URL
                    .crossfade(true) // 开启动画效果
                    .placeholder(android.R.drawable.ic_menu_myplaces) // 用一个"我的"图标占位
                    .error(android.R.drawable.ic_menu_myplaces) // 设置错误图
                    .build(),
                contentDescription = "My Avatar", // 内容描述
                contentScale = ContentScale.Crop, // 图片裁剪方式
                modifier = Modifier
                    .size(40.dp) // 设置大小
                    .clip(CircleShape) // 裁剪成圆形
            )

            // 2. 提示文字
            Text(
                text = "在想什么, ${persona.name}?",
                style = MaterialTheme.typography.bodyMedium, // 使用 Material Design 的正文样式
                modifier = Modifier
                    .padding(start = 16.dp) // 设置左边距
                    .weight(1f) // 占据剩余空间
            )

            // 如果正在发布，显示一个进度条
            if (isPublished) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                // 否则，显示一个发布按钮
                Button(onClick = onPublishClick) {
                    Text("发布")
                }
            }
        }
    }
}
