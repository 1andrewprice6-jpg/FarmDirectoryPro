package com.example.farmdirectoryupgraded.data

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class FarmWebSocketService {

    companion object {
        private const val TAG = "FarmWebSocket"

        // CHANGE THIS to your actual backend URL
        // Examples:
        // - Local development: ws://192.168.1.100:8080
        // - Production: wss://api.yourfarm.com
        // - Docker local: ws://host.docker.internal:8080
        private const val BASE_URL = "ws://YOUR_BACKEND_IP:PORT"

        // Connection timeouts
        private const val CONNECT_TIMEOUT = 10L
        private const val READ_TIMEOUT = 30L
        private const val WRITE_TIMEOUT = 30L

        // Reconnection settings
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val RECONNECT_DELAY_MS = 3000L
    }

    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private var reconnectAttempts = 0
    private var isManualDisconnect = false

    // Store connection parameters for reconnection
    private var currentFarmId: String? = null
    private var currentWorkerId: String? = null

    // State flows
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messageChannel = Channel<String>(Channel.UNLIMITED)
    val messageChannel: Channel<String> = _messageChannel

    private val _errorChannel = Channel<WebSocketError>(Channel.UNLIMITED)
    val errorChannel: Channel<WebSocketError> = _errorChannel

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connection opened")
            _connectionState.value = ConnectionState.Connected
            reconnectAttempts = 0
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Message received: $text")
            _messageChannel.trySend(text)
            handleMessage(text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d(TAG, "Binary message received: ${bytes.hex()}")
            _messageChannel.trySend(bytes.utf8())
            handleMessage(bytes.utf8())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: $code - $reason")
            _connectionState.value = ConnectionState.Disconnecting
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: $code - $reason")
            _connectionState.value = ConnectionState.Disconnected

            // Attempt reconnection if not manually disconnected
            if (!isManualDisconnect && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                attemptReconnect()
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure", t)
            val error = WebSocketError(
                message = t.message ?: "Unknown error",
                code = response?.code ?: -1,
                timestamp = System.currentTimeMillis()
            )
            _errorChannel.trySend(error)
            _connectionState.value = ConnectionState.Error(error)

            // Attempt reconnection
            if (!isManualDisconnect && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                attemptReconnect()
            }
        }
    }

    fun connect(farmId: String, workerId: String) {
        if (_connectionState.value is ConnectionState.Connected) {
            Log.d(TAG, "Already connected")
            return
        }

        // Store parameters for reconnection
        currentFarmId = farmId
        currentWorkerId = workerId

        isManualDisconnect = false
        _connectionState.value = ConnectionState.Connecting

        val url = "$BASE_URL/farm/$farmId/worker/$workerId"
        Log.d(TAG, "Connecting to: $url")

        val request = Request.Builder()
            .url(url)
            .build()

        try {
            webSocket = okHttpClient.newWebSocket(request, webSocketListener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create WebSocket", e)
            val error = WebSocketError(
                message = "Connection failed: ${e.message}",
                code = -1,
                timestamp = System.currentTimeMillis()
            )
            _errorChannel.trySend(error)
            _connectionState.value = ConnectionState.Error(error)
        }
    }

    private fun attemptReconnect() {
        reconnectAttempts++
        Log.d(TAG, "Attempting reconnection $reconnectAttempts/$MAX_RECONNECT_ATTEMPTS")

        _connectionState.value = ConnectionState.Reconnecting(reconnectAttempts)

        // Use coroutine instead of Thread.sleep (non-blocking)
        GlobalScope.launch {
            delay(RECONNECT_DELAY_MS)

            // Reconnect using stored parameters
            if (currentFarmId != null && currentWorkerId != null && !isManualDisconnect) {
                connect(currentFarmId!!, currentWorkerId!!)
            }
        }
    }

    fun disconnect() {
        Log.d(TAG, "Manually disconnecting")
        isManualDisconnect = true
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        currentFarmId = null
        currentWorkerId = null
        _connectionState.value = ConnectionState.Disconnected
    }

    fun sendMessage(message: Any): Boolean {
        val currentState = _connectionState.value
        if (currentState !is ConnectionState.Connected) {
            Log.w(TAG, "Cannot send message - not connected. State: $currentState")
            return false
        }

        return try {
            val json = gson.toJson(message)
            Log.d(TAG, "Sending message: $json")
            webSocket?.send(json) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message", e)
            false
        }
    }

    fun sendLocationUpdate(latitude: Double, longitude: Double, workerId: String): Boolean {
        val update = LocationUpdateDto(
            workerId = workerId,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis()
        )
        return sendMessage(update)
    }

    fun sendHealthUpdate(status: HealthStatus, notes: String?, workerId: String): Boolean {
        val update = HealthUpdateDto(
            workerId = workerId,
            status = status,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
        return sendMessage(update)
    }

    fun joinFarm(farmId: String, workerName: String): Boolean {
        val joinRequest = JoinFarmDto(
            farmId = farmId,
            workerName = workerName,
            timestamp = System.currentTimeMillis()
        )
        return sendMessage(joinRequest)
    }

    fun reconcile(farmId: String): Boolean {
        val reconcileRequest = ReconcileRequestDto(
            type = "reconcile",
            farmId = farmId,
            timestamp = System.currentTimeMillis()
        )
        return sendMessage(reconcileRequest)
    }

    fun optimize(farmId: String): Boolean {
        val optimizeRequest = OptimizeRequestDto(
            type = "optimize",
            farmId = farmId,
            timestamp = System.currentTimeMillis()
        )
        return sendMessage(optimizeRequest)
    }

    private fun handleMessage(text: String) {
        try {
            // Parse the message to determine type
            val messageObj = gson.fromJson(text, Map::class.java)
            val type = messageObj["type"] as? String

            Log.d(TAG, "Handling message of type: $type")

            when (type) {
                "reconcile_result" -> {
                    Log.d(TAG, "Reconcile result received: $text")
                    // Process reconciliation results
                    // Your app logic here
                }
                "optimize_result" -> {
                    Log.d(TAG, "Optimize result received: $text")
                    // Process optimization results
                    // Your app logic here
                }
                "location_update" -> {
                    Log.d(TAG, "Location update received: $text")
                }
                "worker_joined" -> {
                    Log.d(TAG, "Worker joined: $text")
                }
                else -> {
                    Log.d(TAG, "Unknown message type: $type")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle message", e)
        }
    }
}

// Connection states
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    object Disconnecting : ConnectionState()
    data class Reconnecting(val attempt: Int) : ConnectionState()
    data class Error(val error: WebSocketError) : ConnectionState()
}

// Error model
data class WebSocketError(
    val message: String,
    val code: Int,
    val timestamp: Long
)

// DTO models
data class LocationUpdateDto(
    val workerId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

data class HealthUpdateDto(
    val workerId: String,
    val status: HealthStatus,
    val notes: String?,
    val timestamp: Long
)

data class JoinFarmDto(
    val farmId: String,
    val workerName: String,
    val timestamp: Long
)

data class ReconcileRequestDto(
    val type: String,
    val farmId: String,
    val timestamp: Long
)

data class OptimizeRequestDto(
    val type: String,
    val farmId: String,
    val timestamp: Long
)

enum class HealthStatus {
    HEALTHY,
    SICK,
    INJURED,
    QUARANTINE
}
