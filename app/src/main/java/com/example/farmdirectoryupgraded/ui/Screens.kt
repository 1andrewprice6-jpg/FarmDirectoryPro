package com.example.farmdirectoryupgraded.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmdirectoryupgraded.data.AppSettings
import com.example.farmdirectoryupgraded.viewmodel.FarmerViewModel
import com.example.farmdirectoryupgraded.utils.ValidationUtils
import com.example.farmdirectoryupgraded.utils.SanitizationUtils
import java.text.SimpleDateFormat
import java.util.*

// ========================================================================
// SETTINGS SCREEN
// ========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    var backendUrl by remember { mutableStateOf(settings.backendUrl) }
    var farmId by remember { mutableStateOf(settings.farmId) }
    var workerName by remember { mutableStateOf(settings.workerName) }
    var autoConnect by remember { mutableStateOf(settings.autoConnect) }

    // Validation error states
    var backendUrlError by remember { mutableStateOf<String?>(null) }
    var farmIdError by remember { mutableStateOf<String?>(null) }
    var workerNameError by remember { mutableStateOf<String?>(null) }
    var showValidationError by remember { mutableStateOf(false) }

    // Validate before connecting
    fun validateSettings(): Boolean {
        var isValid = true

        val urlValidation = ValidationUtils.validateUrl(backendUrl)
        backendUrlError = urlValidation.errorMessage
        if (!urlValidation.isValid) isValid = false

        val farmIdValidation = ValidationUtils.validateFarmId(farmId)
        farmIdError = farmIdValidation.errorMessage
        if (!farmIdValidation.isValid) isValid = false

        val workerNameValidation = ValidationUtils.validateWorkerName(workerName)
        workerNameError = workerNameValidation.errorMessage
        if (!workerNameValidation.isValid) isValid = false

        return isValid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "WebSocket Backend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = backendUrl,
                    onValueChange = {
                        backendUrl = SanitizationUtils.sanitizeUrl(it)
                        backendUrlError = null
                        settings.backendUrl = backendUrl
                    },
                    label = { Text("Backend URL *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = backendUrlError != null,
                    supportingText = backendUrlError?.let { { Text(it) } },
                    placeholder = { Text("ws://example.com:4000 or http://example.com:4000") }
                )
            }

            item {
                OutlinedTextField(
                    value = farmId,
                    onValueChange = {
                        farmId = SanitizationUtils.sanitizeAlphanumeric(it)
                        farmIdError = null
                        settings.farmId = farmId
                    },
                    label = { Text("Farm ID *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = farmIdError != null,
                    supportingText = farmIdError?.let { { Text(it) } }
                )
            }

            item {
                OutlinedTextField(
                    value = workerName,
                    onValueChange = {
                        workerName = SanitizationUtils.sanitizeText(it)
                        workerNameError = null
                        settings.workerName = workerName
                    },
                    label = { Text("Worker Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = workerNameError != null,
                    supportingText = workerNameError?.let { { Text(it) } }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-connect on startup")
                    Switch(
                        checked = autoConnect,
                        onCheckedChange = {
                            autoConnect = it
                            settings.autoConnect = it
                        }
                    )
                }
            }

            item {
                val isConnected by viewModel.isConnected.collectAsState()
                val connectionState by viewModel.connectionState.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val connectionErrorMessage by viewModel.connectionErrorMessage.collectAsState()

                // Connection status card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (connectionState) {
                            com.example.farmdirectoryupgraded.data.ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
                            com.example.farmdirectoryupgraded.data.ConnectionState.ERROR -> MaterialTheme.colorScheme.errorContainer
                            com.example.farmdirectoryupgraded.data.ConnectionState.CONNECTING,
                            com.example.farmdirectoryupgraded.data.ConnectionState.RECONNECTING -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Connection Status",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (connectionState) {
                                com.example.farmdirectoryupgraded.data.ConnectionState.CONNECTED -> "Connected to backend"
                                com.example.farmdirectoryupgraded.data.ConnectionState.CONNECTING -> "Connecting to backend..."
                                com.example.farmdirectoryupgraded.data.ConnectionState.RECONNECTING -> "Reconnecting to backend..."
                                com.example.farmdirectoryupgraded.data.ConnectionState.ERROR -> "Connection error"
                                com.example.farmdirectoryupgraded.data.ConnectionState.DISCONNECTED -> "Not connected"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (connectionErrorMessage != null && connectionState == com.example.farmdirectoryupgraded.data.ConnectionState.ERROR) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = connectionErrorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                val isConnected by viewModel.isConnected.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (isConnected) {
                                viewModel.disconnectFromBackend()
                            } else {
                                if (validateSettings()) {
                                    viewModel.connectToBackend()
                                    viewModel.joinFarm(
                                        SanitizationUtils.sanitizeAlphanumeric(farmId),
                                        "worker-${System.currentTimeMillis()}",
                                        SanitizationUtils.sanitizeText(workerName)
                                    )
                                } else {
                                    showValidationError = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Icon(
                            if (isConnected) Icons.Default.Close else Icons.Default.Wifi,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isConnected) "Disconnect" else "Connect")
                    }

                    if (!isConnected) {
                        OutlinedButton(
                            onClick = { viewModel.retryConnection() },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }

            if (showValidationError) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Please fix validation errors before connecting",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

// ========================================================================
// IMPORT DATA SCREEN
// ========================================================================

enum class ImportMethod(val displayName: String) {
    FILE("File Upload"),
    CAMERA("Camera Scan"),
    VOICE("Voice Input"),
    EMAIL("Email Import"),
    CLOUD("Cloud Sync"),
    NFC("NFC Tag"),
    API("API Integration")
}

data class ImportRecord(
    val method: String,
    val recordsImported: Int,
    val timestamp: String,
    val success: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDataScreen(
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    val recentImports by viewModel.recentImports.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromFile(it) }
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Import Methods",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                ImportMethodCard(
                    icon = Icons.Default.Upload,
                    title = "File Upload",
                    description = "Import from CSV or JSON file",
                    onClick = { filePickerLauncher.launch("*/*") }
                )
            }

            item {
                ImportMethodCard(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera Scan",
                    description = "Scan documents with camera",
                    onClick = { viewModel.importFromCamera() }
                )
            }

            item {
                ImportMethodCard(
                    icon = Icons.Default.Mic,
                    title = "Voice Input",
                    description = "Dictate farmer information",
                    onClick = { viewModel.startVoiceInput() }
                )
            }

            if (recentImports.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recent Imports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(recentImports) { record ->
                    ImportRecordCard(record)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportMethodCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
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
                modifier = Modifier.size(32.dp),
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

@Composable
fun ImportRecordCard(record: ImportRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (record.success)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (record.success) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (record.success)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.method,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${record.recordsImported} records • ${record.timestamp}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ========================================================================
// LOGS VIEWER SCREEN
// ========================================================================

enum class LogLevel {
    INFO, SUCCESS, WARNING, ERROR
}

data class LogEntry(
    val id: Int = 0,
    val category: String,
    val level: LogLevel,
    val message: String,
    val details: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsViewerScreen(
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    val logs by viewModel.logs.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All") + logs.map { it.category }.distinct()
    val filteredLogs = if (selectedCategory == "All") logs else logs.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Logs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Logs")
                    }
                    IconButton(onClick = { viewModel.exportLogs() }) {
                        Icon(Icons.Default.Share, contentDescription = "Export Logs")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs) { log ->
                    LogEntryCard(log)
                }
            }
        }
    }
}

@Composable
fun LogEntryCard(log: LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (log.level) {
                LogLevel.ERROR -> MaterialTheme.colorScheme.errorContainer
                LogLevel.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                LogLevel.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                LogLevel.INFO -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (log.level) {
                            LogLevel.ERROR -> Icons.Default.Error
                            LogLevel.WARNING -> Icons.Default.Warning
                            LogLevel.SUCCESS -> Icons.Default.CheckCircle
                            LogLevel.INFO -> Icons.Default.Info
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.category,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodyMedium
            )
            if (log.details.isNotEmpty()) {
                Text(
                    text = log.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
