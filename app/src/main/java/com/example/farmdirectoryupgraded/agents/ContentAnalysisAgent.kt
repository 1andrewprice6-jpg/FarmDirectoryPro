package com.example.farmdirectoryupgraded.agents

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Content Analysis Agent
 * Analyzes discovered files, apps, and images to extract useful data
 * and identify missing features/enhancements for the app
 */
class ContentAnalysisAgent(private val context: Context) {
    
    private val TAG = "ContentAnalysisAgent"
    
    /**
     * Analyze discovered content and suggest enhancements
     */
    suspend fun analyzeContent(scanResult: SystemScanResult): AnalysisResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Analyzing discovered content...")
            
            val fileAnalysis = analyzeFiles(scanResult.relatedFiles)
            val appAnalysis = analyzeApps(scanResult.relatedApps)
            val imageAnalysis = analyzeImages(scanResult.relatedImages)
            val databaseAnalysis = analyzeDatabases(scanResult.databaseFiles)
            
            // Generate enhancement suggestions
            val suggestions = generateSuggestions(
                fileAnalysis, appAnalysis, imageAnalysis, databaseAnalysis
            )
            
            // Identify missing features
            val missingFeatures = identifyMissingFeatures(
                fileAnalysis, appAnalysis, databaseAnalysis
            )
            
            // Extract importable data
            val importableData = extractImportableData(scanResult.relatedFiles)
            
            AnalysisResult(
                success = true,
                fileAnalysis = fileAnalysis,
                appAnalysis = appAnalysis,
                imageAnalysis = imageAnalysis,
                databaseAnalysis = databaseAnalysis,
                suggestions = suggestions,
                missingFeatures = missingFeatures,
                importableData = importableData,
                message = "Analysis complete: Found ${suggestions.size} suggestions, " +
                         "${missingFeatures.size} missing features"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Analysis error", e)
            AnalysisResult(
                success = false,
                message = "Error: ${e.message}"
            )
        }
    }
    
    /**
     * Analyze files for content and structure
     */
    private fun analyzeFiles(files: List<DiscoveredFile>): FileAnalysis {
        val jsonFiles = files.filter { it.type == "JSON" }
        val csvFiles = files.filter { it.type == "CSV" }
        val textFiles = files.filter { it.type == "TEXT" }
        
        val farmerData = mutableListOf<String>()
        val farmData = mutableListOf<String>()
        val locationData = mutableListOf<String>()
        
        // Analyze JSON files
        jsonFiles.forEach { file ->
            try {
                val content = File(file.path).readText()
                if (content.contains("\"farm\"", ignoreCase = true)) {
                    farmData.add(file.name)
                }
                if (content.contains("\"farmer\"", ignoreCase = true)) {
                    farmerData.add(file.name)
                }
                if (content.contains("\"latitude\"", ignoreCase = true) ||
                    content.contains("\"longitude\"", ignoreCase = true)) {
                    locationData.add(file.name)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error reading ${file.name}", e)
            }
        }
        
        // Analyze CSV files
        csvFiles.forEach { file ->
            try {
                val firstLine = File(file.path).bufferedReader().readLine()?.toLowerCase() ?: ""
                if (firstLine.contains("farm")) farmData.add(file.name)
                if (firstLine.contains("farmer")) farmerData.add(file.name)
                if (firstLine.contains("lat") || firstLine.contains("lon")) {
                    locationData.add(file.name)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error reading ${file.name}", e)
            }
        }
        
        return FileAnalysis(
            totalFiles = files.size,
            jsonFiles = jsonFiles.size,
            csvFiles = csvFiles.size,
            textFiles = textFiles.size,
            farmerDataFiles = farmerData,
            farmDataFiles = farmData,
            locationDataFiles = locationData,
            hasImportableData = farmerData.isNotEmpty() || farmData.isNotEmpty()
        )
    }
    
    /**
     * Analyze installed apps for integration opportunities
     */
    private fun analyzeApps(apps: List<DiscoveredApp>): AppAnalysis {
        val farmApps = apps.filter { 
            it.appName.contains("farm", ignoreCase = true) ||
            it.packageName.contains("farm", ignoreCase = true)
        }
        
        val gpsApps = apps.filter {
            it.appName.contains("gps", ignoreCase = true) ||
            it.appName.contains("map", ignoreCase = true) ||
            it.appName.contains("navigation", ignoreCase = true)
        }
        
        val cameraApps = apps.filter {
            it.appName.contains("camera", ignoreCase = true) ||
            it.appName.contains("qr", ignoreCase = true) ||
            it.appName.contains("scan", ignoreCase = true)
        }
        
        return AppAnalysis(
            totalApps = apps.size,
            farmRelatedApps = farmApps.map { it.appName },
            gpsApps = gpsApps.map { it.appName },
            cameraApps = cameraApps.map { it.appName },
            hasIntegrationOpportunities = farmApps.isNotEmpty() || gpsApps.isNotEmpty()
        )
    }
    
    /**
     * Analyze images for QR codes and farm photos
     */
    private fun analyzeImages(images: List<DiscoveredImage>): ImageAnalysis {
        val qrCandidates = images.filter { it.isPotentialQR }
        val recentPhotos = images.filter {
            (System.currentTimeMillis() - it.lastModified) < (24 * 60 * 60 * 1000) // Last 24 hours
        }
        
        return ImageAnalysis(
            totalImages = images.size,
            potentialQRCodes = qrCandidates.size,
            recentPhotos = recentPhotos.size,
            imageTypes = images.groupBy { it.type }.mapValues { it.value.size },
            hasUsableContent = qrCandidates.isNotEmpty() || recentPhotos.isNotEmpty()
        )
    }
    
    /**
     * Analyze databases for data migration opportunities
     */
    private fun analyzeDatabases(databases: List<DiscoveredFile>): DatabaseAnalysis {
        val farmDatabases = databases.filter { 
            it.name.contains("farm", ignoreCase = true)
        }
        
        val reconcileDatabases = databases.filter {
            it.name.contains("reconcile", ignoreCase = true)
        }
        
        return DatabaseAnalysis(
            totalDatabases = databases.size,
            farmDatabases = farmDatabases.map { it.name },
            reconcileDatabases = reconcileDatabases.map { it.name },
            hasMigrationOpportunity = farmDatabases.isNotEmpty() || reconcileDatabases.isNotEmpty()
        )
    }
    
    /**
     * Generate enhancement suggestions
     */
    private fun generateSuggestions(
        fileAnalysis: FileAnalysis,
        appAnalysis: AppAnalysis,
        imageAnalysis: ImageAnalysis,
        databaseAnalysis: DatabaseAnalysis
    ): List<EnhancementSuggestion> {
        val suggestions = mutableListOf<EnhancementSuggestion>()
        
        // Import data suggestions
        if (fileAnalysis.hasImportableData) {
            suggestions.add(
                EnhancementSuggestion(
                    title = "Import Farm Data",
                    description = "Found ${fileAnalysis.farmDataFiles.size + fileAnalysis.farmerDataFiles.size} " +
                                 "files with importable farm/farmer data",
                    priority = Priority.HIGH,
                    category = "DATA_IMPORT",
                    actionable = true,
                    files = fileAnalysis.farmDataFiles + fileAnalysis.farmerDataFiles
                )
            )
        }
        
        // GPS integration
        if (fileAnalysis.locationDataFiles.isNotEmpty()) {
            suggestions.add(
                EnhancementSuggestion(
                    title = "Import GPS Coordinates",
                    description = "Found ${fileAnalysis.locationDataFiles.size} files with GPS data",
                    priority = Priority.MEDIUM,
                    category = "GPS",
                    actionable = true,
                    files = fileAnalysis.locationDataFiles
                )
            )
        }
        
        // App integration
        if (appAnalysis.hasIntegrationOpportunities) {
            suggestions.add(
                EnhancementSuggestion(
                    title = "Integrate with Related Apps",
                    description = "Found ${appAnalysis.farmRelatedApps.size} farm-related apps for potential integration",
                    priority = Priority.LOW,
                    category = "APP_INTEGRATION",
                    actionable = false,
                    files = appAnalysis.farmRelatedApps
                )
            )
        }
        
        // QR code processing
        if (imageAnalysis.potentialQRCodes > 0) {
            suggestions.add(
                EnhancementSuggestion(
                    title = "Process QR Codes",
                    description = "Found ${imageAnalysis.potentialQRCodes} potential QR code images",
                    priority = Priority.MEDIUM,
                    category = "QR_PROCESSING",
                    actionable = true,
                    files = emptyList()
                )
            )
        }
        
        // Database migration
        if (databaseAnalysis.hasMigrationOpportunity) {
            suggestions.add(
                EnhancementSuggestion(
                    title = "Migrate Existing Database",
                    description = "Found ${databaseAnalysis.totalDatabases} related databases to migrate",
                    priority = Priority.HIGH,
                    category = "DATABASE_MIGRATION",
                    actionable = true,
                    files = databaseAnalysis.farmDatabases + databaseAnalysis.reconcileDatabases
                )
            )
        }
        
        return suggestions.sortedByDescending { it.priority.ordinal }
    }
    
    /**
     * Identify missing features based on discovered content
     */
    private fun identifyMissingFeatures(
        fileAnalysis: FileAnalysis,
        appAnalysis: AppAnalysis,
        databaseAnalysis: DatabaseAnalysis
    ): List<MissingFeature> {
        val missing = mutableListOf<MissingFeature>()
        
        // Check for batch import feature
        if (fileAnalysis.csvFiles > 0 || fileAnalysis.jsonFiles > 0) {
            missing.add(
                MissingFeature(
                    feature = "Batch Import UI",
                    description = "Add a batch import screen to import multiple files at once",
                    reason = "Found ${fileAnalysis.csvFiles + fileAnalysis.jsonFiles} importable files",
                    priority = Priority.MEDIUM
                )
            )
        }
        
        // Check for database merge feature
        if (databaseAnalysis.totalDatabases > 1) {
            missing.add(
                MissingFeature(
                    feature = "Database Merge Tool",
                    description = "Tool to merge multiple database files",
                    reason = "Found ${databaseAnalysis.totalDatabases} database files",
                    priority = Priority.LOW
                )
            )
        }
        
        // Check for image processing
        if (appAnalysis.cameraApps.isNotEmpty()) {
            missing.add(
                MissingFeature(
                    feature = "Enhanced Image Processing",
                    description = "Better integration with camera apps for farm documentation",
                    reason = "Found ${appAnalysis.cameraApps.size} camera-related apps",
                    priority = Priority.LOW
                )
            )
        }
        
        return missing.sortedByDescending { it.priority.ordinal }
    }
    
    /**
     * Extract importable data from files
     */
    private fun extractImportableData(files: List<DiscoveredFile>): List<ImportableDataSource> {
        val sources = mutableListOf<ImportableDataSource>()
        
        files.filter { it.type == "JSON" || it.type == "CSV" }.forEach { file ->
            try {
                val preview = File(file.path).bufferedReader().use {
                    it.readText().take(500)
                }
                
                sources.add(
                    ImportableDataSource(
                        filePath = file.path,
                        fileName = file.name,
                        fileType = file.type,
                        fileSize = file.size,
                        preview = preview,
                        estimatedRecords = estimateRecordCount(file),
                        confidence = file.relevanceScore
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error extracting data from ${file.name}", e)
            }
        }
        
        return sources.sortedByDescending { it.confidence }
    }
    
    /**
     * Estimate record count in file
     */
    private fun estimateRecordCount(file: DiscoveredFile): Int {
        return try {
            when (file.type) {
                "JSON" -> {
                    val content = File(file.path).readText()
                    val array = JSONArray(content)
                    array.length()
                }
                "CSV" -> {
                    File(file.path).bufferedReader().use {
                        it.readLines().size - 1 // Minus header
                    }
                }
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }
}

/**
 * Analysis result
 */
data class AnalysisResult(
    val success: Boolean,
    val fileAnalysis: FileAnalysis = FileAnalysis(),
    val appAnalysis: AppAnalysis = AppAnalysis(),
    val imageAnalysis: ImageAnalysis = ImageAnalysis(),
    val databaseAnalysis: DatabaseAnalysis = DatabaseAnalysis(),
    val suggestions: List<EnhancementSuggestion> = emptyList(),
    val missingFeatures: List<MissingFeature> = emptyList(),
    val importableData: List<ImportableDataSource> = emptyList(),
    val message: String
)

/**
 * File analysis results
 */
data class FileAnalysis(
    val totalFiles: Int = 0,
    val jsonFiles: Int = 0,
    val csvFiles: Int = 0,
    val textFiles: Int = 0,
    val farmerDataFiles: List<String> = emptyList(),
    val farmDataFiles: List<String> = emptyList(),
    val locationDataFiles: List<String> = emptyList(),
    val hasImportableData: Boolean = false
)

/**
 * App analysis results
 */
data class AppAnalysis(
    val totalApps: Int = 0,
    val farmRelatedApps: List<String> = emptyList(),
    val gpsApps: List<String> = emptyList(),
    val cameraApps: List<String> = emptyList(),
    val hasIntegrationOpportunities: Boolean = false
)

/**
 * Image analysis results
 */
data class ImageAnalysis(
    val totalImages: Int = 0,
    val potentialQRCodes: Int = 0,
    val recentPhotos: Int = 0,
    val imageTypes: Map<String, Int> = emptyMap(),
    val hasUsableContent: Boolean = false
)

/**
 * Database analysis results
 */
data class DatabaseAnalysis(
    val totalDatabases: Int = 0,
    val farmDatabases: List<String> = emptyList(),
    val reconcileDatabases: List<String> = emptyList(),
    val hasMigrationOpportunity: Boolean = false
)

/**
 * Enhancement suggestion
 */
data class EnhancementSuggestion(
    val title: String,
    val description: String,
    val priority: Priority,
    val category: String,
    val actionable: Boolean,
    val files: List<String>
)

/**
 * Missing feature
 */
data class MissingFeature(
    val feature: String,
    val description: String,
    val reason: String,
    val priority: Priority
)

/**
 * Importable data source
 */
data class ImportableDataSource(
    val filePath: String,
    val fileName: String,
    val fileType: String,
    val fileSize: Long,
    val preview: String,
    val estimatedRecords: Int,
    val confidence: Int
)

/**
 * Priority levels
 */
enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}
