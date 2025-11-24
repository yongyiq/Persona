package com.example.persona

import FeedScreen
import PersonaCreationScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.persona.features.chat.ChatScreen
import com.example.persona.features.me.MeScreen

// 定义应用程序中的各个屏幕，这是一个密封类，用于表示有限的屏幕集合
sealed class AppScreen(val route: String, val title: String, val iconResId: Int) {
    // “广场” 屏幕
    data object Feed : AppScreen("feed", "广场", android.R.drawable.ic_menu_search)
    // “对话” 屏幕
    data object Chat : AppScreen("chat", "对话", android.R.drawable.ic_dialog_email) {
        const val routeWithArgs = "chat/{personaId}"

        fun createRoute(personaId: String) = "chat/$personaId"
    }
    // “我的” 屏幕
    data object Me : AppScreen("me", "我的", android.R.drawable.ic_menu_myplaces)
    // “创作” 屏幕，没有图标
    data object PersonaCreation : AppScreen("creation", "创作", 0)

}

// 定义底部导航栏中显示的条目
val bottomNavItems = listOf(
    AppScreen.Feed,
    AppScreen.Chat,
    AppScreen.Me
)

// 主屏幕的 Composable 函数
@Composable
fun MainScreen() {
    // 创建一个 NavController 来处理导航
    val navController = rememberNavController()
    // Scaffold 是一个提供基本应用布局结构的 Composable// 获取当前的导航后退栈条目
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Scaffold(
        // 定义底部导航栏
        bottomBar = {
            if (currentRoute != AppScreen.PersonaCreation.route){
                NavigationBar {

                    // 获取当前的目的地
                    val currentDestination = navBackStackEntry?.destination
                    // 遍历底部导航项，为每一项创建一个 NavigationBarItem
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            // 设置图标
                            icon = { Icon(painterResource(id = screen.iconResId), contentDescription = screen.title) },
                            // 设置标签
                            label = { Text(screen.title) },
                            // 判断当前项是否被选中
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            // 设置点击事件
                            onClick = {
                                navController.navigate(screen.route) {
                                    // 弹出到导航图的起始目的地，避免在后退栈中积累大量的目的地
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    // 避免在栈顶重复创建同一个目的地
                                    launchSingleTop = true
                                    // 恢复状态
                                    if (screen == AppScreen.Feed) {
                                        restoreState = false
                                    } else {
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // NavHost 是一个用于显示导航目的地的 Composable
        NavHost(
            navController = navController,
            // 设置起始目的地
            startDestination = AppScreen.Feed.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // “广场” 屏幕的 Composable
            composable(AppScreen.Feed.route) {
                FeedScreen(
                    onNavigateToChat = { personaId ->
                        navController.navigate(AppScreen.Chat.createRoute(personaId))
                    }
                )
            }
            composable(
                route = AppScreen.Chat.routeWithArgs,
                arguments = listOf(navArgument("personaId"){type = NavType.StringType})
            ) {
                backStackEntry ->
                val personaId = backStackEntry.arguments?.getString("personaId")
                ChatScreen(personaId = personaId)
            }
            // “对话” 屏幕的 Composable
            composable(AppScreen.Chat.route) {
                ChatScreen(personaId = null)
            }
            // “我的” 屏幕的 Composable
            composable(AppScreen.Me.route) {
                MeScreen(
                    onNavigateToChat = { personaId ->
                        navController.navigate(AppScreen.Chat.createRoute(personaId))
                    },
                    onNavigateToCreate = {
                        // 跳转到创作页
                        navController.navigate(AppScreen.PersonaCreation.route)
                    }
                )
            }
            // “创作” 屏幕的 Composable
            composable(AppScreen.PersonaCreation.route) {
                PersonaCreationScreen(
                    onCreationCompleted = {
                        // 创建成功后，返回上一页 (MeScreen)
                        navController.popBackStack()
                    },
                    onBackClick = {
                        // 点击左上角返回按钮，也是返回上一页
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}