package com.vibe.news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vibe.news.ui.theme.VibeNewsTheme
import com.vibe.news.ui.home.HomeScreen
import dagger.hilt.android.AndroidEntryPoint
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

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
                                onArticleClick = { url ->
                                    // Use Custom Tabs for a native feel
                                    val builder = androidx.browser.customtabs.CustomTabsIntent.Builder()
                                    builder.setShowTitle(true)
                                    builder.setInstantAppsEnabled(true)
                                    val customTabsIntent = builder.build()
                                    customTabsIntent.launchUrl(context, Uri.parse(url))
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
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
