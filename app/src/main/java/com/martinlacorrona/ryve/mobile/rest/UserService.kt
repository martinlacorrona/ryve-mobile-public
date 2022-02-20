package com.martinlacorrona.ryve.mobile.rest

import com.martinlacorrona.ryve.mobile.model.UserModel
import com.martinlacorrona.ryve.mobile.rest.model.UserRestModel
import retrofit2.Response
import retrofit2.http.*

interface UserService {
    @GET("v1/user")
    suspend fun getUser(
        @Header("Authorization") auth: String? = null
        ): Response<UserModel>

    @POST("v1/user/register")
    suspend fun register(
        @Body userRegister: UserRestModel
    ): Response<UserModel>

    @PUT("v1/user")
    suspend fun updateUser(
        @Body userModel: UserRestModel
    ): Response<UserModel>
}