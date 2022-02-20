package com.martinlacorrona.ryve.mobile.rest

import com.martinlacorrona.ryve.mobile.model.UserPreferencesModel
import retrofit2.Response
import retrofit2.http.*

interface UserPreferencesService {
    @GET("v1/userpreferences")
    suspend fun getUserPreferences(
        @Header("Authorization") auth: String? = null,
    ): Response<UserPreferencesModel>

    @PUT("v1/userpreferences")
    suspend fun updateUserPreferences(
        @Header("Authorization") auth: String? = null,
        @Body userPreferencesModel: UserPreferencesModel
    ): Response<UserPreferencesModel>
}