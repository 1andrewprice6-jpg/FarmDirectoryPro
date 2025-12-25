package com.example.farmdirectoryupgraded.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY checkInTime DESC")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE farmerId = :farmerId ORDER BY checkInTime DESC")
    fun getAttendanceByFarmer(farmerId: Int): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE checkOutTime IS NULL ORDER BY checkInTime DESC")
    fun getActiveAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE id = :id")
    suspend fun getAttendanceById(id: Int): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord): Long

    @Update
    suspend fun updateAttendance(record: AttendanceRecord)

    @Delete
    suspend fun deleteAttendance(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAllAttendance()
}
