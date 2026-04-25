package com.farmdirectory.pro.logs.mileage

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "mileage_entries",
    indices = [Index("capturedAtEpochMs"), Index("farmId"), Index("sourceCaptureId")],
)
data class MileageEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val capturedAtEpochMs: Long,
    val odometerMiles: Int,
    val farmId: String?,
    val sourceCaptureId: Long?,
    val notes: String? = null,
)

@Dao
interface MileageDao {
    @Query("SELECT * FROM mileage_entries ORDER BY capturedAtEpochMs DESC")
    fun observeAll(): Flow<List<MileageEntryEntity>>

    @Query("SELECT * FROM mileage_entries ORDER BY capturedAtEpochMs DESC LIMIT 1")
    suspend fun mostRecent(): MileageEntryEntity?

    @Query("SELECT * FROM mileage_entries WHERE id = :id")
    suspend fun get(id: Long): MileageEntryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: MileageEntryEntity): Long

    @Query("DELETE FROM mileage_entries WHERE id = :id")
    suspend fun delete(id: Long)

    /** Miles driven between two epoch instants (max - min within the window). */
    @Query("""
        SELECT COALESCE(MAX(odometerMiles), 0) - COALESCE(MIN(odometerMiles), 0)
        FROM mileage_entries
        WHERE capturedAtEpochMs BETWEEN :fromEpochMs AND :toEpochMs
    """)
    suspend fun milesBetween(fromEpochMs: Long, toEpochMs: Long): Int
}
