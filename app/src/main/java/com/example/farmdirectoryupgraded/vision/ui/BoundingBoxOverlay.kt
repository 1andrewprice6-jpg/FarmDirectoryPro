package com.example.farmdirectoryupgraded.vision.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.farmdirectoryupgraded.vision.capture.DetectedRegion

/**
 * Paints colored bounding boxes over the camera preview in real time.
 * Color coding:
 *   - cyan    : digit cluster (likely capturable numeric)
 *   - amber   : handwritten block (tabular log candidate)
 *   - muted   : generic text
 */
@Composable
fun BoundingBoxOverlay(
    regions: List<DetectedRegion>,
    sourceWidth: Int,
    sourceHeight: Int,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        if (sourceWidth == 0 || sourceHeight == 0) return@Canvas
        val sx = size.width / sourceWidth.toFloat()
        val sy = size.height / sourceHeight.toFloat()
        for (r in regions) {
            val color = when (r.kind) {
                DetectedRegion.Kind.DIGIT_CLUSTER     -> Color(0xFF6EE7FF)
                DetectedRegion.Kind.HANDWRITTEN_BLOCK -> Color(0xFFFFB86E)
                DetectedRegion.Kind.GAUGE_NEEDLE      -> Color(0xFFFF6E7F)
                DetectedRegion.Kind.TABLE_CELL        -> Color(0xFF6EFFB0)
                else                                  -> Color(0xFF8B94A3)
            }
            drawRect(
                color = color,
                topLeft = Offset(r.left * sx, r.top * sy),
                size = Size((r.right - r.left) * sx, (r.bottom - r.top) * sy),
                style = Stroke(width = 3f),
            )
        }
    }
}
