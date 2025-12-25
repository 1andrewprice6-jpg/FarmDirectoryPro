package com.example.farmdirectoryupgraded.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    var enableNotifications by remember { mutableStateOf(settings.enableNotifications) }
    var syncInterval by remember { mutableStateOf(settings.syncInterval.toString()) }
    var enableGPS by remember { mutableStateOf(settings.enableGPS) }
    var gpsAccuracy by remember { mutableStateOf(settings.gpsAccuracy.toString()) }
    var darkMode by remember { mutableStateOf(settings.darkMode) }
    var dataBackupEnabled by remember { mutableStateOf(settings.dataBackupEnabled) }
    var showAdvanced by remember { mutableStateOf(false) }
    
    val isConnected by viewModel.isConnected.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Save all settings
                        settings.backendUrl = backendUrl
                        settings.farmId = farmId
                        settings.workerName = workerName
                        settings.autoConnect = autoConnect
                        settings.enableNotifications = enableNotifications
                        settings.syncInterval = syncInterval.toLongOrNull() ?: 30000L
                        settings.enableGPS = enableGPS
                        settings.gpsAccuracy = gpsAccuracy.toIntOrNull() ?: 50
                        settings.darkMode = darkMode
                        settings.dataBackupEnabled = dataBackupEnabled
                        
                        // Reconnect if settings changed
                        if (autoConnect) {
                            viewModel.connectToBackend()
                            viewModel.joinFarm(farmId, "worker-${System.currentTimeMillis()}", workerName)
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connection Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) 
                        MaterialTheme.colorScheme.tertiaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (isConnected) 
                            MaterialTheme.colorScheme.onTertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isConnected) "Connected" else "Disconnected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isConnected) "Real-time sync active" else "Offline mode",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            if (isConnected) {
                                viewModel.disconnectFromBackend()
                            } else {
                                viewModel.connectToBackend()
                                viewModel.joinFarm(farmId, "worker-${System.currentTimeMillis()}", workerName)
                            }
                        }
                    ) {
                        Text(if (isConnected) "Disconnect" else "Connect")
                    }
                }
            }

            // Connection Settings
            SettingsSection(title = "Connection Settings") {
                OutlinedTextField(
                    value = backendUrl,
                    onValueChange = { backendUrl = it },
                    label = { Text("Backend URL") },
                    placeholder = { Text("http://10.0.2.2:4000") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
                )
                
                OutlinedTextField(
                    value = farmId,
                    onValueChange = { farmId = it },
                    label = { Text("Farm ID") },
                    placeholder = { Text("farm-001") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
                )
                
                OutlinedTextField(
                    value = workerName,
                    onValueChange = { workerName = it },
                    label = { Text("Worker Name") },
                    placeholder = { Text("John Doe") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-connect on startup")
                    Switch(
                        checked = autoConnect,
                        onCheckedChange = { autoConnect = it }
                    )
                }
            }

            // Sync Settings
            SettingsSection(title = "Synchronization") {
                OutlinedTextField(
                    value = syncInterval,
                    onValueChange = { syncInterval = it },
                    label = { Text("Sync Interval (ms)") },
                    placeholder = { Text("30000") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Notifications")
                        Text(
                            "Receive real-time alerts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enableNotifications,
                        onCheckedChange = { enableNotifications = it }
                    )
                }
            }

            // GPS Settings
            SettingsSection(title = "GPS & Location") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable GPS Tracking")
                        Text(
                            "Track location during farm visits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enableGPS,
                        onCheckedChange = { enableGPS = it }
                    )
                }
                
                if (enableGPS) {
                    OutlinedTextField(
                        value = gpsAccuracy,
                        onValueChange = { gpsAccuracy = it },
                        label = { Text("GPS Accuracy (meters)") },
                        placeholder = { Text("50") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                    )
                }
            }

            // Data & Backup
            SettingsSection(title = "Data Management") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Backup")
                        Text(
                            "Automatically backup data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = dataBackupEnabled,
                        onCheckedChange = { dataBackupEnabled = it }
                    )
                }
                
                Button(
                    onClick = { viewModel.exportData() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Data")
                }
                
                Button(
                    onClick = { viewModel.clearCache() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Cache")
                }
            }

            // Appearance
            SettingsSection(title = "Appearance") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark Mode")
                        Text(
                            "Use dark theme",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                }
            }

            // Advanced Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showAdvanced = !showAdvanced }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Advanced Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            if (showAdvanced) {
                SettingsSection(title = "Developer Options") {
                    Button(
                        onClick = { viewModel.testConnection() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Connection")
                    }
                    
                    Button(
                        onClick = { viewModel.viewLogs() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Article, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Logs")
                    }
                    
                    Button(
                        onClick = { viewModel.resetSettings() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset to Defaults")
                    }
                }
            }

            // App Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Farm Directory Pro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version 2.0",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "© 2024 - Integrated with Reconciliation & Real-Time Sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}
