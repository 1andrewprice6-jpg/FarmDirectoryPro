package com.example.farmdirectoryupgraded.logs.fuel

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "fuel_logs",
    indices = [Index("capturedAtEpochMs"), Index("farmId"), Index("sourceCaptureId")],
)
data class FuelLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val capturedAtEpochMs: Long,
    val farmId: String?,
    val totalCost: Double?,
    val gallons: Double?,
    val pricePerGallon: Double?,
    val odometerMiles: Int?,
    /** FK back to the capture row that produced this entry — null if added manually. */
    val sourceCaptureId: Long?,
    val notes: String? = null,
)

@Dao
interface FuelLogDao {
    @Query("SELECT * FROM fuel_logs ORDER BY capturedAtEpochMs DESC")
    fun observeAll(): Flow<List<FuelLogEntity>>

    @Query("SELECT * FROM fuel_logs WHERE farmId = :farmId ORDER BY capturedAtEpochMs DESC")
    fun observeByFarm(farmId: String): Flow<List<FuelLogEntity>>

    @Query("SELECT * FROM fuel_logs WHERE id = :id")
    suspend fun get(id: Long): FuelLogEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: FuelLogEntity): Long

    @Query("DELETE FROM fuel_logs WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("""
        SELECT COALESCE(SUM(totalCost), 0)
        FROM fuel_logs
        WHERE capturedAtEpochMs >= :sinceEpochMs
    """)
    suspend fun totalCostSince(sinceEpochMs: Long): Double

    @Query("""
        SELECT COALESCE(SUM(gallons), 0)
        FROM fuel_logs
        WHERE capturedAtEpochMs >= :sinceEpochMs
    """)
    suspend fun totalGallonsSince(sinceEpochMs: Long): Double
}
