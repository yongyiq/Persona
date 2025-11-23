import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.persona.features.creation.PersonaCreationViewModel

@Composable
fun PersonaCreationScreen(
    viewModel: PersonaCreationViewModel = viewModel(),
    onCreationCompleted: () -> Unit // <--- 新增回调：告诉 MainActivity "我完事了"
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), // 加上滚动，防止屏幕不够高
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("定义你的 AI 化身", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

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

        // ... 这里的 BackgroundStory 和 Personality 输入框保持不变 ...
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
                viewModel.onCompleteCreation() // 保存数据
                onCreationCompleted()          // 跳转
            },
            enabled = uiState.name.isNotBlank(), // 只有名字不为空才能完成
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("完成设定，进入世界", fontSize = 18.sp)
        }
    }
}