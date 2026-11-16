package com.example.tasknest.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class UserPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("tasknest_prefs", Context.MODE_PRIVATE)

    var userName by mutableStateOf(prefs.getString(KEY_NAME, "").orEmpty())
        private set

    var defaultPriority by mutableStateOf(prefs.getString(KEY_PRIORITY, "Medium").orEmpty())
        private set

    var themeMode by mutableStateOf(prefs.getString(KEY_THEME, "System").orEmpty())
        private set

    fun updateUserName(name: String) {
        userName = name.trim()
        prefs.edit().putString(KEY_NAME, userName).apply()
    }

    fun updateDefaultPriority(value: String) {
        defaultPriority = value
        prefs.edit().putString(KEY_PRIORITY, value).apply()
    }

    fun updateThemeMode(value: String) {
        themeMode = value
        prefs.edit().putString(KEY_THEME, value).apply()
    }

    private companion object {
        const val KEY_NAME = "user_name"
        const val KEY_PRIORITY = "default_priority"
        const val KEY_THEME = "theme_mode"
    }
}