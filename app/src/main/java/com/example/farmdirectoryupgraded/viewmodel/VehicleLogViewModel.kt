package com.example.farmdirectoryupgraded.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.data.VehicleLog
import com.example.farmdirectoryupgraded.data.VehicleLogDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

/**
 * ViewModel for vehicle log operations
 *
 * Handles:
 * - Vehicle log CRUD operations
 * - Vehicle log state management
 */
class VehicleLogViewModel(
    private val vehicleLogDao: VehicleLogDao
) : ViewModel() {

    companion object {
        private const val TAG = "VehicleLogViewModel"
    }

    private val _vehicleLogs = MutableStateFlow<List<VehicleLog>>(emptyList())
    val vehicleLogs: StateFlow<List<VehicleLog>> = _vehicleLogs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadAllVehicleLogs()
    }

    private fun loadAllVehicleLogs() {
        vehicleLogDao.getAllVehicleLogs()
            .onEach { logs -> _vehicleLogs.value = logs }
            .catch { e ->
                val errorMsg = "Failed to load vehicle logs: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
            .launchIn(viewModelScope)
    }

    fun addVehicleLog(log: VehicleLog) {
        viewModelScope.launch {
            try {
                vehicleLogDao.insertLog(log)
                _successMessage.value = "Vehicle log added successfully"
                Log.d(TAG, "Vehicle log added for vehicle: ${log.vehicleName}")
            } catch (e: Exception) {
                val errorMsg = "Failed to add vehicle log: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    fun updateVehicleLog(log: VehicleLog) {
        viewModelScope.launch {
            try {
                vehicleLogDao.updateLog(log)
                _successMessage.value = "Vehicle log updated successfully"
                Log.d(TAG, "Vehicle log updated: ${log.id}")
            } catch (e: Exception) {
                val errorMsg = "Failed to update vehicle log: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    fun deleteVehicleLog(log: VehicleLog) {
        viewModelScope.launch {
            try {
                vehicleLogDao.deleteLog(log)
                _successMessage.value = "Vehicle log deleted"
                Log.d(TAG, "Vehicle log deleted: ${log.id}")
            } catch (e: Exception) {
                val errorMsg = "Failed to delete vehicle log: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
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
