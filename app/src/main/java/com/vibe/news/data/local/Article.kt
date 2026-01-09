package com.vibe.news.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String?,
    val link: String,
    val imageUrl: String? = null,
    val source: String,
    val pubDate: Long,
    val category: String,
    val interestScore: Int = 0,
    val isBookmarked: Boolean = false
)
