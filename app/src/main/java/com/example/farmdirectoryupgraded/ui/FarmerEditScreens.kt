package com.example.farmdirectoryupgraded.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmdirectoryupgraded.data.Farmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFarmerScreen(
    onSave: (Farmer) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var farmName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cellPhone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var spouse by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Farmer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val newFarmer = Farmer(
                                id = 0,
                                name = name,
                                farmName = farmName,
                                address = address,
                                phone = phone,
                                cellPhone = cellPhone,
                                email = email,
                                type = type,
                                spouse = spouse,
                                latitude = latitude.toDoubleOrNull(),
                                longitude = longitude.toDoubleOrNull(),
                                isFavorite = false,
                                healthStatus = "HEALTHY",
                                healthNotes = ""
                            )
                            onSave(newFarmer)
                        },
                        enabled = name.isNotBlank() && farmName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Farmer Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )

            OutlinedTextField(
                value = farmName,
                onValueChange = { farmName = it },
                label = { Text("Farm Name *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                minLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                )

                OutlinedTextField(
                    value = cellPhone,
                    onValueChange = { cellPhone = it },
                    label = { Text("Cell") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) }
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
            )

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Type (Pullet/Breeder)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = spouse,
                onValueChange = { spouse = it },
                label = { Text("Spouse") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "GPS Coordinates (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                )

                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val newFarmer = Farmer(
                        id = 0,
                        name = name,
                        farmName = farmName,
                        address = address,
                        phone = phone,
                        cellPhone = cellPhone,
                        email = email,
                        type = type,
                        spouse = spouse,
                        latitude = latitude.toDoubleOrNull(),
                        longitude = longitude.toDoubleOrNull(),
                        isFavorite = false,
                        healthStatus = "HEALTHY",
                        healthNotes = ""
                    )
                    onSave(newFarmer)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && farmName.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Farmer")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFarmerScreen(
    farmer: Farmer,
    onSave: (Farmer) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(farmer.name) }
    var farmName by remember { mutableStateOf(farmer.farmName) }
    var address by remember { mutableStateOf(farmer.address) }
    var phone by remember { mutableStateOf(farmer.phone) }
    var cellPhone by remember { mutableStateOf(farmer.cellPhone) }
    var email by remember { mutableStateOf(farmer.email) }
    var type by remember { mutableStateOf(farmer.type) }
    var spouse by remember { mutableStateOf(farmer.spouse) }
    var latitude by remember { mutableStateOf(farmer.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(farmer.longitude?.toString() ?: "") }

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
                            val updatedFarmer = farmer.copy(
                                name = name,
                                farmName = farmName,
                                address = address,
                                phone = phone,
                                cellPhone = cellPhone,
                                email = email,
                                type = type,
                                spouse = spouse,
                                latitude = latitude.toDoubleOrNull(),
                                longitude = longitude.toDoubleOrNull()
                            )
                            onSave(updatedFarmer)
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )

            OutlinedTextField(
                value = farmName,
                onValueChange = { farmName = it },
                label = { Text("Farm Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                minLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                )

                OutlinedTextField(
                    value = cellPhone,
                    onValueChange = { cellPhone = it },
                    label = { Text("Cell") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) }
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
            )

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = spouse,
                onValueChange = { spouse = it },
                label = { Text("Spouse") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
