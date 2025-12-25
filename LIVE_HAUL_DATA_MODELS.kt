package com.example.livehaul.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * LIVE HAUL PRO - Data Models
 * Poultry & Egg Hauling Management System
 */

// ==================== HAUL MODELS ====================

@Entity(tableName = "hauls")
data class Haul(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: HaulType,
    val status: HaulStatus,
    val farmId: Int,
    val farmName: String,
    val destinationId: Int,
    val destinationName: String,
    val haulerId: Int,
    val haulerName: String,
    val crewLeaderId: Int?,
    val crewLeaderName: String?,
    @TypeConverters val catcherIds: List<Int> = emptyList(),
    val clientId: Int, // Perdue, Mountaire
    val clientName: String,
    
    // Timing
    val scheduledTime: Long,
    val startTime: Long? = null,
    val loadStartTime: Long? = null,
    val loadEndTime: Long? = null,
    val deliveryTime: Long? = null,
    val completedTime: Long? = null,
    
    // Counts & Measurements
    val estimatedBirdCount: Int? = null,
    val actualBirdCount: Int? = null,
    val deadOnArrival: Int = 0,
    val totalWeight: Double? = null,
    val avgWeight: Double? = null,
    
    val estimatedCaseCount: Int? = null,
    val actualCaseCount: Int? = null,
    val flatCount: Int? = null,
    
    // Environmental
    val temperature: Double? = null,
    val tempUnit: String = "F",
    
    // GPS Tracking
    val pickupLat: Double? = null,
    val pickupLon: Double? = null,
    val deliveryLat: Double? = null,
    val deliveryLon: Double? = null,
    val distanceMiles: Double? = null,
    
    // Documentation
    val loadPhotoUrls: String = "", // Comma-separated
    val weightTicketUrl: String? = null,
    val deliveryReceiptUrl: String? = null,
    val notes: String = "",
    
    // Payroll
    val payrollProcessed: Boolean = false,
    val payrollAmount: Double = 0.0,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class HaulType {
    CHICKEN, // Live bird hauling
    EGG      // Egg hauling
}

enum class HaulStatus {
    SCHEDULED,   // Assigned but not started
    ASSIGNED,    // Hauler/crew assigned
    EN_ROUTE,    // Traveling to farm
    LOADING,     // Currently loading birds/eggs
    HAULING,     // In transit to plant
    DELIVERED,   // Arrived at plant
    COMPLETED,   // Paperwork done, ready for payroll
    CANCELLED    // Cancelled haul
}

// ==================== PERSONNEL MODELS ====================

@Entity(tableName = "personnel")
data class Personnel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeNumber: String,
    val name: String,
    val role: PersonnelRole,
    val phone: String,
    val email: String = "",
    val address: String = "",
    
    // Employment
    val hireDate: Long,
    val active: Boolean = true,
    val terminationDate: Long? = null,
    
    // Pay Information
    val payRate: Double,
    val payType: PayType,
    val overtimeEligible: Boolean = false,
    
    // Hauler Specific
    val licenseNumber: String? = null,
    val licenseExpiry: Long? = null,
    val licenseClass: String? = null, // CDL A, B, C
    val endorsements: String = "", // H (Hazmat), etc.
    
    // Crew Specific
    val crewId: Int? = null,
    val crewPosition: String? = null, // "Leader", "Catcher"
    
    // Login
    val pin: String? = null,
    val lastLogin: Long? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

enum class PersonnelRole {
    BOSS,              // Owner/Manager - full access
    OFFICE,            // Office staff - dispatch, scheduling
    DISPATCHER,        // Dispatch only
    PAYROLL,           // Payroll administrator
    CHICKEN_HAULER,    // Live bird hauler
    EGG_HAULER,        // Egg hauler
    CREW_LEADER,       // Catching crew leader
    CATCHER            // Catching crew member
}

enum class PayType {
    HOURLY,       // $ per hour
    PER_BIRD,     // $ per bird (chicken haulers/catchers)
    PER_CASE,     // $ per case (egg haulers)
    SALARY,       // Fixed salary
    COMMISSION    // % of haul value
}

// ==================== FARM & CLIENT MODELS ====================

@Entity(tableName = "farms")
data class Farm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val farmName: String,
    val farmNumber: String, // Perdue farm #12345
    val clientId: Int,
    val clientName: String, // "Perdue", "Mountaire"
    val farmType: FarmType,
    
    // Location
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val latitude: Double,
    val longitude: Double,
    
    // Contact
    val farmerName: String,
    val farmerPhone: String,
    val farmerEmail: String = "",
    
    // Farm Details
    val houseCount: Int,
    val totalCapacity: Int,
    val houseNames: String = "", // "House 1, House 2, House 3"
    
    // Schedule
    val preferredPickupTime: String = "", // "6:00 AM"
    val accessInstructions: String = "",
    val gateCode: String = "",
    
    val active: Boolean = true,
    val notes: String = "",
    val lastHaulDate: Long? = null
)

enum class FarmType {
    BROILER,  // Meat chickens
    LAYER     // Egg-laying hens
}

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // "Perdue", "Mountaire"
    val type: ClientType,
    
    // Contact
    val contactName: String,
    val contactPhone: String,
    val contactEmail: String = "",
    
    // Contract
    val contractNumber: String,
    val contractStart: Long,
    val contractEnd: Long,
    
    // Rates
    val payRatePerBird: Double? = null,
    val payRatePerCase: Double? = null,
    
    val active: Boolean = true,
    val notes: String = ""
)

enum class ClientType {
    CHICKEN_PROCESSOR,  // Perdue, Mountaire - process chickens
    EGG_PROCESSOR       // Egg processing plants
}

@Entity(tableName = "destinations")
data class Destination(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // "Perdue Plant - Salisbury"
    val clientId: Int,
    val type: DestinationType,
    
    // Location
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val latitude: Double,
    val longitude: Double,
    
    // Contact
    val contactName: String,
    val contactPhone: String,
    val receivingHours: String = "", // "5 AM - 3 PM"
    val loadingDockInfo: String = "",
    
    val active: Boolean = true
)

enum class DestinationType {
    PROCESSING_PLANT,
    EGG_PROCESSING,
    COLD_STORAGE
}

// ==================== TIME & PAYROLL MODELS ====================

@Entity(tableName = "time_entries")
data class TimeEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personnelId: Int,
    val personnelName: String,
    val haulId: Int,
    val entryType: TimeEntryType,
    
    val clockInTime: Long,
    val clockInLat: Double? = null,
    val clockInLon: Double? = null,
    
    val clockOutTime: Long? = null,
    val clockOutLat: Double? = null,
    val clockOutLon: Double? = null,
    
    val totalHours: Double = 0.0,
    val breakMinutes: Int = 0,
    val approved: Boolean = false,
    val approvedBy: Int? = null,
    val approvedAt: Long? = null,
    
    val notes: String = ""
)

enum class TimeEntryType {
    HAULING,    // Driving time
    LOADING,    // Loading birds/eggs
    CATCHING,   // Catching birds
    OFFICE,     // Office work
    MAINTENANCE // Vehicle maintenance
}

@Entity(tableName = "payroll_records")
data class PayrollRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personnelId: Int,
    val personnelName: String,
    val role: PersonnelRole,
    
    val weekEnding: Long,
    val weekStarting: Long,
    
    // Hours
    val regularHours: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val totalHours: Double = 0.0,
    
    // Pieces
    val totalBirds: Int = 0,
    val totalCases: Int = 0,
    
    // Hauls
    @TypeConverters val haulIds: List<Int> = emptyList(),
    val haulCount: Int = 0,
    
    // Pay Calculation
    val regularPay: Double = 0.0,
    val overtimePay: Double = 0.0,
    val piecePay: Double = 0.0,
    val bonusPay: Double = 0.0,
    val grossPay: Double = 0.0,
    
    // Status
    val processed: Boolean = false,
    val processedBy: Int? = null,
    val processedAt: Long? = null,
    val paidDate: Long? = null,
    
    val notes: String = ""
)

// ==================== TRACKING MODELS ====================

@Entity(tableName = "gps_locations")
data class GPSLocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val haulId: Int,
    val personnelId: Int,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val speed: Float? = null,
    val heading: Float? = null,
    val altitude: Double? = null,
    val timestamp: Long,
    val locationType: LocationType
)

enum class LocationType {
    EN_ROUTE_TO_FARM,
    AT_FARM,
    LOADING,
    IN_TRANSIT,
    AT_PLANT
}

@Entity(tableName = "haul_events")
data class HaulEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val haulId: Int,
    val eventType: HaulEventType,
    val timestamp: Long,
    val personnelId: Int? = null,
    val notes: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

enum class HaulEventType {
    CREATED,
    ASSIGNED,
    ACCEPTED,
    STARTED,
    ARRIVED_FARM,
    LOAD_STARTED,
    LOAD_COMPLETED,
    DEPARTED_FARM,
    ARRIVED_PLANT,
    UNLOAD_STARTED,
    UNLOAD_COMPLETED,
    COMPLETED,
    CANCELLED,
    DELAYED,
    INCIDENT
}
