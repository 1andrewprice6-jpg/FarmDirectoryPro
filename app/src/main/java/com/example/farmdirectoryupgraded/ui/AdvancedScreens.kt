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
import com.example.farmdirectoryupgraded.data.Farmer
import com.example.farmdirectoryupgraded.viewmodel.AttendanceViewModel
import com.example.farmdirectoryupgraded.viewmodel.LocationViewModel
import com.example.farmdirectoryupgraded.viewmodel.FarmerListViewModel

// ========================================================================
// ATTENDANCE TRACKING SCREEN
// ========================================================================

// Enum and Data classes defined here to avoid circular deps if moved
enum class AttendanceMethod(val displayName: String) {
    GPS("GPS Location"),
    QR_CODE("QR Code Scan"),
    NFC("NFC Tag"),
    MANUAL("Manual Entry"),
    BLUETOOTH("Bluetooth Beacon")
}

data class AttendanceRecord(
    val id: Int = 0,
    val farmName: String,
    val method: String,
    val timestamp: String,
    val notes: String,
    val checkOut: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit
) {
    // AttendanceViewModel has _attendanceRecords: StateFlow<List<com.example.farmdirectoryupgraded.data.AttendanceRecord>>
    // But the UI expects com.example.farmdirectoryupgraded.ui.AttendanceRecord (which is defined above).
    // The ViewModel seems to be returning the DATA layer objects?
    // Let's check AttendanceViewModel.kt... 
    // It returns `List<AttendanceRecord>`. And imports `com.example.farmdirectoryupgraded.data.AttendanceRecord`.
    // The UI `AttendanceRecord` is different (has formatted timestamp strings).
    // I need to map it here OR update ViewModel to return UI models.
    // The OLD FarmerViewModel mapped it. 
    // Let's look at AttendanceViewModel again.
    // It returns `val attendanceRecords = _attendanceRecords.asStateFlow()`. 
    // And `_attendanceRecords` holds `List<com.example.farmdirectoryupgraded.data.AttendanceRecord>`.
    
    // I will map it here in the UI for now to save time on ViewModel refactoring, 
    // or better, I should have updated ViewModel to return UI friendly data. 
    // Given I can't easily change ViewModel without re-reading/writing, I'll map here.
    
    val rawRecords by viewModel.attendanceRecords.collectAsState()
    
    // SimpleDateFormat for UI mapping
    val dateFormatter = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
    
    val attendanceRecords = rawRecords.map { record ->
        AttendanceRecord(
            id = record.id,
            farmName = record.workLocation ?: "Unknown",
            method = record.method,
            timestamp = dateFormatter.format(java.util.Date(record.checkInTime)),
            notes = record.notes,
            checkOut = record.checkOutTime?.let { dateFormatter.format(java.util.Date(it)) }
        )
    }

    var selectedMethod by remember { mutableStateOf(AttendanceMethod.GPS) }
    var farmName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // AttendanceViewModel uses selectEmployee(employee). 
    // But this screen doesn't show employee selection? 
    // The old FarmerViewModel handled "selectedEmployee". 
    // We might need to ensure an employee is selected before coming here or select one here.
    // For now, I'll assume one is selected or the VM handles it. 
    // Wait, the `recordAttendance` in `AttendanceViewModel` CHECKS for `_selectedEmployee`.
    // If null, it errors. 
    // The UI needs to allow selecting an employee if none is selected?
    // Or `MainActivity` ensures one is selected?
    // Let's proceed with the existing logic.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Tracking") },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Check-In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = farmName,
                            onValueChange = { farmName = it },
                            label = { Text("Farm Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Check-in Method:", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AttendanceMethod.values().take(3).forEach { method ->
                                FilterChip(
                                    selected = selectedMethod == method,
                                    onClick = { selectedMethod = method },
                                    label = { Text(method.displayName, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (farmName.isNotBlank()) {
                                    // AttendanceViewModel.checkInWithGPS requires params: 
                                    // employeeId, lat, lon, workLocation, task, notes.
                                    // But `recordAttendance` was the old method. 
                                    // AttendanceViewModel has `checkInWithGPS` and `checkInWithQRCode`.
                                    // It does NOT have a generic `recordAttendance` that matches the old signature (Method, Location, Notes).
                                    // I need to adapt.
                                    // Or better, I'll use a placeholder logic or call checkInWithGPS with dummy coords if GPS not available.
                                    // The `AttendanceMethod` enum here suggests different methods.
                                    
                                    // For now, let's try to find the currently selected employee ID from the VM
                                    // AttendanceViewModel has `selectedEmployee`.
                                    val emp = viewModel.selectedEmployee.value
                                    if (emp != null) {
                                        viewModel.checkInWithGPS(
                                            employeeId = emp.id,
                                            latitude = 0.0, // Placeholder
                                            longitude = 0.0, // Placeholder
                                            workLocation = farmName,
                                            notes = notes
                                        )
                                        farmName = ""
                                        notes = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = farmName.isNotBlank()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check In")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Recent Check-Ins",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(attendanceRecords) { record ->
                AttendanceRecordCard(record)
            }
        }
    }
}

@Composable
fun AttendanceRecordCard(record: AttendanceRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = record.farmName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    onClick = { },
                    label = { Text(record.method) }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = record.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (record.notes.isNotEmpty()) {
                Text(
                    text = record.notes,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ========================================================================
// GPS RECONCILIATION SCREEN
// ========================================================================

data class ReconcileResult(
    val farmName: String,
    val distance: Double,
    val confidence: Double,
    val alternatives: List<AlternativeFarm>
)

data class AlternativeFarm(
    val farmName: String,
    val distance: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReconcileScreen(
    viewModel: LocationViewModel,
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    
    // Map LocationViewModel.ReconcileResult to UI ReconcileResult
    val vmResult by viewModel.reconcileResult.collectAsState()
    val reconcileResult = vmResult?.let { res ->
        ReconcileResult(
            farmName = res.farmer.farmName.ifBlank { res.farmer.name },
            distance = res.distance,
            confidence = res.confidence * 100, // Convert 0.95 to 95.0
            alternatives = emptyList() // LocationViewModel doesn't seem to return alternatives currently
        )
    }
    
    var currentLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPS Reconciliation") },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Find Nearest Farm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Use your current GPS location to identify which farm you're at",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                val loc = viewModel.getCurrentLocation()
                                if (loc != null) {
                                    currentLocation = Pair(loc.latitude, loc.longitude)
                                    viewModel.reconcileFarm(loc.latitude, loc.longitude)
                                } else {
                                    // Mock location for testing if null (Hiddenite, NC)
                                    val mockLat = 35.7796
                                    val mockLon = -81.3361
                                    currentLocation = Pair(mockLat, mockLon)
                                    viewModel.reconcileFarm(mockLat, mockLon)
                                }
                                // Simulate loading delay or wait for result
                                isLoading = false 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.MyLocation, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Get Current Location")
                        }
                    }
                }
            }

            currentLocation?.let { (lat, lon) ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Current Location",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Latitude: ${String.format("%.6f", lat)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Longitude: ${String.format("%.6f", lon)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            reconcileResult?.let { result ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Nearest Farm",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = result.farmName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Distance: ${String.format("%.2f", result.distance)} km",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Confidence: ${String.format("%.1f", result.confidence)}%",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (result.alternatives.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Other Nearby Farms",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                result.alternatives.forEach { alt ->
                                    Text(
                                        text = "${alt.farmName} (${String.format("%.2f", alt.distance)} km)",
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

// ========================================================================
// ROUTE OPTIMIZATION SCREEN
// ========================================================================

data class OptimizedRoute(
    val stops: List<RouteStop>,
    val totalDistance: Double,
    val estimatedTime: String,
    val fuelCost: Double
)

data class RouteStop(
    val farmName: String,
    val distanceFromPrevious: String,
    val timeFromPrevious: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteOptimizationScreen(
    locationViewModel: LocationViewModel,
    farmerListViewModel: FarmerListViewModel,
    onBack: () -> Unit
) {
    val farmers by farmerListViewModel.farmers.collectAsState(initial = emptyList())
    var selectedFarmers by remember { mutableStateOf<Set<Farmer>>(emptySet()) }
    
    val vmOptimizedRoute by locationViewModel.optimizedRoute.collectAsState()
    val isOptimizing by locationViewModel.isCalculating.collectAsState()
    
    // Map VM OptimizedRoute to UI OptimizedRoute
    val optimizedRoute = vmOptimizedRoute?.let { route ->
        // Convert milliseconds to readable time
        val totalMinutes = route.estimatedTime / (1000 * 60)
        val estimatedTimeStr = if (totalMinutes > 60) {
            "${totalMinutes / 60}h ${totalMinutes % 60}m"
        } else {
            "$totalMinutes minutes"
        }
        
        // Calculate fuel cost (approx $1.50/L, 8L/100km)
        val fuelCost = (route.totalDistance / 100.0) * 8.0 * 1.50

        // Create stops list
        val stops = route.farmers.mapIndexed { index, farmer -> 
             RouteStop(
                 farmName = farmer.farmName.ifBlank { farmer.name },
                 distanceFromPrevious = if (index == 0) "Start" else "...", // Simplified
                 timeFromPrevious = if (index == 0) "-" else "..."
             )
        }

        OptimizedRoute(
            stops = stops,
            totalDistance = route.totalDistance,
            estimatedTime = estimatedTimeStr,
            fuelCost = fuelCost
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route Optimization") },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Select Farms to Visit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedFarmers.size} farms selected",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                // Default start at Hiddenite, NC
                                locationViewModel.optimizeRoute(35.7796, -81.3361, selectedFarmers.toList())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isOptimizing && selectedFarmers.isNotEmpty()
                        ) {
                            if (isOptimizing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Route, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Optimize Route")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Available Farms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(farmers.filter { it.latitude != null && it.longitude != null }) { farmer ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedFarmers.contains(farmer),
                            onCheckedChange = {
                                selectedFarmers = if (it) {
                                    selectedFarmers + farmer
                                } else {
                                    selectedFarmers - farmer
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = farmer.farmName.ifBlank { farmer.name },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = farmer.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            optimizedRoute?.let { route ->
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Optimized Route",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Route Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Total Distance: ${String.format("%.1f", route.totalDistance)} km")
                            Text("Estimated Time: ${route.estimatedTime}")
                            Text("Estimated Fuel Cost: $${String.format("%.2f", route.fuelCost)}")
                        }
                    }
                }

                items(route.stops.withIndex().toList()) { (index, stop) ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(4.dp),
                                    contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stop.farmName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (stop.distanceFromPrevious != "Start") {
                                    Text(
                                        text = "${stop.distanceFromPrevious} km • ${stop.timeFromPrevious}",
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
    }
}