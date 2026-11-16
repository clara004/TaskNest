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

    fun updateUserName(name: String) {
        userName = name.trim()
        prefs.edit().putString(KEY_NAME, userName).apply()
    }

    private companion object {
        const val KEY_NAME = "user_name"
    }
}