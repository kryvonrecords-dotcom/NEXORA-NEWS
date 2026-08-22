package com.nexoranews.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nexoranews.app.data.model.NewsItem

class BookmarkManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "nexora_bookmarks"
        private const val KEY_BOOKMARKS = "saved_news_bookmarks"
    }

    fun isBookmarked(newsId: String): Boolean {
        val bookmarks = getAllBookmarks()
        return bookmarks.any { it.id == newsId }
    }

    fun toggleBookmark(news: NewsItem): Boolean {
        val bookmarks = getAllBookmarks().toMutableList()
        val existingIndex = bookmarks.indexOfFirst { it.id == news.id }
        val isNowBookmarked: Boolean
        if (existingIndex >= 0) {
            bookmarks.removeAt(existingIndex)
            isNowBookmarked = false
        } else {
            bookmarks.add(0, news)
            isNowBookmarked = true
        }
        saveBookmarks(bookmarks)
        return isNowBookmarked
    }

    fun addBookmark(news: NewsItem) {
        val bookmarks = getAllBookmarks().toMutableList()
        if (bookmarks.none { it.id == news.id }) {
            bookmarks.add(0, news)
            saveBookmarks(bookmarks)
        }
    }

    fun removeBookmark(newsId: String) {
        val bookmarks = getAllBookmarks().toMutableList()
        bookmarks.removeAll { it.id == newsId }
        saveBookmarks(bookmarks)
    }

    fun getAllBookmarks(): List<NewsItem> {
        val json = prefs.getString(KEY_BOOKMARKS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<NewsItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveBookmarks(bookmarks: List<NewsItem>) {
        try {
            val json = gson.toJson(bookmarks)
            prefs.edit().putString(KEY_BOOKMARKS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
