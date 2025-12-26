package com.example.farmdirectoryupgraded.security

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * SSL/TLS Certificate Pinning Configuration
 * Prevents man-in-the-middle attacks by pinning expected certificates
 */
object CertificatePinning {

    /**
     * Create OkHttpClient with certificate pinning
     *
     * To get certificate pins:
     * 1. Run: openssl s_client -connect yourserver.com:443 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
     * 2. Or use: https://www.ssllabs.com/ssltest/ to get the SHA-256 fingerprint
     *
     * Example pins below are placeholders - replace with your actual server pins
     */
    fun createSecureOkHttpClient(hostname: String): OkHttpClient {
        val certificatePinner = CertificatePinner.Builder()
            .add(
                hostname,
                // Primary certificate pin (replace with your actual pin)
                "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
                // Backup certificate pin (replace with your actual backup pin)
                "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
            )
            .build()

        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Create OkHttpClient without certificate pinning (for development)
     * WARNING: Only use this in development/testing environments
     */
    fun createUnsecureOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Validate hostname format before creating client
     */
    fun isValidHostname(hostname: String): Boolean {
        val hostnameRegex = Regex("^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$")
        return hostnameRegex.matches(hostname)
    }

    /**
     * Extract hostname from URL
     */
    fun extractHostname(url: String): String? {
        return try {
            val pattern = Regex("^(?:https?://)?([^:/\\s]+)")
            pattern.find(url)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}
