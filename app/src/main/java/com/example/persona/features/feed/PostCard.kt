package com.example.persona.features.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.persona.data.MockData
import com.example.persona.data.Post

// 显示单个帖子的 Composable 函数
@Composable
fun PostCard(
    post: Post,
    onAvatarClick: (String) -> Unit
) {
    // 使用 Card 作为帖子的容器
    Card(
        modifier = Modifier
            .fillMaxWidth() // 填充最大宽度
            .padding(horizontal = 16.dp, vertical = 8.dp) // 设置水平和垂直内边距
    ) {
        // 使用 Column 在垂直方向上排列 UI 元素
        Column(
            modifier = Modifier
                .fillMaxWidth() // 填充最大宽度
                .padding(16.dp) // 设置内边距
        ) {
            // 使用 Row 在水平方向上排列作者信息
            Row(verticalAlignment = Alignment.CenterVertically) { // 垂直居中对齐
                // 使用 AsyncImage 异步加载作者头像
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.authorPersona.avatarUrl) // 图片 URL
                        .crossfade(true) // 开启动画效果
                        .placeholder(android.R.drawable.ic_menu_gallery) // 设置占位图
                        .error(android.R.drawable.ic_menu_gallery) // 设置错误图
                        .build(),
                    contentDescription = "Avatar", // 内容描述
                    contentScale = ContentScale.Crop, // 图片裁剪方式
                    modifier = Modifier
                        .size(40.dp) // 设置大小
                        .clip(CircleShape) // 裁剪成圆形
                        .clickable { onAvatarClick(post.authorPersona.id) } // 点击事件
                )

                // 显示作者名字
                Text(
                    text = post.authorPersona.name,
                    style = MaterialTheme.typography.titleMedium, // 使用 Material Design 的标题样式
                    modifier = Modifier.padding(start = 16.dp) // 设置左边距
                        .clickable { onAvatarClick(post.authorPersona.id) } // 点击事件
                )
            }
            // 添加一个 16dp 高的垂直间距
            Spacer(modifier = Modifier.height(16.dp))

            // 显示帖子内容
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge // 使用 Material Design 的正文样式
            )
        }
    }
}

// 用于在 Android Studio 中预览 PostCard 的 Composable 函数
@Preview(showBackground = true)
@Composable
fun PostCardPreview() {
    // 使用模拟数据创建一个 PostCard
    PostCard(
        post = MockData.samplePosts.first(),
        onAvatarClick = {}
    )
}