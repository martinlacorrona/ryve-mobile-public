package com.martinlacorrona.ryve.mobile.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.martinlacorrona.ryve.mobile.view.RouteGeneratedActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RouteGeneratedViewModel @Inject constructor(
) : BaseViewModel() {

    val TAG = "RouteGeneratedViewModel"

    var selectedStationServiceName: Array<String> = arrayOf()
    var selectedStationServiceLatLng: Array<RouteGeneratedActivity.LatLngSerializable> = arrayOf()

    var origin: LatLng? = null
    var destination: LatLng? = null
    var avoidTolls: Boolean = false

    val stopsStringList: List<String> = listOf()
    val distance = MutableLiveData<String>()
    val estimatedPrice = MutableLiveData<String>()
    val estimatedPercentageAtArrive = MutableLiveData<String>()
    val estimatedTime = MutableLiveData<String>()
}