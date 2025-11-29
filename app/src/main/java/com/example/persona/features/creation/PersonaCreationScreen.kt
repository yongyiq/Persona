import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.persona.features.creation.PersonaCreationViewModel

@OptIn(ExperimentalMaterial3Api::class) // TopAppBar 需要这个注解
@Composable
fun PersonaCreationScreen(
    viewModel: PersonaCreationViewModel = viewModel(),
    onCreationCompleted: () -> Unit,
    onBackClick: () -> Unit // <--- 1. 新增回调
) {
    val uiState by viewModel.uiState.collectAsState()

    // 1. 图片选择器
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onAvatarSelected(uri) }
    )
    // 🔥 核心修复：监听成功状态
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onCreationCompleted() // 只有当 success 变成 true 时，才执行跳转
        }
    }

    // 2. 使用 Scaffold 添加顶部栏
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建新角色") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // 内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // 注意：必须加上这个 padding
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable {
                        // 点击触发选图
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            ){
                // 显示逻辑：
                // 1. 如果用户选了图 (uiState.avatarUri)，显示选中的图
                // 2. 如果没选但输入了名字 (uiState.name)，显示 DiceBear 预览
                // 3. 否则显示默认占位符
                val model = if (uiState.avatarUri != null) {
                    uiState.avatarUri
                } else if (uiState.name.isNotBlank()) {
                    "https://api.dicebear.com/9.x/bottts/png?seed=${uiState.name}"
                } else {
                    android.R.drawable.ic_menu_camera // 或者其他占位图标
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(model)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // (移除了原来的 "定义你的 AI 化身" 标题，因为 TopBar 有了)

            Spacer(modifier = Modifier.height(8.dp))

            // --- 1. 主题输入区 ---
            Text("你想扮演什么？(输入主题)", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = uiState.topic,
                onValueChange = { viewModel.onTopicChanged(it) },
                placeholder = { Text("例如：火星探险家、中世纪骑士...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.onGeneratePersonaClicked() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI 正在构思中...")
                } else {
                    Text("✨ AI 一键生成设定")
                }
            }

            if (uiState.errorMsg != null) {
                Text(uiState.errorMsg!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. 结果编辑区 ---
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChanged(it) },
                label = { Text("名称") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.backgroundStory,
                onValueChange = { viewModel.onStoryChanged(it) },
                label = { Text("背景故事") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = uiState.personality,
                onValueChange = { viewModel.onPersonalityChanged(it) },
                label = { Text("性格特征") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. 完成按钮 ---
            Button(
                onClick = {
                    viewModel.onCompleteCreation()
                    // 注意：这里最好改为监听 uiState.isSaveSuccess 再调用
                },
                enabled = uiState.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("保存并返回", fontSize = 18.sp)
            }
        }
    }
}