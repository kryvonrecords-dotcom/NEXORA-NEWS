package com.nexoranews.app.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NewsItem(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("summary")
    val summary: String? = null,

    @SerializedName("content")
    val content: String? = null,

    @SerializedName("category")
    val category: String,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("imageCaption")
    val imageCaption: String? = null,

    @SerializedName("gallery")
    val gallery: List<String>? = null,

    @SerializedName("author")
    val author: String? = "Redação Nexora",

    @SerializedName("source")
    val source: String? = "Nexora News",

    @SerializedName("publishedAt")
    val publishedAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("readTime")
    val readTime: Int = 3,

    @SerializedName("isBreaking")
    val isBreaking: Boolean = false,

    @SerializedName("isFeatured")
    val isFeatured: Boolean = false,

    @SerializedName("views")
    val views: Int = 0,

    @SerializedName("tags")
    val tags: List<String>? = null,

    @SerializedName("shares")
    val shares: Int = 0
) : Serializable
