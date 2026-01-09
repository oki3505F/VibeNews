package com.vibe.news.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.news.data.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val themeManager: com.vibe.news.ui.theme.ThemeManager
) : ViewModel() {

    val theme = themeManager.themeFlow

    fun resetInterests() {
        viewModelScope.launch {
            repository.resetInterests()
        }
    }

    fun setTheme(theme: com.vibe.news.ui.theme.AppTheme) {
        viewModelScope.launch {
            themeManager.setTheme(theme)
        }
    }
}
