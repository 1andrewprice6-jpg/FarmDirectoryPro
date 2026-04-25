package com.example.farmdirectoryupgraded.vision.ledger

import android.content.Context
import com.example.farmdirectoryupgraded.vision.capture.CaptureMode
import com.example.farmdirectoryupgraded.vision.capture.CapturedFrame
import com.example.farmdirectoryupgraded.vision.capture.ParsedFields
import com.example.farmdirectoryupgraded.vision.parsing.CategoryClassifier
import com.example.farmdirectoryupgraded.vision.parsing.ValidationGate
import com.example.farmdirectoryupgraded.vision.vision.AnalogGaugeDetector
import com.example.farmdirectoryupgraded.vision.vision.GaugeCalibration
import com.example.farmdirectoryupgraded.vision.vision.HandwrittenLogDetector
import com.example.farmdirectoryupgraded.vision.vision.TextRecognitionEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

/**
 * Single entry point for the whole pipeline. The ViewModel calls [processCapture];
 * everything else is internal.
 *
 * Flow:
 *   1. Persist the raw JPEG to app-private storage (forensic archive).
 *   2. Run ML Kit text recognition.
 *   3. Run mode-specific detectors (analog gauge needle, table structure).
 *   4. Classify raw text into ParsedFields.
 *   5. Run the validation gate.
 *   6. Persist the CaptureEntity with status.
 *   7. If validation passed, propagate to downstream logs.
 *
 * The function always returns a [Result] — even rejected captures are
 * persisted for forensic purposes. The UI decides what to show the user.
 */
class CaptureRepository(
    private val context: Context,
    private val captureDao: CaptureDao,
    private val textEngine: TextRecognitionEngine,
    private val gaugeDetector: AnalogGaugeDetector,
    private val propagator: LedgerPropagator,
    private val calibrationProvider: suspend (gaugeId: String) -> GaugeCalibration?,
) {
    private val json = Json { encodeDefaults = true }

    data class Result(
        val captureId: Long,
        val mode: CaptureMode,
        val parsed: ParsedFields,
        val decision: ValidationGate.Decision,
        val linkedEntities: Map<String, Long?>,
        val rawImagePath: String,
    )

    suspend fun processCapture(
        frame: CapturedFrame,
        mode: CaptureMode,
        gaugeId: String? = null,
    ): Result = withContext(Dispatchers.IO) {
        // 1. Archive raw image
        val imagePath = archiveJpeg(frame.jpegBytes)

        // 2. OCR
        val ocr = runCatching { textEngine.recognize(frame) }
            .getOrDefault(TextRecognitionEngine.Result("", emptyList()))

        // 3. Mode-specific detection
        val detectorFields: ParsedFields = when (mode) {
            CaptureMode.ANALOG_GAUGE -> {
                if (gaugeId != null) runAnalogGauge(frame, gaugeId) else ParsedFields()
            }
            CaptureMode.CHICKEN_HOUSE_LOG -> {
                val table = HandwrittenLogDetector.buildTable(ocr.regions, frame.height)
                structuredTableToMetrics(table)
            }
            else -> ParsedFields()
        }

        // 4. Classify raw text
        val classified = CategoryClassifier.classify(mode, ocr.fullText)

        // Detector fields take precedence (more precise than keyword parsing)
        val merged = detectorFields.mergeOver(classified)

        // 5. Validate
        val decision = ValidationGate.evaluate(mode, merged)
        val statusStr = when (decision) {
            ValidationGate.Decision.Complete          -> "COMPLETE"
            is ValidationGate.Decision.Incomplete     -> "INCOMPLETE"
            is ValidationGate.Decision.Inconsistent   -> "INCONSISTENT"
            is ValidationGate.Decision.Rejected       -> "REJECTED"
        }

        // 6. Persist capture row
        val entity = CaptureEntity(
            capturedAtEpochMs = frame.capturedAtEpochMs,
            mode = mode.name,
            rawImagePath = imagePath,
            latitude = frame.latitude,
            longitude = frame.longitude,
            parsedFieldsJson = json.encodeToString(ParsedFields.serializer(), merged),
            status = statusStr,
            farmId = merged.farmId,
        )
        val captureId = captureDao.insert(entity)
        val persisted = entity.copy(id = captureId)

        // 7. Propagate only if complete
        val links = if (decision is ValidationGate.Decision.Complete) {
            propagator.propagate(persisted, mode, merged)
        } else emptyMap()

        Result(
            captureId = captureId,
            mode = mode,
            parsed = merged,
            decision = decision,
            linkedEntities = links,
            rawImagePath = imagePath,
        )
    }

    /**
     * After the user manually edits fields on CaptureReviewScreen, call this to
     * re-run the validation gate and propagate if the edits made the capture complete.
     */
    suspend fun commitEdits(captureId: Long, editedFields: ParsedFields): Result? =
        withContext(Dispatchers.IO) {
            val existing = captureDao.get(captureId) ?: return@withContext null
            val mode = CaptureMode.valueOf(existing.mode)
            val decision = ValidationGate.evaluate(mode, editedFields)
            val newStatus = when (decision) {
                ValidationGate.Decision.Complete        -> "COMPLETE"
                is ValidationGate.Decision.Incomplete   -> "INCOMPLETE"
                is ValidationGate.Decision.Inconsistent -> "INCONSISTENT"
                is ValidationGate.Decision.Rejected     -> "REJECTED"
            }
            val updated = existing.copy(
                parsedFieldsJson = json.encodeToString(ParsedFields.serializer(), editedFields),
                status = newStatus,
                userEditedAtEpochMs = System.currentTimeMillis(),
                farmId = editedFields.farmId,
            )
            captureDao.update(updated)
            val links = if (decision is ValidationGate.Decision.Complete) {
                propagator.propagate(updated, mode, editedFields)
            } else emptyMap()
            Result(captureId, mode, editedFields, decision, links, existing.rawImagePath)
        }

    // --- internals ---------------------------------------------------------

    private fun archiveJpeg(bytes: ByteArray): String {
        val dir = File(context.filesDir, "captures").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    private suspend fun runAnalogGauge(frame: CapturedFrame, gaugeId: String): ParsedFields {
        val cal = calibrationProvider(gaugeId) ?: return ParsedFields()
        val bmp = android.graphics.BitmapFactory.decodeByteArray(
            frame.jpegBytes, 0, frame.jpegBytes.size,
        ) ?: return ParsedFields()
        val reading = gaugeDetector.detect(bmp, cal)
        return ParsedFields(
            gaugeValue = reading.value,
            gaugeUnit = reading.unit,
            categories = listOf("GAUGE_READING"),
        )
    }

    private fun structuredTableToMetrics(table: HandwrittenLogDetector.Table): ParsedFields {
        if (table.rows.isEmpty()) return ParsedFields()
        // Most recent row = last; for a daily log sheet we grab the bottom-most row.
        val row = table.rows.last()
        val metrics = mutableMapOf<String, Double>()
        table.header.forEachIndexed { i, colName ->
            val v = row.getOrNull(i)?.toDoubleOrNull()
            if (v != null) metrics[colName] = v
        }
        return ParsedFields(metrics = metrics)
    }
}
