package com.nexoranews.app.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SocialMediaSettings(

    @SerializedName("socialLinks")
    val socialLinks: Map<String, String> = emptyMap(),

    @SerializedName("customSocialLinks")
    val customSocialLinks: List<CustomSocialLink> = emptyList(),

    @SerializedName("whatsappFloatingEnabled")
    val whatsappFloatingEnabled: Boolean = true,

    @SerializedName("whatsappFloatingNumber")
    val whatsappFloatingNumber: String = "",

    @SerializedName("whatsappFloatingMessage")
    val whatsappFloatingMessage: String = "",

    @SerializedName("showSocialInHeader")
    val showSocialInHeader: Boolean = true,

    @SerializedName("showSocialInFooter")
    val showSocialInFooter: Boolean = true
) : Serializable


data class CustomSocialLink(

    @SerializedName("name")
    val name: String = "",

    @SerializedName("url")
    val url: String = "",

    @SerializedName("icon")
    val icon: String? = null
) : Serializable
