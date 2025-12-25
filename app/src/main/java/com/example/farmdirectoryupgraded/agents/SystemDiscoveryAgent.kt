package com.example.farmdirectoryupgraded.agents

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * System Discovery Agent
 * Scans system for related files, apps, images, and resources
 * Intelligently discovers assets that can enhance the Farm Directory app
 */
class SystemDiscoveryAgent(private val context: Context) {
    
    private val TAG = "SystemDiscoveryAgent"
    
    companion object {
        // Search patterns for farm-related content
        val FARM_KEYWORDS = listOf(
            "farm", "farmer", "agriculture", "chicken", "pullet", "breeder",
            "reconcile", "attendance", "gps", "location", "route"
        )
        
        // Image file extensions
        val IMAGE_EXTENSIONS = listOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp")
        
        // Data file extensions
        val DATA_EXTENSIONS = listOf(".json", ".csv", ".txt", ".xml", ".db", ".sqlite")
        
        // APK/App related
        val APP_EXTENSIONS = listOf(".apk", ".aab")
    }
    
    /**
     * Comprehensive system scan
     */
    suspend fun scanSystem(): SystemScanResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting comprehensive system scan...")
            
            val relatedFiles = findRelatedFiles()
            val relatedApps = findRelatedApps()
            val relatedImages = findRelatedImages()
            val databaseFiles = findDatabaseFiles()
            val configFiles = findConfigFiles()
            
            SystemScanResult(
                success = true,
                relatedFiles = relatedFiles,
                relatedApps = relatedApps,
                relatedImages = relatedImages,
                databaseFiles = databaseFiles,
                configFiles = configFiles,
                totalItemsFound = relatedFiles.size + relatedApps.size + relatedImages.size + 
                                  databaseFiles.size + configFiles.size,
                message = "Found ${relatedFiles.size} files, ${relatedApps.size} apps, " +
                         "${relatedImages.size} images"
            )
        } catch (e: Exception) {
            Log.e(TAG, "System scan error", e)
            SystemScanResult(
                success = false,
                message = "Error: ${e.message}"
            )
        }
    }
    
    /**
     * Find farm-related files in common directories
     */
    private fun findRelatedFiles(): List<DiscoveredFile> {
        val files = mutableListOf<DiscoveredFile>()
        
        // Search paths
        val searchPaths = listOf(
            "/storage/emulated/0/Download",
            "/storage/emulated/0/Documents",
            "/data/data/com.termux/files/home",
            "/data/data/com.termux/files/home/downloads",
            "/sdcard/Download",
            "/sdcard/Documents"
        )
        
        searchPaths.forEach { path ->
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                try {
                    dir.listFiles()?.forEach { file ->
                        if (isRelevantFile(file)) {
                            files.add(
                                DiscoveredFile(
                                    path = file.absolutePath,
                                    name = file.name,
                                    size = file.length(),
                                    type = getFileType(file),
                                    relevanceScore = calculateRelevance(file),
                                    lastModified = file.lastModified()
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error scanning $path", e)
                }
            }
        }
        
        return files.sortedByDescending { it.relevanceScore }
    }
    
    /**
     * Find related installed apps
     */
    private fun findRelatedApps(): List<DiscoveredApp> {
        val apps = mutableListOf<DiscoveredApp>()
        
        try {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            installedApps.forEach { appInfo ->
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                
                if (isRelevantApp(appName, appInfo.packageName)) {
                    apps.add(
                        DiscoveredApp(
                            packageName = appInfo.packageName,
                            appName = appName,
                            version = try {
                                packageManager.getPackageInfo(appInfo.packageName, 0).versionName
                            } catch (e: Exception) {
                                "unknown"
                            },
                            relevanceScore = calculateAppRelevance(appName, appInfo.packageName),
                            installDate = try {
                                packageManager.getPackageInfo(appInfo.packageName, 0).firstInstallTime
                            } catch (e: Exception) {
                                0L
                            }
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding apps", e)
        }
        
        return apps.sortedByDescending { it.relevanceScore }
    }
    
    /**
     * Find related images (QR codes, farm photos, maps, etc.)
     */
    private fun findRelatedImages(): List<DiscoveredImage> {
        val images = mutableListOf<DiscoveredImage>()
        
        val imagePaths = listOf(
            "/storage/emulated/0/DCIM/Camera",
            "/storage/emulated/0/Pictures",
            "/storage/emulated/0/Download",
            "/sdcard/DCIM/Camera",
            "/sdcard/Pictures"
        )
        
        imagePaths.forEach { path ->
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                try {
                    dir.listFiles()?.forEach { file ->
                        if (isImageFile(file)) {
                            images.add(
                                DiscoveredImage(
                                    path = file.absolutePath,
                                    name = file.name,
                                    size = file.length(),
                                    type = getImageType(file),
                                    isPotentialQR = isPotentialQRCode(file),
                                    relevanceScore = calculateImageRelevance(file),
                                    lastModified = file.lastModified()
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error scanning images in $path", e)
                }
            }
        }
        
        return images.sortedByDescending { it.relevanceScore }
    }
    
    /**
     * Find database files
     */
    private fun findDatabaseFiles(): List<DiscoveredFile> {
        val databases = mutableListOf<DiscoveredFile>()
        
        val dbPaths = listOf(
            "/data/data/com.termux/files/home",
            "/data/data/com.example.farmdirectory/databases",
            "/storage/emulated/0/Download"
        )
        
        dbPaths.forEach { path ->
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                try {
                    dir.walkTopDown().maxDepth(3).forEach { file ->
                        if (file.isFile && (file.name.endsWith(".db") || 
                                           file.name.endsWith(".sqlite") ||
                                           file.name.endsWith(".sqlite3"))) {
                            if (isRelevantDatabase(file)) {
                                databases.add(
                                    DiscoveredFile(
                                        path = file.absolutePath,
                                        name = file.name,
                                        size = file.length(),
                                        type = "DATABASE",
                                        relevanceScore = calculateRelevance(file),
                                        lastModified = file.lastModified()
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error scanning databases in $path", e)
                }
            }
        }
        
        return databases.sortedByDescending { it.relevanceScore }
    }
    
    /**
     * Find configuration files
     */
    private fun findConfigFiles(): List<DiscoveredFile> {
        val configs = mutableListOf<DiscoveredFile>()
        
        val configPaths = listOf(
            "/data/data/com.termux/files/home",
            "/storage/emulated/0/Download"
        )
        
        configPaths.forEach { path ->
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                try {
                    dir.listFiles()?.forEach { file ->
                        if (file.name.endsWith(".json") || 
                            file.name.endsWith(".xml") ||
                            file.name.endsWith(".properties") ||
                            file.name.endsWith(".conf")) {
                            if (isRelevantFile(file)) {
                                configs.add(
                                    DiscoveredFile(
                                        path = file.absolutePath,
                                        name = file.name,
                                        size = file.length(),
                                        type = "CONFIG",
                                        relevanceScore = calculateRelevance(file),
                                        lastModified = file.lastModified()
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error scanning configs in $path", e)
                }
            }
        }
        
        return configs.sortedByDescending { it.relevanceScore }
    }
    
    /**
     * Check if file is relevant to farm management
     */
    private fun isRelevantFile(file: File): Boolean {
        val fileName = file.name.toLowerCase()
        return FARM_KEYWORDS.any { keyword -> fileName.contains(keyword) }
    }
    
    /**
     * Check if app is relevant
     */
    private fun isRelevantApp(appName: String, packageName: String): Boolean {
        val searchText = "$appName $packageName".toLowerCase()
        return FARM_KEYWORDS.any { keyword -> searchText.contains(keyword) }
    }
    
    /**
     * Check if file is an image
     */
    private fun isImageFile(file: File): Boolean {
        return IMAGE_EXTENSIONS.any { ext -> file.name.toLowerCase().endsWith(ext) }
    }
    
    /**
     * Check if database is relevant
     */
    private fun isRelevantDatabase(file: File): Boolean {
        return isRelevantFile(file)
    }
    
    /**
     * Calculate file relevance score (0-100)
     */
    private fun calculateRelevance(file: File): Int {
        var score = 0
        val fileName = file.name.toLowerCase()
        
        // Keywords in filename
        FARM_KEYWORDS.forEach { keyword ->
            if (fileName.contains(keyword)) {
                score += 20
            }
        }
        
        // File type bonus
        when {
            fileName.endsWith(".json") -> score += 15
            fileName.endsWith(".csv") -> score += 15
            fileName.endsWith(".db") || fileName.endsWith(".sqlite") -> score += 25
            fileName.endsWith(".xml") -> score += 10
        }
        
        // Recent files bonus
        val age = System.currentTimeMillis() - file.lastModified()
        val daysSinceModified = age / (1000 * 60 * 60 * 24)
        if (daysSinceModified < 7) score += 10
        
        return score.coerceIn(0, 100)
    }
    
    /**
     * Calculate app relevance score
     */
    private fun calculateAppRelevance(appName: String, packageName: String): Int {
        var score = 0
        val searchText = "$appName $packageName".toLowerCase()
        
        FARM_KEYWORDS.forEach { keyword ->
            if (searchText.contains(keyword)) {
                score += 25
            }
        }
        
        return score.coerceIn(0, 100)
    }
    
    /**
     * Calculate image relevance score
     */
    private fun calculateImageRelevance(file: File): Int {
        var score = calculateRelevance(file)
        
        // Small files might be QR codes
        if (file.length() < 100_000) { // < 100KB
            score += 15
        }
        
        // Recent photos
        val age = System.currentTimeMillis() - file.lastModified()
        val hoursSinceModified = age / (1000 * 60 * 60)
        if (hoursSinceModified < 24) score += 20
        
        return score.coerceIn(0, 100)
    }
    
    /**
     * Check if image might be a QR code
     */
    private fun isPotentialQRCode(file: File): Boolean {
        // QR codes are typically small files
        return file.length() < 200_000 // < 200KB
    }
    
    /**
     * Get file type
     */
    private fun getFileType(file: File): String {
        return when {
            file.name.endsWith(".json") -> "JSON"
            file.name.endsWith(".csv") -> "CSV"
            file.name.endsWith(".txt") -> "TEXT"
            file.name.endsWith(".xml") -> "XML"
            file.name.endsWith(".db") || file.name.endsWith(".sqlite") -> "DATABASE"
            isImageFile(file) -> "IMAGE"
            else -> "OTHER"
        }
    }
    
    /**
     * Get image type
     */
    private fun getImageType(file: File): String {
        return when {
            file.name.endsWith(".jpg") || file.name.endsWith(".jpeg") -> "JPEG"
            file.name.endsWith(".png") -> "PNG"
            file.name.endsWith(".gif") -> "GIF"
            file.name.endsWith(".bmp") -> "BMP"
            file.name.endsWith(".webp") -> "WEBP"
            else -> "UNKNOWN"
        }
    }
}

/**
 * System scan result
 */
data class SystemScanResult(
    val success: Boolean,
    val relatedFiles: List<DiscoveredFile> = emptyList(),
    val relatedApps: List<DiscoveredApp> = emptyList(),
    val relatedImages: List<DiscoveredImage> = emptyList(),
    val databaseFiles: List<DiscoveredFile> = emptyList(),
    val configFiles: List<DiscoveredFile> = emptyList(),
    val totalItemsFound: Int = 0,
    val message: String
)

/**
 * Discovered file
 */
data class DiscoveredFile(
    val path: String,
    val name: String,
    val size: Long,
    val type: String,
    val relevanceScore: Int,
    val lastModified: Long
)

/**
 * Discovered app
 */
data class DiscoveredApp(
    val packageName: String,
    val appName: String,
    val version: String,
    val relevanceScore: Int,
    val installDate: Long
)

/**
 * Discovered image
 */
data class DiscoveredImage(
    val path: String,
    val name: String,
    val size: Long,
    val type: String,
    val isPotentialQR: Boolean,
    val relevanceScore: Int,
    val lastModified: Long
)
