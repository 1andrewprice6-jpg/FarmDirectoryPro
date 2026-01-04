package com.example.farmdirectoryupgraded.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.data.AttendanceRecord
import com.example.farmdirectoryupgraded.data.AttendanceDao
import com.example.farmdirectoryupgraded.data.Employee
import com.example.farmdirectoryupgraded.data.EmployeeDao
import com.example.farmdirectoryupgraded.utils.QRCodeScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for attendance tracking operations
 *
 * Handles:
 * - Employee check-in/check-out
 * - Attendance record management
 * - QR code scanning for attendance
 * - Employee management
 */
class AttendanceViewModel(
    private val attendanceDao: AttendanceDao,
    private val employeeDao: EmployeeDao
) : ViewModel() {

    companion object {
        private const val TAG = "AttendanceViewModel"
    }

    enum class AttendanceMethod {
        GPS, QR_CODE, NFC, MANUAL, PHOTO, BIOMETRIC, BLUETOOTH
    }

    // State management
    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees = _employees.asStateFlow()

    private val _selectedEmployee = MutableStateFlow<Employee?>(null)
    val selectedEmployee = _selectedEmployee.asStateFlow()

    private val _attendanceRecords = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendanceRecords = _attendanceRecords.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private val _isCheckingIn = MutableStateFlow(false)
    val isCheckingIn = _isCheckingIn.asStateFlow()

    init {
        loadEmployees()
    }

    /**
     * Load all employees
     */
    private fun loadEmployees() {
        viewModelScope.launch {
            try {
                employeeDao.getAllEmployees().collect { employees ->
                    _employees.value = employees
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to load employees: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Select employee for attendance tracking
     *
     * @param employee The employee to select
     */
    fun selectEmployee(employee: Employee) {
        _selectedEmployee.value = employee
    }

    /**
     * Record check-in with GPS method
     *
     * @param employeeId Employee ID
     * @param latitude Check-in latitude
     * @param longitude Check-in longitude
     * @param workLocation Work location description
     * @param taskDescription Task description
     * @param notes Additional notes
     */
    fun checkInWithGPS(
        employeeId: Int,
        latitude: Double,
        longitude: Double,
        workLocation: String = "",
        taskDescription: String = "",
        notes: String = ""
    ) {
        _isCheckingIn.value = true
        viewModelScope.launch {
            try {
                val record = AttendanceRecord(
                    employeeId = employeeId,
                    method = "GPS",
                    checkInTime = System.currentTimeMillis(),
                    checkInLatitude = latitude,
                    checkInLongitude = longitude,
                    workLocation = workLocation,
                    taskDescription = taskDescription,
                    notes = notes
                )
                attendanceDao.insertAttendanceRecord(record)
                _successMessage.value = "Check-in recorded successfully"
                Log.d(TAG, "Employee $employeeId checked in at ($latitude, $longitude)")
            } catch (e: Exception) {
                val errorMsg = "Failed to record check-in: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            } finally {
                _isCheckingIn.value = false
            }
        }
    }

    /**
     * Record check-in with QR code method
     *
     * @param qrCodeData QR code data
     * @param latitude Current latitude
     * @param longitude Current longitude
     */
    fun checkInWithQRCode(
        qrCodeData: String,
        latitude: Double,
        longitude: Double
    ) {
        _isCheckingIn.value = true
        viewModelScope.launch {
            try {
                val parsed = QRCodeScanner.parseQRCode(qrCodeData)
                val record = AttendanceRecord(
                    employeeId = parsed.employeeId,
                    method = "QR_CODE",
                    checkInTime = System.currentTimeMillis(),
                    checkInLatitude = latitude,
                    checkInLongitude = longitude,
                    workLocation = parsed.farmName,
                    taskDescription = parsed.taskDescription,
                    notes = "QR Code: $qrCodeData"
                )
                attendanceDao.insertAttendanceRecord(record)
                _successMessage.value = "Check-in via QR code successful"
                Log.d(TAG, "Employee ${parsed.employeeId} checked in via QR code")
            } catch (e: Exception) {
                val errorMsg = "Failed to parse QR code or record check-in: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            } finally {
                _isCheckingIn.value = false
            }
        }
    }

    /**
     * Record check-out and calculate hours worked
     *
     * @param recordId The attendance record ID to check out
     * @param latitude Check-out latitude
     * @param longitude Check-out longitude
     * @param notes Additional check-out notes
     */
    fun checkOut(
        recordId: Int,
        latitude: Double,
        longitude: Double,
        notes: String = ""
    ) {
        viewModelScope.launch {
            try {
                val record = attendanceDao.getAttendanceRecordById(recordId)
                if (record != null) {
                    val hoursWorked = calculateHoursWorked(record.checkInTime)
                    val updatedRecord = record.copy(
                        checkOutTime = System.currentTimeMillis(),
                        checkOutLatitude = latitude,
                        checkOutLongitude = longitude,
                        hoursWorked = hoursWorked,
                        notes = if (notes.isNotEmpty()) "${record.notes}\nCheckOut: $notes" else record.notes
                    )
                    attendanceDao.updateAttendanceRecord(updatedRecord)
                    _successMessage.value = "Check-out recorded (${String.format("%.1f", hoursWorked)} hours)"
                    Log.d(TAG, "Record $recordId checked out")
                } else {
                    _errorMessage.value = "Attendance record not found"
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to record check-out: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Add a new employee
     *
     * @param employee The employee to add
     */
    fun addEmployee(employee: Employee) {
        viewModelScope.launch {
            try {
                employeeDao.insertEmployee(employee)
                _successMessage.value = "Employee added successfully"
                Log.d(TAG, "Employee added: ${employee.name}")
            } catch (e: Exception) {
                val errorMsg = "Failed to add employee: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Update an existing employee
     *
     * @param employee The employee with updated data
     */
    fun updateEmployee(employee: Employee) {
        viewModelScope.launch {
            try {
                employeeDao.updateEmployee(employee)
                _successMessage.value = "Employee updated successfully"
                Log.d(TAG, "Employee updated: ${employee.name}")
            } catch (e: Exception) {
                val errorMsg = "Failed to update employee: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Deactivate an employee (soft delete)
     *
     * @param employeeId The employee ID to deactivate
     */
    fun deactivateEmployee(employeeId: Int) {
        viewModelScope.launch {
            try {
                val employee = employeeDao.getEmployeeById(employeeId)
                if (employee != null) {
                    employeeDao.updateEmployee(employee.copy(isActive = false))
                    _successMessage.value = "Employee deactivated"
                    Log.d(TAG, "Employee $employeeId deactivated")
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to deactivate employee: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Load attendance records for a specific employee
     *
     * @param employeeId The employee ID
     */
    fun loadEmployeeAttendance(employeeId: Int) {
        viewModelScope.launch {
            try {
                attendanceDao.getAttendanceByEmployee(employeeId).collect { records ->
                    _attendanceRecords.value = records
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to load attendance records: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Calculate hours worked from check-in time
     *
     * @param checkInTimeMs Check-in timestamp in milliseconds
     * @return Hours worked as Double
     */
    private fun calculateHoursWorked(checkInTimeMs: Long): Double {
        val currentTimeMs = System.currentTimeMillis()
        val durationMs = currentTimeMs - checkInTimeMs
        return durationMs / (1000.0 * 60 * 60)  // Convert milliseconds to hours
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
    fun clearSuccess() {
        _successMessage.value = null
    }
}
