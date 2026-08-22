package com.nexoranews.app.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "nexora_app_preferences"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_FONT_SIZE_SCALE = "font_size_scale"
    }

    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    var fontSizeScale: Float
        get() = prefs.getFloat(KEY_FONT_SIZE_SCALE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_FONT_SIZE_SCALE, value).apply()
}
