package com.farmdirectory.pro.vision.sync

import com.farmdirectory.pro.vision.ledger.CaptureEntity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Talks to the backend's POST /api/captures multipart endpoint.
 * Stateless — pass the auth token if your API requires one.
 */
class CaptureSyncClient(
    private val baseUrl: String,
    private val authToken: String? = null,
    private val httpClient: OkHttpClient = defaultClient(),
) {

    /**
     * Uploads one capture. Returns the server-assigned ID on success.
     * Throws on non-2xx response or network failure.
     */
    suspend fun upload(capture: CaptureEntity): Long {
        val imageFile = File(capture.rawImagePath)
        if (!imageFile.exists()) {
            throw IllegalStateException("Raw image missing on disk: ${capture.rawImagePath}")
        }

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                imageFile.name,
                imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull()),
            )
            .addFormDataPart("mode", capture.mode)
            .addFormDataPart("status", capture.status)
            .addFormDataPart("capturedAtEpochMs", capture.capturedAtEpochMs.toString())
            .addFormDataPart("parsedFields", capture.parsedFieldsJson)
            .addFormDataPart("linkedEntities", capture.linkedEntitiesJson)
            .also {
                if (capture.farmId != null)    it.addFormDataPart("farmId", capture.farmId)
                if (capture.latitude != null)  it.addFormDataPart("latitude", capture.latitude.toString())
                if (capture.longitude != null) it.addFormDataPart("longitude", capture.longitude.toString())
            }
            .build()

        val reqBuilder = Request.Builder()
            .url("$baseUrl/api/captures")
            .post(body)
        if (authToken != null) reqBuilder.header("Authorization", "Bearer $authToken")

        httpClient.newCall(reqBuilder.build()).execute().use { resp ->
            if (!resp.isSuccessful) {
                val errBody = resp.body?.string()?.take(500) ?: ""
                throw RuntimeException("Sync HTTP ${resp.code}: $errBody")
            }
            val json = resp.body?.string() ?: throw RuntimeException("Empty body")
            // Minimal parse: looking for `"id":N`
            val match = Regex(""""id"\s*:\s*(\d+)""").find(json)
            return match?.groupValues?.get(1)?.toLong()
                ?: throw RuntimeException("Server response missing id: $json")
        }
    }

    companion object {
        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        // Provided for tests / manual JSON builds
        @Suppress("unused")
        private fun emptyJson() = "{}".toRequestBody("application/json".toMediaTypeOrNull())
    }
}
