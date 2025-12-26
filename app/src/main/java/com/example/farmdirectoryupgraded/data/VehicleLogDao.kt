package com.example.farmdirectoryupgraded.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleLogDao {
    @Query("SELECT * FROM vehicle_logs ORDER BY timestamp DESC")
    fun getAllVehicleLogs(): Flow<List<VehicleLog>>

    @Query("SELECT * FROM vehicle_logs WHERE vehicleId = :vehicleId ORDER BY timestamp DESC")
    fun getLogsByVehicle(vehicleId: String): Flow<List<VehicleLog>>

    @Query("SELECT * FROM vehicle_logs WHERE driverId = :driverId ORDER BY timestamp DESC")
    fun getLogsByDriver(driverId: Int): Flow<List<VehicleLog>>

    @Query("SELECT * FROM vehicle_logs WHERE logType = :logType ORDER BY timestamp DESC")
    fun getLogsByType(logType: String): Flow<List<VehicleLog>>

    @Query("SELECT * FROM vehicle_logs WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getLogsByDateRange(startTime: Long, endTime: Long): Flow<List<VehicleLog>>

    @Query("SELECT * FROM vehicle_logs WHERE id = :id")
    suspend fun getLogById(id: Int): VehicleLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: VehicleLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<VehicleLog>)

    @Update
    suspend fun updateLog(log: VehicleLog)

    @Delete
    suspend fun deleteLog(log: VehicleLog)

    @Query("SELECT DISTINCT vehicleId, vehicleName FROM vehicle_logs ORDER BY vehicleName ASC")
    fun getAllVehicles(): Flow<List<VehicleInfo>>

    @Query("SELECT COUNT(*) FROM vehicle_logs WHERE logType = 'MAINTENANCE' AND timestamp >= :startTime")
    fun getMaintenanceCountSince(startTime: Long): Flow<Int>
}

// Data class for vehicle list
data class VehicleInfo(
    val vehicleId: String,
    val vehicleName: String
)
