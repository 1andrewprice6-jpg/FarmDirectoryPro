package com.example.farmdirectoryupgraded.data

/**
 * Structured representation of a haul log's details field.
 *
 * The details string in a haul [LogEntry] is encoded as:
 *   "Truck:<id>|<name>;Trailer:<id>|<name>;Destination:<name>;Farm:<name>"
 *
 * Use [HaulLogDetails.encode] to create the details string and
 * [HaulLogDetails.parse] to decode it.
 */
data class HaulLogDetails(
    val truckId: String = "",
    val truckName: String = "",
    val trailerId: String = "",
    val trailerName: String = "",
    val destination: String = "",
    val farmName: String = ""
) {
    /** Returns the encoded details string suitable for storage in [LogEntry.details]. */
    fun encode(): String =
        "$KEY_TRUCK:$truckId|$truckName;$KEY_TRAILER:$trailerId|$trailerName;" +
            "$KEY_DESTINATION:$destination;$KEY_FARM:$farmName"

    companion object {
        const val KEY_TRUCK = "Truck"
        const val KEY_TRAILER = "Trailer"
        const val KEY_DESTINATION = "Destination"
        const val KEY_FARM = "Farm"

        /** Marker used to detect whether a [LogEntry.details] string contains haul data. */
        const val HAUL_MARKER = "$KEY_TRUCK:"

        /**
         * Parses a [LogEntry.details] string into a [HaulLogDetails].
         * Returns null if the string does not appear to be haul data.
         */
        fun parse(details: String): HaulLogDetails? {
            if (!details.contains(HAUL_MARKER)) return null
            val parts = details.split(";").associate { segment ->
                val idx = segment.indexOf(':')
                if (idx >= 0) segment.substring(0, idx) to segment.substring(idx + 1)
                else segment to ""
            }
            fun idPart(raw: String) = raw.substringBefore('|').trim()
            fun namePart(raw: String) = raw.substringAfter('|', "").trim()
            val truck = parts[KEY_TRUCK] ?: ""
            val trailer = parts[KEY_TRAILER] ?: ""
            return HaulLogDetails(
                truckId = idPart(truck),
                truckName = namePart(truck),
                trailerId = idPart(trailer),
                trailerName = namePart(trailer),
                destination = parts[KEY_DESTINATION] ?: "",
                farmName = parts[KEY_FARM] ?: ""
            )
        }
    }
}
