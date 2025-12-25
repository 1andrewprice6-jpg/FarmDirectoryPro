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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmdirectoryupgraded.agents.*
import kotlinx.coroutines.launch

/**
 * Smart Agent Screen
 * Shows system analysis and auto-enhancement capabilities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAgentScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isScanning by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isEnhancing by remember { mutableStateOf(false) }
    
    var scanResult by remember { mutableStateOf<SystemScanResult?>(null) }
    var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
    var enhancementResult by remember { mutableStateOf<EnhancementResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Agents") },
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
                Text(
                    text = "🤖 AI-Powered System Enhancement",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Automatically discover files, analyze content, and enhance your app",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Step 1: System Scan
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (scanResult != null) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (scanResult != null) Icons.Default.CheckCircle else Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Step 1: System Scan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Find related files, apps, and images",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (scanResult != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("✓ Found ${scanResult!!.totalItemsFound} items:")
                            Text("  • ${scanResult!!.relatedFiles.size} files", style = MaterialTheme.typography.bodySmall)
                            Text("  • ${scanResult!!.relatedApps.size} apps", style = MaterialTheme.typography.bodySmall)
                            Text("  • ${scanResult!!.relatedImages.size} images", style = MaterialTheme.typography.bodySmall)
                            Text("  • ${scanResult!!.databaseFiles.size} databases", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    isScanning = true
                                    val agent = SystemDiscoveryAgent(context)
                                    scanResult = agent.scanSystem()
                                    isScanning = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isScanning
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (scanResult != null) "Re-scan System" else "Start Scan")
                        }
                    }
                }
            }

            // Step 2: Content Analysis
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (analysisResult != null) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (analysisResult != null) Icons.Default.CheckCircle else Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Step 2: Content Analysis",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Analyze discovered content",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (analysisResult != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("✓ Analysis complete:")
                            Text("  • ${analysisResult!!.suggestions.size} suggestions", style = MaterialTheme.typography.bodySmall)
                            Text("  • ${analysisResult!!.missingFeatures.size} missing features", style = MaterialTheme.typography.bodySmall)
                            Text("  • ${analysisResult!!.importableData.size} importable sources", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                scanResult?.let { scan ->
                                    scope.launch {
                                        isAnalyzing = true
                                        val agent = ContentAnalysisAgent(context)
                                        analysisResult = agent.analyzeContent(scan)
                                        isAnalyzing = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isAnalyzing && scanResult != null
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Analytics, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze Content")
                        }
                    }
                }
            }

            // Step 3: Auto-Enhancement
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (enhancementResult != null) 
                            MaterialTheme.colorScheme.tertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (enhancementResult != null) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Step 3: Auto-Enhancement",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Automatically enhance app",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (enhancementResult != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("✓ Enhancement complete:")
                            Text("  • ${enhancementResult!!.successCount} actions succeeded", 
                                 style = MaterialTheme.typography.bodySmall)
                            Text("  • ${enhancementResult!!.failureCount} actions failed", 
                                 style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                analysisResult?.let { analysis ->
                                    scope.launch {
                                        isEnhancing = true
                                        val agent = AutoEnhancementAgent(context)
                                        enhancementResult = agent.autoEnhance(analysis)
                                        isEnhancing = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isEnhancing && analysisResult != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            if (isEnhancing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Enhancement")
                        }
                    }
                }
            }

            // Suggestions
            if (analysisResult?.suggestions?.isNotEmpty() == true) {
                item {
                    Text(
                        text = "💡 Enhancement Suggestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(analysisResult!!.suggestions) { suggestion ->
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
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = suggestion.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                AssistChip(
                                    onClick = { },
                                    label = { Text(suggestion.priority.name) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = when (suggestion.priority) {
                                            Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
                                            Priority.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
                                            Priority.LOW -> MaterialTheme.colorScheme.secondaryContainer
                                        }
                                    )
                                )
                            }
                            Text(
                                text = suggestion.description,
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
