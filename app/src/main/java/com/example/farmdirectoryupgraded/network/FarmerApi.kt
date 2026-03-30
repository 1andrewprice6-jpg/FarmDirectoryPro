package com.example.farmdirectoryupgraded.network

import com.example.farmdirectoryupgraded.data.Farmer
import retrofit2.Response
import retrofit2.http.*

/**
 * Type-safe data contract for the Primary Data Grid
 */
interface FarmerApi {
    @GET("/api/farmers")
    suspend fun getFarmers(@Header("Authorization") token: String): Response<List<Farmer>>

    @POST("/api/farmers")
    suspend fun createFarmer(
        @Header("Authorization") token: String,
        @Body farmer: Farmer
    ): Response<Farmer>

    @PUT("/api/farmers/{id}")
    suspend fun updateFarmer(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body farmer: Farmer
    ): Response<Unit>

    @DELETE("/api/farmers/{id}")
    suspend fun deleteFarmer(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}

data class AuthRequest(val username: String, val password: String)
data class AuthResponse(val token: String, val userId: String)
