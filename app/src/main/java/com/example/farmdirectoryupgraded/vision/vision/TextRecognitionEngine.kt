package com.example.farmdirectoryupgraded.vision.vision

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.farmdirectoryupgraded.vision.capture.CapturedFrame
import com.example.farmdirectoryupgraded.vision.capture.DetectedRegion
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Thin wrapper around ML Kit's on-device Latin text recognizer.
 *
 * ML Kit v2 handles:
 *   - Printed text (any font, any size ≥ ~10px)
 *   - 7-segment LCD displays (fuel pumps, digital odometers) — surprisingly well
 *   - Handwritten digits and block print — reasonably well
 *   - Handwritten cursive — unreliable (the model is trained primarily on print)
 *
 * All processing is on-device. No network, no API key, no per-call cost.
 */
class TextRecognitionEngine(
    private val recognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS),
) {

    data class Result(
        val fullText: String,
        val regions: List<DetectedRegion>,
    )

    suspend fun recognize(frame: CapturedFrame): Result {
        val image = InputImage.fromByteArray(
            frame.jpegBytes,
            frame.width,
            frame.height,
            frame.rotationDegrees,
            InputImage.IMAGE_FORMAT_NV21,
        )
        return runMlKit(image)
    }

    suspend fun recognize(bitmap: Bitmap, rotationDegrees: Int = 0): Result {
        val image = InputImage.fromBitmap(bitmap, rotationDegrees)
        return runMlKit(image)
    }

    /** Convenience for captured JPEG bytes (e.g. loaded from file). */
    suspend fun recognizeJpeg(jpegBytes: ByteArray, rotationDegrees: Int = 0): Result {
        val bmp = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
            ?: throw IllegalArgumentException("Could not decode JPEG")
        return recognize(bmp, rotationDegrees)
    }

    private suspend fun runMlKit(image: InputImage): Result =
        suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val regions = mutableListOf<DetectedRegion>()
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val bbox = line.boundingBox ?: continue
                            val kind = classifyRegionShape(line.text)
                            regions += DetectedRegion.from(
                                r = bbox,
                                kind = kind,
                                rawText = line.text,
                                confidence = line.confidence ?: -1f,
                            )
                        }
                    }
                    cont.resume(Result(visionText.text, regions))
                }
                .addOnFailureListener { cont.resumeWithException(it) }
            cont.invokeOnCancellation { /* ML Kit has no explicit cancel API */ }
        }

    /** Shape-level heuristic: is this line likely a digit cluster vs generic text? */
    private fun classifyRegionShape(text: String): DetectedRegion.Kind {
        val digitRatio = text.count { it.isDigit() }.toFloat() /
            (text.length.takeIf { it > 0 } ?: 1).toFloat()
        return when {
            digitRatio >= 0.7f && text.length in 1..12 -> DetectedRegion.Kind.DIGIT_CLUSTER
            text.length > 20                           -> DetectedRegion.Kind.HANDWRITTEN_BLOCK
            else                                       -> DetectedRegion.Kind.GENERIC_TEXT
        }
    }

    fun close() = recognizer.close()
}
