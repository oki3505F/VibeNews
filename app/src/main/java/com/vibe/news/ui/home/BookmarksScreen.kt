package com.vibe.news.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onArticleClick: (String, String) -> Unit
) {
    val articles by viewModel.getBookmarkedArticles().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Saved Articles", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        if (articles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No bookmarked articles yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(articles, key = { it.id }) { article ->
                    ArticleCard(
                        article = article,
                        onBookmarkClick = { viewModel.toggleBookmark(article) },
                        onClick = { onArticleClick(article.link, article.title) }
                    )
                }
            }
        }
    }
}
