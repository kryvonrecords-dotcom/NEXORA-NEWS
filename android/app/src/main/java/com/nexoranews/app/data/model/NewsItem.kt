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

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("image_caption")
    val imageCaption: String? = null,

    @SerializedName("gallery")
    val gallery: List<String>? = null,

    @SerializedName("author")
    val author: String? = "Redação Nexora",

    @SerializedName("source")
    val source: String? = "Nexora News",

    @SerializedName("published_at")
    val publishedAt: String,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("read_time")
    val readTime: Int = 3,

    @SerializedName("is_breaking")
    val isBreaking: Boolean = false,

    @SerializedName("is_featured")
    val isFeatured: Boolean = false,

    @SerializedName("views")
    val views: Int = 0,

    @SerializedName("tags")
    val tags: List<String>? = null,

    @SerializedName("shares")
    val shares: Int = 0
) : Serializable
