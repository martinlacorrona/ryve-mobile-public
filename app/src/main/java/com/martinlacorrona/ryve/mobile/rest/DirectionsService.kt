package com.martinlacorrona.ryve.mobile.rest

import com.martinlacorrona.ryve.mobile.model.DirectionsModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface DirectionsService {
    @GET("v1/directions")
    suspend fun requestRouteByOriginDestinationAndAvoidTolls(
        @Header("Authorization") auth: String? = null,
        @Query("origin") origin: String? = null,
        @Query("destination") destination: String? = null,
        @Query("avoid") avoid: Boolean? = null,
    ): Response<DirectionsModel>
}