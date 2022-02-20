package com.martinlacorrona.ryve.mobile.rest

import com.martinlacorrona.ryve.mobile.model.HistoryStationServiceModel
import com.martinlacorrona.ryve.mobile.model.StationServiceModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface StationServiceService {
    @GET("v1/stationservice")
    suspend fun getStationServiceByFuelId(
        @Header("Authorization") auth: String? = null,
        @Query("fuelTypeId") fuelTypeId: Long,
    ): Response<List<HistoryStationServiceModel>>

    @GET("v1/stationservice/list")
    suspend fun getStationService(
        @Header("Authorization") auth: String? = null
    ): Response<List<StationServiceModel>>

    @GET("v1/stationservice/history")
    suspend fun getStationServiceHistoryByStationServiceIdAndFuelTypeId(
        @Header("Authorization") auth: String? = null,
        @Query("stationServiceId") stationServiceId: Long,
        @Query("fuelTypeId") fuelTypeId: Long,
    ): Response<List<HistoryStationServiceModel>>
}