package com.example.farmdirectoryupgraded.vision.ui

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.farmdirectoryupgraded.vision.capture.CaptureMode
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

private val Obsidian = Color(0xFF0B0D10)
private val Panel = Color(0xFF13161B)
private val Raise = Color(0xFF1B1F27)
private val Cyan = Color(0xFF6EE7FF)
private val Muted = Color(0xFF8B94A3)
private val Bright = Color(0xFFD7DCE5)

@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    onCaptureReady: (captureId: Long) -> Unit,
) {
    val ui by viewModel.ui.collectAsState()
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var sourceWidth by remember { mutableStateOf(0) }
    var sourceHeight by remember { mutableStateOf(0) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { cameraExecutor.shutdown() } }

    // Bind camera whenever previewView is ready OR torch toggles
    LaunchedEffect(previewView, ui.torchOn) {
        val view = previewView ?: return@LaunchedEffect
        bindCamera(
            ctx = ctx,
            lifecycleOwner = lifecycleOwner,
            previewView = view,
            torchOn = ui.torchOn,
            onAnalyzerFrame = { proxy ->
                val bytes = proxy.toJpegBytes()
                if (bytes != null) {
                    sourceWidth = proxy.width
                    sourceHeight = proxy.height
                    viewModel.analyzePreviewFrame(
                        bytes,
                        proxy.width,
                        proxy.height,
                        proxy.imageInfo.rotationDegrees,
                    )
                }
                proxy.close()
            },
            onCaptureReady = { imageCapture = it },
        )
    }

    // Surface capture result upward
    LaunchedEffect(ui.lastResult) {
        ui.lastResult?.let { onCaptureReady(it.captureId) }
    }

    Box(Modifier.fillMaxSize().background(Obsidian)) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        BoundingBoxOverlay(
            regions = ui.overlayRegions,
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            modifier = Modifier.fillMaxSize(),
        )

        Column(Modifier.fillMaxWidth().align(Alignment.TopCenter)) {
            Surface(color = Panel.copy(alpha = 0.92f), modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Visual Ingest",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { viewModel.toggleTorch() }) {
                        Icon(
                            if (ui.torchOn) Icons.Default.Bolt else Icons.Default.FlashOff,
                            null,
                            tint = if (ui.torchOn) Cyan else Muted,
                        )
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .background(Panel.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CaptureMode.values().forEach { mode ->
                    FilterChip(
                        selected = ui.mode == mode,
                        onClick = { viewModel.setMode(mode) },
                        label = { Text(mode.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Raise,
                            selectedContainerColor = Color(0xFF2B3B50),
                            labelColor = Bright,
                            selectedLabelColor = Cyan,
                        ),
                    )
                }
            }
        }

        // Bottom shutter
        Surface(
            color = Panel.copy(alpha = 0.92f),
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
        ) {
            Column(
                Modifier.padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    ui.mode.description,
                    color = Muted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                ShutterButton(
                    isProcessing = ui.isProcessing,
                    onClick = {
                        val ic = imageCapture ?: return@ShutterButton
                        takeCapture(ic, ctx) { bytes, w, h, rot ->
                            viewModel.onShutter(bytes, w, h, rot)
                        }
                    },
                )
                ui.errorMessage?.let {
                    Text(
                        it,
                        color = Color(0xFFFF6E7F),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ShutterButton(isProcessing: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(74.dp).background(Cyan, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (isProcessing) {
            CircularProgressIndicator(color = Obsidian, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
        } else {
            IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.CameraAlt, "Capture", tint = Obsidian, modifier = Modifier.size(36.dp))
            }
        }
    }
}

// ─────────────────────────── CameraX glue ───────────────────────────

private fun bindCamera(
    ctx: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    torchOn: Boolean,
    onAnalyzerFrame: (ImageProxy) -> Unit,
    onCaptureReady: (ImageCapture) -> Unit,
) {
    val providerFuture = ProcessCameraProvider.getInstance(ctx)
    providerFuture.addListener({
        val provider = providerFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        val analysisExecutor = Executors.newSingleThreadExecutor()
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also {
                it.setAnalyzer(analysisExecutor) { proxy -> onAnalyzerFrame(proxy) }
            }

        val selector = CameraSelector.DEFAULT_BACK_CAMERA
        provider.unbindAll()
        val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture, analysis)
        camera.cameraControl.enableTorch(torchOn)
        onCaptureReady(imageCapture)
    }, ContextCompat.getMainExecutor(ctx))
}

private fun takeCapture(
    imageCapture: ImageCapture,
    ctx: Context,
    onCaptured: (bytes: ByteArray, w: Int, h: Int, rot: Int) -> Unit,
) {
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(ctx),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bytes = image.toJpegBytes()
                val rot = image.imageInfo.rotationDegrees
                val w = image.width
                val h = image.height
                image.close()
                if (bytes != null) onCaptured(bytes, w, h, rot)
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("CaptureScreen", "Capture failed", exception)
            }
        },
    )
}

/** Convert an ImageProxy to JPEG bytes regardless of source format. */
private fun ImageProxy.toJpegBytes(): ByteArray? {
    val plane = planes.firstOrNull() ?: return null
    val buffer = plane.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    // JPEG format is already JPEG bytes. Other formats would need conversion;
    // for MAXIMIZE_QUALITY mode CameraX typically gives JPEG directly.
    if (format == android.graphics.ImageFormat.JPEG) return bytes
    // Fallback: if it's YUV_420_888, serialize via Bitmap
    return try {
        val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return null
        ByteArrayOutputStream().use { out ->
            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
            out.toByteArray()
        }
    } catch (_: Throwable) { null }
}
