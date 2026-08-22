package com.nexoranews.app.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Category(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("color")
    val color: String? = "#146EF5",

    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("order")
    val order: Int = 0,

    @SerializedName("count")
    var count: Int = 0
) : Serializable
