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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.farmdirectoryupgraded.data.Farmer
import com.example.farmdirectoryupgraded.utils.ValidationUtils
import com.example.farmdirectoryupgraded.utils.SanitizationUtils

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

    // Validation error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var farmNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var cellPhoneError by remember { mutableStateOf<String?>(null) }
    var latitudeError by remember { mutableStateOf<String?>(null) }
    var longitudeError by remember { mutableStateOf<String?>(null) }

    // Validate all fields
    fun validateForm(): Boolean {
        var isValid = true

        // Validate name
        val nameValidation = ValidationUtils.validateRequired(name, "Name")
        nameError = nameValidation.errorMessage
        if (!nameValidation.isValid) isValid = false

        // Validate farm name
        val farmNameValidation = ValidationUtils.validateRequired(farmName, "Farm Name")
        farmNameError = farmNameValidation.errorMessage
        if (!farmNameValidation.isValid) isValid = false

        // Validate email
        val emailValidation = ValidationUtils.validateEmail(email)
        emailError = emailValidation.errorMessage
        if (!emailValidation.isValid) isValid = false

        // Validate phone
        val phoneValidation = ValidationUtils.validatePhone(phone)
        phoneError = phoneValidation.errorMessage
        if (!phoneValidation.isValid) isValid = false

        // Validate cell phone
        val cellPhoneValidation = ValidationUtils.validatePhone(cellPhone)
        cellPhoneError = cellPhoneValidation.errorMessage
        if (!cellPhoneValidation.isValid) isValid = false

        // Validate latitude
        val latValidation = ValidationUtils.validateLatitude(latitude)
        latitudeError = latValidation.errorMessage
        if (!latValidation.isValid) isValid = false

        // Validate longitude
        val lonValidation = ValidationUtils.validateLongitude(longitude)
        longitudeError = lonValidation.errorMessage
        if (!lonValidation.isValid) isValid = false

        return isValid
    }

    fun saveFarmer() {
        if (validateForm()) {
            val newFarmer = Farmer(
                id = 0,
                name = SanitizationUtils.sanitizeText(name),
                farmName = SanitizationUtils.sanitizeText(farmName),
                address = SanitizationUtils.sanitizeAddress(address),
                phone = SanitizationUtils.sanitizePhone(phone),
                cellPhone = SanitizationUtils.sanitizePhone(cellPhone),
                email = SanitizationUtils.sanitizeEmail(email),
                type = SanitizationUtils.sanitizeText(type),
                spouse = SanitizationUtils.sanitizeText(spouse),
                latitude = latitude.toDoubleOrNull(),
                longitude = longitude.toDoubleOrNull(),
                isFavorite = false,
                healthStatus = "HEALTHY",
                healthNotes = ""
            )
            onSave(newFarmer)
        }
    }

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
                        onClick = { saveFarmer() },
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
                onValueChange = {
                    name = it
                    nameError = null // Clear error on change
                },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = farmName,
                onValueChange = {
                    farmName = it
                    farmNameError = null
                },
                label = { Text("Farm Name *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                isError = farmNameError != null,
                supportingText = farmNameError?.let { { Text(it) } }
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
                    onValueChange = {
                        phone = it
                        phoneError = null
                    },
                    label = { Text("Phone") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = cellPhone,
                    onValueChange = {
                        cellPhone = it
                        cellPhoneError = null
                    },
                    label = { Text("Cell") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = cellPhoneError != null,
                    supportingText = cellPhoneError?.let { { Text(it) } }
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } }
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
                    onValueChange = {
                        latitude = it
                        latitudeError = null
                    },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = latitudeError != null,
                    supportingText = latitudeError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = longitude,
                    onValueChange = {
                        longitude = it
                        longitudeError = null
                    },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = longitudeError != null,
                    supportingText = longitudeError?.let { { Text(it) } }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { saveFarmer() },
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

    // Validation error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var farmNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var cellPhoneError by remember { mutableStateOf<String?>(null) }
    var latitudeError by remember { mutableStateOf<String?>(null) }
    var longitudeError by remember { mutableStateOf<String?>(null) }

    fun validateAndSave() {
        var isValid = true

        val nameValidation = ValidationUtils.validateRequired(name, "Name")
        nameError = nameValidation.errorMessage
        if (!nameValidation.isValid) isValid = false

        val farmNameValidation = ValidationUtils.validateRequired(farmName, "Farm Name")
        farmNameError = farmNameValidation.errorMessage
        if (!farmNameValidation.isValid) isValid = false

        val emailValidation = ValidationUtils.validateEmail(email)
        emailError = emailValidation.errorMessage
        if (!emailValidation.isValid) isValid = false

        val phoneValidation = ValidationUtils.validatePhone(phone)
        phoneError = phoneValidation.errorMessage
        if (!phoneValidation.isValid) isValid = false

        val cellPhoneValidation = ValidationUtils.validatePhone(cellPhone)
        cellPhoneError = cellPhoneValidation.errorMessage
        if (!cellPhoneValidation.isValid) isValid = false

        val latValidation = ValidationUtils.validateLatitude(latitude)
        latitudeError = latValidation.errorMessage
        if (!latValidation.isValid) isValid = false

        val lonValidation = ValidationUtils.validateLongitude(longitude)
        longitudeError = lonValidation.errorMessage
        if (!lonValidation.isValid) isValid = false

        if (isValid) {
            val updatedFarmer = farmer.copy(
                name = SanitizationUtils.sanitizeText(name),
                farmName = SanitizationUtils.sanitizeText(farmName),
                address = SanitizationUtils.sanitizeAddress(address),
                phone = SanitizationUtils.sanitizePhone(phone),
                cellPhone = SanitizationUtils.sanitizePhone(cellPhone),
                email = SanitizationUtils.sanitizeEmail(email),
                type = SanitizationUtils.sanitizeText(type),
                spouse = SanitizationUtils.sanitizeText(spouse),
                latitude = latitude.toDoubleOrNull(),
                longitude = longitude.toDoubleOrNull()
            )
            onSave(updatedFarmer)
        }
    }

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
                    IconButton(onClick = { validateAndSave() }) {
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
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = farmName,
                onValueChange = {
                    farmName = it
                    farmNameError = null
                },
                label = { Text("Farm Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                isError = farmNameError != null,
                supportingText = farmNameError?.let { { Text(it) } }
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
                    onValueChange = {
                        phone = it
                        phoneError = null
                    },
                    label = { Text("Phone") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = cellPhone,
                    onValueChange = {
                        cellPhone = it
                        cellPhoneError = null
                    },
                    label = { Text("Cell") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = cellPhoneError != null,
                    supportingText = cellPhoneError?.let { { Text(it) } }
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } }
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
                    onValueChange = {
                        latitude = it
                        latitudeError = null
                    },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = latitudeError != null,
                    supportingText = latitudeError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = longitude,
                    onValueChange = {
                        longitude = it
                        longitudeError = null
                    },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = longitudeError != null,
                    supportingText = longitudeError?.let { { Text(it) } }
                )
            }
        }
    }
}
