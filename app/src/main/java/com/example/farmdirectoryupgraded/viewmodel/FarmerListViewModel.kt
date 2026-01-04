package com.example.farmdirectoryupgraded.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.farmdirectoryupgraded.data.Farmer
import com.example.farmdirectoryupgraded.data.FarmerDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for farmer list operations
 *
 * Handles:
 * - Farmer CRUD operations (create, read, update, delete)
 * - Farmer search and filtering
 * - Favorite farmer management
 * - Pagination support for large lists
 */
class FarmerListViewModel(private val farmerDao: FarmerDao) : ViewModel() {

    companion object {
        private const val TAG = "FarmerListViewModel"
        private const val PAGE_SIZE = 20
    }

    // State management
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType = _selectedType.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    // Paginated farmers list
    val pagedFarmers = Pager(PagingConfig(pageSize = PAGE_SIZE)) {
        farmerDao.getFarmersPaged()
    }.flow.cachedIn(viewModelScope)

    /**
     * Update search query and refresh list
     *
     * @param query The search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Filter farmers by type
     *
     * @param type The farmer type to filter by
     */
    fun filterByType(type: String?) {
        _selectedType.value = type
    }

    /**
     * Add a new farmer
     *
     * @param farmer The farmer to add
     */
    fun addFarmer(farmer: Farmer) {
        viewModelScope.launch {
            try {
                farmerDao.insertFarmer(farmer)
                _successMessage.value = "Farmer added successfully"
                Log.d(TAG, "Farmer added: ${farmer.name}")
            } catch (e: Exception) {
                val errorMsg = "Failed to add farmer: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Update an existing farmer
     *
     * @param farmer The farmer with updated data
     */
    fun updateFarmer(farmer: Farmer) {
        viewModelScope.launch {
            try {
                farmerDao.updateFarmer(farmer)
                _successMessage.value = "Farmer updated successfully"
                Log.d(TAG, "Farmer updated: ${farmer.name}")
            } catch (e: Exception) {
                val errorMsg = "Failed to update farmer: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Delete a farmer
     *
     * @param farmer The farmer to delete
     */
    fun deleteFarmer(farmer: Farmer) {
        viewModelScope.launch {
            try {
                farmerDao.deleteFarmer(farmer)
                _successMessage.value = "Farmer deleted successfully"
                Log.d(TAG, "Farmer deleted: ${farmer.name}")
            } catch (e: Exception) {
                val errorMsg = "Failed to delete farmer: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Toggle farmer favorite status
     *
     * @param farmerId The farmer ID to toggle
     * @param currentFavorite The current favorite status
     */
    fun toggleFavorite(farmerId: Int, currentFavorite: Boolean) {
        viewModelScope.launch {
            try {
                farmerDao.updateFavoriteSatus(farmerId, !currentFavorite)
                Log.d(TAG, "Farmer $farmerId favorite toggled")
            } catch (e: Exception) {
                val errorMsg = "Failed to update favorite: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
}
