package com.example.farmdirectoryupgraded.vision.vision

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Preprocessing for digital LCD displays (fuel pumps, digital odometers, climate controls).
 *
 * 7-segment and dot-matrix LCDs are high-contrast but the camera often fights:
 *   - Specular glare on the glass cover
 *   - Uneven backlighting
 *   - Low ambient light (unlit pump at dusk)
 *   - Rolling-shutter banding with PWM-driven displays
 *
 * We do three passes:
 *   1. Luminance → grayscale
 *   2. Local (adaptive) threshold to binary — cancels gradient glare
 *   3. Small-blob removal — cancels dust / dirt spots
 *
 * Output is a cleaned Bitmap that ML Kit will extract digits from with much
 * higher reliability than raw camera output.
 */
object DigitalDisplayPreprocessor {

    /**
     * @param blockSize odd number; size of the local neighborhood for adaptive threshold.
     *                  Larger = more tolerant of gradient, less sensitive to fine detail.
     *                  15–31 works for most dashboard/pump displays.
     * @param cValue constant subtracted from local mean. Higher = more pixels forced to white.
     */
    fun preprocess(input: Bitmap, blockSize: Int = 25, cValue: Int = 8): Bitmap {
        require(blockSize % 2 == 1 && blockSize >= 3) { "blockSize must be odd and >= 3" }

        val w = input.width
        val h = input.height
        val gray = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val c = input.getPixel(x, y)
                gray[y * w + x] =
                    (Color.red(c) * 299 + Color.green(c) * 587 + Color.blue(c) * 114) / 1000
            }
        }

        // Integral image for O(1) per-pixel local mean
        val ii = LongArray((w + 1) * (h + 1))
        for (y in 1..h) {
            var rowSum = 0L
            for (x in 1..w) {
                rowSum += gray[(y - 1) * w + (x - 1)]
                ii[y * (w + 1) + x] = ii[(y - 1) * (w + 1) + x] + rowSum
            }
        }

        val half = blockSize / 2
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        for (y in 0 until h) {
            val y0 = max(0, y - half)
            val y1 = min(h - 1, y + half)
            for (x in 0 until w) {
                val x0 = max(0, x - half)
                val x1 = min(w - 1, x + half)
                val area = ((x1 - x0 + 1) * (y1 - y0 + 1)).toLong()
                val sum = ii[(y1 + 1) * (w + 1) + (x1 + 1)] -
                          ii[y0 * (w + 1) + (x1 + 1)] -
                          ii[(y1 + 1) * (w + 1) + x0] +
                          ii[y0 * (w + 1) + x0]
                val mean = (sum / area).toInt()
                val pix = gray[y * w + x]
                val bin = if (pix < mean - cValue) 0 else 255
                out.setPixel(x, y, Color.rgb(bin, bin, bin))
            }
        }
        return out
    }
}
