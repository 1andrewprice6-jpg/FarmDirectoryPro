package com.example.farmdirectoryupgraded.data

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.min

/**
 * WebSocket Service for real-time farm data communication
 * Implements automatic reconnection with exponential backoff
 */
class FarmWebSocketService(private val baseUrl: String = "https://api.farmdirectory.com") {

    companion object {
        private const val TAG = "FarmWebSocketService"
        private const val INITIAL_RECONNECT_DELAY_MS = 1000L  // 1 second
        private const val MAX_RECONNECT_DELAY_MS = 60000L     // 1 minute
        private const val MAX_RECONNECTION_ATTEMPTS = 10
    }

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING, ERROR
    }

    // State management
    private var socket: Socket? = null
    private val _connectionState = MutableSharedFlow<ConnectionState>()
    val connectionState = _connectionState.asSharedFlow()

    private val _events = MutableSharedFlow<WebSocketEvent>()
    val events = _events.asSharedFlow()

    private val _errors = MutableSharedFlow<String>()
    val errors = _errors.asSharedFlow()

    // Reconnection state
    private var reconnectionAttempts = 0
    private var currentReconnectDelayMs = INITIAL_RECONNECT_DELAY_MS
    private var isManuallyDisconnected = false
    private var farmId: String = ""
    private var workerId: String = ""

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Connect to WebSocket server
     *
     * @param farmId The farm identifier
     * @param workerId The worker identifier
     */
    fun connect(farmId: String, workerId: String) {
        if (socket?.isConnected == true) {
            Log.w(TAG, "Already connected to WebSocket")
            return
        }

        this.farmId = farmId
        this.workerId = workerId
        isManuallyDisconnected = false
        reconnectionAttempts = 0
        currentReconnectDelayMs = INITIAL_RECONNECT_DELAY_MS

        coroutineScope.launch {
            _connectionState.emit(ConnectionState.CONNECTING)
        }

        try {
            val options = IO.Options.builder()
                .setReconnection(false)  // Handle reconnection manually
                .setReconnectionDelay(1000)
                .setReconnectionDelayMax(5000)
                .setRandomizationFactor(0.5)
                .build()

            socket = IO.socket(baseUrl, options)
            setupSocketListeners()
            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create WebSocket: ${e.message}", e)
            coroutineScope.launch {
                _errors.emit("Failed to connect: ${e.message}")
                _connectionState.emit(ConnectionState.ERROR)
            }
            scheduleReconnection()
        }
    }

    /**
     * Disconnect from WebSocket server
     */
    fun disconnect() {
        isManuallyDisconnected = true
        socket?.disconnect()
        socket = null
        coroutineScope.launch {
            _connectionState.emit(ConnectionState.DISCONNECTED)
        }
    }

    /**
     * Retry connection with exponential backoff
     */
    fun retryConnection() {
        if (isManuallyDisconnected) {
            Log.d(TAG, "Manual disconnection active, not retrying")
            return
        }

        if (reconnectionAttempts >= MAX_RECONNECTION_ATTEMPTS) {
            Log.e(TAG, "Max reconnection attempts reached ($MAX_RECONNECTION_ATTEMPTS)")
            coroutineScope.launch {
                _errors.emit("Failed to reconnect after $MAX_RECONNECTION_ATTEMPTS attempts")
                _connectionState.emit(ConnectionState.ERROR)
            }
            return
        }

        scheduleReconnection()
    }

    /**
     * Schedule reconnection with exponential backoff
     */
    private fun scheduleReconnection() {
        reconnectionAttempts++
        val nextDelay = calculateBackoffDelay(reconnectionAttempts)

        Log.d(TAG, "Scheduling reconnection attempt #$reconnectionAttempts in ${nextDelay}ms")

        coroutineScope.launch {
            _connectionState.emit(ConnectionState.RECONNECTING)
            kotlinx.coroutines.delay(nextDelay)
            connect(farmId, workerId)
        }
    }

    /**
     * Calculate exponential backoff delay
     *
     * @param attemptNumber The current attempt number (1-based)
     * @return Delay in milliseconds
     */
    private fun calculateBackoffDelay(attemptNumber: Int): Long {
        // Exponential backoff: (initial * 2^(attempt-1)) capped at max
        val exponentialDelay = INITIAL_RECONNECT_DELAY_MS * (1L shl (attemptNumber - 1))
        return min(exponentialDelay, MAX_RECONNECT_DELAY_MS)
    }

    /**
     * Setup WebSocket event listeners
     */
    private fun setupSocketListeners() {
        socket?.on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "WebSocket connected")
            reconnectionAttempts = 0
            currentReconnectDelayMs = INITIAL_RECONNECT_DELAY_MS
            coroutineScope.launch {
                _connectionState.emit(ConnectionState.CONNECTED)
            }
            joinFarm()
        }

        socket?.on(Socket.EVENT_DISCONNECT) {
            Log.d(TAG, "WebSocket disconnected")
            if (!isManuallyDisconnected) {
                coroutineScope.launch {
                    _connectionState.emit(ConnectionState.RECONNECTING)
                }
                retryConnection()
            } else {
                coroutineScope.launch {
                    _connectionState.emit(ConnectionState.DISCONNECTED)
                }
            }
        }

        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            val error = (args.firstOrNull() as? Exception)?.message ?: "Unknown connection error"
            Log.e(TAG, "WebSocket connection error: $error")
            coroutineScope.launch {
                _errors.emit("Connection error: $error")
                _connectionState.emit(ConnectionState.ERROR)
            }
            if (!isManuallyDisconnected) {
                retryConnection()
            }
        }

        socket?.on("location_broadcast") { args ->
            handleLocationBroadcast(args)
        }

        socket?.on("health_alert") { args ->
            handleHealthAlert(args)
        }

        socket?.on("worker_joined") { args ->
            handleWorkerJoined(args)
        }

        socket?.on("worker_left") { args ->
            handleWorkerLeft(args)
        }

        socket?.on("critical_alert") { args ->
            handleCriticalAlert(args)
        }
    }

    /**
     * Send message to server
     *
     * @param eventName The event name
     * @param data The data to send
     */
    fun sendMessage(eventName: String, data: JSONObject) {
        if (socket?.isConnected != true) {
            Log.w(TAG, "Socket not connected, cannot send message: $eventName")
            coroutineScope.launch {
                _errors.emit("Cannot send message: not connected")
            }
            return
        }

        try {
            socket?.emit(eventName, data)
            Log.d(TAG, "Sent event: $eventName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message: ${e.message}", e)
            coroutineScope.launch {
                _errors.emit("Failed to send message: ${e.message}")
            }
        }
    }

    /**
     * Send location update to server
     */
    fun sendLocationUpdate(latitude: Double, longitude: Double, timestamp: Long = System.currentTimeMillis()) {
        val data = JSONObject().apply {
            put("farmId", farmId)
            put("workerId", workerId)
            put("latitude", latitude)
            put("longitude", longitude)
            put("timestamp", timestamp)
        }
        sendMessage("location_update", data)
    }

    /**
     * Send health update to server
     */
    fun sendHealthUpdate(status: String, notes: String = "") {
        val data = JSONObject().apply {
            put("farmId", farmId)
            put("workerId", workerId)
            put("status", status)
            put("notes", notes)
            put("timestamp", System.currentTimeMillis())
        }
        sendMessage("health_update", data)
    }

    /**
     * Join farm for real-time monitoring
     */
    fun joinFarm() {
        val data = JSONObject().apply {
            put("farmId", farmId)
            put("workerId", workerId)
        }
        sendMessage("join_farm", data)
    }

    /**
     * Leave farm monitoring
     */
    fun leaveFarm() {
        val data = JSONObject().apply {
            put("farmId", farmId)
            put("workerId", workerId)
        }
        sendMessage("leave_farm", data)
    }

    // Event handlers
    private fun handleLocationBroadcast(args: Array<Any>) {
        Log.d(TAG, "Location broadcast received")
        coroutineScope.launch {
            _events.emit(WebSocketEvent.LocationBroadcast(args.firstOrNull().toString()))
        }
    }

    private fun handleHealthAlert(args: Array<Any>) {
        Log.d(TAG, "Health alert received")
        coroutineScope.launch {
            _events.emit(WebSocketEvent.HealthAlert(args.firstOrNull().toString()))
        }
    }

    private fun handleWorkerJoined(args: Array<Any>) {
        Log.d(TAG, "Worker joined event")
        coroutineScope.launch {
            _events.emit(WebSocketEvent.WorkerJoined(args.firstOrNull().toString()))
        }
    }

    private fun handleWorkerLeft(args: Array<Any>) {
        Log.d(TAG, "Worker left event")
        coroutineScope.launch {
            _events.emit(WebSocketEvent.WorkerLeft(args.firstOrNull().toString()))
        }
    }

    private fun handleCriticalAlert(args: Array<Any>) {
        Log.e(TAG, "Critical alert received")
        coroutineScope.launch {
            _events.emit(WebSocketEvent.CriticalAlert(args.firstOrNull().toString()))
        }
    }
}

/**
 * WebSocket events
 */
sealed class WebSocketEvent {
    data class LocationBroadcast(val data: String) : WebSocketEvent()
    data class HealthAlert(val data: String) : WebSocketEvent()
    data class WorkerJoined(val data: String) : WebSocketEvent()
    data class WorkerLeft(val data: String) : WebSocketEvent()
    data class CriticalAlert(val data: String) : WebSocketEvent()
}
