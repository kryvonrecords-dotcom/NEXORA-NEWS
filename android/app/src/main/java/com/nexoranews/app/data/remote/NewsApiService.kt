package com.nexoranews.app.data.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import com.nexoranews.app.data.model.Advertisement
import com.nexoranews.app.data.model.Category
import com.nexoranews.app.data.model.NewsItem
import com.nexoranews.app.data.model.SiteSettings
import com.nexoranews.app.data.model.SocialMediaSettings
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
            val responseObject = gson.fromJson(json, Map::class.java)
            val newsJson = gson.toJson(responseObject["news"])
            val type = object : TypeToken<List<NewsItem>>() {}.type
            gson.fromJson<List<NewsItem>>(newsJson, type) ?: emptyList()
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

    suspend fun getAds(position: String = "top_hero"): Result<List<Advertisement>> = withContext(Dispatchers.IO) {
        val endpoint = "/ads?position=" + URLEncoder.encode(position, "UTF-8")
        val response = ApiClient.get(endpoint)
        response.mapCatching { json ->
            val type = object : TypeToken<List<Advertisement>>() {}.type
            gson.fromJson<List<Advertisement>>(json, type) ?: emptyList()
        }
    }

    suspend fun getSettings(): Result<SiteSettings> = withContext(Dispatchers.IO) {
        val response = ApiClient.get("/settings")
        response.mapCatching { json ->
            gson.fromJson(json, SiteSettings::class.java)
        }
    }

    suspend fun submitAdProposal(
        company: String,
        contactName: String,
        email: String,
        phone: String,
        adFormat: String = "hero_banner",
        budget: String = "1_mes",
        message: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("company", company.trim())
            put("contactName", contactName.trim())
            put("email", email.trim())
            put("phone", phone.trim())
            put("adFormat", adFormat)
            put("budget", budget)
            put("message", message.trim())
        }

        ApiClient.postJson("/ads/proposals", body.toString())
    }

    suspend fun getSocialMedia(): Result<SocialMediaSettings> = withContext(Dispatchers.IO) {
        val response = ApiClient.get("/social-media")
        response.mapCatching { json ->
            gson.fromJson(json, SocialMediaSettings::class.java)
        }
    }
}
