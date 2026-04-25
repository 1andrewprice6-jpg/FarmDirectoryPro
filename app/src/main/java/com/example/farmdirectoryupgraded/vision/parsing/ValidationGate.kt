package com.example.farmdirectoryupgraded.vision.parsing

import com.example.farmdirectoryupgraded.vision.capture.CaptureMode
import com.example.farmdirectoryupgraded.vision.capture.ParsedFields

/**
 * The "Logic Gate." A capture is either:
 *
 *   - COMPLETE: all required fields for its mode are present and self-consistent.
 *     Safe to propagate to downstream logs without user intervention.
 *
 *   - INCOMPLETE: one or more required fields missing. The user must fill in
 *     the gaps on the CaptureReviewScreen before commit.
 *
 *   - INCONSISTENT: fields present but contradict each other (e.g. fuel pump
 *     cost ≠ gallons × price-per-gallon). User must confirm which field(s)
 *     to trust.
 *
 *   - REJECTED: nothing useful extracted (blank frame, unreadable, not the
 *     expected asset). Raw image is archived but no log entries are created.
 *
 * The gate itself does not mutate state — it returns a [Decision] that the
 * repository consumes.
 */
object ValidationGate {

    sealed class Decision {
        object Complete : Decision()
        data class Incomplete(val missing: List<String>, val hints: List<String>) : Decision()
        data class Inconsistent(val issues: List<String>) : Decision()
        data class Rejected(val reason: String) : Decision()
    }

    fun evaluate(mode: CaptureMode, fields: ParsedFields): Decision {
        if (fields.isEmpty) return Decision.Rejected("No text or values extracted")

        val missing = mode.requiredFields.filter { !isFieldPresent(it, fields) }
        val issues = mutableListOf<String>()

        // Cross-field consistency checks
        if (mode == CaptureMode.FUEL_PUMP) {
            val c = fields.totalCost
            val g = fields.gallons
            val p = fields.pricePerGallon
            if (c != null && g != null && p != null) {
                val expected = g * p
                if (kotlin.math.abs(expected - c) > 0.05) {
                    issues += "Cost $%.2f ≠ gallons %.3f × $%.3f/gal = $%.2f"
                        .format(c, g, p, expected)
                }
            }
            // Physical plausibility
            if (g != null && (g !in 0.1..200.0)) issues += "Gallons $g outside plausible 0.1–200 range"
            if (p != null && (p !in 1.0..10.0)) issues += "Price/gal $p outside plausible $1–$10 range"
        }

        if (mode == CaptureMode.DASHBOARD_ODOMETER) {
            val o = fields.odometerMiles
            if (o != null && o !in 1..9_999_999) {
                issues += "Odometer $o outside 1–9,999,999 range"
            }
        }

        return when {
            issues.isNotEmpty() -> Decision.Inconsistent(issues)
            missing.isNotEmpty() -> Decision.Incomplete(
                missing = missing,
                hints = missing.map { hintFor(it, mode) },
            )
            else -> Decision.Complete
        }
    }

    private fun isFieldPresent(name: String, f: ParsedFields): Boolean = when (name) {
        "odometerMiles"   -> f.odometerMiles != null
        "totalCost"       -> f.totalCost != null
        "gallons"         -> f.gallons != null
        "pricePerGallon"  -> f.pricePerGallon != null
        "gaugeValue"      -> f.gaugeValue != null
        "farmId"          -> f.farmId != null
        "date"            -> f.dateIso != null
        "metrics"         -> f.metrics.isNotEmpty()
        else -> false
    }

    private fun hintFor(field: String, mode: CaptureMode): String = when (field) {
        "odometerMiles"  -> "Re-aim at the mileage readout; ensure no glare covers digits."
        "totalCost"      -> "Capture the TOTAL $ line on the pump display."
        "gallons"        -> "Capture the GALLONS line on the pump display."
        "pricePerGallon" -> "Capture the PRICE/GAL line on the pump display."
        "gaugeValue"     -> "Ensure gauge is calibrated; needle must be unobstructed."
        "date"           -> "Log sheet header usually shows the date at top-right."
        "farmId"         -> "Farm ID is usually printed on the silo or at the log-sheet top."
        else -> "Provide $field manually before committing."
    }
}
