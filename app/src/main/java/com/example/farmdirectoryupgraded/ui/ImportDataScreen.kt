package com.example.farmdirectoryupgraded.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.farmdirectoryupgraded.viewmodel.FarmerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDataScreen(
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf<ImportMethod?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var importStatus by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            importStatus = "Importing from file..."
            viewModel.importFromFile(it)
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            importStatus = "Processing image..."
            viewModel.importFromCamera()
        }
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true) {
            // Permission granted, proceed with camera
            selectedMethod?.let { handleImportMethod(it, viewModel, filePickerLauncher, cameraLauncher) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Data") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Choose Import Method",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Import farmer/farm data from various sources",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Import Status
            importStatus?.let { status ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(status)
                    }
                }
            }

            // Import Method Cards
            ImportMethodCard(
                icon = Icons.Default.CameraAlt,
                title = "Camera / QR Code",
                description = "Scan QR codes or capture documents",
                onClick = {
                    selectedMethod = ImportMethod.CAMERA
                    // Check camera permission
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        handleImportMethod(ImportMethod.CAMERA, viewModel, filePickerLauncher, cameraLauncher)
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    }
                }
            )

            ImportMethodCard(
                icon = Icons.Default.Mic,
                title = "Voice Input",
                description = "Dictate farmer information",
                onClick = {
                    selectedMethod = ImportMethod.VOICE
                    viewModel.startVoiceInput()
                }
            )

            ImportMethodCard(
                icon = Icons.Default.Article,
                title = "Text File (CSV/JSON)",
                description = "Import from CSV or JSON files",
                onClick = {
                    selectedMethod = ImportMethod.FILE
                    filePickerLauncher.launch("*/*")
                }
            )

            ImportMethodCard(
                icon = Icons.Default.Image,
                title = "Image OCR",
                description = "Extract text from images",
                onClick = {
                    selectedMethod = ImportMethod.IMAGE_OCR
                    filePickerLauncher.launch("image/*")
                }
            )

            ImportMethodCard(
                icon = Icons.Default.Email,
                title = "Email",
                description = "Parse data from email attachments",
                onClick = {
                    selectedMethod = ImportMethod.EMAIL
                    viewModel.showEmailImportDialog()
                }
            )

            ImportMethodCard(
                icon = Icons.Default.CloudDownload,
                title = "Cloud Import",
                description = "Import from Google Drive, Dropbox, etc.",
                onClick = {
                    selectedMethod = ImportMethod.CLOUD
                    viewModel.showCloudImportDialog()
                }
            )

            ImportMethodCard(
                icon = Icons.Default.Nfc,
                title = "NFC Tag",
                description = "Read data from NFC tags",
                onClick = {
                    selectedMethod = ImportMethod.NFC
                    viewModel.startNFCReader()
                }
            )

            ImportMethodCard(
                icon = Icons.Default.Api,
                title = "REST API",
                description = "Fetch data from external API",
                onClick = {
                    selectedMethod = ImportMethod.API
                    viewModel.showAPIImportDialog()
                }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Recent Imports
            Text(
                text = "Recent Imports",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val recentImports by viewModel.recentImports.collectAsState()
            if (recentImports.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No recent imports",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                recentImports.forEach { import ->
                    ImportHistoryCard(import)
                }
            }
        }
    }
}

@Composable
fun ImportMethodCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ImportHistoryCard(import: ImportRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = import.method,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${import.recordsImported} records • ${import.timestamp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (import.success) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            } else {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

enum class ImportMethod {
    CAMERA,
    VOICE,
    FILE,
    IMAGE_OCR,
    EMAIL,
    CLOUD,
    NFC,
    API
}

data class ImportRecord(
    val method: String,
    val recordsImported: Int,
    val timestamp: String,
    val success: Boolean
)

private fun handleImportMethod(
    method: ImportMethod,
    viewModel: FarmerViewModel,
    filePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>
) {
    when (method) {
        ImportMethod.CAMERA -> {
            // Launch camera
            viewModel.prepareCameraImport()
        }
        ImportMethod.FILE -> {
            filePickerLauncher.launch("*/*")
        }
        ImportMethod.IMAGE_OCR -> {
            filePickerLauncher.launch("image/*")
        }
        else -> {
            // Handle other methods
        }
    }
}
