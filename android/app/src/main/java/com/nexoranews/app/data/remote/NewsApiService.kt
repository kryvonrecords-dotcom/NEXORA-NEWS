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

    suspend fun getNews(
        category: String? = null,
        search: String? = null
    ): Result<List<NewsItem>> = withContext(Dispatchers.IO) {

        val params = mutableListOf<String>()

        params.add("select=*")

        if (!category.isNullOrBlank() && category != "Todos") {
            params.add("category=eq.${URLEncoder.encode(category, "UTF-8")}")
        }

        params.add("order=published_at.desc")

        val endpoint = "/news?" + params.joinToString("&")

        val response = ApiClient.get(endpoint)

        response.mapCatching { json ->
            val type = object : TypeToken<List<NewsItem>>() {}.type
            gson.fromJson<List<NewsItem>>(gson.fromJson(json, Map::class.java)["news"]?.let { gson.toJson(it) } ?: "[]", type) ?: emptyList()
        }
    }

    suspend fun getNewsBySlug(slug: String): Result<NewsItem?> =
        withContext(Dispatchers.IO) {

            val encodedSlug = URLEncoder.encode(slug, "UTF-8")
            val endpoint = "/news?select=*&slug=eq.$encodedSlug&limit=1"

            val response = ApiClient.get(endpoint)

            response.mapCatching { json ->
                val type = object : TypeToken<List<NewsItem>>() {}.type
                val list = gson.fromJson<List<NewsItem>>(gson.fromJson(json, Map::class.java)["news"]?.let { gson.toJson(it) } ?: "[]", type)
                list?.firstOrNull()
            }
        }

    suspend fun getCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            val response = ApiClient.get("/categories")
            response.mapCatching { json ->
                val type = object : TypeToken<List<Category>>() {}.type
                gson.fromJson<List<Category>>(json, type) ?: emptyList()
            }
        }

    suspend fun getSettings(): Result<SiteSettings> =
        withContext(Dispatchers.IO) {
            Result.failure(Exception("Settings not configured"))
        }
}
