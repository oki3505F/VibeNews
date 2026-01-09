package com.vibe.news.ui.article

import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.unit.dp

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
                    val clipboardManager = LocalClipboardManager.current
                    val uriHandler = LocalUriHandler.current
                    val haptic = LocalHapticFeedback.current
                    
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        clipboardManager.setText(AnnotatedString(url))
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link")
                    }
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        uriHandler.openUri(url) 
                    }) {
                        Icon(Icons.Default.Language, contentDescription = "Open in Browser")
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShare()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { paddingValues ->
        var scrollProgress by remember { mutableStateOf(0f) }

        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        setBackgroundColor(android.graphics.Color.BLACK)
                        
                        // Spoof User-Agent to avoid desktop paywalls
                        settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                // Stealth Mode 2.1: Advanced Ad-Blocking & NYT Fix
                                val stealthJs = """
                                    (function() {
                                        const selectors = [
                                            '.expanded-dock', '.css-17lryp7', '.css-mcm697', 
                                            '.nytheader', '.css-19v7m0s', '#gateway-content', 
                                            '.css-1bd9p34', '.tp-modal', '.tp-backdrop', 
                                            '#credential_picker_container', '.social-share',
                                            '.ad-container', 'aside', '.paywall', '#paywall',
                                            '.subscription-modal', '.newsletter-signup',
                                            '.fc-consent-root', '.css-1hy7yjr', '.ad-unit',
                                            '[id*="google_ads"]', '.ad-box', '.ad-wrapper'
                                        ];
                                        selectors.forEach(s => {
                                            const elements = document.querySelectorAll(s);
                                            elements.forEach(el => el.style.setProperty('display', 'none', 'important'));
                                        });
                                        
                                        // NYT Specific Fixes
                                        if (window.location.hostname.includes('nytimes.com')) {
                                            const gate = document.querySelector('#gateway-content');
                                            if (gate) gate.remove();
                                            document.body.style.setProperty('position', 'relative', 'important');
                                            document.body.style.setProperty('overflow', 'auto', 'important');
                                            const story = document.querySelector('.css-v9sv90');
                                            if (story) story.style.setProperty('overflow', 'visible', 'important');
                                        }

                                        // Universal scroll restoration
                                        document.body.style.setProperty('overflow', 'auto', 'important');
                                        document.documentElement.style.setProperty('overflow', 'auto', 'important');
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(stealthJs, null)
                            }
                        }
                        
                        // Scroll listener for reading progress bar
                        setOnScrollChangeListener { _, _, scrollY, _, _ ->
                            val contentHeight = contentHeight.toFloat() * scale
                            val webViewHeight = height.toFloat()
                            if (contentHeight > webViewHeight) {
                                scrollProgress = scrollY / (contentHeight - webViewHeight)
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

                        // Restore Forced Dark Mode for Boss
                        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                            WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
                        }
                        
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Reading Progress Bar
            LinearProgressIndicator(
                progress = { scrollProgress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
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
