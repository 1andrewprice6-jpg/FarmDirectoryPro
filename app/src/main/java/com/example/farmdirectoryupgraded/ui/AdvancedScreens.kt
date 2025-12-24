package com.example.farmdirectoryupgraded.ui

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
import com.example.farmdirectoryupgraded.viewmodel.FarmerViewModel

// ============================================================================
// IMPORT DATA SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDataScreen(
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Data") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Import Methods",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                ImportMethodCard(
                    title = "CSV File",
                    description = "Import farmers from CSV file",
                    icon = Icons.Default.Description,
                    onClick = { /* TODO: Implement CSV import */ }
                )
            }

            item {
                ImportMethodCard(
                    title = "JSON File",
                    description = "Import from JSON format",
                    icon = Icons.Default.Code,
                    onClick = { /* TODO: Implement JSON import */ }
                )
            }

            item {
                ImportMethodCard(
                    title = "Camera/Scan",
                    description = "Scan business cards or documents",
                    icon = Icons.Default.Camera,
                    onClick = { /* TODO: Implement camera scan */ }
                )
            }

            item {
                ImportMethodCard(
                    title = "Voice Input",
                    description = "Add farmer via speech recognition",
                    icon = Icons.Default.Mic,
                    onClick = { /* TODO: Implement voice input */ }
                )
            }

            item {
                ImportMethodCard(
                    title = "Email",
                    description = "Parse data from email",
                    icon = Icons.Default.Email,
                    onClick = { /* TODO: Implement email parsing */ }
                )
            }

            item {
                ImportMethodCard(
                    title = "Image OCR",
                    description = "Extract text from images",
                    icon = Icons.Default.Image,
                    onClick = { /* TODO: Implement OCR */ }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportMethodCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
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
                Text(title, fontWeight = FontWeight.Bold)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

// ============================================================================
// RECONCILE SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReconcileScreen(
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    val farmers by viewModel.farmers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reconcile Locations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Location Reconciliation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Farmers")
                            Text(
                                "${farmers.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("With GPS")
                            Text(
                                "${farmers.count { it.latitude != null }}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text("Missing GPS")
                            Text(
                                "${farmers.count { it.latitude == null }}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Farmers without GPS
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(farmers.filter { it.latitude == null }) { farmer ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(farmer.name, fontWeight = FontWeight.Bold)
                                Text(
                                    farmer.address,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { /* TODO: Get GPS */ }) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Get Location")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// ATTENDANCE SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    val farmers by viewModel.farmers.collectAsState()
    var attendanceMap by remember { mutableStateOf(mapOf<Int, Boolean>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Tracker") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Save attendance */ }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Present")
                        Text(
                            "${attendanceMap.count { it.value }}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Absent")
                        Text(
                            "${attendanceMap.count { !it.value }}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total")
                        Text(
                            "${farmers.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Attendance List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(farmers) { farmer ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = attendanceMap[farmer.id] ?: false,
                                    onCheckedChange = { checked ->
                                        attendanceMap = attendanceMap + (farmer.id to checked)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(farmer.name, fontWeight = FontWeight.Bold)
                                    if (farmer.farmName.isNotEmpty()) {
                                        Text(
                                            farmer.farmName,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                            Icon(
                                if (attendanceMap[farmer.id] == true) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (attendanceMap[farmer.id] == true)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// ROUTE OPTIMIZATION SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteOptimizationScreen(
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    val farmers by viewModel.farmers.collectAsState()
    val farmersWithGPS = farmers.filter { it.latitude != null && it.longitude != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route Optimization") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Multi-Stop Route Planning",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Optimize visiting multiple farms")
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Available")
                            Text(
                                "${farmersWithGPS.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Missing GPS")
                            Text(
                                "${farmers.size - farmersWithGPS.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* TODO: Calculate optimal route */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = farmersWithGPS.size >= 2
                    ) {
                        Icon(Icons.Default.Route, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calculate Best Route")
                    }
                }
            }

            if (farmersWithGPS.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("No farmers with GPS coordinates found. Add GPS data to enable routing.")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(farmersWithGPS) { farmer ->
                        OutlinedCard {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(farmer.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        "GPS: ${String.format("%.4f", farmer.latitude)}, ${String.format("%.4f", farmer.longitude)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// LOGS VIEWER SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsViewerScreen(
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    // Mock logs for demonstration
    val logs = remember {
        listOf(
            LogEntry("Farmer Added", "John Doe added to database", System.currentTimeMillis()),
            LogEntry("Location Updated", "GPS updated for Smith Farm", System.currentTimeMillis() - 300000),
            LogEntry("Health Alert", "Critical alert for Farmer #123", System.currentTimeMillis() - 600000),
            LogEntry("Worker Joined", "Mobile User joined farm-nc-1", System.currentTimeMillis() - 900000),
        )
    }

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
                    IconButton(onClick = { /* TODO: Export logs */ }) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (log.type) {
                                "Farmer Added" -> Icons.Default.PersonAdd
                                "Location Updated" -> Icons.Default.LocationOn
                                "Health Alert" -> Icons.Default.Warning
                                "Worker Joined" -> Icons.Default.Login
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.type, fontWeight = FontWeight.Bold)
                            Text(
                                log.message,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                android.text.format.DateUtils.getRelativeTimeSpanString(log.timestamp).toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

data class LogEntry(
    val type: String,
    val message: String,
    val timestamp: Long
)
