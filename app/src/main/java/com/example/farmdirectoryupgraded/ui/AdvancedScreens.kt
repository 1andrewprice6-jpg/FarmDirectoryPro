package com.example.farmdirectoryupgraded.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.farmdirectoryupgraded.data.Employee
import com.example.farmdirectoryupgraded.data.Farmer
import com.example.farmdirectoryupgraded.data.FuelLog
import com.example.farmdirectoryupgraded.data.VehicleLog
import com.example.farmdirectoryupgraded.viewmodel.AttendanceViewModel
import com.example.farmdirectoryupgraded.viewmodel.FuelLogViewModel
import com.example.farmdirectoryupgraded.viewmodel.LocationViewModel
import com.example.farmdirectoryupgraded.viewmodel.FarmerListViewModel
import com.example.farmdirectoryupgraded.viewmodel.VehicleLogViewModel

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
    val rawRecords by viewModel.attendanceRecords.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val selectedEmployee by viewModel.selectedEmployee.collectAsState()

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
    var employeeDropdownExpanded by remember { mutableStateOf(false) }
    var showEmployeeError by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

                        Text("Employee", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = employeeDropdownExpanded,
                            onExpandedChange = { employeeDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedEmployee?.let { "${it.name} (${it.role})" } ?: "Select Employee",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = employeeDropdownExpanded) },
                                isError = showEmployeeError && selectedEmployee == null
                            )
                            ExposedDropdownMenu(
                                expanded = employeeDropdownExpanded,
                                onDismissRequest = { employeeDropdownExpanded = false }
                            ) {
                                employees.forEach { emp ->
                                    DropdownMenuItem(
                                        text = { Text("${emp.name} (${emp.role})") },
                                        onClick = {
                                            viewModel.selectEmployee(emp)
                                            showEmployeeError = false
                                            employeeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        if (showEmployeeError && selectedEmployee == null) {
                            Text(
                                text = "Please select an employee",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
                                val emp = selectedEmployee
                                if (emp == null) {
                                    showEmployeeError = true
                                    return@Button
                                }
                                if (selectedMethod == AttendanceMethod.GPS) {
                                    val loc = viewModel.lastKnownLocation
                                    if (loc != null) {
                                        viewModel.checkInWithGPS(
                                            employeeId = emp.id,
                                            latitude = loc.first,
                                            longitude = loc.second,
                                            workLocation = farmName,
                                            notes = notes
                                        )
                                    } else {
                                        viewModel.checkInManual(
                                            employeeId = emp.id,
                                            workLocation = farmName,
                                            notes = if (notes.isBlank()) "GPS unavailable" else "$notes (GPS unavailable)"
                                        )
                                    }
                                } else {
                                    viewModel.checkInManual(
                                        employeeId = emp.id,
                                        workLocation = farmName,
                                        notes = notes
                                    )
                                }
                                farmName = ""
                                notes = ""
                            },
                            modifier = Modifier.fillMaxWidth()
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
                AttendanceRecordCard(
                    record = record,
                    onCheckOut = { id -> viewModel.checkOut(id, 0.0, 0.0) }
                )
            }
        }
    }
}

@Composable
fun AttendanceRecordCard(record: AttendanceRecord, onCheckOut: (Int) -> Unit) {
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
            if (record.checkOut != null) {
                Text(
                    text = "Checked out: ${record.checkOut}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (record.notes.isNotEmpty()) {
                Text(
                    text = record.notes,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (record.checkOut == null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onCheckOut(record.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Check Out")
                }
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
// ========================================================================
// FUEL LOG SCREEN
// ========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelLogScreen(
    viewModel: FuelLogViewModel,
    onBack: () -> Unit
) {
    val fuelLogs by viewModel.fuelLogs.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(successMessage) {
        successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSuccess() }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    val dateFormatter = remember { java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fuel Logs") },
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
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Fuel Log")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (fuelLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No fuel logs yet. Tap + to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(fuelLogs, key = { it.id }) { log ->
                Card(modifier = Modifier.fillMaxWidth()) {
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
                            Text(
                                text = log.vehicleName.ifBlank { log.vehicleId },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.deleteFuelLog(log) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Text("Driver: ${log.driverName.ifBlank { "—" }}", style = MaterialTheme.typography.bodySmall)
                        Text("${log.fuelType} • ${log.quantity} gal • \$${String.format("%.2f", log.totalCost)}", style = MaterialTheme.typography.bodySmall)
                        if (log.station.isNotBlank()) {
                            Text("Station: ${log.station}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            dateFormatter.format(java.util.Date(log.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (log.notes.isNotBlank()) {
                            Text(log.notes, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        FuelLogAddDialog(
            onDismiss = { showAddDialog = false },
            onSave = { log ->
                viewModel.addFuelLog(log)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelLogAddDialog(
    onDismiss: () -> Unit,
    onSave: (FuelLog) -> Unit
) {
    var vehicleId by remember { mutableStateOf("") }
    var vehicleName by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("GASOLINE") }
    var fuelTypeExpanded by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var station by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val fuelTypes = listOf("GASOLINE", "DIESEL", "E85", "CNG", "ELECTRIC", "OTHER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Fuel Log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = vehicleId, onValueChange = { vehicleId = it }, label = { Text("Vehicle ID") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = vehicleName, onValueChange = { vehicleName = it }, label = { Text("Vehicle Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = driverName, onValueChange = { driverName = it }, label = { Text("Driver Name") }, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = fuelTypeExpanded, onExpandedChange = { fuelTypeExpanded = it }) {
                    OutlinedTextField(
                        value = fuelType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fuel Type") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelTypeExpanded) }
                    )
                    ExposedDropdownMenu(expanded = fuelTypeExpanded, onDismissRequest = { fuelTypeExpanded = false }) {
                        fuelTypes.forEach { type ->
                            DropdownMenuItem(text = { Text(type) }, onClick = { fuelType = type; fuelTypeExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity (gallons)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = unitPrice, onValueChange = { unitPrice = it }, label = { Text("Unit Price (\$)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = station, onValueChange = { station = it }, label = { Text("Station") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toDoubleOrNull() ?: 0.0
                    val price = unitPrice.toDoubleOrNull() ?: 0.0
                    onSave(
                        FuelLog(
                            vehicleId = vehicleId,
                            vehicleName = vehicleName,
                            driverName = driverName,
                            timestamp = System.currentTimeMillis(),
                            fuelType = fuelType,
                            quantity = qty,
                            unitPrice = price,
                            totalCost = qty * price,
                            station = station,
                            notes = notes
                        )
                    )
                },
                enabled = vehicleId.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ========================================================================
// VEHICLE LOG SCREEN
// ========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleLogScreen(
    viewModel: VehicleLogViewModel,
    onBack: () -> Unit
) {
    val vehicleLogs by viewModel.vehicleLogs.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(successMessage) {
        successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSuccess() }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    val dateFormatter = remember { java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicle Logs") },
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
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Vehicle Log")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (vehicleLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No vehicle logs yet. Tap + to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(vehicleLogs, key = { it.id }) { log ->
                Card(modifier = Modifier.fillMaxWidth()) {
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
                            Text(
                                text = log.vehicleName.ifBlank { log.vehicleId },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            AssistChip(onClick = {}, label = { Text(log.logType) })
                            IconButton(onClick = { viewModel.deleteVehicleLog(log) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Text("Driver: ${log.driverName.ifBlank { "—" }}", style = MaterialTheme.typography.bodySmall)
                        if (log.startLocation.isNotBlank() || log.endLocation.isNotBlank()) {
                            Text("${log.startLocation} → ${log.endLocation}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (log.maintenanceType.isNotBlank()) {
                            Text("Maintenance: ${log.maintenanceType}${log.cost?.let { " (\$${String.format("%.2f", it)})" } ?: ""}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            dateFormatter.format(java.util.Date(log.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (log.notes.isNotBlank()) {
                            Text(log.notes, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        VehicleLogAddDialog(
            onDismiss = { showAddDialog = false },
            onSave = { log ->
                viewModel.addVehicleLog(log)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleLogAddDialog(
    onDismiss: () -> Unit,
    onSave: (VehicleLog) -> Unit
) {
    var vehicleId by remember { mutableStateOf("") }
    var vehicleName by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var logType by remember { mutableStateOf("TRIP_START") }
    var logTypeExpanded by remember { mutableStateOf(false) }
    var startLocation by remember { mutableStateOf("") }
    var endLocation by remember { mutableStateOf("") }
    var maintenanceType by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val logTypes = listOf("TRIP_START", "TRIP_END", "MAINTENANCE", "INSPECTION", "REPAIR", "OTHER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Vehicle Log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = vehicleId, onValueChange = { vehicleId = it }, label = { Text("Vehicle ID") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = vehicleName, onValueChange = { vehicleName = it }, label = { Text("Vehicle Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = driverName, onValueChange = { driverName = it }, label = { Text("Driver Name") }, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = logTypeExpanded, onExpandedChange = { logTypeExpanded = it }) {
                    OutlinedTextField(
                        value = logType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Log Type") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = logTypeExpanded) }
                    )
                    ExposedDropdownMenu(expanded = logTypeExpanded, onDismissRequest = { logTypeExpanded = false }) {
                        logTypes.forEach { type ->
                            DropdownMenuItem(text = { Text(type) }, onClick = { logType = type; logTypeExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = startLocation, onValueChange = { startLocation = it }, label = { Text("Start Location") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = endLocation, onValueChange = { endLocation = it }, label = { Text("End Location") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = maintenanceType, onValueChange = { maintenanceType = it }, label = { Text("Maintenance Type") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost (\$)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        VehicleLog(
                            vehicleId = vehicleId,
                            vehicleName = vehicleName,
                            driverName = driverName,
                            logType = logType,
                            timestamp = System.currentTimeMillis(),
                            startLocation = startLocation,
                            endLocation = endLocation,
                            maintenanceType = maintenanceType,
                            cost = cost.toDoubleOrNull(),
                            notes = notes
                        )
                    )
                },
                enabled = vehicleId.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ========================================================================
// EMPLOYEE LIST SCREEN
// ========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit
) {
    val employees by viewModel.employees.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(successMessage) {
        successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSuccess() }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Employees") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Employee")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (employees.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No employees yet. Tap + to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(employees, key = { it.id }) { employee ->
                Card(modifier = Modifier.fillMaxWidth()) {
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = employee.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(employee.role, style = MaterialTheme.typography.bodySmall)
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text(if (employee.isActive) "Active" else "Inactive") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (employee.isActive)
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                        if (employee.phone.isNotBlank()) {
                            Text("Phone: ${employee.phone}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (employee.email.isNotBlank()) {
                            Text("Email: ${employee.email}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (employee.isActive) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.deactivateEmployee(employee.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Deactivate")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddDialog = false },
            onSave = { employee ->
                viewModel.addEmployee(employee)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeeDialog(
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("CATCHER") }
    var roleExpanded by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val roles = listOf("CATCHER", "DRIVER", "SUPERVISOR", "ADMIN", "OTHER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Employee") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) }
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        roles.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { role = r; roleExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(Employee(name = name, role = role, phone = phone, email = email, notes = notes))
                },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
