package com.martinlacorrona.ryve.mobile.viewmodel.principal

import com.martinlacorrona.ryve.mobile.entity.HistoryStationServiceEntity
import com.martinlacorrona.ryve.mobile.repository.StationServiceRepository
import com.martinlacorrona.ryve.mobile.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private var stationServiceRepository: StationServiceRepository
) : BaseViewModel() {
    val historyStationServiceList: List<HistoryStationServiceEntity> =
        stationServiceRepository.getAllHistoryStationService()

    fun getHistoryMinPrice(): Double = stationServiceRepository.getHistoryMinPrice()

    fun getHistoryMaxPrice(): Double = stationServiceRepository.getHistoryMaxPrice()
}