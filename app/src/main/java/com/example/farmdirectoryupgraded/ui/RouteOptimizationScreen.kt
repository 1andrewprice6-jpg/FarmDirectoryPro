package com.example.farmdirectoryupgraded.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmdirectoryupgraded.data.Farmer
import com.example.farmdirectoryupgraded.viewmodel.FarmerViewModel

/**
 * Route Optimization Screen
 * Multi-stop location optimization for farm visits
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteOptimizationScreen(
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    val farmers by viewModel.farmers.collectAsState()
    var selectedFarmers by remember { mutableStateOf<List<Farmer>>(emptyList()) }
    var optimizedRoute by remember { mutableStateOf<OptimizedRoute?>(null) }
    var isOptimizing by remember { mutableStateOf(false) }
    var showFarmSelector by remember { mutableStateOf(false) }

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
        },
        floatingActionButton = {
            if (selectedFarmers.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        isOptimizing = true
                        viewModel.optimizeRoute(selectedFarmers) { route ->
                            optimizedRoute = route
                            isOptimizing = false
                        }
                    },
                    icon = { Icon(Icons.Default.Route, contentDescription = null) },
                    text = { Text("Optimize Route") }
                )
            }
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
                text = "Plan Your Farm Visits",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Select farms to visit and get the most efficient route",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Selected Farms Card
            Card(
                modifier = Modifier.fillMaxWidth()
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
                            text = "Selected Farms (${selectedFarmers.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showFarmSelector = true }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Farms")
                        }
                    }

                    if (selectedFarmers.isEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No farms selected. Tap 'Add Farms' to get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedFarmers.forEach { farmer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${farmer.farmName}",
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        selectedFarmers = selectedFarmers.filter { it.id != farmer.id }
                                        optimizedRoute = null
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Optimizing Indicator
            if (isOptimizing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Optimizing route...")
                    }
                }
            }

            // Optimized Route Result
            optimizedRoute?.let { route ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "✓ Optimized Route",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Distance", fontWeight = FontWeight.Bold)
                                Text("${String.format("%.1f", route.totalDistance)} km")
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Est. Time", fontWeight = FontWeight.Bold)
                                Text(route.estimatedTime)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Fuel Cost", fontWeight = FontWeight.Bold)
                                Text("$${String.format("%.2f", route.fuelCost)}")
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Stops", fontWeight = FontWeight.Bold)
                                Text("${route.stops.size}")
                            }
                        }

                        Divider()

                        Text(
                            text = "Route Order:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        route.stops.forEachIndexed { index, stop ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stop.farmName,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${stop.distanceFromPrevious} km • ${stop.timeFromPrevious}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (index < route.stops.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { /* Start navigation */ },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Navigate")
                            }

                            OutlinedButton(
                                onClick = { /* Share route */ },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }

            // Info Card
            if (selectedFarmers.isEmpty() && optimizedRoute == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("How it works", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Select farms you want to visit\n" +
                                    "2. Tap 'Optimize Route'\n" +
                                    "3. Get the most efficient order\n" +
                                    "4. Save time and fuel costs!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Farm Selector Dialog
    if (showFarmSelector) {
        AlertDialog(
            onDismissRequest = { showFarmSelector = false },
            title = { Text("Select Farms") },
            text = {
                LazyColumn {
                    items(farmers.size) { index ->
                        val farmer = farmers[index]
                        val isSelected = selectedFarmers.any { it.id == farmer.id }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    selectedFarmers = if (isSelected) {
                                        selectedFarmers.filter { it.id != farmer.id }
                                    } else {
                                        selectedFarmers + farmer
                                    }
                                    optimizedRoute = null
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = farmer.farmName,
                                    fontWeight = FontWeight.Medium
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
            },
            confirmButton = {
                TextButton(onClick = { showFarmSelector = false }) {
                    Text("Done")
                }
            }
        )
    }
}

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
