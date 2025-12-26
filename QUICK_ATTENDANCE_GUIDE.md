# Quick Start: Employee Attendance Features

## What Was Fixed

| Issue | Status | Solution |
|-------|--------|----------|
| GPS not correct | ✓ FIXED | Now uses WebSocket location updates |
| Attendance not changed | ✓ FIXED | Added check-out functionality |
| QR in attendance doesn't work | ✓ FIXED | Integrated ML Kit QR scanning |
| Route optimization doesn't work | ✓ FIXED | Improved algorithm with realistic estimates |

---

## How To Use

### 1. Basic Employee Check-In/Check-Out

```kotlin
// Step 1: Get list of employees
val employees = viewModel.employees.value

// Step 2: Select an employee
viewModel.selectEmployee(employees[0])

// Step 3: Check in
viewModel.recordAttendance(
    method = AttendanceMethod.MANUAL,
    workLocation = "Farm A",
    notes = "Morning shift"
)

// Step 4: Later... Check out
viewModel.checkOutAttendance()
```

**Result**:
- ✓ Employee checked in
- ✓ Hours automatically calculated on check-out
- ✓ Location and task logged
- ✓ All data saved to database

---

### 2. QR Code Check-In

```kotlin
// When QR code is scanned, get the raw data
val qrData = "EMP:123|FARM:John's Farm|TASK:Harvesting"

// Check in with QR code
viewModel.checkInWithQRCode(qrData)

// User feedback
val success = viewModel.successMessage.value
val error = viewModel.errorMessage.value
```

**QR Code Format**:
```
EMP:123|FARM:WorkLocation|TASK:TaskName
```

---

### 3. GPS Reconciliation (Find Nearest Farm)

```kotlin
// Get current GPS location
viewModel.getCurrentLocation { lat, lon ->
    // Get nearest farm at this location
    viewModel.reconcileFarm(lat, lon) { result ->
        println("Nearest farm: ${result.farmName}")
        println("Distance: ${result.distance} km")
        println("Confidence: ${result.confidence}%")
        println("Alternatives: ${result.alternatives}")
    }
}
```

---

### 4. Route Optimization

```kotlin
// Select farms to optimize
val selectedFarms = listOf(farm1, farm2, farm3, farm4)

// Optimize route
viewModel.optimizeRoute(selectedFarms) { route ->
    println("Total Distance: ${route.totalDistance} km")
    println("Estimated Time: ${route.estimatedTime}")
    println("Fuel Cost: \$${String.format("%.2f", route.fuelCost)}")

    route.stops.forEach { stop ->
        println("→ ${stop.farmName}")
        println("  Distance: ${stop.distanceFromPrevious}")
        println("  Time: ${stop.timeFromPrevious}")
    }
}
```

---

### 5. Employee Management

```kotlin
// Add new employee
viewModel.addEmployee(
    name = "John Doe",
    role = "CATCHER",
    phone = "555-1234",
    email = "john@farm.com"
)

// Update employee
val updatedEmployee = employee.copy(phone = "555-5678")
viewModel.updateEmployee(updatedEmployee)

// Deactivate employee
viewModel.deactivateEmployee(employeeId = 123)
```

---

## UI Integration Example

```kotlin
@Composable
fun AttendanceScreen(viewModel: FarmerViewModel) {
    val employees by viewModel.employees.collectAsState()
    val selectedEmployee by viewModel.selectedEmployee.collectAsState()
    val successMsg by viewModel.successMessage.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()

    // Employee Selection
    LazyRow {
        items(employees) { employee ->
            Button(
                onClick = { viewModel.selectEmployee(employee) },
                colors = if (selectedEmployee?.id == employee.id)
                    ButtonDefaults.buttonColors()
                else
                    ButtonDefaults.outlinedButtonColors()
            ) {
                Text(employee.name)
            }
        }
    }

    // Check In Button
    Button(
        onClick = {
            viewModel.recordAttendance(
                method = AttendanceMethod.MANUAL,
                workLocation = farmName,
                notes = notes
            )
        },
        enabled = selectedEmployee != null
    ) {
        Text("Check In")
    }

    // Check Out Button
    Button(
        onClick = { viewModel.checkOutAttendance() },
        enabled = selectedEmployee != null
    ) {
        Text("Check Out")
    }

    // Show messages
    successMsg?.let { Text("✓ $it", color = Color.Green) }
    errorMsg?.let { Text("✗ $it", color = Color.Red) }
}
```

---

## Database Schema

### Attendance Records Table
```
attendance_records:
- id (PK, auto-increment)
- employeeId (FK)
- employeeName (string)
- employeeRole (string)
- method (GPS, QR_CODE, MANUAL, NFC, PHOTO, BIOMETRIC, BLUETOOTH)
- checkInTime (long, timestamp)
- checkOutTime (long, nullable)
- hoursWorked (double, nullable)
- checkInLatitude (double, nullable)
- checkInLongitude (double, nullable)
- checkOutLatitude (double, nullable)
- checkOutLongitude (double, nullable)
- workLocation (string)
- taskDescription (string)
- notes (string)
- photoPath (string, nullable)
- createdAt (long, timestamp)
```

### Employees Table
```
employees:
- id (PK, auto-increment)
- name (string)
- role (string: CATCHER, DRIVER, SUPERVISOR, ADMIN, OTHER)
- phone (string)
- email (string)
- photoPath (string, nullable)
- isActive (boolean)
- hireDate (long, nullable)
- notes (string)
- version (int, for optimistic locking)
```

---

## Logging

All activities are logged to the database. Check logs with:

```kotlin
val logs by viewModel.logs.collectAsState()

logs.forEach { log ->
    println("[${log.timestamp}] ${log.category}/${log.level}: ${log.message}")
    println("  Details: ${log.details}")
}
```

---

## Error Handling

Always check error/success messages:

```kotlin
val errorMsg by viewModel.errorMessage.collectAsState()
val successMsg by viewModel.successMessage.collectAsState()

LaunchedEffect(errorMsg) {
    errorMsg?.let { error ->
        // Show error to user
        showSnackbar(error)
        // Clear message
        viewModel.clearError()
    }
}

LaunchedEffect(successMsg) {
    successMsg?.let { msg ->
        // Show success to user
        showSnackbar(msg)
        // Clear message
        viewModel.clearSuccess()
    }
}
```

---

## Common Patterns

### Pattern 1: Full Check-In/Check-Out Workflow
```kotlin
// User selects employee
viewModel.selectEmployee(employee)

// User checks in at farm
viewModel.recordAttendance(
    method = AttendanceMethod.MANUAL,
    workLocation = "Farm A",
    notes = "Catcher - Morning shift"
)

// Several hours later...
// User checks out
viewModel.checkOutAttendance()

// App automatically:
// ✓ Calculates hours worked
// ✓ Updates database
// ✓ Saves location data
// ✓ Logs the activity
```

### Pattern 2: QR Code Attendance
```kotlin
// QR code reader scans: EMP:42|FARM:Alpha Farm|TASK:Harvesting

// App automatically:
viewModel.checkInWithQRCode(qrData)

// Result:
// ✓ Finds employee #42
// ✓ Checks in at Alpha Farm
// ✓ Marks task as Harvesting
// ✓ Logs QR-based check-in
```

### Pattern 3: Route Planning
```kotlin
// Manager wants to optimize visits
val farmsToVisit = listOf(farm1, farm2, farm3)

viewModel.optimizeRoute(farmsToVisit) { route ->
    // Display optimized route
    displayRoute(route)
}

// App calculates:
// ✓ Optimal order (nearest-neighbor)
// ✓ Distance between stops
// ✓ Estimated travel time (30 km/h avg)
// ✓ Fuel costs (8 L/100km @ $1.50/L)
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Employee not found in QR | Check QR format: `EMP:ID\|FARM:Name\|TASK:Task` |
| Check-out fails | Ensure employee selected and has active check-in |
| GPS returns null | Check WebSocket connection and location permissions |
| Route shows error | Verify farms have lat/lon coordinates |
| No employees showing | Add employees via `addEmployee()` first |

---

## Performance Notes

- **Employees list**: Loaded automatically on app start
- **Attendance records**: Loaded in real-time from database
- **Route optimization**: Fast for up to 20 farms (O(n²))
- **GPS**: Uses WebSocket updates (near real-time)
- **Logging**: All activities logged (check database size)

---

## Next Steps

After implementing these fixes:

1. ✓ Test with real employee data
2. ✓ Connect QR code scanner hardware
3. ✓ Enable device GPS permissions
4. ✓ Set up WebSocket location broadcasts
5. ✓ Configure backend API for attendance sync
6. ✓ Add attendance reports/analytics
7. ✓ Implement photo attendance verification

---

**Last Updated**: 2025-12-26
