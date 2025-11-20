package com.example.persona.features.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.persona.data.ChatMessage

@Composable
fun MessageBubble(msg: ChatMessage) {
    // 1. 判断是哪一边的消息
    val isMe = msg.isFromUser

    // 2. 设置对齐方式 (Row 用于水平排列)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        // 如果是"我"，内容靠右；如果是"AI"，内容靠左
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        // 3. 气泡容器
        Box(
            modifier = Modifier
                // 气泡背景色：我是主色(Primary)，AI是次级容器色(SecondaryContainer)
                .background(
                    color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        // 根据左右位置，调整底角的圆角，形成"气泡"形状
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
                // 限制气泡最大宽度，不要占满整行，稍微留点白
                .widthIn(max = 300.dp)
        ) {
            // 4. 消息文本
            Text(
                text = msg.text,
                // 文本颜色：深色背景用浅色字，浅色背景用深色字
                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}