package com.example.farmdirectoryupgraded.security

import android.content.Context
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * SSL/TLS Certificate Pinning Configuration
 * Prevents man-in-the-middle attacks by pinning expected certificates
 *
 * To generate certificate pins:
 * 1. openssl s_client -connect yourserver.com:443 </dev/null | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
 * 2. Or use: https://www.ssllabs.com/ssltest/ to get SHA-256 fingerprint
 */
object CertificatePinning {

    /**
     * Production certificate pins - MUST be updated with real values before production release
     */
    private val PRODUCTION_PINS = listOf(
        "sha256/YOUR_PRODUCTION_PIN_1_HERE",
        "sha256/YOUR_PRODUCTION_PIN_2_HERE"  // Backup pin
    )

    /**
     * Staging certificate pins
     */
    private val STAGING_PINS = listOf(
        "sha256/YOUR_STAGING_PIN_1_HERE",
        "sha256/YOUR_STAGING_PIN_2_HERE"
    )

    /**
     * Development certificate pins (can be less strict)
     */
    private val DEVELOPMENT_PINS = listOf(
        "sha256/YOUR_DEV_PIN_1_HERE"
    )

    /**
     * Create OkHttpClient with certificate pinning for the given environment
     *
     * @param hostname The hostname to pin certificates for
     * @param isDebug Whether this is a debug build
     * @param isProduction Whether this is a production environment
     * @return Configured OkHttpClient instance
     */
    fun createSecureOkHttpClient(
        hostname: String,
        isDebug: Boolean = false,
        isProduction: Boolean = false
    ): OkHttpClient {
        val certificatePinner = CertificatePinner.Builder()
            .add(
                hostname,
                *when {
                    isProduction -> PRODUCTION_PINS.toTypedArray()
                    isDebug -> DEVELOPMENT_PINS.toTypedArray()
                    else -> STAGING_PINS.toTypedArray()
                }
            )
            .build()

        val builder = OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        // Only add logging in debug builds to prevent sensitive data leaks
        if (isDebug) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    /**
     * Create OkHttpClient without certificate pinning (for development only)
     * WARNING: Only use this in non-production environments
     *
     * @param isDebug Whether logging should be enabled
     * @return Configured OkHttpClient instance
     */
    fun createDevelopmentOkHttpClient(isDebug: Boolean = true): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (isDebug) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    /**
     * Validate hostname format before creating client
     *
     * @param hostname The hostname to validate
     * @return True if hostname is valid, false otherwise
     */
    fun isValidHostname(hostname: String): Boolean {
        val hostnameRegex = Regex("^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$")
        return hostnameRegex.matches(hostname)
    }

    /**
     * Extract hostname from URL
     *
     * @param url The URL to extract hostname from
     * @return The extracted hostname or null if invalid
     */
    fun extractHostname(url: String): String? {
        return try {
            val pattern = Regex("^(?:https?://)?([^\\/\\s]+)")
            pattern.find(url)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}
