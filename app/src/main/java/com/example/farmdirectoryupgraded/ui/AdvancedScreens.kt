package com.example.farmdirectoryupgraded.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    onBack: () -> Unit,
    onManageEmployeesClick: () -> Unit = {}
) {
    val rawRecords by viewModel.attendanceRecords.collectAsState()
    val allEmployees by viewModel.employees.collectAsState()
    val employees = remember(allEmployees) { allEmployees.filter { it.isActive } }
    val selectedEmployee by viewModel.selectedEmployee.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val dateFormatter = remember { java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()) }

    val attendanceRecords = remember(rawRecords) {
        rawRecords.map { record ->
            AttendanceRecord(
                id = record.id,
                farmName = record.workLocation.ifBlank { "Unknown" },
                method = record.method,
                timestamp = dateFormatter.format(java.util.Date(record.checkInTime)),
                notes = record.notes,
                checkOut = record.checkOutTime?.let { dateFormatter.format(java.util.Date(it)) }
            )
        }
    }

    var selectedMethod by rememberSaveable { mutableStateOf(AttendanceMethod.GPS) }
    var farmName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Tracking") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onManageEmployeesClick) {
                        Icon(Icons.Default.Group, contentDescription = "Manage Employees")
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
            // Success/error messages
            if (successMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(successMessage!!)
                        }
                    }
                }
            }
            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss")
                            }
                        }
                    }
                }
            }

            // Employee selection section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                                text = "Employee",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = onManageEmployeesClick) {
                                Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Manage")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (employees.isEmpty()) {
                            Text(
                                text = "No employees found. Tap 'Manage' to add employees first.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            Text(
                                text = "Select who is checking in:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            employees.forEach { employee ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedEmployee?.id == employee.id,
                                        onClick = { viewModel.selectEmployee(employee) }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = employee.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (selectedEmployee?.id == employee.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = employee.role,
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

            // Check-in form
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

                        val canCheckIn = farmName.isNotBlank() && selectedEmployee != null
                        if (!canCheckIn && farmName.isNotBlank() && selectedEmployee == null) {
                            Text(
                                text = "Select an employee above before checking in.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        val gpsAvailable = viewModel.lastKnownLocation != null
                        if (!gpsAvailable) {
                            Text(
                                text = "GPS unavailable — check-in will be recorded without coordinates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = {
                                val emp = selectedEmployee ?: return@Button
                                val loc = viewModel.lastKnownLocation
                                // Record check-in; coordinates are null-safe — the DB stores
                                // them as nullable and (null) is preferable to a fake (0, 0).
                                // Until GPS is integrated, use 0.0 only as an explicit placeholder
                                // that is already documented as "no GPS" in the notes field.
                                val (lat, lon) = loc ?: (0.0 to 0.0)
                                val checkInNotes = if (loc == null && notes.isBlank()) {
                                    "Manual check-in (GPS unavailable)"
                                } else if (loc == null) {
                                    "$notes [GPS unavailable]"
                                } else {
                                    notes
                                }
                                viewModel.checkInWithGPS(
                                    employeeId = emp.id,
                                    latitude = lat,
                                    longitude = lon,
                                    workLocation = farmName,
                                    notes = checkInNotes
                                )
                                farmName = ""
                                notes = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canCheckIn
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

            items(attendanceRecords, key = { it.id }) { record ->
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
    // Use the ViewModel's isCalculating so the spinner correctly reflects async completion
    val isCalculating by viewModel.isCalculating.collectAsState()
    
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
                                val loc = viewModel.getCurrentLocation()
                                if (loc != null) {
                                    currentLocation = Pair(loc.latitude, loc.longitude)
                                    viewModel.reconcileFarm(loc.latitude, loc.longitude)
                                } else {
                                    // Fallback location for testing (Hiddenite, NC)
                                    val mockLat = 35.7796
                                    val mockLon = -81.3361
                                    currentLocation = Pair(mockLat, mockLon)
                                    viewModel.reconcileFarm(mockLat, mockLon)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isCalculating
                        ) {
                            if (isCalculating) {
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
    // Store farmer IDs instead of full Farmer objects for lighter equality checks
    var selectedFarmers by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
    val vmOptimizedRoute by locationViewModel.optimizedRoute.collectAsState()
    val isOptimizing by locationViewModel.isCalculating.collectAsState()

    // Pre-filter farmers with GPS coordinates; only recompute when farmers list changes
    val locatableFarmers = remember(farmers) { farmers.filter { it.latitude != null && it.longitude != null } }

    // Memoize expensive route formatting; only recompute when the VM route changes
    val optimizedRoute = remember(vmOptimizedRoute) {
        vmOptimizedRoute?.let { route ->
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
            val stops = route.farmers.mapIndexed { index, f: Farmer ->
                RouteStop(
                    farmName = f.farmName.ifBlank { f.name },
                    distanceFromPrevious = if (index == 0) "Start" else "...",
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
                                // Reconstruct the selected Farmer objects from IDs
                                locationViewModel.optimizeRoute(
                                    35.7796,
                                    -81.3361,
                                    farmers.filter { selectedFarmers.contains(it.id) }
                                )
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

            items(locatableFarmers, key = { it.id }) { farmer ->
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
                            checked = selectedFarmers.contains(farmer.id),
                            onCheckedChange = { isChecked ->
                                selectedFarmers = if (isChecked) {
                                    selectedFarmers + farmer.id
                                } else {
                                    selectedFarmers - farmer.id
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

                items(route.stops.withIndex().toList(), key = { (index, _) -> index }) { (index, stop) ->
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