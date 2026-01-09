package com.vibe.news.di

import android.app.Application
import androidx.room.Room
import com.vibe.news.data.local.NewsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNewsDatabase(app: Application): NewsDatabase {
        return Room.databaseBuilder(
            app,
            NewsDatabase::class.java,
            "news_db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideNewsDao(db: NewsDatabase) = db.newsDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideThemeManager(app: Application): com.vibe.news.ui.theme.ThemeManager {
        return com.vibe.news.ui.theme.ThemeManager(app)
    }
}
