package com.example.farmdirectoryupgraded.vision.ledger

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Resolves a capture's extracted FarmId + GPS coords to the canonical
 * Farm record in the 147-farm master directory.
 *
 * Matching strategy (strongest evidence wins):
 *   1. Exact Farm ID match (from explicit "FARM #1234" OCR).
 *   2. GPS within [gpsRadiusMeters] of a registered farm location.
 *   3. Fuzzy Farm ID match (1 digit transposition tolerated) if only one
 *      candidate is within 2× [gpsRadiusMeters].
 *   4. No match → returns null; upstream stores farmId as the raw extracted
 *      string and flags the capture for user linkage.
 *
 * The FarmLookup interface is intentionally minimal so this module can
 * plug into FarmDirectoryPro's existing Farm table without importing its DAO.
 */
interface FarmLookup {
    data class Farm(
        val id: String,
        val label: String,
        val latitude: Double,
        val longitude: Double,
    )

    /** All farms — the directory is ~147 entries so no pagination needed. */
    suspend fun all(): List<Farm>

    /** Exact ID match. */
    suspend fun byId(id: String): Farm?
}

class FarmGridMapper(
    private val lookup: FarmLookup,
    private val gpsRadiusMeters: Double = 150.0,
) {

    data class Match(
        val farm: FarmLookup.Farm,
        val method: String,
        val confidence: Float,
    )

    suspend fun resolve(
        extractedFarmId: String?,
        latitude: Double?,
        longitude: Double?,
    ): Match? {
        if (!extractedFarmId.isNullOrBlank()) {
            lookup.byId(extractedFarmId)?.let {
                return Match(it, method = "ID_EXACT", confidence = 1.0f)
            }
        }

        val all = lookup.all()

        if (latitude != null && longitude != null) {
            val nearest = all
                .map { it to haversine(latitude, longitude, it.latitude, it.longitude) }
                .minByOrNull { it.second }

            if (nearest != null && nearest.second <= gpsRadiusMeters) {
                return Match(
                    farm = nearest.first,
                    method = "GPS_WITHIN_${gpsRadiusMeters.toInt()}M",
                    confidence = (1.0f - (nearest.second / gpsRadiusMeters).toFloat()).coerceIn(0f, 1f),
                )
            }

            // GPS + fuzzy ID
            if (!extractedFarmId.isNullOrBlank()) {
                val widenedCandidates = all.filter {
                    haversine(latitude, longitude, it.latitude, it.longitude) <= gpsRadiusMeters * 2
                }
                val fuzzyMatches = widenedCandidates.filter { levenshtein(it.id, extractedFarmId) <= 1 }
                if (fuzzyMatches.size == 1) {
                    return Match(fuzzyMatches.first(), method = "ID_FUZZY+GPS", confidence = 0.75f)
                }
            }
        }
        return null
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_008.8
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * asin(sqrt(a))
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)
        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(curr[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
            }
            System.arraycopy(curr, 0, prev, 0, prev.size)
        }
        return prev[b.length]
    }
}
