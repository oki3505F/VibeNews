package com.vibe.news.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_scores")
data class CategoryScore(
    @PrimaryKey val category: String,
    val score: Int = 0
)
