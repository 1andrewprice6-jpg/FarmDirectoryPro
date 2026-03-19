package com.example.farmdirectoryupgraded.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.data.HaulLogDetails
import com.example.farmdirectoryupgraded.data.LogDao
import com.example.farmdirectoryupgraded.data.LogEntry
import com.example.farmdirectoryupgraded.ui.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogViewModel(
    private val context: Context,
    private val logDao: LogDao
) : ViewModel() {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    private val csvDateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Event to trigger sharing in UI
    private val _shareFileEvent = MutableSharedFlow<Uri>()
    val shareFileEvent: SharedFlow<Uri> = _shareFileEvent.asSharedFlow()

    val logs: StateFlow<List<com.example.farmdirectoryupgraded.ui.LogEntry>> =
        logDao.getAllLogs()
            .map { entries ->
                entries.map {
                    com.example.farmdirectoryupgraded.ui.LogEntry(
                        id = it.id,
                        category = it.category,
                        level = LogLevel.valueOf(it.level),
                        message = it.message,
                        details = it.details,
                        timestamp = dateFormatter.format(Date(it.timestamp))
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun addLog(category: String, level: String, message: String, details: String) {
        viewModelScope.launch {
            val logEntry = LogEntry(
                category = category,
                level = level,
                message = message,
                details = details,
                timestamp = System.currentTimeMillis()
            )
            logDao.insertLog(logEntry)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            logDao.deleteAllLogs()
            addLog("System", "INFO", "Logs cleared", "")
        }
    }

    fun exportLogs() {
        viewModelScope.launch {
            try {
                // Get all logs directly from DB (suspend function needed in DAO, but currently it returns Flow)
                // We'll collect the current value of the flow for export
                val currentLogs = logs.value
                
                if (currentLogs.isEmpty()) {
                    addLog("System", "WARNING", "Export failed", "No logs to export")
                    return@launch
                }

                val fileName = "farm_logs_${System.currentTimeMillis()}.csv"
                val file = File(context.cacheDir, fileName)

                withContext(Dispatchers.IO) {
                    FileWriter(file).use { writer ->
                        writer.append("Timestamp,Level,Category,Message,Truck,Trailer,Destination,Details\n")
                        currentLogs.forEach {
                            val haul = HaulLogDetails.parse(it.details)
                            val truck = haul?.let { h -> if (h.truckName.isNotEmpty()) h.truckName else h.truckId } ?: ""
                            val trailer = haul?.let { h -> if (h.trailerName.isNotEmpty()) h.trailerName else h.trailerId } ?: ""
                            val destination = haul?.destination ?: ""
                            val extra = haul?.farmName ?: it.details
                            writer.append("${it.timestamp},${it.level},${it.category},\"${it.message}\",\"$truck\",\"$trailer\",\"$destination\",\"$extra\"\n")
                        }
                    }
                }

                // Create URI using FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                _shareFileEvent.emit(uri)
                addLog("System", "SUCCESS", "Logs exported", fileName)

            } catch (e: Exception) {
                addLog("System", "ERROR", "Export failed", e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Logs a haul event with truck, trailer, destination, and farm details.
     * Destination should be one of: "Purdue", "Mountaire", or another plant name.
     */
    fun addHaulLog(
        truckId: String,
        truckName: String,
        trailerId: String,
        trailerName: String,
        destination: String,
        farmName: String,
        level: String = "INFO",
        message: String,
        farmerId: Int? = null
    ) {
        viewModelScope.launch {
            val details = HaulLogDetails(
                truckId = truckId,
                truckName = truckName,
                trailerId = trailerId,
                trailerName = trailerName,
                destination = destination,
                farmName = farmName
            ).encode()
            val logEntry = LogEntry(
                category = "Haul",
                level = level,
                message = message,
                details = details,
                timestamp = System.currentTimeMillis(),
                farmerId = farmerId
            )
            logDao.insertLog(logEntry)
        }
    }
}