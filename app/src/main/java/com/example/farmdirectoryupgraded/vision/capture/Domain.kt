package com.example.farmdirectoryupgraded.vision.capture

import android.graphics.Rect
import kotlinx.serialization.Serializable

/**
 * A single frame from the camera surfaced to the analysis pipeline.
 * Held in memory only; committed frames are persisted as JPEG on disk.
 */
data class CapturedFrame(
    val jpegBytes: ByteArray,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val capturedAtEpochMs: Long,
    /** GPS at the moment of capture, null if location permission denied/unavailable. */
    val latitude: Double? = null,
    val longitude: Double? = null,
) {
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(this)
}

/**
 * A rectangle the pipeline has identified as meaningful content —
 * a digit cluster, a gauge dial, a table row.
 */
@Serializable
data class DetectedRegion(
    val kind: Kind,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val rawText: String,
    val confidence: Float,
) {
    enum class Kind { DIGIT_CLUSTER, GAUGE_DIAL, GAUGE_NEEDLE, TABLE_CELL, HANDWRITTEN_BLOCK, GENERIC_TEXT }

    fun toAndroidRect(): Rect = Rect(left, top, right, bottom)

    companion object {
        fun from(r: Rect, kind: Kind, rawText: String, confidence: Float): DetectedRegion =
            DetectedRegion(kind, r.left, r.top, r.right, r.bottom, rawText, confidence)
    }
}

/**
 * The structured output of Layer 1 — every field the classifier managed to
 * extract from the frame's text. All fields are nullable; the validation
 * gate decides whether the subset present is sufficient.
 */
@Serializable
data class ParsedFields(
    val odometerMiles: Int? = null,
    val totalCost: Double? = null,
    val gallons: Double? = null,
    val pricePerGallon: Double? = null,
    val gaugeValue: Double? = null,
    val gaugeUnit: String? = null,
    val farmId: String? = null,
    val dateIso: String? = null,
    /** Chicken-house tabular metrics: column-name → value, e.g. {"mortality"→3.0, "feed_lb"→1450.0} */
    val metrics: Map<String, Double> = emptyMap(),
    /** Full concatenated raw text, preserved for audit even when structured fields are empty. */
    val rawText: String = "",
    /** Category labels assigned by CategoryClassifier, for UI badges. */
    val categories: List<String> = emptyList(),
) {
    val isEmpty: Boolean get() =
        odometerMiles == null && totalCost == null && gallons == null &&
        gaugeValue == null && farmId == null && dateIso == null &&
        metrics.isEmpty() && rawText.isBlank()

    fun mergeOver(other: ParsedFields): ParsedFields = ParsedFields(
        odometerMiles   = odometerMiles   ?: other.odometerMiles,
        totalCost       = totalCost       ?: other.totalCost,
        gallons         = gallons         ?: other.gallons,
        pricePerGallon  = pricePerGallon  ?: other.pricePerGallon,
        gaugeValue      = gaugeValue      ?: other.gaugeValue,
        gaugeUnit       = gaugeUnit       ?: other.gaugeUnit,
        farmId          = farmId          ?: other.farmId,
        dateIso         = dateIso         ?: other.dateIso,
        metrics         = other.metrics + metrics,
        rawText         = listOf(rawText, other.rawText).filter { it.isNotBlank() }.joinToString("\n"),
        categories      = (categories + other.categories).distinct(),
    )
}
