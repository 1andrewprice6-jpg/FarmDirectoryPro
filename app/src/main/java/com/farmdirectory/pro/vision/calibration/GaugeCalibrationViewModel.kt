package com.farmdirectory.pro.vision.calibration

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farmdirectory.pro.vision.vision.GaugeCalibration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Walks the user through a 4-tap calibration:
 *   1. Tap dial center
 *   2. Tap the OUTER edge of the dial (sets radius)
 *   3. Tap the needle TIP at the known minimum value
 *   4. Tap the needle TIP at the known maximum value
 *
 * After step 4 with min/max numeric values entered and unit chosen,
 * a [GaugeCalibration] is built and persisted.
 */
class GaugeCalibrationViewModel(
    app: Application,
    private val dao: GaugeCalibrationDao,
) : AndroidViewModel(app) {

    enum class Step { LOAD_PHOTO, CENTER, RADIUS, MIN_NEEDLE, MAX_NEEDLE, METADATA, DONE }

    data class TapPoint(val x: Float, val y: Float)

    data class State(
        val step: Step = Step.LOAD_PHOTO,
        val bitmap: Bitmap? = null,
        val center: TapPoint? = null,
        val radiusEdge: TapPoint? = null,
        val minNeedleTip: TapPoint? = null,
        val maxNeedleTip: TapPoint? = null,
        val gaugeId: String = "",
        val gaugeLabel: String = "",
        val minValue: String = "",
        val maxValue: String = "",
        val unit: String = "PSI",
        val counterclockwise: Boolean = false,
        val saved: Boolean = false,
        val error: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    fun loadPhoto(jpegBytes: ByteArray) {
        val bmp = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
        if (bmp == null) {
            _state.update { it.copy(error = "Could not decode image") }
            return
        }
        _state.update { it.copy(bitmap = bmp, step = Step.CENTER, error = null) }
    }

    fun loadPhotoBitmap(bitmap: Bitmap) {
        _state.update { it.copy(bitmap = bitmap, step = Step.CENTER, error = null) }
    }

    /** Coordinates in image-space (0..width, 0..height), not view-space. */
    fun onTap(x: Float, y: Float) {
        _state.update { s ->
            when (s.step) {
                Step.CENTER     -> s.copy(center = TapPoint(x, y), step = Step.RADIUS)
                Step.RADIUS     -> s.copy(radiusEdge = TapPoint(x, y), step = Step.MIN_NEEDLE)
                Step.MIN_NEEDLE -> s.copy(minNeedleTip = TapPoint(x, y), step = Step.MAX_NEEDLE)
                Step.MAX_NEEDLE -> s.copy(maxNeedleTip = TapPoint(x, y), step = Step.METADATA)
                else -> s
            }
        }
    }

    fun back() {
        _state.update { s ->
            when (s.step) {
                Step.RADIUS     -> s.copy(center = null, step = Step.CENTER)
                Step.MIN_NEEDLE -> s.copy(radiusEdge = null, step = Step.RADIUS)
                Step.MAX_NEEDLE -> s.copy(minNeedleTip = null, step = Step.MIN_NEEDLE)
                Step.METADATA   -> s.copy(maxNeedleTip = null, step = Step.MAX_NEEDLE)
                else -> s
            }
        }
    }

    fun setGaugeId(v: String) = _state.update { it.copy(gaugeId = v) }
    fun setGaugeLabel(v: String) = _state.update { it.copy(gaugeLabel = v) }
    fun setMinValue(v: String) = _state.update { it.copy(minValue = v) }
    fun setMaxValue(v: String) = _state.update { it.copy(maxValue = v) }
    fun setUnit(v: String) = _state.update { it.copy(unit = v) }
    fun setCounterclockwise(v: Boolean) = _state.update { it.copy(counterclockwise = v) }

    fun save() {
        val s = _state.value
        val bmp = s.bitmap
        val center = s.center
        val radiusEdge = s.radiusEdge
        val minTip = s.minNeedleTip
        val maxTip = s.maxNeedleTip
        val minVal = s.minValue.toDoubleOrNull()
        val maxVal = s.maxValue.toDoubleOrNull()

        if (bmp == null || center == null || radiusEdge == null || minTip == null || maxTip == null) {
            _state.update { it.copy(error = "Complete all 4 taps first") }
            return
        }
        if (s.gaugeId.isBlank()) {
            _state.update { it.copy(error = "Gauge ID required (e.g. shop_air)") }
            return
        }
        if (minVal == null || maxVal == null || minVal == maxVal) {
            _state.update { it.copy(error = "Enter distinct min and max values") }
            return
        }

        val w = bmp.width.toDouble()
        val h = bmp.height.toDouble()
        val radiusPx = hypot(
            (radiusEdge.x - center.x).toDouble(),
            (radiusEdge.y - center.y).toDouble(),
        )
        val minAngle = angleDegFromCenter(center, minTip)
        val maxAngle = angleDegFromCenter(center, maxTip)

        val cal = GaugeCalibration(
            gaugeId = s.gaugeId.trim(),
            gaugeLabel = s.gaugeLabel.ifBlank { s.gaugeId.trim() },
            centerXNorm = center.x / w,
            centerYNorm = center.y / h,
            radiusNorm = radiusPx / w,
            minAngleDeg = minAngle,
            minValue = minVal,
            maxAngleDeg = maxAngle,
            maxValue = maxVal,
            unit = s.unit,
            counterclockwise = s.counterclockwise,
        )

        viewModelScope.launch {
            dao.upsert(GaugeCalibrationEntity.fromDomain(cal))
            _state.update { it.copy(saved = true, step = Step.DONE) }
        }
    }

    fun reset() = _state.update { State() }

    /** Angle in degrees, with 0° = right, 90° = up (screen Y inverted). */
    private fun angleDegFromCenter(center: TapPoint, tip: TapPoint): Double {
        val dx = (tip.x - center.x).toDouble()
        val dy = (center.y - tip.y).toDouble()  // invert: screen Y grows downward
        var deg = Math.toDegrees(atan2(dy, dx))
        if (deg < 0) deg += 360.0
        return deg
    }
}
