package com.vibe.news.ui.article

import android.graphics.Color
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    url: String,
    title: String,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        setBackgroundColor(Color.BLACK)
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                // Stealth Mode: Refined Paywall and Banner Stripper
                                val stealthJs = """
                                    (function() {
                                        const selectors = [
                                            '.expanded-dock', '.css-17lryp7', '.css-mcm697', 
                                            '.nytheader', '.css-19v7m0s', '#gateway-content', 
                                            '.css-1bd9p34', '.tp-modal', '.tp-backdrop', 
                                            '#credential_picker_container', '.social-share',
                                            '.ad-container', 'aside', '.paywall', '#paywall',
                                            '.subscription-modal', '.newsletter-signup',
                                            '.fc-consent-root', '.css-1hy7yjr'
                                        ];
                                        selectors.forEach(s => {
                                            const elements = document.querySelectorAll(s);
                                            elements.forEach(el => el.style.display = 'none');
                                        });
                                        // Force scrollability in case paywall disabled it
                                        document.body.style.overflow = 'auto';
                                        document.documentElement.style.overflow = 'auto';
                                        document.body.style.setProperty('overflow', 'auto', 'important');
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(stealthJs, null)
                            }
                        }
                        
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            builtInZoomControls = true
                            displayZoomControls = true
                            setSupportZoom(true)
                        }

                        // Force Dark removed as per user request for "original" look
                        
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
