package com.example.farmdirectoryupgraded.data

import android.util.Log
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URISyntaxException

/**
 * WebSocket Service for Real-Time Farm Monitoring
 * Connects to Skeleton Key backend for live updates
 */
class FarmWebSocketService(private val backendUrl: String = "http://10.0.2.2:4000") {

    private val TAG = "FarmWebSocket"
    private var socket: Socket? = null
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Connection state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Location updates
    private val _locationUpdates = MutableSharedFlow<LocationBroadcast>()
    val locationUpdates: SharedFlow<LocationBroadcast> = _locationUpdates.asSharedFlow()

    // Health alerts
    private val _healthAlerts = MutableSharedFlow<HealthAlert>()
    val healthAlerts: SharedFlow<HealthAlert> = _healthAlerts.asSharedFlow()

    // Critical alerts
    private val _criticalAlerts = MutableSharedFlow<HealthAlert>()
    val criticalAlerts: SharedFlow<HealthAlert> = _criticalAlerts.asSharedFlow()

    // Worker presence
    private val _workerPresence = MutableStateFlow<List<WorkerInfo>>(emptyList())
    val workerPresence: StateFlow<List<WorkerInfo>> = _workerPresence.asStateFlow()

    // Worker joined/left
    private val _workerJoined = MutableSharedFlow<WorkerJoined>()
    val workerJoined: SharedFlow<WorkerJoined> = _workerJoined.asSharedFlow()

    private val _workerLeft = MutableSharedFlow<WorkerLeft>()
    val workerLeft: SharedFlow<WorkerLeft> = _workerLeft.asSharedFlow()

    /**
     * Connect to WebSocket server
     */
    fun connect() {
        if (socket?.connected() == true) {
            Log.d(TAG, "Already connected")
            return
        }

        try {
            val opts = IO.Options().apply {
                reconnection = true
                reconnectionDelay = 1000
                reconnectionAttempts = 5
            }

            socket = IO.socket("$backendUrl/farms", opts)

            setupEventListeners()
            socket?.connect()

            Log.d(TAG, "Connecting to $backendUrl/farms")
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Invalid URI: $backendUrl", e)
        }
    }

    /**
     * Disconnect from WebSocket server
     */
    fun disconnect() {
        socket?.disconnect()
        socket = null
        _isConnected.value = false
        Log.d(TAG, "Disconnected")
    }

    /**
     * Join a farm room
     */
    fun joinFarm(farmId: String, workerId: String, workerName: String, callback: (Boolean) -> Unit) {
        if (socket?.connected() != true) {
            Log.e(TAG, "Cannot join farm - not connected")
            callback(false)
            return
        }

        val joinData = JSONObject().apply {
            put("farmId", farmId)
            put("workerId", workerId)
            put("workerName", workerName)
        }

        socket?.emit(FarmEvent.JOIN_FARM, joinData) { args ->
            if (args.isNotEmpty()) {
                val response = args[0] as? JSONObject
                val success = response?.optBoolean("success", false) ?: false
                Log.d(TAG, "Join farm response: $success")
                callback(success)
            } else {
                callback(false)
            }
        }
    }

    /**
     * Leave current farm
     */
    fun leaveFarm() {
        socket?.emit(FarmEvent.LEAVE_FARM)
    }

    /**
     * Update animal location
     */
    fun updateLocation(entityId: String, location: GPSCoordinates, workerId: String) {
        if (socket?.connected() != true) {
            Log.e(TAG, "Cannot update location - not connected")
            return
        }

        val locationData = JSONObject().apply {
            put("entityId", entityId)
            put("location", JSONObject().apply {
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                put("timestamp", location.timestamp.time)
            })
            put("workerId", workerId)
        }

        socket?.emit(FarmEvent.LOCATION_UPDATE, locationData)
        Log.d(TAG, "Location update sent for $entityId")
    }

    /**
     * Update health status
     */
    fun updateHealth(entityId: String, healthStatus: HealthStatus, healthNotes: String, workerId: String) {
        if (socket?.connected() != true) {
            Log.e(TAG, "Cannot update health - not connected")
            return
        }

        val healthData = JSONObject().apply {
            put("entityId", entityId)
            put("healthStatus", healthStatus.name)
            put("healthNotes", healthNotes)
            put("workerId", workerId)
        }

        socket?.emit(FarmEvent.HEALTH_UPDATE, healthData)
        Log.d(TAG, "Health update sent for $entityId")
    }

    /**
     * Setup event listeners for all WebSocket events
     */
    private fun setupEventListeners() {
        socket?.apply {
            // Connection events
            on(Socket.EVENT_CONNECT) {
                _isConnected.value = true
                Log.d(TAG, "✅ Connected to farm monitoring")
            }

            on(Socket.EVENT_DISCONNECT) {
                _isConnected.value = false
                Log.d(TAG, "❌ Disconnected from farm monitoring")
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Connection error: ${if (args.isNotEmpty()) args[0].toString() else "Unknown error"}")
            }

            // Location updates
            on(FarmEvent.LOCATION_BROADCAST) { args ->
                try {
                    val data = args[0] as JSONObject
                    val broadcast = LocationBroadcast(
                        entityId = data.getString("entityId"),
                        location = parseGPSCoordinates(data.getJSONObject("location")),
                        updatedBy = data.getString("updatedBy"),
                        version = data.getInt("version"),
                        timestamp = java.util.Date(data.getLong("timestamp"))
                    )
                    scope.launch {
                        _locationUpdates.emit(broadcast)
                    }
                    Log.d(TAG, "📍 Location update: ${broadcast.entityId} by ${broadcast.updatedBy}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing location broadcast", e)
                }
            }

            // Health alerts
            on(FarmEvent.HEALTH_ALERT) { args ->
                try {
                    val data = args[0] as JSONObject
                    val alert = HealthAlert(
                        entityId = data.getString("entityId"),
                        healthStatus = data.optString("healthStatus")?.let { HealthStatus.valueOf(it) },
                        healthNotes = data.optString("healthNotes"),
                        alert = data.optString("alert"),
                        updatedBy = data.optString("updatedBy"),
                        timestamp = java.util.Date(data.getLong("timestamp"))
                    )
                    scope.launch {
                        _healthAlerts.emit(alert)
                    }
                    Log.d(TAG, "🏥 Health alert: ${alert.entityId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing health alert", e)
                }
            }

            // Critical alerts
            on(FarmEvent.CRITICAL_ALERT) { args ->
                try {
                    val data = args[0] as JSONObject
                    val alert = HealthAlert(
                        entityId = data.getString("entityId"),
                        healthNotes = data.optString("healthNotes"),
                        updatedBy = data.optString("updatedBy"),
                        timestamp = java.util.Date(data.getLong("timestamp")),
                        priority = data.optString("priority")
                    )
                    scope.launch {
                        _criticalAlerts.emit(alert)
                    }
                    Log.d(TAG, "🚨 CRITICAL ALERT: ${alert.entityId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing critical alert", e)
                }
            }

            // Worker presence
            on(FarmEvent.WORKER_PRESENCE) { args ->
                try {
                    val data = args[0] as JSONObject
                    val workersArray = data.getJSONArray("workers")
                    val workers = mutableListOf<WorkerInfo>()

                    for (i in 0 until workersArray.length()) {
                        val worker = workersArray.getJSONObject(i)
                        workers.add(WorkerInfo(
                            workerId = worker.getString("workerId"),
                            workerName = worker.getString("workerName")
                        ))
                    }

                    _workerPresence.value = workers
                    Log.d(TAG, "👥 ${workers.size} workers online")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing worker presence", e)
                }
            }

            // Worker joined
            on(FarmEvent.WORKER_JOINED) { args ->
                try {
                    val data = args[0] as JSONObject
                    val joined = WorkerJoined(
                        workerId = data.getString("workerId"),
                        workerName = data.getString("workerName"),
                        timestamp = java.util.Date(data.getLong("timestamp"))
                    )
                    scope.launch {
                        _workerJoined.emit(joined)
                    }
                    Log.d(TAG, "👨‍🌾 ${joined.workerName} joined")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing worker joined", e)
                }
            }

            // Worker left
            on(FarmEvent.WORKER_LEFT) { args ->
                try {
                    val data = args[0] as JSONObject
                    val left = WorkerLeft(
                        workerName = data.getString("workerName"),
                        timestamp = java.util.Date(data.getLong("timestamp"))
                    )
                    scope.launch {
                        _workerLeft.emit(left)
                    }
                    Log.d(TAG, "👋 ${left.workerName} left")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing worker left", e)
                }
            }
        }
    }

    /**
     * Parse GPS coordinates from JSON
     */
    private fun parseGPSCoordinates(json: JSONObject): GPSCoordinates {
        return GPSCoordinates(
            latitude = json.getDouble("latitude"),
            longitude = json.getDouble("longitude"),
            altitude = json.optDouble("altitude").takeIf { !it.isNaN() },
            accuracy = json.optDouble("accuracy").takeIf { !it.isNaN() },
            timestamp = java.util.Date(json.getLong("timestamp"))
        )
    }

    companion object {
        // Singleton instance
        @Volatile
        private var instance: FarmWebSocketService? = null

        fun getInstance(backendUrl: String = "http://10.0.2.2:4000"): FarmWebSocketService {
            return instance ?: synchronized(this) {
                instance ?: FarmWebSocketService(backendUrl).also { instance = it }
            }
        }
    }
}
