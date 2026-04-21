package com.example.farmdirectoryupgraded.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.data.VehicleLog
import com.example.farmdirectoryupgraded.data.VehicleLogDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleLogViewModel(private val vehicleLogDao: VehicleLogDao) : ViewModel() {
    companion object { private const val TAG = "VehicleLogViewModel" }

    private val _vehicleLogs = MutableStateFlow<List<VehicleLog>>(emptyList())
    val vehicleLogs: StateFlow<List<VehicleLog>> = _vehicleLogs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init { loadVehicleLogs() }

    private fun loadVehicleLogs() {
        viewModelScope.launch {
            vehicleLogDao.getAllVehicleLogs().collect { logs -> _vehicleLogs.value = logs }
        }
    }

    fun addVehicleLog(log: VehicleLog) {
        viewModelScope.launch {
            try {
                vehicleLogDao.insertLog(log)
                _successMessage.value = "Vehicle log added"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add vehicle log: ${e.message}"
                Log.e(TAG, "addVehicleLog", e)
            }
        }
    }

    fun deleteVehicleLog(log: VehicleLog) {
        viewModelScope.launch {
            try {
                vehicleLogDao.deleteLog(log)
                _successMessage.value = "Vehicle log deleted"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete vehicle log: ${e.message}"
                Log.e(TAG, "deleteVehicleLog", e)
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
    fun clearSuccess() { _successMessage.value = null }
}
