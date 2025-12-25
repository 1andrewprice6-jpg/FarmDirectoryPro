package com.example.farmdirectoryupgraded.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries WHERE category = :category ORDER BY timestamp DESC")
    fun getLogsByCategory(category: String): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries WHERE level = :level ORDER BY timestamp DESC")
    fun getLogsByLevel(level: String): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries WHERE farmerId = :farmerId ORDER BY timestamp DESC")
    fun getLogsByFarmer(farmerId: Int): Flow<List<LogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntry): Long

    @Delete
    suspend fun deleteLog(log: LogEntry)

    @Query("DELETE FROM log_entries")
    suspend fun deleteAllLogs()

    @Query("DELETE FROM log_entries WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldLogs(beforeTimestamp: Long)
}
