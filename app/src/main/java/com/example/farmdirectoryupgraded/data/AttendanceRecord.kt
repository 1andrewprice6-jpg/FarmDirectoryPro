package com.example.farmdirectoryupgraded.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Attendance record for employee check-in/check-out tracking
 * Used for catchers, drivers, and other workers
 */
@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["employeeId"], name = "idx_attendance_employee"),
        Index(value = ["checkInTime"], name = "idx_attendance_checkin"),
        Index(value = ["checkOutTime"], name = "idx_attendance_checkout")
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val employeeId: Int, // Reference to Employee ID
    val employeeName: String, // Employee name for display
    val employeeRole: String = "", // Role at time of check-in (for historical accuracy)
    val method: String, // GPS, QR_CODE, MANUAL, NFC, PHOTO, BIOMETRIC, BLUETOOTH
    val checkInTime: Long, // Timestamp in milliseconds
    val checkOutTime: Long? = null, // Null if still checked in
    val hoursWorked: Double? = null, // Calculated hours (checkOut - checkIn)

    // Location tracking
    val checkInLatitude: Double? = null,
    val checkInLongitude: Double? = null,
    val checkOutLatitude: Double? = null,
    val checkOutLongitude: Double? = null,

    // Work details
    val workLocation: String = "", // Farm name, building, area, etc.
    val taskDescription: String = "", // What work was performed
    val notes: String = "",
    val photoPath: String? = null,

    val createdAt: Long = System.currentTimeMillis()
)
