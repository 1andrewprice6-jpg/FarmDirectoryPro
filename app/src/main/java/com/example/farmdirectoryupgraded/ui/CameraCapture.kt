package com.example.farmdirectoryupgraded.ui

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.io.File
import java.util.concurrent.Executors

/**
 * Camera capture screen with OCR text recognition
 */
@Composable
fun CameraCaptureScreen(
    onImageCaptured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        // Permission request screen
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Camera permission required",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Please grant camera permission to capture farmer data",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
    } else {
        // Camera preview screen
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val preview = Preview.Builder().build()
                    val selector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    try {
                        cameraProviderFuture.get().unbindAll()
                        cameraProviderFuture.get().bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview
                        )
                    } catch (e: Exception) {
                        errorMessage = "Failed to start camera: ${e.message}"
                    }

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay UI
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                        Text(
                            "Capture Farmer Data",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Instructions
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Position document or sign with farmer information",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                isProcessing = true
                                captureAndProcessImage(
                                    context = context,
                                    cameraProvider = cameraProviderFuture.get(),
                                    lifecycleOwner = lifecycleOwner,
                                    executor = executor,
                                    onSuccess = { text ->
                                        isProcessing = false
                                        onImageCaptured(text)
                                    },
                                    onError = { error ->
                                        isProcessing = false
                                        errorMessage = error
                                    }
                                )
                            },
                            enabled = !isProcessing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Processing...")
                            } else {
                                Icon(Icons.Default.CameraAlt, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Capture & Extract Text")
                            }
                        }
                    }
                }
            }

            // Error message
            errorMessage?.let { error ->
                AlertDialog(
                    onDismissRequest = { errorMessage = null },
                    icon = { Icon(Icons.Default.Error, null) },
                    title = { Text("Error") },
                    text = { Text(error) },
                    confirmButton = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

/**
 * Capture image and process with OCR
 */
private fun captureAndProcessImage(
    context: Context,
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    executor: java.util.concurrent.Executor,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val imageCapture = ImageCapture.Builder()
        .setTargetRotation(android.view.Surface.ROTATION_0)
        .build()

    val selector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            selector,
            imageCapture
        )
    } catch (e: Exception) {
        onError("Failed to bind camera: ${e.message}")
        return
    }

    val photoFile = File(
        context.cacheDir,
        "farmer_capture_${System.currentTimeMillis()}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                processImageWithOCR(
                    context = context,
                    imageUri = Uri.fromFile(photoFile),
                    onSuccess = onSuccess,
                    onError = onError
                )
            }

            override fun onError(exception: ImageCaptureException) {
                onError("Failed to capture image: ${exception.message}")
            }
        }
    )
}

/**
 * Process captured image with ML Kit OCR
 */
private fun processImageWithOCR(
    context: Context,
    imageUri: Uri,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, imageUri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                if (extractedText.isBlank()) {
                    onError("No text found in image")
                } else {
                    onSuccess(extractedText)
                }
            }
            .addOnFailureListener { e ->
                onError("OCR failed: ${e.message}")
            }
    } catch (e: Exception) {
        onError("Failed to process image: ${e.message}")
    }
}
