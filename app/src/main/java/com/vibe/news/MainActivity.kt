package com.vibe.news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.vibe.news.ui.theme.VibeNewsTheme
import com.vibe.news.ui.home.HomeScreen
import dagger.hilt.android.AndroidEntryPoint
import android.net.Uri
import android.util.Log
import android.content.Intent

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VibeNewsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = androidx.navigation.compose.rememberNavController()
                    val context = LocalContext.current
                    
                    androidx.navigation.compose.NavHost(navController = navController, startDestination = "home") {
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
