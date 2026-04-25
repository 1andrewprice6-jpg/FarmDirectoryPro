package com.farmdirectory.pro.vision.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.farmdirectory.pro.data.AppDatabase
import java.util.concurrent.TimeUnit

/**
 * WorkManager job that uploads any captures with [CaptureEntity.syncedAt] = null.
 *
 * Schedule from your Application.onCreate():
 *   CaptureSyncWorker.schedulePeriodic(context)
 *
 * Or fire one immediately after a successful capture:
 *   CaptureSyncWorker.enqueueImmediate(context)
 *
 * Constraints: requires network. Backs off exponentially on failure (default
 * 30s → 5min cap). Per-row failures don't fail the whole job; only no-network
 * triggers a retry.
 */
class CaptureSyncWorker(
    ctx: Context,
    params: WorkerParameters,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val baseUrl = inputData.getString(KEY_BASE_URL) ?: SyncConfig.baseUrl
        val token = inputData.getString(KEY_TOKEN) ?: SyncConfig.authToken
        if (baseUrl.isNullOrBlank()) {
            // No backend configured — completed successfully (nothing to do)
            return Result.success()
        }

        val dao = AppDatabase.getInstance(applicationContext).captureDao()
        val pending = dao.getUnsynced()
        if (pending.isEmpty()) return Result.success()

        val client = CaptureSyncClient(baseUrl = baseUrl, authToken = token)

        var anyTransient = false
        var anySuccess = false

        for (capture in pending) {
            val outcome = runCatching { client.upload(capture) }
            outcome
                .onSuccess {
                    dao.update(capture.copy(syncedAt = System.currentTimeMillis()))
                    anySuccess = true
                }
                .onFailure { t ->
                    if (isTransient(t)) anyTransient = true
                    // Non-transient (e.g. 400 schema mismatch) is logged but not retried
                    // forever — the row stays unsynced and the user can review it
                    // in the History tab.
                }
        }

        return when {
            anyTransient && !anySuccess -> Result.retry()
            anyTransient && anySuccess -> Result.success()  // partial; next run picks up the rest
            else -> Result.success()
        }
    }

    private fun isTransient(t: Throwable): Boolean {
        // Network exceptions, 5xx, timeouts — anything where retry might help
        val msg = t.message ?: return true
        return msg.contains("HTTP 5", ignoreCase = true) ||
                msg.contains("timeout", ignoreCase = true) ||
                msg.contains("ECONNRESET", ignoreCase = true) ||
                msg.contains("UnknownHost", ignoreCase = true) ||
                t is java.io.IOException
    }

    companion object {
        const val KEY_BASE_URL = "baseUrl"
        const val KEY_TOKEN    = "authToken"
        private const val UNIQUE_PERIODIC  = "capture_sync_periodic"
        private const val UNIQUE_IMMEDIATE = "capture_sync_immediate"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val req = PeriodicWorkRequestBuilder<CaptureSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                req,
            )
        }

        fun enqueueImmediate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val req = OneTimeWorkRequestBuilder<CaptureSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_IMMEDIATE,
                ExistingWorkPolicy.KEEP,
                req,
            )
        }
    }
}

/**
 * Holder for backend URL + token. Set once at app boot from your config or DI.
 * Kept as a simple object instead of DI-injected to keep the worker constructor
 * compatible with WorkManager's default factory.
 */
object SyncConfig {
    @Volatile var baseUrl: String? = null
    @Volatile var authToken: String? = null
}
