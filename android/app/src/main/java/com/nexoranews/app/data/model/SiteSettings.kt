package com.nexoranews.app.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SiteSettings(
    @SerializedName("siteName")
    val siteName: String = "Nexora News",

    @SerializedName("siteTagline")
    val siteTagline: String = "Jornalismo Independente, Rigor & Credibilidade",

    @SerializedName("contactEmail")
    val contactEmail: String = "redacao@nexoranews.ao",

    @SerializedName("aboutText")
    val aboutText: String? = null
) : Serializable
