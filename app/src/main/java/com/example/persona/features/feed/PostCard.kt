package com.example.persona.features.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// 显示单个帖子的 Composable 函数
@Composable
fun PostCard(
    post: Post,
    onAvatarClick: (String) -> Unit,
    onFollowClick: (Post) -> Unit
) {
    // 1. 定义状态：控制大图是否显示
    var showImageDialog by remember { mutableStateOf(false) }
    // 2. 全屏大图弹窗 (Lightbox)
    if (showImageDialog && !post.imageUrl.isNullOrBlank()) {
        Dialog(
            onDismissRequest = { showImageDialog = false },
            // 设置为全屏模式，不限制宽度
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black) // 黑色背景
                    .clickable { showImageDialog = false }, // 点击任意处关闭
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full Screen Image",
                    modifier = Modifier.fillMaxWidth(), // 宽度撑满
                    contentScale = ContentScale.Fit // 保持比例完整显示
                )
            }
        }
    }
    // 使用 Card 作为帖子的容器
    Card(
        modifier = Modifier
            .fillMaxWidth() // 填充最大宽度
            .padding(horizontal = 16.dp, vertical = 8.dp), // 设置水平和垂直内边距
        // 优化 1: 增加圆角和阴影，提升层次感
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // 优化 2: 使用 Surface 颜色，确保在深色模式下有区分度
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        // 使用 Column 在垂直方向上排列 UI 元素
        Column(
            modifier = Modifier
                .fillMaxWidth() // 填充最大宽度
                .padding(16.dp) // 设置内边距
        ) {
            // 使用 Row 在水平方向上排列作者信息
            Row(
                modifier = Modifier.fillMaxWidth(), // 撑满宽度
                verticalAlignment = Alignment.CenterVertically
            ) { // 垂直居中对齐
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
                // 使用 Spacer 把后面的元素顶到最右边
                Spacer(modifier = Modifier.weight(1f))

                // 关注按钮
                if (!post.authorPersona.isMine) {
                    val buttonText = if (post.isFollowing) "已关注" else "+ 关注"
                    val buttonColor = if (post.isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer

                    FilledTonalButton(
                        onClick = { onFollowClick(post) },
                        colors = ButtonDefaults.textButtonColors(containerColor = buttonColor),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(text = buttonText, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            // 添加一个 16dp 高的垂直间距
            Spacer(modifier = Modifier.height(12.dp))

            // 2. 帖子文字内容
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            // 3. 新增：帖子配图渲染
            if (!post.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.imageUrl)
                        .crossfade(true)
                        .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位图
                        .error(android.R.drawable.ic_delete) // 加载失败占位图
                        .build(),
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop, // 裁剪模式：填满容器，多余裁剪
                    modifier = Modifier
                        .fillMaxWidth() // 宽度撑满
                        .aspectRatio(16f / 9f) // 🔥 关键：限制最大高度，防止长图占满屏幕
                        .clip(RoundedCornerShape(12.dp)) // 给图片加个圆角，更好看
                        .clickable { showImageDialog = true } // 点击弹出大图
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostCardPreview() {
    // 使用 MockData 仅仅是为了预览 UI 样式
    // 这里的 lambda 传空函数 {} 即可，因为预览时不需要真的处理点击
    PostCard(
        post = MockData.samplePosts.first(),
        onAvatarClick = {},
        onFollowClick = {} // <--- 补上这个空回调，让代码能编译通过
    )
}