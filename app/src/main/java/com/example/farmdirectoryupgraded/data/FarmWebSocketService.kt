package com.example.farmdirectoryupgraded.data

import android.util.Log
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
 * Connection State for WebSocket
 */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR
}

/**
 * WebSocket Error Types
 */
sealed class WebSocketError {
    data class ConnectionFailed(val message: String, val cause: Throwable? = null) : WebSocketError()
    data class JoinFarmFailed(val farmId: String, val reason: String) : WebSocketError()
    data class NetworkError(val message: String) : WebSocketError()
    data class TimeoutError(val operation: String) : WebSocketError()
    data class InvalidToken(val message: String) : WebSocketError()
    data class BackendOffline(val backendUrl: String) : WebSocketError()
    data class UnknownError(val message: String) : WebSocketError()
}

/**
 * WebSocket Service for Real-Time Farm Monitoring
 * Connects to Skeleton Key backend for live updates
 * Enhanced with comprehensive error handling, retry logic, and loading states
 */
class FarmWebSocketService(private val backendUrl: String = "http://10.0.2.2:4000") {

    private val TAG = "FarmWebSocket"
    private var socket: Socket? = null
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Retry configuration
    private var retryCount = 0
    private val maxRetries = 5
    private val baseRetryDelay = 1000L // 1 second
    private val maxRetryDelay = 30000L // 30 seconds

    // Connection state with detailed status
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Legacy support - maps to connected state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Loading state for operations
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error events
    private val _errors = MutableSharedFlow<WebSocketError>()
    val errors: SharedFlow<WebSocketError> = _errors.asSharedFlow()

    // Connection error message
    private val _connectionErrorMessage = MutableStateFlow<String?>(null)
    val connectionErrorMessage: StateFlow<String?> = _connectionErrorMessage.asStateFlow()

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
     * Connect to WebSocket server with retry logic
     */
    fun connect() {
        if (socket?.connected() == true) {
            Log.d(TAG, "Already connected")
            return
        }

        scope.launch {
            try {
                _connectionState.value = ConnectionState.CONNECTING
                _isLoading.value = true
                _connectionErrorMessage.value = null

                val opts = IO.Options().apply {
                    reconnection = true
                    reconnectionDelay = baseRetryDelay
                    reconnectionDelayMax = maxRetryDelay
                    reconnectionAttempts = maxRetries
                    timeout = 10000 // 10 second connection timeout
                }

                socket = IO.socket("$backendUrl/farms", opts)

                setupEventListeners()
                socket?.connect()

                Log.d(TAG, "Connecting to $backendUrl/farms")
            } catch (e: URISyntaxException) {
                Log.e(TAG, "Invalid URI: $backendUrl", e)
                handleConnectionError("Invalid backend URL: ${e.message}", e)
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed", e)
                handleConnectionError("Connection failed: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Retry connection with exponential backoff
     */
    fun retryConnection() {
        if (_connectionState.value == ConnectionState.CONNECTING ||
            _connectionState.value == ConnectionState.RECONNECTING) {
            Log.d(TAG, "Connection already in progress")
            return
        }

        scope.launch {
            if (retryCount >= maxRetries) {
                Log.e(TAG, "Max retry attempts reached")
                _errors.emit(WebSocketError.ConnectionFailed(
                    "Failed to connect after $maxRetries attempts. Please check your network and backend URL.",
                    null
                ))
                return@launch
            }

            _connectionState.value = ConnectionState.RECONNECTING
            retryCount++

            val delay = calculateRetryDelay(retryCount)
            Log.d(TAG, "Retrying connection in ${delay}ms (attempt $retryCount/$maxRetries)")
            delay(delay)

            disconnect()
            connect()
        }
    }

    /**
     * Calculate exponential backoff delay
     */
    private fun calculateRetryDelay(attempt: Int): Long {
        val delay = baseRetryDelay * (1 shl (attempt - 1)) // Exponential: 1s, 2s, 4s, 8s, 16s...
        return minOf(delay, maxRetryDelay)
    }

    /**
     * Handle connection errors
     */
    private suspend fun handleConnectionError(message: String, cause: Throwable? = null) {
        _connectionState.value = ConnectionState.ERROR
        _connectionErrorMessage.value = message
        _isConnected.value = false
        _errors.emit(WebSocketError.ConnectionFailed(message, cause))
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _connectionErrorMessage.value = null
    }

    /**
     * Disconnect from WebSocket server
     */
    fun disconnect() {
        socket?.disconnect()
        socket = null
        _isConnected.value = false
        _connectionState.value = ConnectionState.DISCONNECTED
        _connectionErrorMessage.value = null
        retryCount = 0
        Log.d(TAG, "Disconnected")
    }

    /**
     * Join a farm room with error handling and timeout
     */
    fun joinFarm(farmId: String, workerId: String, workerName: String, callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                if (socket?.connected() != true) {
                    Log.e(TAG, "Cannot join farm - not connected")
                    _errors.emit(WebSocketError.JoinFarmFailed(
                        farmId,
                        "Not connected to WebSocket server"
                    ))
                    callback(false)
                    return@launch
                }

                _isLoading.value = true

                val joinData = JSONObject().apply {
                    put("farmId", farmId)
                    put("workerId", workerId)
                    put("workerName", workerName)
                }

                var responseReceived = false

                socket?.emit(FarmEvent.JOIN_FARM, joinData, io.socket.client.Ack { args ->
                    responseReceived = true
                    scope.launch {
                        try {
                            if (args.isNotEmpty()) {
                                val response = args[0] as? JSONObject
                                val success = response?.optBoolean("success", false) ?: false
                                Log.d(TAG, "Join farm response: $success")

                                if (!success) {
                                    val message = response?.optString("message") ?: "Unknown error"
                                    _errors.emit(WebSocketError.JoinFarmFailed(farmId, message))
                                }

                                callback(success)
                            } else {
                                _errors.emit(WebSocketError.JoinFarmFailed(
                                    farmId,
                                    "Empty response from server"
                                ))
                                callback(false)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing join farm response", e)
                            _errors.emit(WebSocketError.UnknownError(
                                "Failed to parse join farm response: ${e.message}"
                            ))
                            callback(false)
                        } finally {
                            _isLoading.value = false
                        }
                    }
                })

                // Timeout handler
                delay(10000) // 10 second timeout
                if (!responseReceived) {
                    Log.e(TAG, "Join farm timeout")
                    _errors.emit(WebSocketError.TimeoutError("Join farm"))
                    _isLoading.value = false
                    callback(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Join farm failed", e)
                _errors.emit(WebSocketError.UnknownError("Join farm failed: ${e.message}"))
                _isLoading.value = false
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
     * Update animal location with error handling
     */
    fun updateLocation(entityId: String, location: GPSCoordinates, workerId: String) {
        scope.launch {
            try {
                if (socket?.connected() != true) {
                    Log.e(TAG, "Cannot update location - not connected")
                    _errors.emit(WebSocketError.NetworkError(
                        "Cannot update location: Not connected to server"
                    ))
                    return@launch
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
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update location", e)
                _errors.emit(WebSocketError.UnknownError(
                    "Failed to update location: ${e.message}"
                ))
            }
        }
    }

    /**
     * Update health status with error handling
     */
    fun updateHealth(entityId: String, healthStatus: HealthStatus, healthNotes: String, workerId: String) {
        scope.launch {
            try {
                if (socket?.connected() != true) {
                    Log.e(TAG, "Cannot update health - not connected")
                    _errors.emit(WebSocketError.NetworkError(
                        "Cannot update health: Not connected to server"
                    ))
                    return@launch
                }

                val healthData = JSONObject().apply {
                    put("entityId", entityId)
                    put("healthStatus", healthStatus.name)
                    put("healthNotes", healthNotes)
                    put("workerId", workerId)
                }

                socket?.emit(FarmEvent.HEALTH_UPDATE, healthData)
                Log.d(TAG, "Health update sent for $entityId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update health", e)
                _errors.emit(WebSocketError.UnknownError(
                    "Failed to update health: ${e.message}"
                ))
            }
        }
    }

    /**
     * Setup event listeners for all WebSocket events with comprehensive error handling
     */
    private fun setupEventListeners() {
        socket?.apply {
            // Connection events
            on(Socket.EVENT_CONNECT) {
                scope.launch {
                    _isConnected.value = true
                    _connectionState.value = ConnectionState.CONNECTED
                    _connectionErrorMessage.value = null
                    retryCount = 0 // Reset retry count on successful connection
                    Log.d(TAG, "✅ Connected to farm monitoring")
                }
            }

            on(Socket.EVENT_DISCONNECT) { args ->
                scope.launch {
                    _isConnected.value = false
                    _connectionState.value = ConnectionState.DISCONNECTED
                    val reason = if (args.isNotEmpty()) args[0].toString() else "Unknown reason"
                    Log.d(TAG, "❌ Disconnected from farm monitoring: $reason")

                    // Check if disconnect was intentional or due to error
                    if (reason != "io client disconnect") {
                        _connectionErrorMessage.value = "Disconnected: $reason"
                    }
                }
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                scope.launch {
                    val errorMessage = if (args.isNotEmpty()) {
                        args[0].toString()
                    } else {
                        "Unknown error"
                    }
                    Log.e(TAG, "Connection error: $errorMessage")

                    val detailedMessage = when {
                        errorMessage.contains("timeout") -> "Connection timed out. Please check your network."
                        errorMessage.contains("ECONNREFUSED") -> "Backend server is offline or unreachable."
                        errorMessage.contains("ERR_NAME_NOT_RESOLVED") -> "Invalid backend URL. Please check settings."
                        errorMessage.contains("Network") -> "Network error. Please check your internet connection."
                        else -> "Connection error: $errorMessage"
                    }

                    handleConnectionError(detailedMessage)

                    // Emit specific error types
                    when {
                        errorMessage.contains("ECONNREFUSED") -> {
                            _errors.emit(WebSocketError.BackendOffline(backendUrl))
                        }
                        errorMessage.contains("timeout") -> {
                            _errors.emit(WebSocketError.TimeoutError("Connection"))
                        }
                        errorMessage.contains("Network") -> {
                            _errors.emit(WebSocketError.NetworkError(detailedMessage))
                        }
                        else -> {
                            _errors.emit(WebSocketError.UnknownError(detailedMessage))
                        }
                    }
                }
            }

            on(Socket.EVENT_RECONNECT) { args ->
                scope.launch {
                    val attemptNumber = if (args.isNotEmpty()) args[0] else 0
                    Log.d(TAG, "🔄 Reconnected after $attemptNumber attempts")
                    _connectionState.value = ConnectionState.CONNECTED
                    _isConnected.value = true
                    _connectionErrorMessage.value = null
                    retryCount = 0
                }
            }

            on(Socket.EVENT_RECONNECT_ATTEMPT) { args ->
                scope.launch {
                    val attemptNumber = if (args.isNotEmpty()) args[0] else 0
                    Log.d(TAG, "🔄 Reconnection attempt $attemptNumber")
                    _connectionState.value = ConnectionState.RECONNECTING
                }
            }

            on(Socket.EVENT_RECONNECT_ERROR) { args ->
                scope.launch {
                    val error = if (args.isNotEmpty()) args[0].toString() else "Unknown error"
                    Log.e(TAG, "Reconnection error: $error")
                }
            }

            on(Socket.EVENT_RECONNECT_FAILED) {
                scope.launch {
                    Log.e(TAG, "❌ Reconnection failed after all attempts")
                    _connectionState.value = ConnectionState.ERROR
                    _errors.emit(WebSocketError.ConnectionFailed(
                        "Failed to reconnect after $maxRetries attempts. Please try manually.",
                        null
                    ))
                }
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
