package com.example.farmdirectoryupgraded.vision.parsing

import com.example.farmdirectoryupgraded.vision.capture.CaptureMode
import com.example.farmdirectoryupgraded.vision.capture.ParsedFields

/**
 * Converts raw OCR text → structured ParsedFields, using the capture mode as
 * a bias. The same number ("14523") is interpreted very differently whether
 * the user aimed at their dashboard (odometer) or the chicken house wall
 * (temperature × 100, or a farm ID).
 *
 * The classifier is rule-based and deterministic — every decision it makes
 * can be traced back to a specific regex or keyword. That auditability is
 * more important here than accuracy on edge cases, because the user ALWAYS
 * reviews before committing (CaptureReviewScreen).
 */
object CategoryClassifier {

    fun classify(mode: CaptureMode, rawText: String): ParsedFields = when (mode) {
        CaptureMode.DASHBOARD_ODOMETER -> classifyDashboard(rawText)
        CaptureMode.FUEL_PUMP          -> classifyFuelPump(rawText)
        CaptureMode.ANALOG_GAUGE       -> classifyAnalogGauge(rawText)
        CaptureMode.CHICKEN_HOUSE_LOG  -> classifyChickenHouse(rawText)
        CaptureMode.GENERIC_LOG        -> ParsedFields(rawText = rawText, categories = listOf("GENERIC"))
    }

    // ─────────────────────── DASHBOARD / ODOMETER ──────────────────────────
    private fun classifyDashboard(text: String): ParsedFields {
        val odo = NumericExtractor.firstOdometer(text)
        val farm = tryFarmIdContext(text)
        val categories = buildList {
            if (odo != null) add("ODOMETER")
            if (farm != null) add("FARM_ID")
        }
        return ParsedFields(
            odometerMiles = odo,
            farmId = farm,
            rawText = text,
            categories = categories,
        )
    }

    // ─────────────────────── FUEL PUMP ─────────────────────────────────────
    private fun classifyFuelPump(text: String): ParsedFields {
        val cost = NumericExtractor.firstCurrency(text)
            ?: findLabeled(text, listOf("TOTAL", "SALE", "AMOUNT"))
        val gal = NumericExtractor.firstGallons(text)
            ?: findLabeled(text, listOf("GALLONS", "GAL", "VOLUME"))
        val ppg = NumericExtractor.firstPricePerGallon(text)
            ?: findLabeled(text, listOf("PRICE", "/GAL", "PPG"))

        // Cross-validate: cost ≈ gal × ppg. If two are present and the third
        // isn't, derive it. If all three are present but wildly inconsistent,
        // flag with a category and let the validation gate handle.
        val (finalCost, finalGal, finalPpg) = reconcileFuelTriplet(cost, gal, ppg)

        val categories = buildList {
            add("FUEL_PUMP")
            if (finalCost != null) add("COST")
            if (finalGal != null) add("GALLONS")
            if (finalPpg != null) add("PRICE_PER_GAL")
            if (cost != null && gal != null && ppg != null) {
                val expected = gal * ppg
                if (kotlin.math.abs(expected - cost) > 0.05) add("TRIPLET_MISMATCH")
            }
        }
        return ParsedFields(
            totalCost = finalCost,
            gallons = finalGal,
            pricePerGallon = finalPpg,
            rawText = text,
            categories = categories,
        )
    }

    private fun reconcileFuelTriplet(
        cost: Double?, gal: Double?, ppg: Double?,
    ): Triple<Double?, Double?, Double?> {
        return when {
            cost != null && gal != null && ppg == null && gal > 0 ->
                Triple(cost, gal, kotlin.math.round(cost / gal * 1000) / 1000)
            cost != null && ppg != null && gal == null && ppg > 0 ->
                Triple(cost, kotlin.math.round(cost / ppg * 1000) / 1000, ppg)
            gal != null && ppg != null && cost == null ->
                Triple(kotlin.math.round(gal * ppg * 100) / 100, gal, ppg)
            else -> Triple(cost, gal, ppg)
        }
    }

    // ─────────────────────── ANALOG GAUGE ──────────────────────────────────
    private fun classifyAnalogGauge(text: String): ParsedFields {
        // Text OCR on an analog gauge usually only captures the scale labels
        // ("0", "50", "100"), not the reading itself. The AnalogGaugeDetector
        // provides the numeric reading separately; this classifier only parses
        // any unit annotation the camera happened to capture.
        val unit = Regex("""\b(PSI|BAR|°?F|°?C|PPM|%|GAL|KPA|MPH|RPM|HZ)\b""",
            RegexOption.IGNORE_CASE).find(text)?.value?.uppercase()
        return ParsedFields(
            gaugeUnit = unit,
            rawText = text,
            categories = buildList {
                add("ANALOG_GAUGE")
                if (unit != null) add("UNIT_$unit")
            },
        )
    }

    // ─────────────────────── CHICKEN HOUSE LOG ─────────────────────────────
    private fun classifyChickenHouse(text: String): ParsedFields {
        val date = NumericExtractor.firstDateIso(text)
        val farm = tryFarmIdContext(text)
        val metrics = extractChickenHouseMetrics(text)
        val categories = buildList {
            add("CHICKEN_HOUSE")
            if (date != null) add("DATE")
            if (farm != null) add("FARM_ID")
            metrics.keys.forEach { add("METRIC_${it.uppercase()}") }
        }
        return ParsedFields(
            dateIso = date,
            farmId = farm,
            metrics = metrics,
            rawText = text,
            categories = categories,
        )
    }

    /**
     * Keyword-based extraction for common poultry-house metrics. Operates on
     * the *concatenated* OCR text — the HandwrittenLogDetector handles proper
     * tabular structure separately.
     */
    private fun extractChickenHouseMetrics(text: String): Map<String, Double> {
        val metrics = mutableMapOf<String, Double>()
        val patterns = mapOf(
            "mortality"   to Regex("""(?:MORT(?:ALITY)?|DEAD|DEAD\s+BIRDS?)\s*:?\s*(\d+)""", RegexOption.IGNORE_CASE),
            "feed_lb"     to Regex("""FEED\s*:?\s*(\d+(?:\.\d+)?)\s*(?:LB|LBS|POUNDS?)?""", RegexOption.IGNORE_CASE),
            "water_gal"   to Regex("""WATER\s*:?\s*(\d+(?:\.\d+)?)\s*(?:GAL|G)?""", RegexOption.IGNORE_CASE),
            "temp_f"      to Regex("""TEMP(?:ERATURE)?\s*:?\s*(\d+(?:\.\d+)?)\s*°?F?""", RegexOption.IGNORE_CASE),
            "weight_lb"   to Regex("""(?:AVG\s*)?(?:WT|WEIGHT)\s*:?\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE),
            "humidity"    to Regex("""HUMID(?:ITY)?\s*:?\s*(\d+(?:\.\d+)?)\s*%?""", RegexOption.IGNORE_CASE),
            "age_days"    to Regex("""AGE\s*:?\s*(\d+)\s*(?:D(?:AYS?)?)?""", RegexOption.IGNORE_CASE),
        )
        patterns.forEach { (key, regex) ->
            regex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()?.let {
                metrics[key] = it
            }
        }
        return metrics
    }

    // ─────────────────────── Helpers ───────────────────────────────────────
    private fun findLabeled(text: String, labels: List<String>): Double? {
        for (label in labels) {
            val regex = Regex("""${Regex.escape(label)}\s*:?\s*\$?\s*(\d+(?:[,.]\d+)?)""",
                RegexOption.IGNORE_CASE)
            regex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()?.let { return it }
        }
        return null
    }

    /** Look for explicit "FARM ID: 1234", "FARM #1234", "HOUSE 7", etc. */
    private fun tryFarmIdContext(text: String): String? {
        val explicit = Regex("""(?:FARM|HOUSE|SITE)\s*(?:ID|#|NO\.?)?\s*:?\s*(\d{1,4})""",
            RegexOption.IGNORE_CASE).find(text)
        if (explicit != null) return explicit.groupValues[1]
        return null
    }
}
