package com.example.farmdirectoryupgraded.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String, // Connection, Import, Reconcile, Attendance, Error, Farmer
    val level: String, // INFO, SUCCESS, WARNING, ERROR
    val message: String,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val farmerId: Int? = null,
    val workerId: String = ""
)
