package com.vibe.news

import android.os.Bundle
import android.net.Uri
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vibe.news.ui.theme.VibeNewsTheme
import com.vibe.news.ui.home.HomeScreen
import com.vibe.news.ui.home.BookmarksScreen
import com.vibe.news.ui.theme.ThemeManager
import com.vibe.news.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appTheme by themeManager.themeFlow.collectAsState(initial = AppTheme.SYSTEM)
            val context = LocalContext.current
            
            VibeNewsTheme(appTheme = appTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val haptic = LocalHapticFeedback.current

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = {
                            if (currentRoute != null && !currentRoute.startsWith("article/")) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    tonalElevation = 0.dp
                                ) {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Home, "Home") },
                                        label = { Text("Home") },
                                        selected = currentRoute == "home",
                                        onClick = { 
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            navController.navigate("home") {
                                                popUpTo("home") { inclusive = true }
                                                launchSingleTop = true 
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Favorite, "Bookmarks") },
                                        label = { Text("Saved") },
                                        selected = currentRoute == "bookmarks",
                                        onClick = { 
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            navController.navigate("bookmarks") {
                                                launchSingleTop = true 
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Settings, "Settings") },
                                        label = { Text("Settings") },
                                        selected = currentRoute == "settings",
                                        onClick = { 
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            navController.navigate("settings") {
                                                launchSingleTop = true 
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController, 
                            startDestination = "home",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("home") {
                                HomeScreen(
                                    onArticleClick = { url, title ->
                                        val encodedUrl = Uri.encode(url)
                                        val encodedTitle = Uri.encode(title)
                                        navController.navigate("article/$encodedUrl/$encodedTitle")
                                    },
                                    onSettingsClick = { 
                                        navController.navigate("settings")
                                    }
                                )
                            }
                            composable("bookmarks") {
                                BookmarksScreen(
                                    onArticleClick = { url, title ->
                                        val encodedUrl = Uri.encode(url)
                                        val encodedTitle = Uri.encode(title)
                                        navController.navigate("article/$encodedUrl/$encodedTitle")
                                    }
                                )
                            }
                            composable(
                                route = "article/{url}/{title}",
                                arguments = listOf(
                                    androidx.navigation.navArgument("url") { type = androidx.navigation.NavType.StringType },
                                    androidx.navigation.navArgument("title") { type = androidx.navigation.NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val url = Uri.decode(backStackEntry.arguments?.getString("url") ?: "")
                                val title = Uri.decode(backStackEntry.arguments?.getString("title") ?: "")
                                com.vibe.news.ui.article.ArticleDetailScreen(
                                    url = url,
                                    title = title,
                                    onBack = { navController.popBackStack() },
                                    onShare = {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, url)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share Article"))
                                    }
                                )
                            }
                            composable("settings") {
                                com.vibe.news.ui.settings.SettingsScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
