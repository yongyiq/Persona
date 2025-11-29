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
import com.example.persona.features.auth.LoginScreen
import com.example.persona.features.auth.RegisterScreen
import com.example.persona.features.chat.ChatListScreen
import com.example.persona.features.chat.ChatScreen
import com.example.persona.features.follow.FollowListScreen
import com.example.persona.features.me.MeScreen
import com.example.persona.features.profile.PersonaProfileScreen

// 定义应用程序中的各个屏幕，这是一个密封类，用于表示有限的屏幕集合
sealed class AppScreen(val route: String, val title: String, val iconResId: Int) {
    // “广场” 屏幕
    data object Feed : AppScreen("feed", "广场", android.R.drawable.ic_menu_search)
    data object FollowList : AppScreen("follow_list", "关注", 0)
    // “对话” 屏幕
    data object Chat : AppScreen("chat", "对话", android.R.drawable.ic_dialog_email) {
        const val routeWithArgs = "chat/{personaId}"

        fun createRoute(personaId: String) = "chat/$personaId"
    }
    // “我的” 屏幕
    data object Me : AppScreen("me", "我的", android.R.drawable.ic_menu_myplaces)
    // “创作” 屏幕，没有图标
    data object PersonaCreation : AppScreen("creation", "创作", 0)
    data object Login : AppScreen("login", "登录", 0) // 新增

    data object Register : AppScreen("register", "注册", 0) // 🔥 新增

    data object PersonaProfile : AppScreen("profile", "主页", 0) {
        const val routeWithArgs = "profile/{personaId}"
        fun createRoute(personaId: String) = "profile/$personaId"
    }

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

    val isLoggedIn = kotlinx.coroutines.runBlocking {
        com.example.persona.MyApplication.prefs.getUserId() != 0L // 假设 0L 是未登录
    }

    val startRoute = if (isLoggedIn) AppScreen.Feed.route else AppScreen.Login.route
    // Scaffold 是一个提供基本应用布局结构的 Composable// 获取当前的导航后退栈条目
    Scaffold(
        // 定义底部导航栏
        bottomBar = {

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute != AppScreen.PersonaCreation.route && currentRoute != AppScreen.Login.route){
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
            startDestination = startRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            // “广场” 屏幕的 Composable
            composable(AppScreen.Feed.route) {
                FeedScreen(
                    onNavigateToChat = { personaId ->
                        navController.navigate(AppScreen.PersonaProfile.createRoute(personaId))
                    }
                )
            }
            composable(AppScreen.Chat.route) {
                ChatListScreen(
                    onNavigateToChatDetail = { personaId ->
                        // 点击列表项，跳转到具体的聊天界面 (带参数)
                        navController.navigate(AppScreen.Chat.createRoute(personaId))
                    },
                    // 🔥 传入跳转逻辑
                    onNavigateToFollowList = {
                        navController.navigate(AppScreen.FollowList.route)
                    }
                )
            }
            // 这就是具体的聊天窗口
            composable(
                route = AppScreen.Chat.routeWithArgs,
                arguments = listOf(navArgument("personaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val personaId = backStackEntry.arguments?.getString("personaId")
                ChatScreen(personaId = personaId)
            }

            composable(AppScreen.FollowList.route) {
                FollowListScreen(
                    onBackClick = { navController.popBackStack() },
                    onPersonaClick = { personaId ->
                        // 点击关注的人，跳转到 TA 的主页
                        navController.navigate(AppScreen.PersonaProfile.createRoute(personaId))
                    }
                )
            }

//            // “对话” 屏幕的 Composable
//            composable(AppScreen.Chat.route) {
//                ChatScreen(personaId = null)
//            }
            // “我的” 屏幕的 Composable
            composable(AppScreen.Me.route) {
                MeScreen(
                    onNavigateToChat = { personaId ->
                        navController.navigate(AppScreen.Chat.createRoute(personaId))
                    },
                    onNavigateToCreate = {
                        // 跳转到创作页
                        navController.navigate(AppScreen.PersonaCreation.route)
                    },
                    onLogout = {
                        // 退出后跳转到登录页，并清空栈
                        navController.navigate(AppScreen.Login.route) {
                            popUpTo(0) { inclusive = true } // 清空所有历史
                        }
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
            // 🔥 新增：登录页路由
            composable(AppScreen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        // 登录成功，跳转到 Feed，并清空返回栈 (不能按返回键回到登录页)
                        navController.navigate(AppScreen.Feed.route) {
                            popUpTo(AppScreen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        // 跳转到注册页
                        navController.navigate(AppScreen.Register.route)
                    }
                )
            }
            composable(AppScreen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        // 注册成功 == 登录成功，直接进 Feed
                        // 并清空回退栈，防止按返回键回到注册/登录页
                        navController.navigate(AppScreen.Feed.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBackToLogin = {
                        // 返回登录页
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = AppScreen.PersonaProfile.routeWithArgs,
                arguments = listOf(navArgument("personaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val personaId = backStackEntry.arguments?.getString("personaId") ?: ""
                PersonaProfileScreen(
                    personaId = personaId,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToChat = { id ->
                        // 点击按钮，跳转到 Chat
                        navController.navigate(AppScreen.Chat.createRoute(id))
                    }
                )
            }
        }
    }
}