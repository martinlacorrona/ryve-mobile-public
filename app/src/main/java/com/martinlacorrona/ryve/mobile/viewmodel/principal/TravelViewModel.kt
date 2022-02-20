package com.martinlacorrona.ryve.mobile.viewmodel.principal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.martinlacorrona.ryve.mobile.model.DirectionsModel
import com.martinlacorrona.ryve.mobile.model.UserModel
import com.martinlacorrona.ryve.mobile.repository.DirectionsRepository
import com.martinlacorrona.ryve.mobile.repository.UserRepository
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TravelViewModel @Inject constructor(
    userRepository: UserRepository,
    private val directionsRepository: DirectionsRepository
) : BaseViewModel() {

    val origin = MutableLiveData<String>()
    val originLatLng = MutableLiveData<LatLng>()
    val destination = MutableLiveData<String>()
    val destinationLatLng = MutableLiveData<LatLng>()
    val avoidTolls = MutableLiveData<Boolean>().apply { value = false }

    val maxDistance = MutableLiveData<String>().apply {
        //Cargamos la por defecto si la tiene, si no 200km
        userRepository.loadUserPreferences().kmRange?.let {
            value = it.toString()
        } ?: run {
            value = 200.0.toString()
        }
    }
    val percentageLoaded = MutableLiveData<String>().apply { value = 100.0.toString() }

    val maxPrice = MutableLiveData<String>().apply { value = 2.0.toString() }

    //Resource de Directions
    private val _directionsResource = MutableLiveData<Resource<DirectionsModel>>()
    val directionsResource: LiveData<Resource<DirectionsModel>> = _directionsResource

    fun loadDirections() {
        directionsRepository.requestRouteByOriginDestinationAndAvoidTolls(
            originLatLng.value?.formatted(),
            destinationLatLng.value?.formatted(),
            avoidTolls.value,
            _directionsResource
        )
    }

    private fun LatLng.formatted() = "$latitude,$longitude"
}