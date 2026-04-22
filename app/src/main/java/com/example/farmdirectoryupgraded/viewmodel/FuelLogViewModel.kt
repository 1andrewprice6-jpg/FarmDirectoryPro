package com.example.farmdirectoryupgraded.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.data.FuelLog
import com.example.farmdirectoryupgraded.data.FuelLogDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for fuel log operations
 *
 * Handles:
 * - Fuel log CRUD operations
 * - Fuel log listing and filtering
 */
class FuelLogViewModel(private val fuelLogDao: FuelLogDao) : ViewModel() {

    companion object {
        private const val TAG = "FuelLogViewModel"
    }

    val fuelLogs = fuelLogDao.getAllFuelLogs()
        .onEach { Log.d(TAG, "Fuel logs updated: ${it.size} entries") }
        .catch { e -> Log.e(TAG, "Error loading fuel logs", e) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    /**
     * Add a new fuel log entry
     *
     * @param log The fuel log to add
     */
    fun addFuelLog(log: FuelLog) {
        viewModelScope.launch {
            try {
                fuelLogDao.insertLog(log)
                _successMessage.value = "Fuel log added"
                Log.d(TAG, "Fuel log added for vehicle: ${log.vehicleName}")
            } catch (e: Exception) {
                val msg = "Failed to add fuel log: ${e.message}"
                _errorMessage.value = msg
                Log.e(TAG, msg, e)
            }
        }
    }

    /**
     * Delete a fuel log entry
     *
     * @param log The fuel log to delete
     */
    fun deleteFuelLog(log: FuelLog) {
        viewModelScope.launch {
            try {
                fuelLogDao.deleteLog(log)
                _successMessage.value = "Fuel log deleted"
                Log.d(TAG, "Fuel log deleted: ${log.id}")
            } catch (e: Exception) {
                val msg = "Failed to delete fuel log: ${e.message}"
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
