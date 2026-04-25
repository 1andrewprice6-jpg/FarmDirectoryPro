package com.example.farmdirectoryupgraded.vision.vision

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Per-gauge calibration. The user walks through this once per physical gauge
 * (e.g. "shop air compressor pressure", "feed silo level", "coop thermometer").
 *
 * Calibration is stored keyed by gaugeId and reused on every subsequent capture
 * of the same gauge. Without calibration, the detector can still find the
 * needle angle but cannot convert it to a real-world value.
 */
@Serializable
data class GaugeCalibration(
    val gaugeId: String,
    val gaugeLabel: String,
    /** Dial center, normalized 0..1 relative to captured image width/height. */
    val centerXNorm: Double,
    val centerYNorm: Double,
    /** Radius of the dial face, normalized 0..1 relative to image width. */
    val radiusNorm: Double,
    /** Angle (degrees, 0 = right, 90 = up, CCW) at which the needle reads [minValue]. */
    val minAngleDeg: Double,
    val minValue: Double,
    val maxAngleDeg: Double,
    val maxValue: Double,
    val unit: String,
    /**
     * If true, the needle sweeps counter-clockwise from min to max (most gauges
     * are CW, but some pressure gauges are reversed).
     */
    val counterclockwise: Boolean = false,
)

/**
 * Needle detector.
 *
 * Strategy (no OpenCV dependency — pure Kotlin for portability):
 *   1. From dial center, sample pixel luminance along N radial spokes (every 1°).
 *   2. A needle is the spoke with the longest continuous run of dark pixels
 *      starting from the dial center.
 *   3. The angle of that spoke is the needle angle.
 *   4. Linear interpolate angle → value using the calibration's (minAngle, minValue)
 *      and (maxAngle, maxValue) anchors.
 *
 * This is robust against glare (glare is bright, needle is dark) and works for
 * any single-needle gauge. For dual-needle gauges (e.g. combined oil pressure/
 * temp), register each needle as a separate GaugeCalibration with a different
 * angular search window.
 */
class AnalogGaugeDetector {

    data class Reading(
        val needleAngleDeg: Double,
        val value: Double?,
        val unit: String?,
        val confidence: Float,
    )

    fun detect(bitmap: Bitmap, calibration: GaugeCalibration): Reading {
        val w = bitmap.width
        val h = bitmap.height
        val cx = (calibration.centerXNorm * w).toInt()
        val cy = (calibration.centerYNorm * h).toInt()
        val radius = (calibration.radiusNorm * w).toInt().coerceAtLeast(20)

        // Luminance threshold for "dark enough to be needle"
        val darkThreshold = 80

        var bestAngle = 0.0
        var bestScore = 0
        for (degInt in 0 until 360) {
            val rad = Math.toRadians(degInt.toDouble())
            var runLen = 0
            var totalDark = 0
            for (r in 2 until radius) {
                val x = cx + (r * cos(rad)).toInt()
                val y = cy - (r * sin(rad)).toInt()   // screen Y flips
                if (x !in 0 until w || y !in 0 until h) break
                val p = bitmap.getPixel(x, y)
                val lum = (Color.red(p) * 299 + Color.green(p) * 587 + Color.blue(p) * 114) / 1000
                if (lum < darkThreshold) {
                    runLen++
                    totalDark++
                } else if (runLen > 0 && r > radius / 3) {
                    // needle ended; keep running total but stop extending runLen
                    break
                }
            }
            // Scoring favors long contiguous dark runs, penalizes sparse dark specks
            val score = runLen * 2 + totalDark
            if (score > bestScore) {
                bestScore = score
                bestAngle = degInt.toDouble()
            }
        }

        // Confidence ~ how much the best run dominates the 2nd-best.
        // Low score or suspicious near-uniform scores → low confidence.
        val confidence = min(1.0f, max(0.0f, (bestScore - radius * 0.3f) / (radius * 2f)))

        val value = angleToValue(bestAngle, calibration)
        return Reading(bestAngle, value, calibration.unit, confidence)
    }

    private fun angleToValue(angleDeg: Double, cal: GaugeCalibration): Double? {
        val sweep = angularSweep(cal.minAngleDeg, cal.maxAngleDeg, cal.counterclockwise)
        val progressAngle = angularSweep(cal.minAngleDeg, angleDeg, cal.counterclockwise)
        if (sweep <= 0.0) return null
        val t = (progressAngle / sweep).coerceIn(0.0, 1.0)
        return cal.minValue + t * (cal.maxValue - cal.minValue)
    }

    /** Distance in degrees from [from] to [to], going either CW or CCW, always positive. */
    private fun angularSweep(from: Double, to: Double, ccw: Boolean): Double {
        val d = if (ccw) to - from else from - to
        return ((d % 360.0) + 360.0) % 360.0
    }

    private fun max(a: Float, b: Float) = if (a > b) a else b
    private fun min(a: Float, b: Float) = if (a < b) a else b
}
