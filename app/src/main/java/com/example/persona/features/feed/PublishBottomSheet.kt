package com.example.persona.features.feed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Image // 记得导入这个图标

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishBottomSheet(
    onDismiss: () -> Unit,       // 关闭回调
    content: String,             // 输入框内容
    onContentChange: (String) -> Unit,
    onAiGenerate: () -> Unit,    // AI 生成回调
    onPublish: () -> Unit,       // 发布回调
    selectedImageUri: Uri?, // 新增
    onImageSelect: (Uri?) -> Unit, // 新增
    isGenerating: Boolean        // 是否正在生成
) {
    // 图片选择器启动器
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onImageSelect(uri) }
    )
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp) // 留出底部空间，防止被导航栏遮挡
        ) {
            Text(
                text = "发布新动态",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 输入框
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                placeholder = { Text("此刻的想法... (点击左下角星星让 AI 帮你写)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp), // 稍微高一点
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            // 新增：图片预览区
            if (selectedImageUri != null) {
                Box(modifier = Modifier.padding(vertical = 8.dp)) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    // 删除按钮 (右上角小叉号)
                    IconButton(
                        onClick = { onImageSelect(null) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            // 底部按钮栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：AI 生成按钮
                FilledTonalButton(
                    onClick = onAiGenerate,
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("构思中...")
                    } else {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI 帮我写")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))

                // 2. 新增：图片选择按钮
                // 只有在没有生成 AI 内容时才允许选图 (或者你可以随时允许)
                IconButton(
                    onClick = {
                        // 启动照片选择器
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isGenerating
                ) {
                    Icon(
                        imageVector = Icons.Default.Image, // 需要导入 Icons.Filled.Image
                        contentDescription = "添加图片"
                    )
                }


                // 右侧：发布按钮
                Button(
                    onClick = onPublish,
                    enabled = content.isNotBlank() && !isGenerating
                ) {
                    Text("发布")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}