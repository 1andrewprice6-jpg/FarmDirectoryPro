package com.example.farmdirectoryupgraded.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraCapture(
    onTextCaptured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var preview by remember { mutableStateOf<Preview?>(null) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Check camera permission
    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasCameraPermission) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Camera permission is required to scan addresses")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val previewUseCase = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    preview = previewUseCase

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            previewUseCase,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraCapture", "Camera binding failed", e)
                        errorMessage = "Failed to start camera: ${e.message}"
                    }
                }, executor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isProcessing) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Processing image...", color = MaterialTheme.colorScheme.onBackground)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Close button
                    FloatingActionButton(
                        onClick = onDismiss,
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Icon(Icons.Filled.Close, "Close")
                    }

                    // Capture button
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                isProcessing = true
                                errorMessage = null

                                try {
                                    val text = captureAndProcessImage(context, imageCapture)
                                    if (text.isNotBlank()) {
                                        onTextCaptured(text)
                                    } else {
                                        errorMessage = "No text detected in image"
                                    }
                                } catch (e: Exception) {
                                    Log.e("CameraCapture", "Image processing failed", e)
                                    errorMessage = "Failed to process image: ${e.message}"
                                } finally {
                                    isProcessing = false
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Filled.Camera, "Capture")
                    }
                }
            }
        }

        // Instructions
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            Text(
                text = "Point camera at address and tap capture",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

private suspend fun captureAndProcessImage(
    context: Context,
    imageCapture: ImageCapture
): String = suspendCoroutine { continuation ->
    // Create executor in a try-with-resources like pattern
    val executor = Executors.newSingleThreadExecutor()

    try {
        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    var recognizedText = ""

                    try {
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                            recognizer.process(image)
                                .addOnSuccessListener { visionText ->
                                    recognizedText = visionText.text
                                    Log.d("CameraCapture", "Recognized text: $recognizedText")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("CameraCapture", "Text recognition failed", e)
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                    // Resume with the recognized text
                                    continuation.resume(recognizedText)
                                    // Shutdown executor when done
                                    executor.shutdown()
                                }
                        } else {
                            imageProxy.close()
                            continuation.resume("")
                            executor.shutdown()
                        }
                    } catch (e: Exception) {
                        Log.e("CameraCapture", "Image processing error", e)
                        imageProxy.close()
                        continuation.resume("")
                        executor.shutdown()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraCapture", "Image capture failed", exception)
                    continuation.resume("")
                    executor.shutdown()
                }
            }
        )
    } catch (e: Exception) {
        Log.e("CameraCapture", "Error in captureAndProcessImage", e)
        continuation.resume("")
        executor.shutdown()
    }
}
