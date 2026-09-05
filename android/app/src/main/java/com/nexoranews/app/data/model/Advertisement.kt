package com.nexoranews.app.data.model

data class Advertisement(
    val id: String,
    val title: String,
    val subtitle: String,
    val tagline: String,
    val badgeText: String,
    val mediaType: String,
    val mediaUrl: String,
    val videoThumbnail: String,
    val linkUrl: String,
    val targetNewTab: Boolean,
    val callToAction: String,
    val position: String,
    val status: String,
    val order: Int
)
