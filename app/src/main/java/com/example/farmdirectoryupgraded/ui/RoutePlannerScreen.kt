package com.example.farmdirectoryupgraded.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmdirectoryupgraded.viewmodel.RoutePlannerViewModel
import com.example.farmdirectoryupgraded.viewmodel.LatLng
import com.example.farmdirectoryupgraded.viewmodel.Stop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerScreen(
    viewModel: RoutePlannerViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val routePlan by viewModel.routePlan.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route Planner") },
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
                            text = "Route Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (routePlan.origin != null) 
                                    "Origin set: ${String.format("%.4f", routePlan.origin!!.latitude)}, ${String.format("%.4f", routePlan.origin!!.longitude)}"
                                else "Origin not set",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.optimizeRoute() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isOptimizing && routePlan.stops.isNotEmpty() && routePlan.origin != null
                        ) {
                            if (isOptimizing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
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
                    text = "Selected Stops (${routePlan.stops.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (routePlan.stops.isEmpty()) {
                item {
                    Text("No stops added yet.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(8.dp))
                }
            } else {
                items(routePlan.stops) { stop ->
                    StopItem(stop = stop, onRemove = { viewModel.removeStop(stop.id) })
                }
            }

            if (routePlan.optimizedStops.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Optimized Sequence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total distance: ${String.format("%.2f", routePlan.totalDistance)} km",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                items(routePlan.optimizedStops.withIndex().toList()) { (index, stop) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${index + 1}", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stop.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StopItem(stop: Stop, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stop.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${String.format("%.4f", stop.location.latitude)}, ${String.format("%.4f", stop.location.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
