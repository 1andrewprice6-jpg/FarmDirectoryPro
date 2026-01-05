package com.example.farmdirectoryupgraded

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmdirectoryupgraded.data.AppSettings
import com.example.farmdirectoryupgraded.data.Farmer
import com.example.farmdirectoryupgraded.data.FarmDatabase
import com.example.farmdirectoryupgraded.ui.*
import com.example.farmdirectoryupgraded.ui.theme.FarmDirectoryTheme
import com.example.farmdirectoryupgraded.viewmodel.FarmerListViewModel
import com.example.farmdirectoryupgraded.viewmodel.AttendanceViewModel
import com.example.farmdirectoryupgraded.viewmodel.LocationViewModel
import com.example.farmdirectoryupgraded.viewmodel.WebSocketViewModel
import com.example.farmdirectoryupgraded.viewmodel.FarmViewModelFactory
import com.example.farmdirectoryupgraded.data.FarmWebSocketService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FarmDirectoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FarmDirectoryApp()
                }
            }
        }
    }
}

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
            webSocketService = FarmWebSocketService()
        )
    }

    // Create each specialized ViewModel
    val farmerListViewModel: FarmerListViewModel = viewModel(factory = viewModelFactory)
    val attendanceViewModel: AttendanceViewModel = viewModel(factory = viewModelFactory)
    val locationViewModel: LocationViewModel = viewModel(factory = viewModelFactory)
    val webSocketViewModel: WebSocketViewModel = viewModel(factory = viewModelFactory)

    var currentScreen by remember { mutableStateOf("list") }
    var selectedFarmer by remember { mutableStateOf<Farmer?>(null) }
    val appSettings = remember { AppSettings(context) }

    // WebSocket state (from WebSocketViewModel)
    val connectionState by webSocketViewModel.connectionState.collectAsState()
    val errorMessage by webSocketViewModel.errorMessage.collectAsState()

    // Farmer list state (from FarmerListViewModel)
    val farmers by farmerListViewModel.farmers.collectAsState(initial = emptyList())
    val farmerError by farmerListViewModel.errorMessage.collectAsState()
    val farmerSuccess by farmerListViewModel.successMessage.collectAsState()

    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    // Connect to WebSocket backend on app start
    LaunchedEffect(appSettings.backendUrl) {
        if (appSettings.autoConnect) {
            webSocketViewModel.connectToBackend(
                farmId = appSettings.farmId,
                workerId = "worker-${System.currentTimeMillis()}"
            )
        }
    }

    // Show error dialog when error occurs
    LaunchedEffect(errorMessage, farmerError) {
        if (errorMessage != null || farmerError != null) {
            showErrorDialog = true
        }
    }

    // Show success snackbar when success message occurs
    LaunchedEffect(farmerSuccess) {
        if (farmerSuccess != null) {
            showSuccessSnackbar = true
            kotlinx.coroutines.delay(3000)
            farmerListViewModel.clearSuccessMessage()
            showSuccessSnackbar = false
        }
    }

    // Error Dialog
    if (showErrorDialog && (errorMessage != null || farmerError != null)) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                webSocketViewModel.clearError()
                farmerListViewModel.clearError()
            },
            icon = {
                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = {
                Text("Error")
            },
            text = {
                Column {
                    Text(errorMessage ?: farmerError ?: "Unknown error")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connection state: ${connectionState.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        webSocketViewModel.clearError()
                        farmerListViewModel.clearError()
                        if (errorMessage != null) {
                            webSocketViewModel.reconnect()
                        }
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        webSocketViewModel.clearError()
                        farmerListViewModel.clearError()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

    // Success Snackbar
    if (showSuccessSnackbar && farmerSuccess != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = farmerSuccess!!,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    when (currentScreen) {
        "list" -> FarmerListScreen(
            viewModel = farmerListViewModel,
            onFarmerClick = { farmer ->
                selectedFarmer = farmer
                currentScreen = "details"
            },
            onAddClick = { currentScreen = "add" },
            onSettingsClick = { currentScreen = "settings" },
            onImportClick = { currentScreen = "import" },
            onReconcileClick = { currentScreen = "reconcile" },
            onAttendanceClick = { currentScreen = "attendance" },
            onRouteClick = { currentScreen = "route" },
            onLogsClick = { currentScreen = "logs" }
        )
        "details" -> selectedFarmer?.let { farmer ->
            FarmerDetailsScreen(
                farmer = farmer,
                onBack = { currentScreen = "list" },
                onToggleFavorite = { viewModel.toggleFavorite(farmer) },
                onEdit = {
                    selectedFarmer = farmer
                    currentScreen = "edit"
                },
                viewModel = viewModel
            )
        }
        "edit" -> selectedFarmer?.let { farmer ->
            EditFarmerScreen(
                farmer = farmer,
                onSave = { updatedFarmer ->
                    viewModel.updateFarmer(updatedFarmer)
                    currentScreen = "details"
                },
                onBack = { currentScreen = "details" }
            )
        }
        "add" -> AddFarmerScreen(
            onSave = { newFarmer ->
                viewModel.addFarmer(newFarmer)
                currentScreen = "list"
            },
            onBack = { currentScreen = "list" }
        )
        "settings" -> SettingsScreen(
            settings = appSettings,
            viewModel = viewModel,
            onBack = { currentScreen = "list" }
        )
        "import" -> ImportDataScreen(
            viewModel = viewModel,
            onBack = { currentScreen = "list" }
        )
        "reconcile" -> ReconcileScreen(
            viewModel = viewModel,
            onBack = { currentScreen = "list" }
        )
        "attendance" -> AttendanceScreen(
            viewModel = viewModel,
            onBack = { currentScreen = "list" }
        )
        "route" -> RouteOptimizationScreen(
            viewModel = viewModel,
            onBack = { currentScreen = "list" }
        )
        "logs" -> LogsViewerScreen(
            viewModel = viewModel,
            onBack = { currentScreen = "list" }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerListScreen(
    viewModel: FarmerViewModel,
    onFarmerClick: (Farmer) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onImportClick: () -> Unit = {},
    onReconcileClick: () -> Unit = {},
    onAttendanceClick: () -> Unit = {},
    onRouteClick: () -> Unit = {},
    onLogsClick: () -> Unit = {}
) {
    val farmers by viewModel.farmers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val types = listOf("All", "Pullet", "Breeder")

    // Real-time WebSocket state
    val isConnected by viewModel.isConnected.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val activeWorkers by viewModel.activeWorkers.collectAsState()
    val recentLocationUpdate by viewModel.recentLocationUpdate.collectAsState()
    val connectionErrorMessage by viewModel.connectionErrorMessage.collectAsState()

    // Snackbar for health alerts
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect health alerts
    LaunchedEffect(Unit) {
        viewModel.healthAlerts.collect { alert ->
            snackbarHostState.showSnackbar(
                message = "Health Alert: ${alert.healthNotes ?: "Check ${alert.entityId}"}",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Collect critical alerts
    LaunchedEffect(Unit) {
        viewModel.criticalAlerts.collect { alert ->
            snackbarHostState.showSnackbar(
                message = "🚨 CRITICAL: ${alert.healthNotes ?: alert.entityId}",
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Farm Directory",
                            fontWeight = FontWeight.Bold
                        )
                        // Enhanced connection status indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Loading indicator
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Circle,
                                    contentDescription = connectionState.name,
                                    modifier = Modifier.size(8.dp),
                                    tint = when (connectionState) {
                                        com.example.farmdirectoryupgraded.data.ConnectionState.CONNECTED -> MaterialTheme.colorScheme.tertiary
                                        com.example.farmdirectoryupgraded.data.ConnectionState.CONNECTING -> MaterialTheme.colorScheme.primary
                                        com.example.farmdirectoryupgraded.data.ConnectionState.RECONNECTING -> MaterialTheme.colorScheme.secondary
                                        com.example.farmdirectoryupgraded.data.ConnectionState.ERROR -> MaterialTheme.colorScheme.error
                                        com.example.farmdirectoryupgraded.data.ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (connectionState) {
                                    com.example.farmdirectoryupgraded.data.ConnectionState.CONNECTED -> "Live (${activeWorkers.size} workers)"
                                    com.example.farmdirectoryupgraded.data.ConnectionState.CONNECTING -> "Connecting..."
                                    com.example.farmdirectoryupgraded.data.ConnectionState.RECONNECTING -> "Reconnecting..."
                                    com.example.farmdirectoryupgraded.data.ConnectionState.ERROR -> "Error"
                                    com.example.farmdirectoryupgraded.data.ConnectionState.DISCONNECTED -> "Offline"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                actions = {
                    // Import button
                    IconButton(onClick = onImportClick) {
                        Icon(Icons.Default.Upload, contentDescription = "Import Data")
                    }
                    // Settings button
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    // Add button
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Farmer")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already on list */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Farmers") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onReconcileClick,
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Reconcile") },
                    label = { Text("Reconcile") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onAttendanceClick,
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Attendance") },
                    label = { Text("Attendance") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onRouteClick,
                    icon = { Icon(Icons.Default.Route, contentDescription = "Routes") },
                    label = { Text("Routes") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onLogsClick,
                    icon = { Icon(Icons.Default.Article, contentDescription = "Logs") },
                    label = { Text("Logs") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search farmers...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

            // Type Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                types.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { viewModel.updateSelectedType(type) },
                        label = { Text(type) }
                    )
                }
            }

            // Connection error banner with retry button
            if (connectionState == com.example.farmdirectoryupgraded.data.ConnectionState.ERROR && connectionErrorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Connection Error",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = connectionErrorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Button(
                            onClick = { viewModel.retryConnection() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                        }
                    }
                }
            }

            // Reconnecting indicator
            if (connectionState == com.example.farmdirectoryupgraded.data.ConnectionState.RECONNECTING) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Reconnecting to backend...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Real-time location update indicator
            recentLocationUpdate?.let { update ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Location Updated",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Entity ${update.entityId} moved by ${update.updatedBy}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Farmers List
            if (farmers.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(farmers) { farmer ->
                        FarmerCard(
                            farmer = farmer,
                            onClick = { onFarmerClick(farmer) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerCard(
    farmer: Farmer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = farmer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (farmer.farmName.isNotEmpty()) {
                        Text(
                            text = farmer.farmName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (farmer.type.isNotEmpty()) {
                        AssistChip(
                            onClick = { },
                            label = { Text(farmer.type) }
                        )
                    }
                    // Health status indicator
                    if (farmer.healthStatus != "HEALTHY") {
                        Spacer(modifier = Modifier.height(4.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text(farmer.healthStatus) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = when (farmer.healthStatus) {
                                    "CRITICAL" -> MaterialTheme.colorScheme.errorContainer
                                    "SICK" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                }
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = farmer.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (farmer.phone.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = farmer.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // GPS Location (if available)
            if (farmer.latitude != null && farmer.longitude != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GPS: ${String.format("%.4f", farmer.latitude)}, ${String.format("%.4f", farmer.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDetailsScreen(
    farmer: Farmer,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    viewModel: FarmerViewModel
) {
    val context = LocalContext.current
    val isConnected by viewModel.isConnected.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farmer Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (farmer.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (farmer.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
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
                            text = farmer.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (farmer.farmName.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = farmer.farmName,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        if (farmer.spouse.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Spouse: ${farmer.spouse}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (farmer.type.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AssistChip(
                                onClick = { },
                                label = { Text(farmer.type) }
                            )
                        }
                    }
                }
            }

            item {
                DetailSection(title = "Contact Information") {
                    if (farmer.phone.isNotEmpty()) {
                        DetailItem(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = farmer.phone,
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${farmer.phone}")))
                            }
                        )
                    }
                    if (farmer.cellPhone.isNotEmpty()) {
                        DetailItem(
                            icon = Icons.Default.Phone,
                            label = "Cell Phone",
                            value = farmer.cellPhone,
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${farmer.cellPhone}")))
                            }
                        )
                    }
                    if (farmer.email.isNotEmpty()) {
                        DetailItem(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = farmer.email,
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${farmer.email}")))
                            }
                        )
                    }
                }
            }

            item {
                DetailSection(title = "Address") {
                    DetailItem(
                        icon = Icons.Default.Place,
                        label = "Location",
                        value = farmer.address,
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(farmer.address)}")))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No farmers found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
