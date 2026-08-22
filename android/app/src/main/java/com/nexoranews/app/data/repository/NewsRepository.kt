package com.nexoranews.app.data.repository

import android.content.Context
import com.nexoranews.app.data.local.BookmarkManager
import com.nexoranews.app.data.local.InitialNewsSeed
import com.nexoranews.app.data.local.OfflineCacheManager
import com.nexoranews.app.data.model.Category
import com.nexoranews.app.data.model.NewsItem
import com.nexoranews.app.data.remote.NewsApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NewsResult(
    val news: List<NewsItem>,
    val isOffline: Boolean,
    val errorMessage: String? = null
)

data class CategoriesResult(
    val categories: List<Category>,
    val isOffline: Boolean
)

class NewsRepository(context: Context) {

    private val apiService = NewsApiService()
    private val cacheManager = OfflineCacheManager(context.applicationContext)
    private val bookmarkManager = BookmarkManager(context.applicationContext)

    suspend fun getNews(category: String? = null, search: String? = null, forceRefresh: Boolean = false): NewsResult = withContext(Dispatchers.IO) {
        val cachedNews = cacheManager.getNews()

        // If filtering locally on cached items
        var filteredLocal = cachedNews
        if (!category.isNullOrBlank() && category != "Todos") {
            filteredLocal = filteredLocal.filter { it.category.equals(category, ignoreCase = true) }
        }
        if (!search.isNullOrBlank()) {
            val q = search.trim().lowercase()
            filteredLocal = filteredLocal.filter {
                it.title.lowercase().contains(q) ||
                (it.summary?.lowercase()?.contains(q) == true) ||
                (it.author?.lowercase()?.contains(q) == true) ||
                (it.tags?.any { tag -> tag.lowercase().contains(q) } == true)
            }
        }

        // Attempt remote API call
        val apiResult = apiService.getNews(category, search)
        if (apiResult.isSuccess) {
            val remoteList = apiResult.getOrNull()
            if (!remoteList.isNullOrEmpty()) {
                if (category.isNullOrBlank() && search.isNullOrBlank()) {
                    cacheManager.saveNews(remoteList)
                }
                return@withContext NewsResult(
                    news = remoteList,
                    isOffline = false
                )
            }
        }

        // Fallback gracefully to offline cache or seed data
        NewsResult(
            news = filteredLocal.ifEmpty { InitialNewsSeed.getDefaultNews() },
            isOffline = true,
            errorMessage = apiResult.exceptionOrNull()?.localizedMessage
        )
    }

    suspend fun getNewsBySlug(slug: String): NewsItem? = withContext(Dispatchers.IO) {
        val apiResult = apiService.getNewsBySlug(slug)
        if (apiResult.isSuccess && apiResult.getOrNull() != null) {
            return@withContext apiResult.getOrNull()
        }

        // Search local cache
        val local = cacheManager.getNews().firstOrNull { it.slug == slug || it.id == slug }
        if (local != null) return@withContext local

        // Search default seed
        InitialNewsSeed.getDefaultNews().firstOrNull { it.slug == slug || it.id == slug }
    }

    suspend fun getCategories(): CategoriesResult = withContext(Dispatchers.IO) {
        val apiResult = apiService.getCategories()
        if (apiResult.isSuccess) {
            val remoteCats = apiResult.getOrNull()
            if (!remoteCats.isNullOrEmpty()) {
                cacheManager.saveCategories(remoteCats)
                return@withContext CategoriesResult(
                    categories = remoteCats,
                    isOffline = false
                )
            }
        }

        val localCats = cacheManager.getCategories()
        CategoriesResult(
            categories = localCats.ifEmpty { InitialNewsSeed.getDefaultCategories() },
            isOffline = true
        )
    }

    fun isBookmarked(newsId: String): Boolean = bookmarkManager.isBookmarked(newsId)

    fun toggleBookmark(news: NewsItem): Boolean = bookmarkManager.toggleBookmark(news)

    fun getBookmarks(): List<NewsItem> = bookmarkManager.getAllBookmarks()

    fun removeBookmark(newsId: String) = bookmarkManager.removeBookmark(newsId)

    fun clearCache() = cacheManager.clearCache()

    fun getCacheSize(): String = cacheManager.getCacheSizeDescription()
}
