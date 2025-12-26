package com.example.farmdirectoryupgraded.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Fuel log entity for tracking fuel purchases and consumption
 */
@Entity(
    tableName = "fuel_logs",
    indices = [
        Index(value = ["vehicleId"], name = "idx_fuel_vehicle_id"),
        Index(value = ["driverId"], name = "idx_fuel_driver_id"),
        Index(value = ["timestamp"], name = "idx_fuel_timestamp"),
        Index(value = ["fuelType"], name = "idx_fuel_type")
    ]
)
data class FuelLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val vehicleId: String, // Vehicle identifier
    val vehicleName: String = "", // Vehicle name for display
    val driverId: Int? = null, // Reference to Employee ID
    val driverName: String = "", // Driver name for display
    val timestamp: Long, // Timestamp in milliseconds

    // Fuel details
    val fuelType: String, // GASOLINE, DIESEL, E85, CNG, ELECTRIC, OTHER
    val quantity: Double, // Gallons or liters
    val unitPrice: Double, // Price per unit
    val totalCost: Double, // Total cost
    val currency: String = "USD",

    // Location and odometer
    val station: String = "", // Gas station name
    val location: String = "", // Address or city
    val odometer: Double? = null, // Odometer reading at fill-up
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Efficiency tracking
    val distanceSinceLastFill: Double? = null, // Miles or km since last fill
    val fuelEfficiency: Double? = null, // MPG or km/L (calculated)

    // Payment and receipt
    val paymentMethod: String = "", // CASH, CREDIT_CARD, FLEET_CARD, OTHER
    val receiptPhoto: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
