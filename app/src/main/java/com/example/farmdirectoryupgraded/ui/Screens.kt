package com.example.farmdirectoryupgraded.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.farmdirectoryupgraded.data.AppSettings
import com.example.farmdirectoryupgraded.data.Farmer
import com.example.farmdirectoryupgraded.viewmodel.FarmerViewModel

// ============================================================================
// SETTINGS SCREEN
// ============================================================================

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

    val isConnected by viewModel.isConnected.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Backend Connection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = backendUrl,
                            onValueChange = { backendUrl = it },
                            label = { Text("Backend URL") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Cloud, null) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Connection Status:")
                            AssistChip(
                                onClick = { },
                                label = { Text(if (isConnected) "Connected" else "Disconnected") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Circle,
                                        null,
                                        tint = if (isConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }

            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Farm Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = farmId,
                            onValueChange = { farmId = it },
                            label = { Text("Farm ID") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Agriculture, null) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = workerName,
                            onValueChange = { workerName = it },
                            label = { Text("Worker Name") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            settings.resetToDefaults()
                            backendUrl = settings.backendUrl
                            farmId = settings.farmId
                            workerName = settings.workerName
                            autoConnect = settings.autoConnect
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset to Defaults")
                    }

                    Button(
                        onClick = {
                            settings.backendUrl = backendUrl
                            settings.farmId = farmId
                            settings.workerName = workerName
                            settings.autoConnect = autoConnect
                            onBack()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Settings")
                    }
                }
            }
        }
    }
}

// ============================================================================
// ADD FARMER SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFarmerScreen(
    onSave: (Farmer) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var spouse by remember { mutableStateOf("") }
    var farmName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cellPhone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Farmer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank() && address.isNotBlank() && phone.isNotBlank()) {
                                onSave(
                                    Farmer(
                                        name = name,
                                        spouse = spouse,
                                        farmName = farmName,
                                        address = address,
                                        phone = phone,
                                        cellPhone = cellPhone,
                                        email = email,
                                        type = type
                                    )
                                )
                            }
                        },
                        enabled = name.isNotBlank() && address.isNotBlank() && phone.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )
            }

            item {
                OutlinedTextField(
                    value = spouse,
                    onValueChange = { spouse = it },
                    label = { Text("Spouse") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.People, null) }
                )
            }

            item {
                OutlinedTextField(
                    value = farmName,
                    onValueChange = { farmName = it },
                    label = { Text("Farm Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Agriculture, null) }
                )
            }

            item {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Place, null) },
                    minLines = 2
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            item {
                OutlinedTextField(
                    value = cellPhone,
                    onValueChange = { cellPhone = it },
                    label = { Text("Cell Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }

            item {
                var expanded by remember { mutableStateOf(false) }
                val types = listOf("", "Pullet", "Breeder")

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.Category, null) }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        types.forEach { farmerType ->
                            DropdownMenuItem(
                                text = { Text(farmerType.ifEmpty { "None" }) },
                                onClick = {
                                    type = farmerType
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "* Required fields",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// EDIT FARMER SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFarmerScreen(
    farmer: Farmer,
    onSave: (Farmer) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(farmer.name) }
    var spouse by remember { mutableStateOf(farmer.spouse) }
    var farmName by remember { mutableStateOf(farmer.farmName) }
    var address by remember { mutableStateOf(farmer.address) }
    var phone by remember { mutableStateOf(farmer.phone) }
    var cellPhone by remember { mutableStateOf(farmer.cellPhone) }
    var email by remember { mutableStateOf(farmer.email) }
    var type by remember { mutableStateOf(farmer.type) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Farmer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onSave(
                                farmer.copy(
                                    name = name,
                                    spouse = spouse,
                                    farmName = farmName,
                                    address = address,
                                    phone = phone,
                                    cellPhone = cellPhone,
                                    email = email,
                                    type = type
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )
            }

            item {
                OutlinedTextField(
                    value = spouse,
                    onValueChange = { spouse = it },
                    label = { Text("Spouse") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.People, null) }
                )
            }

            item {
                OutlinedTextField(
                    value = farmName,
                    onValueChange = { farmName = it },
                    label = { Text("Farm Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Agriculture, null) }
                )
            }

            item {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Place, null) },
                    minLines = 2
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            item {
                OutlinedTextField(
                    value = cellPhone,
                    onValueChange = { cellPhone = it },
                    label = { Text("Cell Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }

            item {
                var expanded by remember { mutableStateOf(false) }
                val types = listOf("", "Pullet", "Breeder")

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.Category, null) }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        types.forEach { farmerType ->
                            DropdownMenuItem(
                                text = { Text(farmerType.ifEmpty { "None" }) },
                                onClick = {
                                    type = farmerType
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
