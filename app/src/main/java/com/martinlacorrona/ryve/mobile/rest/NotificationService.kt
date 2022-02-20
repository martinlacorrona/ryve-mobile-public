package com.martinlacorrona.ryve.mobile.rest

import com.martinlacorrona.ryve.mobile.model.SubscribeNotificationModel
import retrofit2.Response
import retrofit2.http.*

interface NotificationService {

    @POST("v1/notification")
    suspend fun registertoken(
        @Header("Authorization") auth: String? = null,
        @Query("tokenFirebase") tokenFirebase: String? = null
    ): Response<Void>

    @DELETE("v1/notification")
    suspend fun unregistertoken(
        @Header("Authorization") auth: String? = null,
        @Query("tokenFirebase") tokenFirebase: String? = null
    ): Response<Void>

    @GET("v1/notification/subscribe")
    suspend fun getSubscribeNotification(
        @Header("Authorization") auth: String? = null
    ): Response<List<SubscribeNotificationModel>>

    @POST("v1/notification/subscribe")
    suspend fun subscribeNotification(
        @Header("Authorization") auth: String? = null,
        @Query("stationServiceId") stationServiceId: Long? = null,
        @Query("fuelTypeId") fuelTypeId: Long? = null,
        @Query("periodInDays") periodInDays: Int? = null
    ): Response<List<SubscribeNotificationModel>>

    @DELETE("v1/notification/subscribe")
    suspend fun unsubscribeNotification(
        @Header("Authorization") auth: String? = null,
        @Query("subscribeNotificationId") subscribeNotificationId: Long? = null
    ): Response<List<SubscribeNotificationModel>>
}