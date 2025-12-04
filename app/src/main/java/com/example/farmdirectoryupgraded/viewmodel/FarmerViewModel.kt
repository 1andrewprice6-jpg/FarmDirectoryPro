package com.example.farmdirectoryupgraded.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.farmdirectoryupgraded.data.Farmer
import com.example.farmdirectoryupgraded.data.FarmerDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FarmerViewModel(private val farmerDao: FarmerDao) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow("All")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

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
