package com.example.farmdirectoryupgraded.vision.ui

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.vision.capture.CaptureMode
import com.example.farmdirectoryupgraded.vision.capture.CapturedFrame
import com.example.farmdirectoryupgraded.vision.capture.DetectedRegion
import com.example.farmdirectoryupgraded.vision.capture.ParsedFields
import com.example.farmdirectoryupgraded.vision.ledger.CaptureRepository
import com.example.farmdirectoryupgraded.vision.parsing.ValidationGate
import com.example.farmdirectoryupgraded.vision.vision.TextRecognitionEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CaptureUi(
    val mode: CaptureMode = CaptureMode.DASHBOARD_ODOMETER,
    val overlayRegions: List<DetectedRegion> = emptyList(),
    val isProcessing: Boolean = false,
    val lastResult: CaptureRepository.Result? = null,
    val errorMessage: String? = null,
    val torchOn: Boolean = false,
)

class CaptureViewModel(
    app: Application,
    private val repository: CaptureRepository,
    private val textEngine: TextRecognitionEngine,
) : AndroidViewModel(app) {

    private val _ui = MutableStateFlow(CaptureUi())
    val ui: StateFlow<CaptureUi> = _ui

    /** Last known location; updated by the UI layer when it has a fresh fix. */
    @Volatile private var lastLocation: Location? = null

    private var previewAnalyzeJob: Job? = null

    fun setMode(mode: CaptureMode) = _ui.update { it.copy(mode = mode) }

    fun toggleTorch() = _ui.update { it.copy(torchOn = !it.torchOn) }

    fun onLocationUpdate(location: Location) {
        lastLocation = location
    }

    /**
     * Called by the CameraX ImageAnalysis use case on each preview frame.
     * Cancels any previous in-flight analysis to stay responsive under camera FPS.
     */
    fun analyzePreviewFrame(bytes: ByteArray, width: Int, height: Int, rotation: Int) {
        previewAnalyzeJob?.cancel()
        previewAnalyzeJob = viewModelScope.launch {
            val frame = CapturedFrame(
                jpegBytes = bytes,
                width = width,
                height = height,
                rotationDegrees = rotation,
                capturedAtEpochMs = System.currentTimeMillis(),
            )
            runCatching { textEngine.recognize(frame) }
                .onSuccess { result ->
                    _ui.update { it.copy(overlayRegions = result.regions) }
                }
        }
    }

    /**
     * Called when the user taps the shutter button. Captures the current frame
     * and runs the full ingestion pipeline.
     */
    fun onShutter(jpegBytes: ByteArray, width: Int, height: Int, rotation: Int, gaugeId: String? = null) {
        val loc = lastLocation
        val frame = CapturedFrame(
            jpegBytes = jpegBytes,
            width = width,
            height = height,
            rotationDegrees = rotation,
            capturedAtEpochMs = System.currentTimeMillis(),
            latitude = loc?.latitude,
            longitude = loc?.longitude,
        )
        viewModelScope.launch {
            _ui.update { it.copy(isProcessing = true, errorMessage = null) }
            runCatching { repository.processCapture(frame, _ui.value.mode, gaugeId) }
                .onSuccess { result -> _ui.update { it.copy(isProcessing = false, lastResult = result) } }
                .onFailure { t ->
                    _ui.update { it.copy(isProcessing = false, errorMessage = t.message ?: "Processing failed") }
                }
        }
    }

    /** Called after the user accepts or corrects fields on the review screen. */
    fun commitReview(captureId: Long, edited: ParsedFields) {
        viewModelScope.launch {
            repository.commitEdits(captureId, edited)?.let { updated ->
                _ui.update { it.copy(lastResult = updated) }
            }
        }
    }

    fun dismissResult() = _ui.update { it.copy(lastResult = null, errorMessage = null) }
}
