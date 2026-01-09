package com.vibe.news.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Article::class, CategoryScore::class], version = 2, exportSchema = false)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
}
