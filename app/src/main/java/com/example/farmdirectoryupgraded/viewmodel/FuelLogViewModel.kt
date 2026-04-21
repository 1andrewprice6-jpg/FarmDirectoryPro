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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FuelLogViewModel(private val fuelLogDao: FuelLogDao) : ViewModel() {
    companion object { private const val TAG = "FuelLogViewModel" }

    private val _fuelLogs = MutableStateFlow<List<FuelLog>>(emptyList())
    val fuelLogs: StateFlow<List<FuelLog>> = _fuelLogs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init { loadFuelLogs() }

    private fun loadFuelLogs() {
        fuelLogDao.getAllFuelLogs()
            .onEach { logs -> _fuelLogs.value = logs }
            .catch { e -> _errorMessage.value = "Failed to load fuel logs: ${e.message}" }
            .launchIn(viewModelScope)
    }

    fun addFuelLog(log: FuelLog) {
        viewModelScope.launch {
            try {
                fuelLogDao.insertLog(log)
                _successMessage.value = "Fuel log added"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add fuel log: ${e.message}"
                Log.e(TAG, "addFuelLog", e)
            }
        }
    }

    fun deleteFuelLog(log: FuelLog) {
        viewModelScope.launch {
            try {
                fuelLogDao.deleteLog(log)
                _successMessage.value = "Fuel log deleted"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete fuel log: ${e.message}"
                Log.e(TAG, "deleteFuelLog", e)
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
    fun clearSuccess() { _successMessage.value = null }
}
