package com.example.farmdirectoryupgraded.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelLogDao {
    @Query("SELECT * FROM fuel_logs ORDER BY timestamp DESC")
    fun getAllFuelLogs(): Flow<List<FuelLog>>

    @Query("SELECT * FROM fuel_logs WHERE vehicleId = :vehicleId ORDER BY timestamp DESC")
    fun getLogsByVehicle(vehicleId: String): Flow<List<FuelLog>>

    @Query("SELECT * FROM fuel_logs WHERE driverId = :driverId ORDER BY timestamp DESC")
    fun getLogsByDriver(driverId: Int): Flow<List<FuelLog>>

    @Query("SELECT * FROM fuel_logs WHERE fuelType = :fuelType ORDER BY timestamp DESC")
    fun getLogsByFuelType(fuelType: String): Flow<List<FuelLog>>

    @Query("SELECT * FROM fuel_logs WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getLogsByDateRange(startTime: Long, endTime: Long): Flow<List<FuelLog>>

    @Query("SELECT * FROM fuel_logs WHERE vehicleId = :vehicleId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastFuelLogForVehicle(vehicleId: String): FuelLog?

    @Query("SELECT * FROM fuel_logs WHERE id = :id")
    suspend fun getLogById(id: Int): FuelLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FuelLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<FuelLog>)

    @Update
    suspend fun updateLog(log: FuelLog)

    @Delete
    suspend fun deleteLog(log: FuelLog)

    // Analytics queries
    @Query("SELECT SUM(totalCost) FROM fuel_logs WHERE timestamp >= :startTime")
    fun getTotalCostSince(startTime: Long): Flow<Double?>

    @Query("SELECT SUM(quantity) FROM fuel_logs WHERE timestamp >= :startTime")
    fun getTotalQuantitySince(startTime: Long): Flow<Double?>

    @Query("SELECT AVG(fuelEfficiency) FROM fuel_logs WHERE fuelEfficiency IS NOT NULL AND timestamp >= :startTime")
    fun getAverageFuelEfficiencySince(startTime: Long): Flow<Double?>

    @Query("SELECT vehicleId, vehicleName, SUM(totalCost) as totalCost FROM fuel_logs WHERE timestamp >= :startTime GROUP BY vehicleId ORDER BY totalCost DESC")
    fun getCostByVehicleSince(startTime: Long): Flow<List<VehicleFuelSummary>>
}

// Data class for fuel analytics
data class VehicleFuelSummary(
    val vehicleId: String,
    val vehicleName: String,
    val totalCost: Double
)
