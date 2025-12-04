package com.example.farmdirectoryupgraded.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerDao {
    @Query("SELECT * FROM farmers ORDER BY name ASC")
    fun getAllFarmers(): Flow<List<Farmer>>

    @Query("SELECT * FROM farmers WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteFarmers(): Flow<List<Farmer>>

    @Query("SELECT * FROM farmers WHERE name LIKE '%' || :query || '%' OR farmName LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFarmers(query: String): Flow<List<Farmer>>

    @Query("SELECT * FROM farmers WHERE type = :type ORDER BY name ASC")
    fun getFarmersByType(type: String): Flow<List<Farmer>>

    @Query("SELECT * FROM farmers WHERE id = :id")
    suspend fun getFarmerById(id: Int): Farmer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: Farmer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmers(farmers: List<Farmer>)

    @Delete
    suspend fun deleteFarmer(farmer: Farmer)

    @Update
    suspend fun updateFarmer(farmer: Farmer)

    @Query("DELETE FROM farmers")
    suspend fun deleteAllFarmers()
}
