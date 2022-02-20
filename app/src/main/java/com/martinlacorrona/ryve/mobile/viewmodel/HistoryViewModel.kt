package com.martinlacorrona.ryve.mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.martinlacorrona.ryve.mobile.model.HistoryStationServiceModel
import com.martinlacorrona.ryve.mobile.repository.StationServiceRepository
import com.martinlacorrona.ryve.mobile.repository.UserRepository
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val stationServiceRepository: StationServiceRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    val TAG = "HistoryViewModel"

    val stationServiceId = MutableLiveData<Long>()
    val fuelTypeId = MutableLiveData<Long>()

    val mode = MutableLiveData<Byte>()

    //Resource de history station service
    private val _historyStationServices =
        MediatorLiveData<Resource<List<HistoryStationServiceModel>>>()
    val historyStationServices: LiveData<Resource<List<HistoryStationServiceModel>>> =
        _historyStationServices

    init {
        //Cuando recibe o el stationServiceId intenta buscarlas
        _historyStationServices.addSource(stationServiceId) {
            getStationServiceHistoryByStationServiceIdAndFuelTypeId()
        }
    }

    fun getUserFavouriteFuelId(): Long {
        return userRepository.loadUserPreferences().favouriteFuel.id
    }

    /**
     * Busca las historicas de las estaciones cuando se tenemos stationServiceId y fuelTypeId
     */
    private fun getStationServiceHistoryByStationServiceIdAndFuelTypeId() {
        stationServiceRepository
            .getStationServiceHistoryByStationServiceIdAndFuelTypeId(
                stationServiceId.value!!,
                fuelTypeId.value!!, _historyStationServices
            )
    }
}