package com.example.farmdirectoryupgraded.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.data.FarmWebSocketService
import com.example.farmdirectoryupgraded.data.WebSocketEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for real-time WebSocket communication
 *
 * Handles:
 * - WebSocket connection lifecycle
 * - Location updates and broadcasts
 * - Health alerts
 * - Worker presence tracking
 * - Reconnection logic
 */
class WebSocketViewModel(private val webSocketService: FarmWebSocketService) : ViewModel() {

    companion object {
        private const val TAG = "WebSocketViewModel"
    }

    // State management
    private val _connectionState = MutableStateFlow(FarmWebSocketService.ConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _locationUpdates = MutableStateFlow<String?>(null)
    val locationUpdates = _locationUpdates.asStateFlow()

    private val _healthAlerts = MutableStateFlow<String?>(null)
    val healthAlerts = _healthAlerts.asStateFlow()

    private val _workerPresence = MutableStateFlow<String?>(null)
    val workerPresence = _workerPresence.asStateFlow()

    private val _criticalAlerts = MutableStateFlow<String?>(null)
    val criticalAlerts = _criticalAlerts.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private var farmId: String = ""
    private var workerId: String = ""

    init {
        setupWebSocketListeners()
    }

    /**
     * Setup WebSocket event listeners
     */
    private fun setupWebSocketListeners() {
        viewModelScope.launch {
            webSocketService.connectionState.collect { state ->
                _connectionState.value = state
                Log.d(TAG, "Connection state changed: $state")
            }
        }

        viewModelScope.launch {
            webSocketService.errors.collect { error ->
                _errorMessage.value = error
                Log.e(TAG, "WebSocket error: $error")
            }
        }

        viewModelScope.launch {
            webSocketService.events.collect { event ->
                handleWebSocketEvent(event)
            }
        }
    }

    /**
     * Connect to WebSocket backend
     *
     * @param farmId The farm identifier
     * @param workerId The worker identifier
     */
    fun connectToBackend(farmId: String, workerId: String) {
        this.farmId = farmId
        this.workerId = workerId
        webSocketService.connect(farmId, workerId)
        Log.d(TAG, "Connecting to backend for farm $farmId, worker $workerId")
    }

    /**
     * Disconnect from WebSocket backend
     */
    fun disconnectFromBackend() {
        webSocketService.disconnect()
        Log.d(TAG, "Disconnected from backend")
    }

    /**
     * Retry connection to backend
     */
    fun retryConnection() {
        Log.d(TAG, "Retrying connection to backend")
        webSocketService.retryConnection()
    }

    /**
     * Join farm for real-time updates
     */
    fun joinFarm() {
        webSocketService.joinFarm()
        Log.d(TAG, "Joined farm $farmId")
    }

    /**
     * Leave farm monitoring
     */
    fun leaveFarm() {
        webSocketService.leaveFarm()
        Log.d(TAG, "Left farm $farmId")
    }

    /**
     * Send location update to backend
     *
     * @param latitude Current latitude
     * @param longitude Current longitude
     */
    fun sendLocationUpdate(latitude: Double, longitude: Double) {
        webSocketService.sendLocationUpdate(latitude, longitude)
        Log.d(TAG, "Sent location update: ($latitude, $longitude)")
    }

    /**
     * Send health status update to backend
     *
     * @param status Health status (e.g., "HEALTHY", "SICK", "INJURED")
     * @param notes Additional health notes
     */
    fun sendHealthUpdate(status: String, notes: String = "") {
        webSocketService.sendHealthUpdate(status, notes)
        Log.d(TAG, "Sent health update: $status")
    }

    /**
     * Handle incoming WebSocket events
     *
     * @param event The WebSocket event
     */
    private fun handleWebSocketEvent(event: WebSocketEvent) {
        when (event) {
            is WebSocketEvent.LocationBroadcast -> {
                _locationUpdates.value = event.data
                Log.d(TAG, "Location broadcast received: ${event.data}")
            }
            is WebSocketEvent.HealthAlert -> {
                _healthAlerts.value = event.data
                Log.w(TAG, "Health alert received: ${event.data}")
            }
            is WebSocketEvent.WorkerJoined -> {
                _workerPresence.value = "Worker joined: ${event.data}"
                Log.d(TAG, "Worker joined: ${event.data}")
            }
            is WebSocketEvent.WorkerLeft -> {
                _workerPresence.value = "Worker left: ${event.data}"
                Log.d(TAG, "Worker left: ${event.data}")
            }
            is WebSocketEvent.CriticalAlert -> {
                _criticalAlerts.value = event.data
                Log.e(TAG, "CRITICAL ALERT: ${event.data}")
            }
        }
    }

    /**
     * Check if WebSocket is currently connected
     *
     * @return True if connected, false otherwise
     */
    fun isConnected(): Boolean {
        return _connectionState.value == FarmWebSocketService.ConnectionState.CONNECTED
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clear location updates
     */
    fun clearLocationUpdates() {
        _locationUpdates.value = null
    }

    /**
     * Clear health alerts
     */
    fun clearHealthAlerts() {
        _healthAlerts.value = null
    }

    /**
     * Clear critical alerts
     */
    fun clearCriticalAlerts() {
        _criticalAlerts.value = null
    }

    override fun onCleared() {
        super.onCleared()
        disconnectFromBackend()
        Log.d(TAG, "WebSocketViewModel cleared")
    }
}
