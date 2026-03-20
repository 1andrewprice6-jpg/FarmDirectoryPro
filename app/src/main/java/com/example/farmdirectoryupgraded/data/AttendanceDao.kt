package com.example.farmdirectoryupgraded.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY checkInTime DESC")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId ORDER BY checkInTime DESC")
    fun getAttendanceByEmployee(employeeId: Int): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId AND checkOutTime IS NULL LIMIT 1")
    suspend fun getActiveCheckInForEmployee(employeeId: Int): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE checkInTime >= :startTime AND checkInTime <= :endTime ORDER BY checkInTime DESC")
    fun getAttendanceByDateRange(startTime: Long, endTime: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE checkOutTime IS NULL ORDER BY checkInTime DESC")
    fun getActiveAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE id = :id")
    suspend fun getAttendanceRecordById(id: Int): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: AttendanceRecord): Long

    @Update
    suspend fun updateAttendanceRecord(record: AttendanceRecord)

    @Delete
    suspend fun deleteAttendance(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAllAttendance()

    @Query("UPDATE attendance_records SET checkOutTime = :checkOutTime, hoursWorked = :hoursWorked, checkOutLatitude = :latitude, checkOutLongitude = :longitude WHERE id = :id")
    suspend fun checkOut(id: Int, checkOutTime: Long, hoursWorked: Double, latitude: Double?, longitude: Double?)

    @Query("SELECT COUNT(*) FROM attendance_records WHERE checkOutTime IS NULL")
    fun getActiveCheckInCount(): Flow<Int>

    @Query("SELECT SUM(hoursWorked) FROM attendance_records WHERE employeeId = :employeeId AND checkInTime >= :startTime")
    fun getTotalHoursForEmployee(employeeId: Int, startTime: Long): Flow<Double?>
}
