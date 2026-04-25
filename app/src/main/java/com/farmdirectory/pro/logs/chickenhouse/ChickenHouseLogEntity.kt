package com.farmdirectory.pro.logs.chickenhouse

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Entity(
    tableName = "chicken_house_logs",
    indices = [Index("dateIso"), Index("farmId"), Index("sourceCaptureId")],
)
@TypeConverters(MetricsConverter::class)
data class ChickenHouseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateIso: String?,
    val farmId: String?,
    /** key→value, e.g. {"mortality"→3.0, "feed_lb"→1450.0, "water_gal"→850.0} */
    val metrics: Map<String, Double>,
    val sourceCaptureId: Long?,
    val notes: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
)

class MetricsConverter {
    private val json = Json { ignoreUnknownKeys = true }
    @TypeConverter
    fun toJson(m: Map<String, Double>): String = json.encodeToString(
        kotlinx.serialization.builtins.MapSerializer(
            kotlinx.serialization.builtins.serializer<String>(),
            kotlinx.serialization.builtins.serializer<Double>(),
        ),
        m,
    )
    @TypeConverter
    fun fromJson(s: String): Map<String, Double> = if (s.isBlank()) emptyMap() else
        json.decodeFromString(
            kotlinx.serialization.builtins.MapSerializer(
                kotlinx.serialization.builtins.serializer<String>(),
                kotlinx.serialization.builtins.serializer<Double>(),
            ),
            s,
        )
}

@Dao
interface ChickenHouseLogDao {
    @Query("SELECT * FROM chicken_house_logs ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<ChickenHouseLogEntity>>

    @Query("SELECT * FROM chicken_house_logs WHERE farmId = :farmId ORDER BY createdAtEpochMs DESC")
    fun observeByFarm(farmId: String): Flow<List<ChickenHouseLogEntity>>

    @Query("SELECT * FROM chicken_house_logs WHERE id = :id")
    suspend fun get(id: Long): ChickenHouseLogEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: ChickenHouseLogEntity): Long

    @Query("DELETE FROM chicken_house_logs WHERE id = :id")
    suspend fun delete(id: Long)

    /**
     * Aggregate a single metric across a farm's logs.
     * SQLite can't sum inside the JSON blob, so we Just Get All Rows and sum
     * client-side. For the typical 147-farm × 30-day window this is hundreds
     * of rows — trivially fast on-device.
     */
    @Query("SELECT * FROM chicken_house_logs WHERE farmId = :farmId AND createdAtEpochMs >= :sinceEpochMs")
    suspend fun rowsForAggregation(farmId: String, sinceEpochMs: Long): List<ChickenHouseLogEntity>
}
