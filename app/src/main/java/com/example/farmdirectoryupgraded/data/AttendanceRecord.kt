package com.example.farmdirectoryupgraded.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val farmerId: Int? = null,
    val farmName: String,
    val method: String, // GPS, QR_CODE, MANUAL, NFC, PHOTO, BIOMETRIC
    val checkInTime: Long, // Timestamp in milliseconds
    val checkOutTime: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = "",
    val photoPath: String? = null,
    val workerId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
