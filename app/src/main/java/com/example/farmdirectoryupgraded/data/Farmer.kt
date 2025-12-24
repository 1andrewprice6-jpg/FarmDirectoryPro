package com.example.farmdirectoryupgraded.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmers")
data class Farmer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val spouse: String = "",
    val farmName: String = "",
    val address: String,
    val phone: String,
    val cellPhone: String = "",
    val email: String = "",
    val type: String = "", // Pullet, Breeder, etc.
    val isFavorite: Boolean = false,

    // GPS Location (for real-time tracking)
    val latitude: Double? = null,
    val longitude: Double? = null,
    val lastLocationUpdate: Long? = null, // Timestamp in milliseconds

    // Health Monitoring
    val healthStatus: String = "HEALTHY", // HEALTHY, SICK, RECOVERING, CRITICAL, DECEASED
    val healthNotes: String = "",

    // Optimistic Locking (for conflict prevention)
    val version: Int = 1
)
