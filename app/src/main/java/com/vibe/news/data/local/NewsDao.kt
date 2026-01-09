package com.vibe.news.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM articles ORDER BY interestScore DESC, pubDate DESC")
    fun getAllArticles(): Flow<List<Article>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<Article>)

    @Query("UPDATE articles SET interestScore = interestScore + :points WHERE id = :articleId")
    suspend fun updateScore(articleId: Int, points: Int)

    @Query("SELECT * FROM category_scores")
    suspend fun getAllCategoryScores(): List<CategoryScore>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCategoryScore(categoryScore: CategoryScore)
    
    @Query("UPDATE category_scores SET score = score + :points WHERE category = :category")
    suspend fun boostCategory(category: String, points: Int)
    
    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY pubDate DESC")
    fun searchArticles(query: String): Flow<List<Article>>

    @Query("DELETE FROM category_scores")
    suspend fun clearCategoryScores()

    @Query("DELETE FROM articles")
    suspend fun clearAll()
}
