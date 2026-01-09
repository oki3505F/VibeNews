package com.vibe.news.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.news.data.NewsRepository
import com.vibe.news.data.local.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val articles: StateFlow<List<Article>> = repository.articles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = kotlinx.coroutines.flow.MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<Article>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.articles else repository.search(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentCity = kotlinx.coroutines.flow.flow {
        emit(repository.getCurrentCity())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        refresh()
    }
    
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshNews()
        }
    }
    
    fun onArticleClick(article: Article) {
        viewModelScope.launch {
             repository.voteArticle(article)
        }
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            repository.toggleBookmark(article)
        }
    }

    fun getBookmarkedArticles() = repository.getBookmarkedArticles()
}
