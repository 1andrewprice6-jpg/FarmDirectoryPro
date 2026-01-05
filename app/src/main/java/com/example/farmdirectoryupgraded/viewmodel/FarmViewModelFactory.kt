package com.example.farmdirectoryupgraded.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.farmdirectoryupgraded.data.AttendanceDao
import com.example.farmdirectoryupgraded.data.EmployeeDao
import com.example.farmdirectoryupgraded.data.FarmerDao
import com.example.farmdirectoryupgraded.data.FarmWebSocketService

/**
 * Factory for creating ViewModels with their required dependencies
 *
 * Handles:
 * - FarmerListViewModel for farmer CRUD operations
 * - AttendanceViewModel for attendance tracking
 * - LocationViewModel for GPS and route optimization
 * - WebSocketViewModel for real-time communication
 */
class FarmViewModelFactory(
    private val farmerDao: FarmerDao,
    private val attendanceDao: AttendanceDao,
    private val employeeDao: EmployeeDao,
    private val webSocketService: FarmWebSocketService
) : ViewModelProvider.Factory {

    /**
     * Create a ViewModel instance based on the model class
     *
     * @param modelClass The ViewModel class to instantiate
     * @return A new instance of the requested ViewModel
     * @throws IllegalArgumentException If the modelClass is not recognized
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            FarmerListViewModel::class.java -> {
                @Suppress("UNCHECKED_CAST")
                FarmerListViewModel(farmerDao) as T
            }
            AttendanceViewModel::class.java -> {
                @Suppress("UNCHECKED_CAST")
                AttendanceViewModel(attendanceDao, employeeDao) as T
            }
            LocationViewModel::class.java -> {
                @Suppress("UNCHECKED_CAST")
                LocationViewModel(farmerDao, employeeDao) as T
            }
            WebSocketViewModel::class.java -> {
                @Suppress("UNCHECKED_CAST")
                WebSocketViewModel(webSocketService) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
