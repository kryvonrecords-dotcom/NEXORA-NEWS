package com.nexoranews.app.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Advertisement(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("subtitle")
    val subtitle: String? = null,

    @SerializedName("tagline")
    val tagline: String? = null,

    @SerializedName("badgeText")
    val badgeText: String? = null,

    @SerializedName("mediaType")
    val mediaType: String? = null,

    @SerializedName("mediaUrl")
    val mediaUrl: String? = null,

    @SerializedName("videoThumbnail")
    val videoThumbnail: String? = null,

    @SerializedName("linkUrl")
    val linkUrl: String? = null,

    @SerializedName("targetNewTab")
    val targetNewTab: Boolean = false,

    @SerializedName("callToAction")
    val callToAction: String? = null,

    @SerializedName("position")
    val position: String? = null,

    @SerializedName("status")
    val status: String? = null
) : Serializable
