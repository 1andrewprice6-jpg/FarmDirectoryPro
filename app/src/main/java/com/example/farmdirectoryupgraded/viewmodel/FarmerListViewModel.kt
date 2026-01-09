package com.example.farmdirectoryupgraded.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.farmdirectoryupgraded.data.Farmer
import com.example.farmdirectoryupgraded.data.FarmerDao
import com.example.farmdirectoryupgraded.ui.ImportMethod
import com.example.farmdirectoryupgraded.ui.ImportRecord
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for farmer list operations
 *
 * Handles:
 * - Farmer CRUD operations (create, read, update, delete)
 * - Farmer search and filtering
 * - Favorite farmer management
 * - Pagination support for large lists
 * - Data Import (CSV/JSON/Camera)
 */
class FarmerListViewModel(
    private val context: Context,
    private val farmerDao: FarmerDao
) : ViewModel() {

    companion object {
        private const val TAG = "FarmerListViewModel"
        private const val PAGE_SIZE = 20
    }

    private val gson = Gson()
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    // State management
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType = _selectedType.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private val _recentImports = MutableStateFlow<List<ImportRecord>>(emptyList())
    val recentImports = _recentImports.asStateFlow()

    // Paginated farmers list
    val pagedFarmers = Pager(PagingConfig(pageSize = PAGE_SIZE)) {
        farmerDao.getFarmersPaged()
    }.flow.cachedIn(viewModelScope)

    // Full list for map/search (legacy/compatibility)
    // Note: In a real large app, you wouldn't load all for map, but for this scale it's fine.
    // For now, let's expose a Flow that filters based on search query for the non-paged views
    val farmers = com.example.farmdirectoryupgraded.utils.combine(
        _searchQuery,
        _selectedType,
        farmerDao.getAllFarmers() // This returns Flow<List<Farmer>>
    ) { query, type, list ->
        list.filter {
            (query.isEmpty() || it.name.contains(query, ignoreCase = true) || 
             it.farmName.contains(query, ignoreCase = true)) &&
            (type == null || type == "All" || it.type == type)
        }
    }

    /**
     * Update search query and refresh list
     *
     * @param query The search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Filter farmers by type
     *
     * @param type The farmer type to filter by
     */
    fun updateSelectedType(type: String?) {
        _selectedType.value = type
    }
    
    // Alias for compatibility
    fun filterByType(type: String?) {
        updateSelectedType(type)
    }

    /**
     * Add a new farmer
     *
     * @param farmer The farmer to add
     */
    fun addFarmer(farmer: Farmer) {
        viewModelScope.launch {
            try {
                farmerDao.insertFarmer(farmer)
                _successMessage.value = "Farmer added successfully"
                Log.d(TAG, "Farmer added: ${farmer.name}")
            } catch (e: Exception) {
                val errorMsg = "Failed to add farmer: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Update an existing farmer
     *
     * @param farmer The farmer with updated data
     */
    fun updateFarmer(farmer: Farmer) {
        viewModelScope.launch {
            try {
                farmerDao.updateFarmer(farmer)
                _successMessage.value = "Farmer updated successfully"
                Log.d(TAG, "Farmer updated: ${farmer.name}")
            } catch (e: Exception) {
                val errorMsg = "Failed to update farmer: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Delete a farmer
     *
     * @param farmer The farmer to delete
     */
    fun deleteFarmer(farmer: Farmer) {
        viewModelScope.launch {
            try {
                farmerDao.deleteFarmer(farmer)
                _successMessage.value = "Farmer deleted successfully"
                Log.d(TAG, "Farmer deleted: ${farmer.name}")
            } catch (e: Exception) {
                val errorMsg = "Failed to delete farmer: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Toggle farmer favorite status
     */
    fun toggleFavorite(farmer: Farmer) {
        viewModelScope.launch {
            try {
                farmerDao.updateFavoriteSatus(farmer.id, !farmer.isFavorite)
                Log.d(TAG, "Farmer ${farmer.id} favorite toggled")
            } catch (e: Exception) {
                val errorMsg = "Failed to update favorite: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
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

                val fileName = getFileName(uri) ?: uri.lastPathSegment ?: ""
                val mimeType = context.contentResolver.getType(uri) ?: ""

                val importedCount = when {
                    fileName.endsWith(".json", ignoreCase = true) || mimeType.contains("json") -> importFromJson(content)
                    fileName.endsWith(".csv", ignoreCase = true) || mimeType.contains("csv") || mimeType.contains("comma-separated") -> importFromCsv(content)
                    content.trimStart().startsWith("{") || content.trimStart().startsWith("[") -> importFromJson(content)
                    content.contains(",") -> importFromCsv(content)
                    else -> {
                        _errorMessage.value = "Unsupported file format. Please use CSV or JSON files."
                        0
                    }
                }

                if (importedCount > 0) {
                    addImportRecord(ImportMethod.FILE.name, importedCount, true)
                    _successMessage.value = "Imported $importedCount farmers"
                } else {
                    addImportRecord(ImportMethod.FILE.name, 0, false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Import failed: ${e.message}"
                addImportRecord(ImportMethod.FILE.name, 0, false)
            }
        }
    }

    private suspend fun importFromJson(content: String): Int {
        return try {
            val farmerList = gson.fromJson(content, Array<Farmer>::class.java).toList()
            farmerDao.insertFarmers(farmerList)
            farmerList.size
        } catch (e: JsonSyntaxException) {
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

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val currentField = StringBuilder()
        var insideQuotes = false

        var i = 0
        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        currentField.append('"')
                        i++
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

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = it.getString(displayNameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.let {
                val cut = it.lastIndexOf('/')
                if (cut != -1) it.substring(cut + 1) else it
            }
        }
        return result
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

    fun importFromCameraText(ocrText: String) {
        viewModelScope.launch {
            try {
                val count = parseOCRTextAndImport(ocrText)
                _successMessage.value = "Imported $count farmer(s) from camera"
            } catch (e: Exception) {
                _errorMessage.value = "Camera import failed: ${e.message}"
            }
        }
    }

    private suspend fun parseOCRTextAndImport(text: String): Int {
        val lines = text.split("\n").filter { it.isNotBlank() }
        val farmers = mutableListOf<Farmer>()
        var currentFarmer = mutableMapOf<String, String>()

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.matches(Regex("(?i)name\s*:?\s*(.+)", RegexOption.IGNORE_CASE)) -> {
                    val match = Regex("(?i)name\s*:?\s*(.+)").find(trimmed)
                    match?.groupValues?.get(1)?.let { currentFarmer["name"] = it.trim() }
                }
                trimmed.matches(Regex("(?i)phone\s*:?\s*(.+)", RegexOption.IGNORE_CASE)) -> {
                    val match = Regex("(?i)phone\s*:?\s*(.+)").find(trimmed)
                    match?.groupValues?.get(1)?.let { currentFarmer["phone"] = it.trim() }
                }
                trimmed.matches(Regex("(?i)address\s*:?\s*(.+)", RegexOption.IGNORE_CASE)) -> {
                    val match = Regex("(?i)address\s*:?\s*(.+)").find(trimmed)
                    match?.groupValues?.get(1)?.let { currentFarmer["address"] = it.trim() }
                }
                trimmed.matches(Regex("(?i)type\s*:?\s*(.+)", RegexOption.IGNORE_CASE)) -> {
                    val match = Regex("(?i)type\s*:?\s*(.+)").find(trimmed)
                    match?.groupValues?.get(1)?.let { currentFarmer["type"] = it.trim() }
                }
                trimmed.matches(Regex("(?i)email\s*:?\s*(.+)", RegexOption.IGNORE_CASE)) -> {
                    val match = Regex("(?i)email\s*:?\s*(.+)").find(trimmed)
                    match?.groupValues?.get(1)?.let { currentFarmer["email"] = it.trim() }
                }
                trimmed.matches(Regex("(?i)farm\s*name\s*:?\s*(.+)", RegexOption.IGNORE_CASE)) -> {
                    val match = Regex("(?i)farm\s*name\s*:?\s*(.+)").find(trimmed)
                    match?.groupValues?.get(1)?.let { currentFarmer["farmName"] = it.trim() }
                }
                trimmed == "---" || trimmed == "===" -> {
                    if (currentFarmer.containsKey("name") && currentFarmer.containsKey("address")) {
                        farmers.add(createFarmerFromMap(currentFarmer))
                        currentFarmer = mutableMapOf()
                    }
                }
            }
        }

        if (currentFarmer.containsKey("name") && currentFarmer.containsKey("address")) {
            farmers.add(createFarmerFromMap(currentFarmer))
        }

        if (farmers.isEmpty()) {
            throw Exception("No valid farmer data found in text.")
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
        // Placeholder
    }
}