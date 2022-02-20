package com.martinlacorrona.ryve.mobile.repository

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.martinlacorrona.ryve.mobile.model.DirectionsModel
import com.martinlacorrona.ryve.mobile.model.ErrorModel
import com.martinlacorrona.ryve.mobile.rest.DirectionsService
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DirectionsRepository @Inject constructor(
    private val directionsService: DirectionsService,
    private val gson: Gson
) {

    private val TAG = "DirectionsRepository"

    fun requestRouteByOriginDestinationAndAvoidTolls(
        origin: String?,
        destination: String?,
        avoid: Boolean?,
        result: MutableLiveData<Resource<DirectionsModel>>
    ) {
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = directionsService.requestRouteByOriginDestinationAndAvoidTolls(
                    origin = origin,
                    destination = destination,
                    avoid = avoid
                )
                withContext(Dispatchers.Main) {
                    if (response.code() == 200) {
                        result.value = Resource.success(response.body(), response.code())
                    } else { //error convertido a objeto
                        val errorModel: ErrorModel = gson.fromJson(
                            response.errorBody()?.string(),
                            object : TypeToken<ErrorModel>() {}.type
                        )
                        result.value = Resource.error(errorModel.message, code = response.code())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
    }
}