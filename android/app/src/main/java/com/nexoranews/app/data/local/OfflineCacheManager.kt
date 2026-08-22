package com.nexoranews.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nexoranews.app.data.model.Category
import com.nexoranews.app.data.model.NewsItem

class OfflineCacheManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "nexora_offline_cache"
        private const val KEY_CACHED_NEWS = "cached_news_items"
        private const val KEY_CACHED_CATEGORIES = "cached_categories"
        private const val KEY_LAST_SYNC_TIME = "last_sync_timestamp"
    }

    fun saveNews(newsList: List<NewsItem>) {
        if (newsList.isEmpty()) return
        try {
            val json = gson.toJson(newsList)
            prefs.edit()
                .putString(KEY_CACHED_NEWS, json)
                .putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getNews(): List<NewsItem> {
        val json = prefs.getString(KEY_CACHED_NEWS, null)
        if (json.isNullOrBlank()) {
            return InitialNewsSeed.getDefaultNews()
        }
        return try {
            val type = object : TypeToken<List<NewsItem>>() {}.type
            val list: List<NewsItem> = gson.fromJson(json, type)
            if (list.isEmpty()) InitialNewsSeed.getDefaultNews() else list
        } catch (e: Exception) {
            InitialNewsSeed.getDefaultNews()
        }
    }

    fun saveCategories(categories: List<Category>) {
        if (categories.isEmpty()) return
        try {
            val json = gson.toJson(categories)
            prefs.edit()
                .putString(KEY_CACHED_CATEGORIES, json)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCategories(): List<Category> {
        val json = prefs.getString(KEY_CACHED_CATEGORIES, null)
        if (json.isNullOrBlank()) {
            return InitialNewsSeed.getDefaultCategories()
        }
        return try {
            val type = object : TypeToken<List<Category>>() {}.type
            val list: List<Category> = gson.fromJson(json, type)
            if (list.isEmpty()) InitialNewsSeed.getDefaultCategories() else list
        } catch (e: Exception) {
            InitialNewsSeed.getDefaultCategories()
        }
    }

    fun clearCache() {
        prefs.edit().clear().apply()
    }

    fun getCacheSizeDescription(): String {
        val newsJson = prefs.getString(KEY_CACHED_NEWS, "") ?: ""
        val catJson = prefs.getString(KEY_CACHED_CATEGORIES, "") ?: ""
        val bytes = (newsJson.length + catJson.length).toLong()
        val kb = bytes / 1024.0
        return if (kb > 1024) {
            String.format("%.1f MB em cache", kb / 1024.0)
        } else {
            String.format("%.1f KB em cache", kb)
        }
    }
}
