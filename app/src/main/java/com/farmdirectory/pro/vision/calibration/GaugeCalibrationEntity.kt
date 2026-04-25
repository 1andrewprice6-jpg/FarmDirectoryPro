package com.farmdirectory.pro.vision.calibration

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.farmdirectory.pro.vision.vision.GaugeCalibration
import kotlinx.coroutines.flow.Flow

/**
 * Persisted form of [GaugeCalibration] keyed by gaugeId.
 * One row per physical gauge (e.g. "shop_air_compressor", "feed_silo_a").
 */
@Entity(tableName = "gauge_calibrations")
data class GaugeCalibrationEntity(
    @PrimaryKey val gaugeId: String,
    val gaugeLabel: String,
    val centerXNorm: Double,
    val centerYNorm: Double,
    val radiusNorm: Double,
    val minAngleDeg: Double,
    val minValue: Double,
    val maxAngleDeg: Double,
    val maxValue: Double,
    val unit: String,
    val counterclockwise: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
) {
    fun toDomain(): GaugeCalibration = GaugeCalibration(
        gaugeId = gaugeId,
        gaugeLabel = gaugeLabel,
        centerXNorm = centerXNorm,
        centerYNorm = centerYNorm,
        radiusNorm = radiusNorm,
        minAngleDeg = minAngleDeg,
        minValue = minValue,
        maxAngleDeg = maxAngleDeg,
        maxValue = maxValue,
        unit = unit,
        counterclockwise = counterclockwise,
    )

    companion object {
        fun fromDomain(d: GaugeCalibration): GaugeCalibrationEntity {
            val now = System.currentTimeMillis()
            return GaugeCalibrationEntity(
                gaugeId = d.gaugeId,
                gaugeLabel = d.gaugeLabel,
                centerXNorm = d.centerXNorm,
                centerYNorm = d.centerYNorm,
                radiusNorm = d.radiusNorm,
                minAngleDeg = d.minAngleDeg,
                minValue = d.minValue,
                maxAngleDeg = d.maxAngleDeg,
                maxValue = d.maxValue,
                unit = d.unit,
                counterclockwise = d.counterclockwise,
                createdAtEpochMs = now,
                updatedAtEpochMs = now,
            )
        }
    }
}

@Dao
interface GaugeCalibrationDao {
    @Query("SELECT * FROM gauge_calibrations ORDER BY gaugeLabel COLLATE NOCASE")
    fun observeAll(): Flow<List<GaugeCalibrationEntity>>

    @Query("SELECT * FROM gauge_calibrations WHERE gaugeId = :id")
    suspend fun byId(id: String): GaugeCalibrationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GaugeCalibrationEntity)

    @Query("DELETE FROM gauge_calibrations WHERE gaugeId = :id")
    suspend fun delete(id: String)
}
