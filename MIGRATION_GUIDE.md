# FarmDirectoryPro Migration Guide - Refactored ViewModels

## Overview

This guide explains how to update your MainActivity and related files to use the new refactored ViewModels.

## What's Changed

### Old Architecture (Monolithic)
```
FarmerViewModel (1200+ lines)
├── Farmer CRUD
├── Attendance tracking
├── Employee management
├── WebSocket communication
├── Route optimization
├── Location tracking
└── Activity logging
```

### New Architecture (Specialized)
```
FarmerListViewModel → Farmer CRUD + Search/Filter
AttendanceViewModel → Check-in/out + Employee management
LocationViewModel → GPS + Route optimization
WebSocketViewModel → Real-time backend communication
```

---

## Step 1: Update ViewModel Imports in MainActivity.kt

### Replace these imports:
```kotlin
// OLD (DELETE THESE)
import com.example.farmdirectoryupgraded.viewmodel.FarmerViewModel
import com.example.farmdirectoryupgraded.viewmodel.FarmerViewModelFactory
```

### With these new imports:
```kotlin
// NEW (ADD THESE)
import com.example.farmdirectoryupgraded.viewmodel.FarmerListViewModel
import com.example.farmdirectoryupgraded.viewmodel.AttendanceViewModel
import com.example.farmdirectoryupgraded.viewmodel.LocationViewModel
import com.example.farmdirectoryupgraded.viewmodel.WebSocketViewModel
import com.example.farmdirectoryupgraded.viewmodel.FarmViewModelFactory
```

---

## Step 2: Update FarmDirectoryApp Composable

### OLD Implementation:
```kotlin
@Composable
fun FarmDirectoryApp() {
    val context = LocalContext.current
    val database = remember { FarmDatabase.getDatabase(context) }
    val viewModel: FarmerViewModel = viewModel(
        factory = FarmerViewModelFactory(
            context = context,
            farmerDao = database.farmerDao(),
            attendanceDao = database.attendanceDao(),
            logDao = database.logDao()
        )
    )

    // Using single viewModel...
    val connectionState by viewModel.connectionState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    // ... etc
}
```

### NEW Implementation:
```kotlin
@Composable
fun FarmDirectoryApp() {
    val context = LocalContext.current
    val database = remember { FarmDatabase.getDatabase(context) }

    // Create factory with all required DAOs
    val viewModelFactory = remember {
        FarmViewModelFactory(
            farmerDao = database.farmerDao(),
            attendanceDao = database.attendanceDao(),
            employeeDao = database.employeeDao(),
            webSocketService = FarmWebSocketService()  // Initialize WebSocket service
        )
    }

    // Create each specialized ViewModel
    val farmerListViewModel: FarmerListViewModel = viewModel(factory = viewModelFactory)
    val attendanceViewModel: AttendanceViewModel = viewModel(factory = viewModelFactory)
    val locationViewModel: LocationViewModel = viewModel(factory = viewModelFactory)
    val webSocketViewModel: WebSocketViewModel = viewModel(factory = viewModelFactory)

    // UI State
    var currentScreen by remember { mutableStateOf("list") }
    var selectedFarmer by remember { mutableStateOf<Farmer?>(null) }
    val appSettings = remember { AppSettings(context) }

    // WebSocket state (now from WebSocketViewModel)
    val connectionState by webSocketViewModel.connectionState.collectAsState()
    val errorMessage by webSocketViewModel.errorMessage.collectAsState()
    val healthAlerts by webSocketViewModel.healthAlerts.collectAsState()

    // Farmer list state (from FarmerListViewModel)
    val farmers by farmerListViewModel.farmers.collectAsState(initial = emptyList())
    val searchQuery by farmerListViewModel.searchQuery.collectAsState()
    val farmerError by farmerListViewModel.errorMessage.collectAsState()
    val farmerSuccess by farmerListViewModel.successMessage.collectAsState()

    // Attendance state (from AttendanceViewModel)
    val employees by attendanceViewModel.employees.collectAsState()
    val attendanceRecords by attendanceViewModel.attendanceRecords.collectAsState()

    // Location state (from LocationViewModel)
    val optimizedRoute by locationViewModel.optimizedRoute.collectAsState()
    val reconcileResult by locationViewModel.reconcileResult.collectAsState()

    // Connect to WebSocket on app start
    LaunchedEffect(appSettings.backendUrl) {
        if (appSettings.autoConnect) {
            webSocketViewModel.connectToBackend(
                farmId = appSettings.farmId,
                workerId = "worker-${System.currentTimeMillis()}"
            )
        }
    }

    // Error handling
    var showErrorDialog by remember { mutableStateOf(false) }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            showErrorDialog = true
        }
    }

    // Screen navigation
    when (currentScreen) {
        "list" -> FarmerListScreen(
            farmers = farmers,
            onAddFarmer = { farmerListViewModel.addFarmer(it) },
            onEditFarmer = { farmerListViewModel.updateFarmer(it) },
            onDeleteFarmer = { farmerListViewModel.deleteFarmer(it) },
            onToggleFavorite = { id, isFav -> farmerListViewModel.toggleFavorite(id, isFav) },
            onSelectFarmer = { selectedFarmer = it; currentScreen = "details" }
        )

        "attendance" -> AttendanceScreen(
            employees = employees,
            attendanceRecords = attendanceRecords,
            onCheckIn = { empId, lat, lon, loc, task, notes ->
                attendanceViewModel.checkInWithGPS(empId, lat, lon, loc, task, notes)
            },
            onCheckOut = { recordId, lat, lon, notes ->
                attendanceViewModel.checkOut(recordId, lat, lon, notes)
            }
        )

        "route" -> RouteOptimizationScreen(
            optimizedRoute = optimizedRoute,
            onOptimize = { lat, lon, farmers ->
                locationViewModel.optimizeRoute(lat, lon, farmers)
            }
        )

        "reconcile" -> ReconcileScreen(
            reconcileResult = reconcileResult,
            onReconcile = { lat, lon ->
                locationViewModel.reconcileFarm(lat, lon)
            }
        )

        "details" -> {
            if (selectedFarmer != null) {
                FarmerDetailsScreen(
                    farmer = selectedFarmer!!,
                    onEdit = { currentScreen = "edit" },
                    onBack = { currentScreen = "list"; selectedFarmer = null }
                )
            }
        }
    }

    // Error dialog
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                webSocketViewModel.clearError()
                farmerListViewModel.clearError()
                attendanceViewModel.clearError()
                locationViewModel.clearError()
            },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
```

---

## Step 3: Update Screen Composables

Each screen should now accept the specific ViewModel it needs.

### Example: FarmerListScreen

OLD:
```kotlin
@Composable
fun FarmerListScreen(viewModel: FarmerViewModel) {
    val farmers by viewModel.farmers.collectAsState()

    Button(onClick = { viewModel.addFarmer(...) })
}
```

NEW:
```kotlin
@Composable
fun FarmerListScreen(
    farmers: List<Farmer>,
    onAddFarmer: (Farmer) -> Unit,
    onEditFarmer: (Farmer) -> Unit,
    onDeleteFarmer: (Farmer) -> Unit,
    onToggleFavorite: (Int, Boolean) -> Unit,
    onSelectFarmer: (Farmer) -> Unit
) {
    LazyColumn {
        items(farmers) { farmer ->
            FarmerCard(
                farmer = farmer,
                onEdit = { onEditFarmer(farmer) },
                onDelete = { onDeleteFarmer(farmer) },
                onToggleFavorite = { onToggleFavorite(farmer.id, !farmer.isFavorite) },
                onClick = { onSelectFarmer(farmer) }
            )
        }
    }
}
```

**Benefits:**
- Screens are now pure composables (easier to test)
- No ViewModel coupling
- Composables are reusable in different contexts
- Better for preview/testing

---

## Step 4: Update AttendanceScreen

```kotlin
@Composable
fun AttendanceScreen(
    employees: List<Employee>,
    attendanceRecords: List<AttendanceRecord>,
    onCheckIn: (Int, Double, Double, String, String, String) -> Unit,
    onCheckOut: (Int, Double, Double, String) -> Unit
) {
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var showCheckInDialog by remember { mutableStateOf(false) }

    Column {
        // Employee selection
        DropdownMenu(
            expanded = true,
            onDismissRequest = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            employees.forEach { employee ->
                DropdownMenuItem(
                    text = { Text(employee.name) },
                    onClick = { selectedEmployee = employee }
                )
            }
        }

        // Check-in button
        Button(onClick = { showCheckInDialog = true }) {
            Text("Check In")
        }

        // Attendance records list
        LazyColumn {
            items(attendanceRecords) { record ->
                AttendanceRecordCard(record)
            }
        }
    }
}
```

---

## Step 5: Update LocationScreen

```kotlin
@Composable
fun RouteOptimizationScreen(
    optimizedRoute: LocationViewModel.OptimizedRoute?,
    onOptimize: (Double, Double, List<Farmer>) -> Unit
) {
    var latitude by remember { mutableStateOf(35.7796) }
    var longitude by remember { mutableStateOf(-81.3361) }

    Column {
        Text("Route Optimization")

        Button(onClick = {
            // Get current location and farmers
            // onOptimize(lat, lon, farmers)
        }) {
            Text("Optimize Route")
        }

        if (optimizedRoute != null) {
            Text("Total Distance: ${optimizedRoute.totalDistance} km")
            Text("Estimated Time: ${optimizedRoute.estimatedTime / 60000} minutes")

            LazyColumn {
                items(optimizedRoute.farmers) { farmer ->
                    FarmerCard(farmer = farmer)
                }
            }
        }
    }
}
```

---

## Step 6: Test Each Screen

After migration, test each feature:

```kotlin
// Test Farmer CRUD
farmerListViewModel.addFarmer(newFarmer)
farmerListViewModel.updateFarmer(updatedFarmer)
farmerListViewModel.deleteFarmer(farmer)

// Test Attendance
attendanceViewModel.checkInWithGPS(1, 35.7796, -81.3361)
attendanceViewModel.checkOut(1, 35.7800, -81.3370)

// Test Location
locationViewModel.optimizeRoute(35.7796, -81.3361, farmers)
locationViewModel.reconcileFarm(35.7796, -81.3361)

// Test WebSocket
webSocketViewModel.connectToBackend("farm-1", "worker-1")
webSocketViewModel.sendLocationUpdate(35.7796, -81.3361)
```

---

## Important Notes

### ViewModelFactory Updates

The new `FarmViewModelFactory` handles all ViewModel creation:

```kotlin
FarmViewModelFactory(
    farmerDao = database.farmerDao(),
    attendanceDao = database.attendanceDao(),
    employeeDao = database.employeeDao(),
    webSocketService = FarmWebSocketService()
)
```

### State Flow Updates

All ViewModels expose state via StateFlow:

```kotlin
// FarmerListViewModel
val searchQuery: StateFlow<String>
val selectedType: StateFlow<String?>
val errorMessage: StateFlow<String?>
val successMessage: StateFlow<String?>

// AttendanceViewModel
val employees: StateFlow<List<Employee>>
val selectedEmployee: StateFlow<Employee?>
val isCheckingIn: StateFlow<Boolean>

// LocationViewModel
val currentLocation: StateFlow<Location?>
val optimizedRoute: StateFlow<OptimizedRoute?>
val isCalculating: StateFlow<Boolean>

// WebSocketViewModel
val connectionState: StateFlow<ConnectionState>
val locationUpdates: StateFlow<String?>
val healthAlerts: StateFlow<String?>
```

---

## Files Modified

- `app/src/main/java/com/example/farmdirectoryupgraded/MainActivity.kt` ← Update imports and FarmDirectoryApp composable
- `app/src/main/java/com/example/farmdirectoryupgraded/ui/Screens.kt` ← Update screen composables to accept parameters instead of ViewModels
- `app/src/main/java/com/example/farmdirectoryupgraded/data/CertificatePinning.kt` ← Already updated ✅
- `app/src/main/java/com/example/farmdirectoryupgraded/data/FarmWebSocketService.kt` ← Already updated ✅

## New Files Added

- `app/src/main/java/com/example/farmdirectoryupgraded/viewmodel/FarmerListViewModel.kt` ✅
- `app/src/main/java/com/example/farmdirectoryupgraded/viewmodel/AttendanceViewModel.kt` ✅
- `app/src/main/java/com/example/farmdirectoryupgraded/viewmodel/LocationViewModel.kt` ✅
- `app/src/main/java/com/example/farmdirectoryupgraded/viewmodel/WebSocketViewModel.kt` ✅
- `app/src/main/java/com/example/farmdirectoryupgraded/viewmodel/FarmViewModelFactory.kt` ✅

---

## Compilation Issues & Fixes

### Issue: "FarmerViewModel not found"
**Fix:** Delete the old `FarmerViewModel.kt` file and `FarmerViewModelFactory.kt` (old one).

### Issue: "Method not found" in MainActivity
**Fix:** Check that you're calling the correct methods on the correct ViewModels.

### Issue: "StateFlow" import errors
**Fix:** Ensure imports include: `import kotlinx.coroutines.flow.StateFlow`

---

## Benefits of This Migration

✅ **Smaller ViewModels** - Each handles one responsibility
✅ **Easier Testing** - Mock individual ViewModels
✅ **Better Reusability** - Compose screens without ViewModels
✅ **Cleaner Architecture** - Clear separation of concerns
✅ **Easier Debugging** - State organized by feature
✅ **Improved Maintainability** - Changes don't affect other features

---

## Next Steps

1. Update imports in MainActivity.kt
2. Update FarmDirectoryApp composable
3. Update all screen composables
4. Delete old FarmerViewModel.kt
5. Test each screen thoroughly
6. Run unit tests: `./gradlew test`
7. Fix any compilation errors
8. Commit changes

---

Good luck with the migration! 🚀
