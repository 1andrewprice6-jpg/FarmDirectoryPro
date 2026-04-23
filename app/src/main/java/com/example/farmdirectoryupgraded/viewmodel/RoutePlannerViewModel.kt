package com.example.farmdirectoryupgraded.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

data class LatLng(val latitude: Double, val longitude: Double)

data class Stop(
    val id: Int,
    val label: String,
    val location: LatLng
)

data class RoutePlan(
    val origin: LatLng?,
    val stops: List<Stop>,
    val optimizedStops: List<Stop> = emptyList(),
    val totalDistance: Double = 0.0
)

class RoutePlannerViewModel : ViewModel() {

    private val _routePlan = MutableStateFlow(RoutePlan(null, emptyList()))
    val routePlan = _routePlan.asStateFlow()

    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing = _isOptimizing.asStateFlow()

    fun setOrigin(location: LatLng) {
        _routePlan.value = _routePlan.value.copy(origin = location)
    }

    fun addStop(stop: Stop) {
        val currentStops = _routePlan.value.stops.toMutableList()
        if (currentStops.none { it.id == stop.id }) {
            currentStops.add(stop)
            _routePlan.value = _routePlan.value.copy(stops = currentStops)
        }
    }

    fun removeStop(stopId: Int) {
        val currentStops = _routePlan.value.stops.filter { it.id != stopId }
        _routePlan.value = _routePlan.value.copy(stops = currentStops)
    }

    fun optimizeRoute() {
        val plan = _routePlan.value
        val origin = plan.origin ?: return
        val stops = plan.stops
        if (stops.isEmpty()) return

        _isOptimizing.value = true
        viewModelScope.launch {
            val remaining = stops.toMutableList()
            val optimized = mutableListOf<Stop>()
            var currentPos = origin
            var totalDist = 0.0

            while (remaining.isNotEmpty()) {
                var nearestIndex = -1
                var minDist = Double.MAX_VALUE

                for (i in remaining.indices) {
                    val dist = calculateDistance(currentPos, remaining[i].location)
                    if (dist < minDist) {
                        minDist = dist
                        nearestIndex = i
                    }
                }

                if (nearestIndex != -1) {
                    val nearest = remaining.removeAt(nearestIndex)
                    optimized.add(nearest)
                    totalDist += minDist
                    currentPos = nearest.location
                }
            }

            _routePlan.value = _routePlan.value.copy(
                optimizedStops = optimized,
                totalDistance = totalDist
            )
            _isOptimizing.value = false
        }
    }

    private fun calculateDistance(p1: LatLng, p2: LatLng): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
