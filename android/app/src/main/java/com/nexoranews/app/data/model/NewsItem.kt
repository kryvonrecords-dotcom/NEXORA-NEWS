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

    @SerializedName("excerpt")
    val summary: String? = null,

    @SerializedName("content")
    val content: String? = null,

    @SerializedName("categoryName")
    val category: String,

    @SerializedName("featuredImage")
    val imageUrl: String? = null,

    @SerializedName("featuredImageCaption")
    val imageCaption: String? = null,

    @SerializedName("galleryImages")
    val gallery: List<String>? = null,

    @SerializedName("authorName")
    val author: String? = "Redação Nexora",

    @SerializedName("source")
    val source: String? = "Nexora News",

    @SerializedName("publishedAt")
    val publishedAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("readTimeMinutes")
    val readTime: Int = 3,

    @SerializedName("isBreaking")
    val isBreaking: Boolean = false,

    @SerializedName("isHero")
    val isFeatured: Boolean = false,

    @SerializedName("views")
    val views: Int = 0,

    @SerializedName("tags")
    val tags: List<String>? = null,

    @SerializedName("shares")
    val shares: Int = 0
) : Serializable
