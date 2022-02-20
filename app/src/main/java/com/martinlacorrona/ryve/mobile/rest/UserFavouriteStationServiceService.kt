package com.martinlacorrona.ryve.mobile.rest

import com.martinlacorrona.ryve.mobile.model.FavouriteStationServiceModel
import retrofit2.Response
import retrofit2.http.*

interface UserFavouriteStationServiceService {
    @GET("v1/userfavouritestation")
    suspend fun getFavouriteStations(
        @Header("Authorization") auth: String? = null,
    ): Response<List<FavouriteStationServiceModel>>

    @POST("v1/userfavouritestation")
    suspend fun addFavouriteStation(
        @Header("Authorization") auth: String? = null,
        @Query("idStation") idStation: Long,
    ): Response<List<FavouriteStationServiceModel>>


    @DELETE("v1/userfavouritestation")
    suspend fun removeFavouriteStation(
        @Header("Authorization") auth: String? = null,
        @Query("idStation") idStation: Long,
    ): Response<List<FavouriteStationServiceModel>>
}