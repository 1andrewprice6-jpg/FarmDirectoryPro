package com.example.farmdirectoryupgraded.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Vehicle log entity for tracking vehicle usage, maintenance, and trips
 */
@Entity(
    tableName = "vehicle_logs",
    indices = [
        Index(value = ["vehicleId"], name = "idx_vehicle_id"),
        Index(value = ["driverId"], name = "idx_driver_id"),
        Index(value = ["logType"], name = "idx_log_type"),
        Index(value = ["timestamp"], name = "idx_vehicle_timestamp")
    ]
)
data class VehicleLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val vehicleId: String, // Vehicle identifier (license plate, number, etc.)
    val vehicleName: String = "", // Vehicle name/description (e.g., "Truck #1", "Blue Ford F-150")
    val driverId: Int? = null, // Reference to Employee ID
    val driverName: String = "", // Driver name for display
    val logType: String, // TRIP_START, TRIP_END, MAINTENANCE, INSPECTION, REPAIR, FUEL, OTHER
    val timestamp: Long, // Timestamp in milliseconds

    // Trip details
    val startLocation: String = "",
    val endLocation: String = "",
    val startOdometer: Double? = null,
    val endOdometer: Double? = null,
    val distance: Double? = null, // Miles or kilometers

    // Maintenance details
    val maintenanceType: String = "", // OIL_CHANGE, TIRE_ROTATION, BRAKE_SERVICE, etc.
    val cost: Double? = null,
    val vendor: String = "",

    // General
    val notes: String = "",
    val photoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
