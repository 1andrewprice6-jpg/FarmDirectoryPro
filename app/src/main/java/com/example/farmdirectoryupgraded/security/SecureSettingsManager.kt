package com.example.farmdirectoryupgraded.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure Settings Manager using EncryptedSharedPreferences
 * Stores sensitive data like tokens, API keys securely
 */
class SecureSettingsManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_farm_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_WORKER_ID = "worker_id"
        private const val KEY_SESSION_TOKEN = "session_token"
    }

    /**
     * Store access token securely
     */
    fun setAccessToken(token: String) {
        securePrefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    /**
     * Retrieve access token
     */
    fun getAccessToken(): String? {
        return securePrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Store refresh token securely
     */
    fun setRefreshToken(token: String) {
        securePrefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    /**
     * Retrieve refresh token
     */
    fun getRefreshToken(): String? {
        return securePrefs.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * Store API key securely
     */
    fun setApiKey(apiKey: String) {
        securePrefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    /**
     * Retrieve API key
     */
    fun getApiKey(): String? {
        return securePrefs.getString(KEY_API_KEY, null)
    }

    /**
     * Store worker ID
     */
    fun setWorkerId(workerId: String) {
        securePrefs.edit().putString(KEY_WORKER_ID, workerId).apply()
    }

    /**
     * Retrieve worker ID
     */
    fun getWorkerId(): String? {
        return securePrefs.getString(KEY_WORKER_ID, null)
    }

    /**
     * Store session token
     */
    fun setSessionToken(token: String) {
        securePrefs.edit().putString(KEY_SESSION_TOKEN, token).apply()
    }

    /**
     * Retrieve session token
     */
    fun getSessionToken(): String? {
        return securePrefs.getString(KEY_SESSION_TOKEN, null)
    }

    /**
     * Clear all secure data (logout)
     */
    fun clearAll() {
        securePrefs.edit().clear().apply()
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }
}
