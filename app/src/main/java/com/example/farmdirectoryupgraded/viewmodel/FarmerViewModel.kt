package com.example.farmdirectoryupgraded.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.data.Farmer
import com.example.farmdirectoryupgraded.data.FarmerDao
import com.example.farmdirectoryupgraded.data.FarmWebSocketService
import com.example.farmdirectoryupgraded.data.GPSCoordinates
import com.example.farmdirectoryupgraded.data.HealthAlert
import com.example.farmdirectoryupgraded.data.HealthStatus
import com.example.farmdirectoryupgraded.data.LocationBroadcast
import com.example.farmdirectoryupgraded.data.WorkerInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FarmerViewModel(
    private val farmerDao: FarmerDao,
    private val webSocketService: FarmWebSocketService = FarmWebSocketService.getInstance()
) : ViewModel() {

    private val TAG = "FarmerViewModel"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow("All")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    // WebSocket connection state
    val isConnected: StateFlow<Boolean> = webSocketService.isConnected

    // Real-time location updates
    private val _recentLocationUpdate = MutableStateFlow<LocationBroadcast?>(null)
    val recentLocationUpdate: StateFlow<LocationBroadcast?> = _recentLocationUpdate.asStateFlow()

    // Health alerts
    private val _healthAlerts = MutableSharedFlow<HealthAlert>()
    val healthAlerts: SharedFlow<HealthAlert> = _healthAlerts.asSharedFlow()

    // Critical alerts
    private val _criticalAlerts = MutableSharedFlow<HealthAlert>()
    val criticalAlerts: SharedFlow<HealthAlert> = _criticalAlerts.asSharedFlow()

    // Worker presence
    val activeWorkers: StateFlow<List<WorkerInfo>> = webSocketService.workerPresence

    init {
        // Collect WebSocket events
        collectWebSocketEvents()
    }

    val farmers: StateFlow<List<Farmer>> = combine(
        _searchQuery,
        _selectedType
    ) { query, type ->
        Pair(query, type)
    }.flatMapLatest { (query, type) ->
        when {
            query.isNotEmpty() -> farmerDao.searchFarmers(query)
            type != "All" -> farmerDao.getFarmersByType(type)
            else -> farmerDao.getAllFarmers()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteFarmers: StateFlow<List<Farmer>> = farmerDao.getFavoriteFarmers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedType(type: String) {
        _selectedType.value = type
    }

    fun toggleFavorite(farmer: Farmer) {
        viewModelScope.launch {
            farmerDao.updateFarmer(farmer.copy(isFavorite = !farmer.isFavorite))
        }
    }

    fun addFarmer(farmer: Farmer) {
        viewModelScope.launch {
            farmerDao.insertFarmer(farmer)
        }
    }

    fun deleteFarmer(farmer: Farmer) {
        viewModelScope.launch {
            farmerDao.deleteFarmer(farmer)
        }
    }

    // ========================================================================
    // WEBSOCKET INTEGRATION
    // ========================================================================

    /**
     * Collect WebSocket events and update state
     */
    private fun collectWebSocketEvents() {
        // Location updates
        viewModelScope.launch {
            webSocketService.locationUpdates.collect { locationUpdate ->
                _recentLocationUpdate.value = locationUpdate
                Log.d(TAG, "Location updated: ${locationUpdate.entityId}")

                // TODO: Update local database with new location
                // This would sync the real-time update to the local farmer record
            }
        }

        // Health alerts
        viewModelScope.launch {
            webSocketService.healthAlerts.collect { alert ->
                _healthAlerts.emit(alert)
                Log.d(TAG, "Health alert: ${alert.entityId}")
            }
        }

        // Critical alerts
        viewModelScope.launch {
            webSocketService.criticalAlerts.collect { alert ->
                _criticalAlerts.emit(alert)
                Log.d(TAG, "CRITICAL alert: ${alert.entityId}")
            }
        }

        // Worker joined
        viewModelScope.launch {
            webSocketService.workerJoined.collect { joined ->
                Log.d(TAG, "${joined.workerName} joined the farm")
            }
        }

        // Worker left
        viewModelScope.launch {
            webSocketService.workerLeft.collect { left ->
                Log.d(TAG, "${left.workerName} left the farm")
            }
        }
    }

    /**
     * Connect to WebSocket server
     */
    fun connectToBackend() {
        webSocketService.connect()
    }

    /**
     * Disconnect from WebSocket server
     */
    fun disconnectFromBackend() {
        webSocketService.disconnect()
    }

    /**
     * Join a farm for real-time monitoring
     */
    fun joinFarm(farmId: String, workerId: String, workerName: String) {
        webSocketService.joinFarm(farmId, workerId, workerName) { success ->
            if (success) {
                Log.d(TAG, "Successfully joined farm: $farmId")
            } else {
                Log.e(TAG, "Failed to join farm: $farmId")
            }
        }
    }

    /**
     * Leave current farm
     */
    fun leaveFarm() {
        webSocketService.leaveFarm()
    }

    /**
     * Update farmer location (GPS coordinates)
     */
    fun updateFarmerLocation(farmerId: Int, latitude: Double, longitude: Double, workerId: String) {
        val location = GPSCoordinates(
            latitude = latitude,
            longitude = longitude,
            timestamp = java.util.Date()
        )

        webSocketService.updateLocation(
            entityId = farmerId.toString(),
            location = location,
            workerId = workerId
        )

        // Also update local database
        viewModelScope.launch {
            val farmer = farmers.value.find { it.id == farmerId }
            farmer?.let {
                farmerDao.updateFarmer(
                    it.copy(
                        latitude = latitude,
                        longitude = longitude,
                        lastLocationUpdate = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    /**
     * Update farmer health status
     */
    fun updateFarmerHealth(
        farmerId: Int,
        healthStatus: HealthStatus,
        healthNotes: String,
        workerId: String
    ) {
        webSocketService.updateHealth(
            entityId = farmerId.toString(),
            healthStatus = healthStatus,
            healthNotes = healthNotes,
            workerId = workerId
        )

        // Also update local database
        viewModelScope.launch {
            val farmer = farmers.value.find { it.id == farmerId }
            farmer?.let {
                farmerDao.updateFarmer(
                    it.copy(
                        healthStatus = healthStatus.name,
                        healthNotes = healthNotes
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }
}

class FarmerViewModelFactory(private val farmerDao: FarmerDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FarmerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FarmerViewModel(farmerDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
