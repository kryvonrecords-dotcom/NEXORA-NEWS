package com.nexoranews.app.data.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nexoranews.app.data.model.Category
import com.nexoranews.app.data.model.NewsItem
import com.nexoranews.app.data.model.SiteSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class NewsApiService {

    private val gson = Gson()

    suspend fun getNews(category: String? = null, search: String? = null): Result<List<NewsItem>> = withContext(Dispatchers.IO) {
        val queryParams = mutableListOf<String>()
        if (!category.isNullOrBlank() && category != "Todos") {
            queryParams.add("category=" + URLEncoder.encode(category, "UTF-8"))
        }
        if (!search.isNullOrBlank()) {
            queryParams.add("search=" + URLEncoder.encode(search, "UTF-8"))
        }

        val endpoint = if (queryParams.isNotEmpty()) {
            "/news?" + queryParams.joinToString("&")
        } else {
            "/news"
        }

        val response = ApiClient.get(endpoint)
        response.mapCatching { json ->
            val type = object : TypeToken<List<NewsItem>>() {}.type
            gson.fromJson<List<NewsItem>>(json, type) ?: emptyList()
        }
    }

    suspend fun getNewsBySlug(slug: String): Result<NewsItem?> = withContext(Dispatchers.IO) {
        val endpoint = "/news/slug/" + URLEncoder.encode(slug, "UTF-8")
        val response = ApiClient.get(endpoint)
        response.mapCatching { json ->
            gson.fromJson(json, NewsItem::class.java)
        }
    }

    suspend fun getCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        val response = ApiClient.get("/categories")
        response.mapCatching { json ->
            val type = object : TypeToken<List<Category>>() {}.type
            gson.fromJson<List<Category>>(json, type) ?: emptyList()
        }
    }

    suspend fun getSettings(): Result<SiteSettings> = withContext(Dispatchers.IO) {
        val response = ApiClient.get("/settings")
        response.mapCatching { json ->
            gson.fromJson(json, SiteSettings::class.java)
        }
    }
}
