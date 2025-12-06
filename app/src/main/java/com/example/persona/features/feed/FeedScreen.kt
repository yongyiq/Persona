import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.persona.features.feed.FeedViewModel
import com.example.persona.features.feed.PostCard
import com.example.persona.features.feed.PublishBottomSheet
import com.example.persona.features.feed.PublishCard
// 👇 必须手动添加这行，才能直接传 List 给 items()
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = viewModel(),
    onNavigateToChat: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    // 使用 PullToRefreshBox 包裹列表
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshFeed() }, // 触发 ViewModel 的刷新
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. 列表主体
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // 顶部发布入口
            item {
                // 如果 myPersona 还没加载出来，用占位符或 loading
                if (uiState.myPersona != null) {
                    PublishCard(
                        persona = uiState.myPersona!!, // 显示真实的我的数据
                        isPublished = false, // 这里不再需要 loading 状态了，弹窗里有
                        onPublishClick = {
                            viewModel.openPublishSheet() // 点击仅仅是打开弹窗
                        }
                    )
                }
            }

            items(uiState.posts) { post ->
                PostCard(post = post, onAvatarClick = { onNavigateToChat(it) },onFollowClick = { viewModel.toggleFollow(it) } )
            }
        }
    }


    // 2. 挂载底部弹窗 (只有当 isSheetOpen 为 true 时才显示)
    if (uiState.isSheetOpen) {
        PublishBottomSheet(
            onDismiss = { viewModel.closePublishSheet() },
            content = uiState.publishContent,
            onContentChange = { viewModel.onContentChanged(it) },
            onAiGenerate = { viewModel.onAiGenerateClick() },
            onPublish = { viewModel.publishPost() },
            isGenerating = uiState.isGenerating,
            // 新增：传递图片选择相关的参数
            selectedImageUri = uiState.selectedImageUri,
            onImageSelect = { uri -> viewModel.onImageSelected(uri) }
        )
    }
}