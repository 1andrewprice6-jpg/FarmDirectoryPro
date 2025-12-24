package com.example.farmdirectoryupgraded.data

import java.util.Date

/**
 * WebSocket Data Models
 * Matching the Skeleton Key backend API
 */

// GPS Coordinates
data class GPSCoordinates(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Double? = null,
    val timestamp: Date = Date()
)

// Health Status
enum class HealthStatus {
    HEALTHY,
    SICK,
    RECOVERING,
    CRITICAL,
    DECEASED
}

// Farm Event Types
object FarmEvent {
    // Connection events
    const val JOIN_FARM = "farm:join"
    const val LEAVE_FARM = "farm:leave"

    // Location events
    const val LOCATION_UPDATE = "animal:location"
    const val LOCATION_BROADCAST = "farm:location:broadcast"

    // Health events
    const val HEALTH_UPDATE = "animal:health"
    const val HEALTH_ALERT = "farm:health:alert"
    const val CRITICAL_ALERT = "farm:critical:alert"

    // Worker events
    const val WORKER_JOINED = "worker:joined"
    const val WORKER_LEFT = "worker:left"
    const val WORKER_PRESENCE = "farm:workers"

    // Bulk operations
    const val BULK_MOVE_START = "farm:bulk:start"
    const val BULK_MOVE_PROGRESS = "farm:bulk:progress"
    const val BULK_MOVE_COMPLETE = "farm:bulk:complete"
}

// Data Transfer Objects (DTOs)
data class JoinFarmDto(
    val farmId: String,
    val workerId: String,
    val workerName: String
)

data class LocationUpdateDto(
    val entityId: String,
    val location: GPSCoordinates,
    val workerId: String
)

data class HealthUpdateDto(
    val entityId: String,
    val healthStatus: HealthStatus,
    val healthNotes: String,
    val workerId: String
)

// Response Models
data class JoinFarmResponse(
    val success: Boolean,
    val entities: List<FarmEntity>? = null,
    val message: String? = null
)

data class LocationBroadcast(
    val entityId: String,
    val location: GPSCoordinates,
    val updatedBy: String,
    val version: Int,
    val timestamp: Date
)

data class HealthAlert(
    val entityId: String,
    val healthStatus: HealthStatus? = null,
    val healthNotes: String? = null,
    val alert: String? = null,
    val updatedBy: String? = null,
    val timestamp: Date,
    val priority: String? = null
)

data class WorkerPresence(
    val workers: List<WorkerInfo>,
    val count: Int,
    val timestamp: Date
)

data class WorkerInfo(
    val workerId: String,
    val workerName: String
)

data class WorkerJoined(
    val workerId: String,
    val workerName: String,
    val timestamp: Date
)

data class WorkerLeft(
    val workerName: String,
    val timestamp: Date
)

// Farm Entity (simplified for client-side)
data class FarmEntity(
    val id: String,
    val farmId: String,
    val name: String,
    val version: Int,
    val location: GPSCoordinates,
    val zone: String? = null,
    val healthStatus: HealthStatus,
    val healthNotes: String? = null
)
