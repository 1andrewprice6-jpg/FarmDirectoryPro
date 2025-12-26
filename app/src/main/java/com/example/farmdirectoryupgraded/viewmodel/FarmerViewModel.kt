package com.example.farmdirectoryupgraded.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.data.*
import com.example.farmdirectoryupgraded.ui.ImportMethod
import com.example.farmdirectoryupgraded.ui.ImportRecord
import com.example.farmdirectoryupgraded.ui.AttendanceMethod
import com.example.farmdirectoryupgraded.ui.ReconcileResult
import com.example.farmdirectoryupgraded.ui.AlternativeFarm
import com.example.farmdirectoryupgraded.ui.OptimizedRoute
import com.example.farmdirectoryupgraded.ui.RouteStop
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class FarmerViewModel(
    private val context: Context,
    private val farmerDao: FarmerDao,
    private val attendanceDao: AttendanceDao,
    private val logDao: LogDao,
    private val webSocketService: FarmWebSocketService = FarmWebSocketService.getInstance()
) : ViewModel() {

    private val TAG = "FarmerViewModel"
    private val gson = Gson()
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow("All")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    // WebSocket connection state - enhanced
    val isConnected: StateFlow<Boolean> = webSocketService.isConnected
    val connectionState: StateFlow<ConnectionState> = webSocketService.connectionState
    val isLoading: StateFlow<Boolean> = webSocketService.isLoading
    val connectionErrorMessage: StateFlow<String?> = webSocketService.connectionErrorMessage

    // User-facing error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Success messages
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Real-time location updates
    private val _recentLocationUpdate = MutableStateFlow<LocationBroadcast?>(null)
    val recentLocationUpdate: StateFlow<LocationBroadcast?> = _recentLocationUpdate.asStateFlow()

    // Health alerts
    private val _healthAlerts = MutableSharedFlow<HealthAlert>()
    val healthAlerts: SharedFlow<HealthAlert> = _healthAlerts.asSharedFlow()

    // Critical alerts
    private val _criticalAlerts = MutableSharedFlow<HealthAlert>()
    val criticalAlerts: SharedFlow<HealthAlert> = _criticalAlerts.asSharedFlow()

    // Worker presence
    val activeWorkers: StateFlow<List<WorkerInfo>> = webSocketService.workerPresence

    // Attendance records
    val attendanceRecords: StateFlow<List<com.example.farmdirectoryupgraded.ui.AttendanceRecord>> =
        attendanceDao.getAllAttendanceRecords()
            .map { records ->
                records.map { record ->
                    com.example.farmdirectoryupgraded.ui.AttendanceRecord(
                        id = record.id,
                        farmName = record.farmName,
                        method = record.method,
                        timestamp = dateFormatter.format(Date(record.checkInTime)),
                        notes = record.notes,
                        checkOut = record.checkOutTime?.let { dateFormatter.format(Date(it)) }
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Logs
    val logs: StateFlow<List<com.example.farmdirectoryupgraded.ui.LogEntry>> =
        logDao.getAllLogs()
            .map { entries ->
                entries.map { entry ->
                    com.example.farmdirectoryupgraded.ui.LogEntry(
                        id = entry.id,
                        category = entry.category,
                        level = com.example.farmdirectoryupgraded.ui.LogLevel.valueOf(entry.level),
                        message = entry.message,
                        details = entry.details,
                        timestamp = dateFormatter.format(Date(entry.timestamp))
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Recent imports
    private val _recentImports = MutableStateFlow<List<ImportRecord>>(emptyList())
    val recentImports: StateFlow<List<ImportRecord>> = _recentImports.asStateFlow()

    init {
        // Collect WebSocket events
        collectWebSocketEvents()
        // Collect WebSocket errors
        collectWebSocketErrors()
    }

    val farmers: StateFlow<List<Farmer>> = combine(
        _searchQuery,
        _selectedType
    ) { query, type ->
        Pair(query, type)
    }.flatMapLatest { (query, type) ->
        when {
            query.isNotEmpty() -> farmerDao.searchFarmers(query)
            type != "All" -> farmerDao.getFarmersByType(type)
            else -> farmerDao.getAllFarmers()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteFarmers: StateFlow<List<Farmer>> = farmerDao.getFavoriteFarmers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedType(type: String) {
        _selectedType.value = type
    }

    fun toggleFavorite(farmer: Farmer) {
        viewModelScope.launch {
            farmerDao.updateFarmer(farmer.copy(isFavorite = !farmer.isFavorite))
        }
    }

    fun addFarmer(farmer: Farmer) {
        viewModelScope.launch {
            farmerDao.insertFarmer(farmer)
            addLog("Farmer", "SUCCESS", "Added farmer: ${farmer.name}", "Farm: ${farmer.farmName}")
        }
    }

    fun updateFarmer(farmer: Farmer) {
        viewModelScope.launch {
            farmerDao.updateFarmer(farmer)
            addLog("Farmer", "SUCCESS", "Updated farmer: ${farmer.name}", "Farm: ${farmer.farmName}")
        }
    }

    fun deleteFarmer(farmer: Farmer) {
        viewModelScope.launch {
            farmerDao.deleteFarmer(farmer)
            addLog("Farmer", "SUCCESS", "Deleted farmer: ${farmer.name}", "Farm: ${farmer.farmName}")
        }
    }

    // ========================================================================
    // WEBSOCKET INTEGRATION
    // ========================================================================

    /**
     * Collect WebSocket events and update state
     */
    private fun collectWebSocketEvents() {
        // Location updates
        viewModelScope.launch {
            webSocketService.locationUpdates.collect { locationUpdate ->
                try {
                    _recentLocationUpdate.value = locationUpdate
                    Log.d(TAG, "Location updated: ${locationUpdate.entityId}")

                    // Update local database with new location
                    val farmerId = locationUpdate.entityId.toIntOrNull()
                    farmerId?.let { id ->
                        val farmer = farmers.value.find { it.id == id }
                        farmer?.let {
                            farmerDao.updateFarmer(
                                it.copy(
                                    latitude = locationUpdate.location.latitude,
                                    longitude = locationUpdate.location.longitude,
                                    lastLocationUpdate = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing location update", e)
                    _errorMessage.value = "Failed to process location update: ${e.message}"
                }
            }
        }

        // Health alerts
        viewModelScope.launch {
            webSocketService.healthAlerts.collect { alert ->
                try {
                    _healthAlerts.emit(alert)
                    Log.d(TAG, "Health alert: ${alert.entityId}")
                    addLog("WebSocket", "WARNING", "Health alert received", "Entity: ${alert.entityId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing health alert", e)
                }
            }
        }

        // Critical alerts
        viewModelScope.launch {
            webSocketService.criticalAlerts.collect { alert ->
                try {
                    _criticalAlerts.emit(alert)
                    Log.d(TAG, "CRITICAL alert: ${alert.entityId}")
                    addLog("WebSocket", "ERROR", "CRITICAL alert received", "Entity: ${alert.entityId}, Notes: ${alert.healthNotes}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing critical alert", e)
                }
            }
        }

        // Worker joined
        viewModelScope.launch {
            webSocketService.workerJoined.collect { joined ->
                try {
                    Log.d(TAG, "${joined.workerName} joined the farm")
                    _successMessage.value = "${joined.workerName} joined"
                    addLog("WebSocket", "INFO", "Worker joined", "Worker: ${joined.workerName}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing worker joined", e)
                }
            }
        }

        // Worker left
        viewModelScope.launch {
            webSocketService.workerLeft.collect { left ->
                try {
                    Log.d(TAG, "${left.workerName} left the farm")
                    addLog("WebSocket", "INFO", "Worker left", "Worker: ${left.workerName}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing worker left", e)
                }
            }
        }
    }

    /**
     * Collect WebSocket errors and display user-friendly messages
     */
    private fun collectWebSocketErrors() {
        viewModelScope.launch {
            webSocketService.errors.collect { error ->
                val errorMsg = when (error) {
                    is WebSocketError.ConnectionFailed -> {
                        "Connection failed: ${error.message}"
                    }
                    is WebSocketError.JoinFarmFailed -> {
                        "Failed to join farm ${error.farmId}: ${error.reason}"
                    }
                    is WebSocketError.NetworkError -> {
                        "Network error: ${error.message}"
                    }
                    is WebSocketError.TimeoutError -> {
                        "Operation timed out: ${error.operation}"
                    }
                    is WebSocketError.InvalidToken -> {
                        "Authentication failed: ${error.message}"
                    }
                    is WebSocketError.BackendOffline -> {
                        "Backend server is offline (${error.backendUrl})"
                    }
                    is WebSocketError.UnknownError -> {
                        "Error: ${error.message}"
                    }
                }

                _errorMessage.value = errorMsg
                addLog("WebSocket", "ERROR", "WebSocket error", errorMsg)
                Log.e(TAG, "WebSocket error: $errorMsg")
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
        webSocketService.clearError()
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Connect to WebSocket server with error handling
     */
    fun connectToBackend() {
        viewModelScope.launch {
            try {
                webSocketService.connect()
                addLog("WebSocket", "INFO", "Connecting to backend", "")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect", e)
                _errorMessage.value = "Failed to connect: ${e.message}"
                addLog("WebSocket", "ERROR", "Connection failed", e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Retry WebSocket connection
     */
    fun retryConnection() {
        viewModelScope.launch {
            try {
                webSocketService.retryConnection()
                addLog("WebSocket", "INFO", "Retrying connection", "")
            } catch (e: Exception) {
                Log.e(TAG, "Retry failed", e)
                _errorMessage.value = "Retry failed: ${e.message}"
            }
        }
    }

    /**
     * Disconnect from WebSocket server
     */
    fun disconnectFromBackend() {
        viewModelScope.launch {
            try {
                webSocketService.disconnect()
                _successMessage.value = "Disconnected from backend"
                addLog("WebSocket", "INFO", "Disconnected from backend", "")
            } catch (e: Exception) {
                Log.e(TAG, "Disconnect error", e)
            }
        }
    }

    /**
     * Join a farm for real-time monitoring with enhanced error handling
     */
    fun joinFarm(farmId: String, workerId: String, workerName: String, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                webSocketService.joinFarm(farmId, workerId, workerName) { success ->
                    viewModelScope.launch {
                        if (success) {
                            Log.d(TAG, "Successfully joined farm: $farmId")
                            _successMessage.value = "Joined farm: $farmId"
                            addLog("WebSocket", "SUCCESS", "Joined farm", "Farm ID: $farmId, Worker: $workerName")
                        } else {
                            Log.e(TAG, "Failed to join farm: $farmId")
                            _errorMessage.value = "Failed to join farm: $farmId"
                            addLog("WebSocket", "ERROR", "Failed to join farm", "Farm ID: $farmId")
                        }
                        onResult?.invoke(success)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Join farm exception", e)
                _errorMessage.value = "Exception joining farm: ${e.message}"
                addLog("WebSocket", "ERROR", "Join farm exception", e.message ?: "Unknown error")
                onResult?.invoke(false)
            }
        }
    }

    /**
     * Leave current farm
     */
    fun leaveFarm() {
        webSocketService.leaveFarm()
    }

    /**
     * Update farmer location (GPS coordinates)
     */
    fun updateFarmerLocation(farmerId: Int, latitude: Double, longitude: Double, workerId: String) {
        val location = GPSCoordinates(
            latitude = latitude,
            longitude = longitude,
            timestamp = java.util.Date()
        )

        webSocketService.updateLocation(
            entityId = farmerId.toString(),
            location = location,
            workerId = workerId
        )

        // Also update local database
        viewModelScope.launch {
            val farmer = farmers.value.find { it.id == farmerId }
            farmer?.let {
                farmerDao.updateFarmer(
                    it.copy(
                        latitude = latitude,
                        longitude = longitude,
                        lastLocationUpdate = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    /**
     * Update farmer health status
     */
    fun updateFarmerHealth(
        farmerId: Int,
        healthStatus: HealthStatus,
        healthNotes: String,
        workerId: String
    ) {
        webSocketService.updateHealth(
            entityId = farmerId.toString(),
            healthStatus = healthStatus,
            healthNotes = healthNotes,
            workerId = workerId
        )

        // Also update local database
        viewModelScope.launch {
            val farmer = farmers.value.find { it.id == farmerId }
            farmer?.let {
                farmerDao.updateFarmer(
                    it.copy(
                        healthStatus = healthStatus.name,
                        healthNotes = healthNotes
                    )
                )
            }
        }
    }

    // ========================================================================
    // CSV/JSON IMPORT
    // ========================================================================

    fun importFromFile(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                reader.close()

                // Get actual filename from ContentResolver
                val fileName = getFileName(uri) ?: uri.lastPathSegment ?: ""

                // Also check MIME type as fallback
                val mimeType = context.contentResolver.getType(uri) ?: ""

                val importedCount = when {
                    fileName.endsWith(".json", ignoreCase = true) || mimeType.contains("json") -> importFromJson(content)
                    fileName.endsWith(".csv", ignoreCase = true) || mimeType.contains("csv") || mimeType.contains("comma-separated") -> importFromCsv(content)
                    // Try to auto-detect format by content
                    content.trimStart().startsWith("{") || content.trimStart().startsWith("[") -> importFromJson(content)
                    content.contains(",") -> importFromCsv(content) // Likely CSV
                    else -> {
                        addLog("Import", "ERROR", "Unsupported file format", "File: $fileName, MIME: $mimeType")
                        _errorMessage.value = "Unsupported file format. Please use CSV or JSON files."
                        0
                    }
                }

                if (importedCount > 0) {
                    addImportRecord(ImportMethod.FILE.name, importedCount, true)
                    addLog("Import", "SUCCESS", "Imported $importedCount farmers from file", "File: $fileName")
                } else {
                    addImportRecord(ImportMethod.FILE.name, 0, false)
                }
            } catch (e: Exception) {
                addLog("Import", "ERROR", "Import failed: ${e.message}", e.stackTraceToString())
                addImportRecord(ImportMethod.FILE.name, 0, false)
            }
        }
    }

    /**
     * Parse a CSV line handling quoted fields with commas
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val currentField = StringBuilder()
        var insideQuotes = false

        var i = 0
        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' -> {
                    // Check for escaped quote ("")
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        currentField.append('"')
                        i++ // Skip next quote
                    } else {
                        insideQuotes = !insideQuotes
                    }
                }
                char == ',' && !insideQuotes -> {
                    result.add(currentField.toString().trim())
                    currentField.clear()
                }
                else -> {
                    currentField.append(char)
                }
            }
            i++
        }
        result.add(currentField.toString().trim())
        return result
    }

    /**
     * Get actual filename from content URI
     */
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.let { path ->
                val cut = path.lastIndexOf('/')
                if (cut != -1) path.substring(cut + 1) else path
            }
        }
        return result
    }

    private suspend fun importFromJson(content: String): Int {
        return try {
            val farmerList = gson.fromJson(content, Array<Farmer>::class.java).toList()
            farmerDao.insertFarmers(farmerList)
            farmerList.size
        } catch (e: JsonSyntaxException) {
            addLog("Import", "ERROR", "Invalid JSON format", e.message ?: "")
            0
        }
    }

    private suspend fun importFromCsv(content: String): Int {
        val lines = content.split("\n").filter { it.isNotBlank() }
        if (lines.isEmpty()) return 0

        val farmers = mutableListOf<Farmer>()
        val header = parseCsvLine(lines.first())

        for (i in 1 until lines.size) {
            val values = parseCsvLine(lines[i])
            if (values.size < 2) continue

            val farmerMap = header.zip(values).toMap()

            val farmer = Farmer(
                name = farmerMap["owner"] ?: farmerMap["Owner"] ?: farmerMap["name"] ?: farmerMap["Name"] ?: "",
                spouse = farmerMap["spouse"] ?: farmerMap["Spouse"] ?: "",
                farmName = farmerMap["name"] ?: farmerMap["Name"] ?: farmerMap["farmName"] ?: farmerMap["FarmName"] ?: farmerMap["farm_name"] ?: "",
                address = farmerMap["address"] ?: farmerMap["Address"] ?: "",
                phone = farmerMap["phone"] ?: farmerMap["Phone"] ?: "",
                cellPhone = farmerMap["cellPhone"] ?: farmerMap["CellPhone"] ?: farmerMap["cell_phone"] ?: "",
                email = farmerMap["email"] ?: farmerMap["Email"] ?: "",
                type = farmerMap["type"] ?: farmerMap["Type"] ?: "",
                company = farmerMap["company"] ?: farmerMap["Company"] ?: "",
                latitude = farmerMap["latitude"]?.toDoubleOrNull() ?: farmerMap["Latitude"]?.toDoubleOrNull(),
                longitude = farmerMap["longitude"]?.toDoubleOrNull() ?: farmerMap["Longitude"]?.toDoubleOrNull()
            )

            if (farmer.name.isNotBlank() && farmer.address.isNotBlank()) {
                farmers.add(farmer)
            }
        }

        farmerDao.insertFarmers(farmers)
        return farmers.size
    }

    private fun addImportRecord(method: String, count: Int, success: Boolean) {
        val record = ImportRecord(
            method = method,
            recordsImported = count,
            timestamp = dateFormatter.format(Date()),
            success = success
        )
        _recentImports.value = listOf(record) + _recentImports.value.take(9)
    }

    // Placeholder methods for UI compatibility
    /**
     * Import farmers from OCR text extracted from camera
     */
    fun importFromCameraText(ocrText: String) {
        viewModelScope.launch {
            try {
                val count = parseOCRTextAndImport(ocrText)
                _successMessage.value = "Imported $count farmer(s) from camera"
                addLog("Import", "SUCCESS", "Camera import completed", "Farmers imported: $count")
            } catch (e: Exception) {
                _errorMessage.value = "Camera import failed: ${e.message}"
                addLog("Import", "ERROR", "Camera import failed", e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Parse OCR text and extract farmer data
     * Supports various formats:
     * - Name: John Doe
     * - Phone: 123-456-7890
     * - Address: 123 Main St
     * - Type: Broiler/Layer/etc
     */
    private suspend fun parseOCRTextAndImport(text: String): Int {
        val lines = text.split("\n").filter { it.isNotBlank() }
        val farmers = mutableListOf<Farmer>()

        var currentFarmer = mutableMapOf<String, String>()

        for (line in lines) {
            val trimmed = line.trim()

            // Parse key-value pairs
            when {
                trimmed.matches(Regex("(?i)name\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)) -> {
                    val match = Regex("(?i)name\\s*:?\\s*(.+)").find(trimmed)
                    match?.groupValues?.get(1)?.let { currentFarmer["name"] = it.trim() }
                }
                trimmed.matches(Regex("(?i)phone\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)) -> {
                    val match = Regex("(?i)phone\\s*:?\\s*(.+)").find(trimmed)
                    match?.groupValues?.get(1)?.let { currentFarmer["phone"] = it.trim() }
                }
                trimmed.matches(Regex("(?i)address\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)) -> {
                    val match = Regex("(?i)address\\s*:?\\s*(.+)").find(trimmed)
                    match?.groupValues?.get(1)?.let { currentFarmer["address"] = it.trim() }
                }
                trimmed.matches(Regex("(?i)type\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)) -> {
                    val match = Regex("(?i)type\\s*:?\\s*(.+)").find(trimmed)
                    match?.groupValues?.get(1)?.let { currentFarmer["type"] = it.trim() }
                }
                trimmed.matches(Regex("(?i)email\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)) -> {
                    val match = Regex("(?i)email\\s*:?\\s*(.+)").find(trimmed)
                    match?.groupValues?.get(1)?.let { currentFarmer["email"] = it.trim() }
                }
                trimmed.matches(Regex("(?i)farm\\s*name\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)) -> {
                    val match = Regex("(?i)farm\\s*name\\s*:?\\s*(.+)").find(trimmed)
                    match?.groupValues?.get(1)?.let { currentFarmer["farmName"] = it.trim() }
                }
                trimmed == "---" || trimmed == "===" -> {
                    // Separator - save current farmer and start new one
                    if (currentFarmer.containsKey("name") && currentFarmer.containsKey("address")) {
                        farmers.add(createFarmerFromMap(currentFarmer))
                        currentFarmer = mutableMapOf()
                    }
                }
            }
        }

        // Add last farmer if exists
        if (currentFarmer.containsKey("name") && currentFarmer.containsKey("address")) {
            farmers.add(createFarmerFromMap(currentFarmer))
        }

        if (farmers.isEmpty()) {
            throw Exception("No valid farmer data found in text. Make sure image contains Name and Address fields.")
        }

        farmerDao.insertFarmers(farmers)
        return farmers.size
    }

    private fun createFarmerFromMap(data: Map<String, String>): Farmer {
        return Farmer(
            name = data["name"] ?: "",
            phone = data["phone"] ?: "",
            email = data["email"] ?: "",
            address = data["address"] ?: "",
            type = data["type"] ?: "Unknown",
            farmName = data["farmName"] ?: ""
        )
    }

    fun startVoiceInput() {
        viewModelScope.launch {
            addLog("Import", "INFO", "Voice input not yet implemented", "")
        }
    }

    fun showEmailImportDialog() {
        viewModelScope.launch {
            addLog("Import", "INFO", "Email import not yet implemented", "")
        }
    }

    fun showCloudImportDialog() {
        viewModelScope.launch {
            addLog("Import", "INFO", "Cloud import not yet implemented", "")
        }
    }

    fun startNFCReader() {
        viewModelScope.launch {
            addLog("Import", "INFO", "NFC reader not yet implemented", "")
        }
    }

    fun showAPIImportDialog() {
        viewModelScope.launch {
            addLog("Import", "INFO", "API import not yet implemented", "")
        }
    }

    fun prepareCameraImport() {
        viewModelScope.launch {
            addLog("Import", "INFO", "Camera import not yet implemented", "")
        }
    }

    // ========================================================================
    // ATTENDANCE TRACKING
    // ========================================================================

    fun recordAttendance(method: AttendanceMethod, farmName: String, notes: String) {
        viewModelScope.launch {
            val record = AttendanceRecord(
                farmName = farmName,
                method = method.name,
                checkInTime = System.currentTimeMillis(),
                notes = notes,
                workerId = "worker-${System.currentTimeMillis()}"
            )
            attendanceDao.insertAttendance(record)
            addLog("Attendance", "SUCCESS", "Check-in recorded", "Farm: $farmName, Method: ${method.displayName}")
        }
    }

    // ========================================================================
    // GPS RECONCILIATION
    // ========================================================================

    fun getCurrentLocation(callback: (Double, Double) -> Unit) {
        // Placeholder - in real app, use FusedLocationProviderClient
        // For now, return a sample location
        viewModelScope.launch {
            addLog("Reconcile", "INFO", "GPS location requested", "")
        }
        callback(35.7796, -81.3361) // Sample: Hiddenite, NC area
    }

    fun reconcileFarm(latitude: Double, longitude: Double, callback: (ReconcileResult) -> Unit) {
        viewModelScope.launch {
            try {
                val farmersWithLocation = farmers.value.filter {
                    it.latitude != null && it.longitude != null
                }

                if (farmersWithLocation.isEmpty()) {
                    addLog("Reconcile", "WARNING", "No farms with GPS coordinates", "")
                    return@launch
                }

                val distances = farmersWithLocation.map { farmer ->
                    val distance = calculateHaversineDistance(
                        latitude, longitude,
                        farmer.latitude!!, farmer.longitude!!
                    )
                    Triple(farmer, distance, calculateConfidence(distance))
                }.sortedBy { it.second }

                val nearest = distances.first()
                val alternatives = distances.drop(1).take(3).map {
                    AlternativeFarm(
                        farmName = it.first.farmName.ifBlank { it.first.name },
                        distance = it.second
                    )
                }

                val result = ReconcileResult(
                    farmName = nearest.first.farmName.ifBlank { nearest.first.name },
                    distance = nearest.second,
                    confidence = nearest.third,
                    alternatives = alternatives
                )

                addLog("Reconcile", "SUCCESS", "Reconciled to: ${result.farmName}",
                    "Distance: ${String.format("%.2f", result.distance)} km, Confidence: ${String.format("%.1f", result.confidence)}%")

                callback(result)
            } catch (e: Exception) {
                addLog("Reconcile", "ERROR", "Reconciliation failed", e.message ?: "")
            }
        }
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Earth radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun calculateConfidence(distance: Double): Double {
        return when {
            distance < 0.5 -> 95.0
            distance < 1.0 -> 85.0
            distance < 2.0 -> 75.0
            distance < 5.0 -> 60.0
            else -> 40.0
        }
    }

    // ========================================================================
    // ROUTE OPTIMIZATION
    // ========================================================================

    fun optimizeRoute(selectedFarmers: List<Farmer>, callback: (OptimizedRoute) -> Unit) {
        viewModelScope.launch {
            try {
                if (selectedFarmers.isEmpty()) {
                    addLog("Route", "WARNING", "No farms selected for optimization", "")
                    return@launch
                }

                val farmersWithLocation = selectedFarmers.filter {
                    it.latitude != null && it.longitude != null
                }

                if (farmersWithLocation.isEmpty()) {
                    addLog("Route", "ERROR", "Selected farms have no GPS coordinates", "")
                    return@launch
                }

                // Nearest Neighbor Algorithm
                val route = mutableListOf<Farmer>()
                val remaining = farmersWithLocation.toMutableList()

                // Start with the first farm
                var current = remaining.removeAt(0)
                route.add(current)

                // Find nearest neighbor iteratively
                while (remaining.isNotEmpty()) {
                    val nearest = remaining.minByOrNull { next ->
                        calculateHaversineDistance(
                            current.latitude!!, current.longitude!!,
                            next.latitude!!, next.longitude!!
                        )
                    }!!

                    remaining.remove(nearest)
                    route.add(nearest)
                    current = nearest
                }

                // Calculate route details
                var totalDistance = 0.0
                val stops = mutableListOf<RouteStop>()

                route.forEachIndexed { index, farmer ->
                    val distanceFromPrev = if (index == 0) {
                        0.0
                    } else {
                        calculateHaversineDistance(
                            route[index - 1].latitude!!,
                            route[index - 1].longitude!!,
                            farmer.latitude!!,
                            farmer.longitude!!
                        )
                    }

                    totalDistance += distanceFromPrev

                    stops.add(
                        RouteStop(
                            farmName = farmer.farmName.ifBlank { farmer.name },
                            distanceFromPrevious = if (index == 0) "Start" else String.format("%.1f", distanceFromPrev),
                            timeFromPrevious = if (index == 0) "-" else "${(distanceFromPrev * 1.5).toInt()} min"
                        )
                    )
                }

                val estimatedTime = "${(totalDistance * 1.5).toInt()} minutes"
                val fuelCost = totalDistance * 0.15 // Assume $0.15 per km

                val optimizedRoute = OptimizedRoute(
                    stops = stops,
                    totalDistance = totalDistance,
                    estimatedTime = estimatedTime,
                    fuelCost = fuelCost
                )

                addLog("Route", "SUCCESS", "Optimized route for ${selectedFarmers.size} farms",
                    "Total distance: ${String.format("%.1f", totalDistance)} km")

                callback(optimizedRoute)
            } catch (e: Exception) {
                addLog("Route", "ERROR", "Route optimization failed", e.message ?: "")
            }
        }
    }

    // ========================================================================
    // ACTIVITY LOGGING
    // ========================================================================

    private suspend fun addLog(category: String, level: String, message: String, details: String) {
        val logEntry = LogEntry(
            category = category,
            level = level,
            message = message,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        logDao.insertLog(logEntry)
    }

    fun exportLogs() {
        viewModelScope.launch {
            addLog("System", "INFO", "Export logs requested", "")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            logDao.deleteAllLogs()
            addLog("System", "INFO", "Logs cleared", "")
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }
}

class FarmerViewModelFactory(
    private val context: Context,
    private val farmerDao: FarmerDao,
    private val attendanceDao: AttendanceDao,
    private val logDao: LogDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FarmerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FarmerViewModel(context, farmerDao, attendanceDao, logDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
