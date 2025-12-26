package com.example.farmdirectoryupgraded.data

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Farmer entity
 * All queries use parameterized statements to prevent SQL injection
 */
@Dao
interface FarmerDao {
    @Query("SELECT * FROM farmers ORDER BY name ASC")
    fun getAllFarmers(): Flow<List<Farmer>>

    /**
     * Paginated query for large datasets
     */
    @Query("SELECT * FROM farmers ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getFarmersPaginated(limit: Int, offset: Int): List<Farmer>

    /**
     * Get total count for pagination
     */
    @Query("SELECT COUNT(*) FROM farmers")
    suspend fun getFarmerCount(): Int

    @Query("SELECT * FROM farmers WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteFarmers(): Flow<List<Farmer>>

    /**
     * Search farmers with parameterized query to prevent SQL injection
     * Room automatically handles parameter escaping
     */
    @Query("SELECT * FROM farmers WHERE name LIKE '%' || :query || '%' OR farmName LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFarmers(query: String): Flow<List<Farmer>>

    @Query("SELECT * FROM farmers WHERE type = :type ORDER BY name ASC")
    fun getFarmersByType(type: String): Flow<List<Farmer>>

    @Query("SELECT * FROM farmers WHERE healthStatus = :status ORDER BY name ASC")
    fun getFarmersByHealthStatus(status: String): Flow<List<Farmer>>

    @Query("SELECT * FROM farmers WHERE id = :id")
    suspend fun getFarmerById(id: Int): Farmer?

    /**
     * Insert with conflict resolution
     * OnConflictStrategy.REPLACE prevents duplicate entries
     */
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

    /**
     * Optimized query using index on name
     */
    @Query("SELECT * FROM farmers WHERE name = :name LIMIT 1")
    suspend fun getFarmerByName(name: String): Farmer?
}
