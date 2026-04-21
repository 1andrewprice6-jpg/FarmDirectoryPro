package com.example.farmdirectoryupgraded.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.data.FuelLog
import com.example.farmdirectoryupgraded.data.FuelLogDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

/**
 * ViewModel for fuel log operations
 *
 * Handles:
 * - Fuel log CRUD operations
 * - Fuel log state management
 */
class FuelLogViewModel(
    private val fuelLogDao: FuelLogDao
) : ViewModel() {

    companion object {
        private const val TAG = "FuelLogViewModel"
    }

    private val _fuelLogs = MutableStateFlow<List<FuelLog>>(emptyList())
    val fuelLogs: StateFlow<List<FuelLog>> = _fuelLogs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadAllFuelLogs()
    }

    private fun loadAllFuelLogs() {
        fuelLogDao.getAllFuelLogs()
            .onEach { logs -> _fuelLogs.value = logs }
            .catch { e ->
                val errorMsg = "Failed to load fuel logs: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
            .launchIn(viewModelScope)
    }

    fun addFuelLog(log: FuelLog) {
        viewModelScope.launch {
            try {
                fuelLogDao.insertLog(log)
                _successMessage.value = "Fuel log added successfully"
                Log.d(TAG, "Fuel log added for vehicle: ${log.vehicleName}")
            } catch (e: Exception) {
                val errorMsg = "Failed to add fuel log: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    fun updateFuelLog(log: FuelLog) {
        viewModelScope.launch {
            try {
                fuelLogDao.updateLog(log)
                _successMessage.value = "Fuel log updated successfully"
                Log.d(TAG, "Fuel log updated: ${log.id}")
            } catch (e: Exception) {
                val errorMsg = "Failed to update fuel log: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    fun deleteFuelLog(log: FuelLog) {
        viewModelScope.launch {
            try {
                fuelLogDao.deleteLog(log)
                _successMessage.value = "Fuel log deleted"
                Log.d(TAG, "Fuel log deleted: ${log.id}")
            } catch (e: Exception) {
                val errorMsg = "Failed to delete fuel log: ${e.message}"
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
