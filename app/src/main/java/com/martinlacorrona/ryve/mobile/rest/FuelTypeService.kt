package com.martinlacorrona.ryve.mobile.rest

import com.martinlacorrona.ryve.mobile.model.FuelTypeModel
import com.martinlacorrona.ryve.mobile.model.HistoryStationServiceModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FuelTypeService {
    @GET("v1/fueltype")
    suspend fun getFuels(
        @Header("Authorization") auth: String? = null,
    ): Response<List<FuelTypeModel>>
}