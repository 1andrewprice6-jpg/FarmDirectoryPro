package com.example.farmdirectoryupgraded.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.farmdirectoryupgraded.data.AttendanceDao
import com.example.farmdirectoryupgraded.data.EmployeeDao
import com.example.farmdirectoryupgraded.data.FarmerDao
import com.example.farmdirectoryupgraded.data.LogDao
import com.example.farmdirectoryupgraded.data.FarmWebSocketService

/**
 * Factory for creating ViewModels with their required dependencies
 */
class FarmViewModelFactory(
    private val context: Context,
    private val farmerDao: FarmerDao,
    private val attendanceDao: AttendanceDao,
    private val employeeDao: EmployeeDao,
    private val logDao: LogDao,
    private val webSocketService: FarmWebSocketService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(FarmerListViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                FarmerListViewModel(context, farmerDao) as T
            }
            modelClass.isAssignableFrom(AttendanceViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AttendanceViewModel(attendanceDao, employeeDao) as T
            }
            modelClass.isAssignableFrom(LocationViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                LocationViewModel(farmerDao, employeeDao) as T
            }
            modelClass.isAssignableFrom(WebSocketViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                WebSocketViewModel(webSocketService) as T
            }
            modelClass.isAssignableFrom(LogViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                LogViewModel(context, logDao) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
