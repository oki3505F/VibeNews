package com.vibe.news.data

import com.vibe.news.data.local.NewsDao
import com.vibe.news.data.local.Article
import com.vibe.news.data.local.CategoryScore
import com.vibe.news.data.remote.RssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val newsDao: NewsDao,
    private val client: OkHttpClient // We'll use raw OkHttp for flexibility with RssParser
) {
    val articles = newsDao.getAllArticles()
    private val parser = RssParser()

    suspend fun refreshNews() {
        withContext(Dispatchers.IO) {
            // Updated list of sources with valid RSS Feeds
            val sources = mapOf(
                "BBC Technology" to "http://feeds.bbci.co.uk/news/technology/rss.xml",
                "BBC World" to "http://feeds.bbci.co.uk/news/world/rss.xml",
                "NYT Technology" to "https://rss.nytimes.com/services/xml/rss/nyt/Technology.xml",
                "The Verge" to "https://www.theverge.com/rss/index.xml"
            )

            sources.forEach { (name, url) ->
                try {
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.let { stream ->
                            val parsedArticles = parser.parse(stream, name)
                            // Remove HTML tags from description if present
                            val categoryScores = newsDao.getAllCategoryScores().associate { it.category to it.score }
                            
                            val cleanArticles = parsedArticles.map { article -> 
                                // Boost score if category matches user interest
                                val categoryBoost = categoryScores[article.category] ?: 0
                                val initialScore = categoryBoost * 5 // 5 points per level of interest
                                
                                article.copy(
                                    description = article.description?.replace(Regex("<.*?>"), ""),
                                    interestScore = initialScore
                                ) 
                            }
                            newsDao.insertArticles(cleanArticles)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    suspend fun voteArticle(article: Article) {
        newsDao.updateScore(article.id, 10) // Vote for specific article
        
        // Boost category score
        val scores = newsDao.getAllCategoryScores()
        val existing = scores.find { it.category == article.category }
        if (existing != null) {
            newsDao.boostCategory(article.category, 1)
        } else {
            newsDao.updateCategoryScore(CategoryScore(article.category, 1))
        }
    }
    
    fun search(query: String) = newsDao.searchArticles(query)

    fun getBookmarkedArticles() = newsDao.getBookmarkedArticles()

    suspend fun toggleBookmark(article: Article) {
        newsDao.updateBookmarkStatus(article.id, !article.isBookmarked)
    }
    
    suspend fun resetInterests() {
        newsDao.clearCategoryScores()
        // Optionally reset article scores too
        val articles = newsDao.getAllArticles() // Note: this is flow, need suspend one
        // For simplicity, just clearing category scores affects future fetches.
        // To update current list, we'd need to re-fetch or update all.
        refreshNews()
    }
}
