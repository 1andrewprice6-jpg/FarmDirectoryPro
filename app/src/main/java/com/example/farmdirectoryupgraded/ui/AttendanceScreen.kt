package com.example.farmdirectoryupgraded.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.farmdirectoryupgraded.viewmodel.FarmerViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Attendance Tracking Screen
 * Multiple check-in/check-out methods
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf<AttendanceMethod?>(null) }
    var showCheckInDialog by remember { mutableStateOf(false) }
    val attendanceRecords by viewModel.attendanceRecords.collectAsState()

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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCheckInDialog = true },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                text = { Text("Check In") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Attendance Methods",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Attendance Methods Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceMethodCard(
                    icon = Icons.Default.LocationOn,
                    title = "GPS Check-in",
                    description = "Auto-record when entering farm",
                    enabled = true,
                    onClick = {
                        selectedMethod = AttendanceMethod.GPS
                        showCheckInDialog = true
                    }
                )

                AttendanceMethodCard(
                    icon = Icons.Default.QrCode,
                    title = "QR Code Scan",
                    description = "Scan farm QR code",
                    enabled = true,
                    onClick = {
                        selectedMethod = AttendanceMethod.QR_CODE
                        showCheckInDialog = true
                    }
                )

                AttendanceMethodCard(
                    icon = Icons.Default.Edit,
                    title = "Manual Entry",
                    description = "Traditional time logging",
                    enabled = true,
                    onClick = {
                        selectedMethod = AttendanceMethod.MANUAL
                        showCheckInDialog = true
                    }
                )

                AttendanceMethodCard(
                    icon = Icons.Default.Nfc,
                    title = "NFC Tag",
                    description = "Tap NFC tag at entrance",
                    enabled = false,
                    onClick = {
                        selectedMethod = AttendanceMethod.NFC
                    }
                )

                AttendanceMethodCard(
                    icon = Icons.Default.CameraAlt,
                    title = "Photo Verification",
                    description = "Take photo with GPS stamp",
                    enabled = true,
                    onClick = {
                        selectedMethod = AttendanceMethod.PHOTO
                        showCheckInDialog = true
                    }
                )

                AttendanceMethodCard(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric",
                    description = "Fingerprint/face recognition",
                    enabled = false,
                    onClick = {
                        selectedMethod = AttendanceMethod.BIOMETRIC
                    }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Recent Attendance
            Text(
                text = "Recent Attendance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (attendanceRecords.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No attendance records yet",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(attendanceRecords) { record ->
                        AttendanceRecordCard(record)
                    }
                }
            }
        }
    }

    // Check-in Dialog
    if (showCheckInDialog && selectedMethod != null) {
        CheckInDialog(
            method = selectedMethod!!,
            onDismiss = { showCheckInDialog = false },
            onCheckIn = { farmName, notes ->
                viewModel.recordAttendance(selectedMethod!!, farmName, notes)
                showCheckInDialog = false
            }
        )
    }
}

@Composable
fun AttendanceMethodCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (enabled) onClick else { {} },
        enabled = enabled
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
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                if (!enabled) {
                    Text(
                        text = "Coming Soon",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            if (enabled) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AttendanceRecordCard(record: AttendanceRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.farmName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${record.method} • ${record.timestamp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (record.notes.isNotBlank()) {
                    Text(
                        text = record.notes,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            AssistChip(
                onClick = { },
                label = { Text(if (record.checkOut == null) "Active" else "Complete") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (record.checkOut == null)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }
}

@Composable
fun CheckInDialog(
    method: AttendanceMethod,
    onDismiss: () -> Unit,
    onCheckIn: (String, String) -> Unit
) {
    var farmName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Check In - ${method.displayName}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = farmName,
                    onValueChange = { farmName = it },
                    label = { Text("Farm Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                when (method) {
                    AttendanceMethod.GPS -> Text(
                        "GPS location will be recorded automatically",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    AttendanceMethod.QR_CODE -> Text(
                        "Scan the farm QR code to complete check-in",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    AttendanceMethod.PHOTO -> Text(
                        "Photo will be captured with GPS coordinates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCheckIn(farmName, notes)
                },
                enabled = farmName.isNotBlank()
            ) {
                Text("Check In")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

enum class AttendanceMethod(val displayName: String) {
    GPS("GPS Check-in"),
    QR_CODE("QR Code Scan"),
    MANUAL("Manual Entry"),
    NFC("NFC Tag"),
    PHOTO("Photo Verification"),
    BIOMETRIC("Biometric")
}

data class AttendanceRecord(
    val id: Int,
    val farmName: String,
    val method: String,
    val timestamp: String,
    val notes: String,
    val checkOut: String?
)
