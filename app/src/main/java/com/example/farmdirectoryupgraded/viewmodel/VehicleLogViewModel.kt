package com.example.farmdirectoryupgraded.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.data.VehicleLog
import com.example.farmdirectoryupgraded.data.VehicleLogDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for vehicle log operations
 *
 * Handles:
 * - Vehicle log CRUD operations
 * - Vehicle log listing and filtering
 */
class VehicleLogViewModel(private val vehicleLogDao: VehicleLogDao) : ViewModel() {

    companion object {
        private const val TAG = "VehicleLogViewModel"
    }

    val vehicleLogs = vehicleLogDao.getAllVehicleLogs()
        .onEach { Log.d(TAG, "Vehicle logs updated: ${it.size} entries") }
        .catch { e -> Log.e(TAG, "Error loading vehicle logs", e) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    /**
     * Add a new vehicle log entry
     *
     * @param log The vehicle log to add
     */
    fun addVehicleLog(log: VehicleLog) {
        viewModelScope.launch {
            try {
                vehicleLogDao.insertLog(log)
                _successMessage.value = "Vehicle log added"
                Log.d(TAG, "Vehicle log added for: ${log.vehicleName}")
            } catch (e: Exception) {
                val msg = "Failed to add vehicle log: ${e.message}"
                _errorMessage.value = msg
                Log.e(TAG, msg, e)
            }
        }
    }

    /**
     * Delete a vehicle log entry
     *
     * @param log The vehicle log to delete
     */
    fun deleteVehicleLog(log: VehicleLog) {
        viewModelScope.launch {
            try {
                vehicleLogDao.deleteLog(log)
                _successMessage.value = "Vehicle log deleted"
                Log.d(TAG, "Vehicle log deleted: ${log.id}")
            } catch (e: Exception) {
                val msg = "Failed to delete vehicle log: ${e.message}"
                _errorMessage.value = msg
                Log.e(TAG, msg, e)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }
}
