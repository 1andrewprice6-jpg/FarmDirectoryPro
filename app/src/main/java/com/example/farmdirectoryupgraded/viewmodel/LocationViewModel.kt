package com.example.farmdirectoryupgraded.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.data.Farmer
import com.example.farmdirectoryupgraded.data.FarmerDao
import com.example.farmdirectoryupgraded.data.Employee
import com.example.farmdirectoryupgraded.data.EmployeeDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * ViewModel for location, GPS, and route optimization operations
 *
 * Handles:
 * - GPS coordinate tracking
 * - Route optimization using nearest neighbor algorithm
 * - Farm reconciliation with GPS-based matching
 * - Distance calculations
 */
class LocationViewModel(
    private val farmerDao: FarmerDao,
    private val employeeDao: EmployeeDao
) : ViewModel() {

    companion object {
        private const val TAG = "LocationViewModel"
        private const val EARTH_RADIUS_KM = 6371.0
    }

    data class OptimizedRoute(
        val farmers: List<Farmer>,
        val totalDistance: Double,
        val estimatedTime: Long
    )

    data class ReconcileResult(
        val farmer: Farmer,
        val distance: Double,
        val confidence: Double
    )

    // State management
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _optimizedRoute = MutableStateFlow<OptimizedRoute?>(null)
    val optimizedRoute = _optimizedRoute.asStateFlow()

    private val _reconcileResult = MutableStateFlow<ReconcileResult?>(null)
    val reconcileResult = _reconcileResult.asStateFlow()

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating = _isCalculating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    /**
     * Update farmer GPS location
     *
     * @param farmerId The farmer ID
     * @param latitude New latitude
     * @param longitude New longitude
     */
    fun updateFarmerLocation(farmerId: Int, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                farmerDao.updateFarmerLocation(farmerId, latitude, longitude, System.currentTimeMillis())
                Log.d(TAG, "Farmer $farmerId location updated to ($latitude, $longitude)")
            } catch (e: Exception) {
                val errorMsg = "Failed to update farmer location: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Update employee GPS location
     *
     * @param employeeId The employee ID
     * @param latitude New latitude
     * @param longitude New longitude
     */
    fun updateEmployeeLocation(employeeId: Int, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                // Create a Location object
                val location = Location("gps").apply {
                    this.latitude = latitude
                    this.longitude = longitude
                }
                _currentLocation.value = location
                Log.d(TAG, "Employee $employeeId location updated to ($latitude, $longitude)")
            } catch (e: Exception) {
                val errorMsg = "Failed to update employee location: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Get current device location (placeholder - should be implemented with LocationManager/FusedLocationProvider)
     *
     * @return Location object or null
     */
    fun getCurrentLocation(): Location? {
        return _currentLocation.value
    }

    /**
     * Optimize route using nearest neighbor algorithm
     *
     * @param currentLatitude Current starting latitude
     * @param currentLongitude Current starting longitude
     * @param farmers List of farmers to visit
     */
    fun optimizeRoute(currentLatitude: Double, currentLongitude: Double, farmers: List<Farmer>) {
        _isCalculating.value = true
        viewModelScope.launch {
            try {
                // Filter farmers with GPS coordinates
                val farmersWithLocation = farmers.filter { it.latitude != null && it.longitude != null }

                if (farmersWithLocation.isEmpty()) {
                    _errorMessage.value = "No farmers with GPS coordinates available"
                    _isCalculating.value = false
                    return@launch
                }

                // Use nearest neighbor algorithm
                val optimizedList = mutableListOf<Farmer>()
                val remaining = farmersWithLocation.toMutableList()
                var currentLat = currentLatitude
                var currentLon = currentLongitude
                var totalDistance = 0.0

                while (remaining.isNotEmpty()) {
                    // Find nearest farmer
                    var nearestIndex = 0
                    var minDistance = Double.MAX_VALUE

                    for (i in remaining.indices) {
                        val farmer = remaining[i]
                        val distance = calculateHaversineDistance(
                            currentLat, currentLon,
                            farmer.latitude!!, farmer.longitude!!
                        )
                        if (distance < minDistance) {
                            minDistance = distance
                            nearestIndex = i
                        }
                    }

                    // Add nearest farmer to route
                    val nearest = remaining.removeAt(nearestIndex)
                    optimizedList.add(nearest)
                    totalDistance += minDistance
                    currentLat = nearest.latitude!!
                    currentLon = nearest.longitude!!
                }

                // Calculate estimated time (assume average 30 km/h travel speed)
                val estimatedTimeMs = (totalDistance / 30.0 * 60 * 60 * 1000).toLong()

                _optimizedRoute.value = OptimizedRoute(
                    farmers = optimizedList,
                    totalDistance = totalDistance,
                    estimatedTime = estimatedTimeMs
                )

                Log.d(TAG, "Route optimized: ${optimizedList.size} farmers, ${String.format("%.2f", totalDistance)} km")
            } catch (e: Exception) {
                val errorMsg = "Failed to optimize route: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            } finally {
                _isCalculating.value = false
            }
        }
    }

    /**
     * Reconcile current location with nearby farmers
     *
     * @param currentLatitude Current latitude
     * @param currentLongitude Current longitude
     */
    fun reconcileFarm(currentLatitude: Double, currentLongitude: Double) {
        _isCalculating.value = true
        viewModelScope.launch {
            try {
                val allFarmers = farmerDao.getAllFarmersSync()
                val farmersWithLocation = allFarmers.filter { it.latitude != null && it.longitude != null }

                // Find closest farmer
                var closestFarmer: Farmer? = null
                var minDistance = Double.MAX_VALUE

                for (farmer in farmersWithLocation) {
                    val distance = calculateHaversineDistance(
                        currentLatitude, currentLongitude,
                        farmer.latitude!!, farmer.longitude!!
                    )
                    if (distance < minDistance) {
                        minDistance = distance
                        closestFarmer = farmer
                    }
                }

                if (closestFarmer != null) {
                    // Calculate confidence based on distance (within 5km = high confidence)
                    val confidence = when {
                        minDistance < 1.0 -> 0.95
                        minDistance < 5.0 -> 0.80
                        minDistance < 10.0 -> 0.60
                        else -> 0.30
                    }

                    _reconcileResult.value = ReconcileResult(
                        farmer = closestFarmer,
                        distance = minDistance,
                        confidence = confidence
                    )

                    Log.d(TAG, "Reconciliation: ${closestFarmer.name} is ${String.format("%.2f", minDistance)} km away (confidence: ${String.format("%.1f%%", confidence * 100)})")
                } else {
                    _errorMessage.value = "No farmers with GPS coordinates found"
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to reconcile farm: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            } finally {
                _isCalculating.value = false
            }
        }
    }

    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     *
     * @param lat1 Starting latitude
     * @param lon1 Starting longitude
     * @param lat2 Ending latitude
     * @param lon2 Ending longitude
     * @return Distance in kilometers
     */
    fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Calculate centroid of multiple GPS coordinates
     *
     * @param farmers List of farmers with GPS coordinates
     * @return Pair of (latitude, longitude) for centroid
     */
    fun findCentroid(farmers: List<Farmer>): Pair<Double, Double> {
        val farmersWithLocation = farmers.filter { it.latitude != null && it.longitude != null }
        if (farmersWithLocation.isEmpty()) {
            return Pair(0.0, 0.0)
        }

        val avgLat = farmersWithLocation.map { it.latitude!! }.average()
        val avgLon = farmersWithLocation.map { it.longitude!! }.average()
        return Pair(avgLat, avgLon)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
