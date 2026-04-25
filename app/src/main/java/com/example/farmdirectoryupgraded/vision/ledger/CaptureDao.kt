package com.example.farmdirectoryupgraded.vision.ledger

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptureDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(capture: CaptureEntity): Long

    @Update
    suspend fun update(capture: CaptureEntity)

    @Query("SELECT * FROM captures WHERE id = :id")
    suspend fun get(id: Long): CaptureEntity?

    @Query("SELECT * FROM captures ORDER BY capturedAtEpochMs DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<CaptureEntity>>

    @Query("SELECT * FROM captures WHERE farmId = :farmId ORDER BY capturedAtEpochMs DESC")
    fun observeForFarm(farmId: String): Flow<List<CaptureEntity>>

    @Query("SELECT * FROM captures WHERE status = 'INCOMPLETE' OR status = 'INCONSISTENT' ORDER BY capturedAtEpochMs DESC")
    fun observePending(): Flow<List<CaptureEntity>>

    @Query("SELECT * FROM captures WHERE syncedAt IS NULL")
    suspend fun getUnsynced(): List<CaptureEntity>

    @Query("SELECT COUNT(*) FROM captures WHERE mode = :mode AND capturedAtEpochMs >= :sinceEpochMs")
    suspend fun countSince(mode: String, sinceEpochMs: Long): Int

    @Query("DELETE FROM captures WHERE id = :id")
    suspend fun delete(id: Long)
}
