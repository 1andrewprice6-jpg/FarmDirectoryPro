package com.example.farmdirectoryupgraded.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Application Settings Manager
 * Stores and retrieves app configuration
 */
class AppSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "farm_directory_settings",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_BACKEND_URL = "backend_url"
        private const val KEY_FARM_ID = "farm_id"
        private const val KEY_WORKER_NAME = "worker_name"
        private const val KEY_AUTO_CONNECT = "auto_connect"

        const val DEFAULT_BACKEND_URL = "http://10.0.2.2:4000"
        const val DEFAULT_FARM_ID = "farm-nc-1"
        const val DEFAULT_WORKER_NAME = "Mobile User"
    }

    var backendUrl: String
        get() = prefs.getString(KEY_BACKEND_URL, DEFAULT_BACKEND_URL) ?: DEFAULT_BACKEND_URL
        set(value) = prefs.edit().putString(KEY_BACKEND_URL, value).apply()

    var farmId: String
        get() = prefs.getString(KEY_FARM_ID, DEFAULT_FARM_ID) ?: DEFAULT_FARM_ID
        set(value) = prefs.edit().putString(KEY_FARM_ID, value).apply()

    var workerName: String
        get() = prefs.getString(KEY_WORKER_NAME, DEFAULT_WORKER_NAME) ?: DEFAULT_WORKER_NAME
        set(value) = prefs.edit().putString(KEY_WORKER_NAME, value).apply()

    var autoConnect: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CONNECT, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CONNECT, value).apply()

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
}
