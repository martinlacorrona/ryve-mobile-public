package com.martinlacorrona.ryve.mobile.viewmodel.principal

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.martinlacorrona.ryve.mobile.entity.HistoryStationServiceEntity
import com.martinlacorrona.ryve.mobile.model.FilterModel
import com.martinlacorrona.ryve.mobile.repository.StationServiceRepository
import com.martinlacorrona.ryve.mobile.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stationServiceRepository: StationServiceRepository
) : BaseViewModel() {

    val TAG = "HomeViewModel"

    val filterModel = FilterModel()

    val stationServiceList = MediatorLiveData<PagedList<HistoryStationServiceEntity>>()
    private var sourceStationServiceList: LiveData<PagedList<HistoryStationServiceEntity>> =
        stationServiceRepository.getHistoryStationServiceByFilter(filterModel = filterModel, pageSize = 20)

    init {
        stationServiceList.addSource(sourceStationServiceList) {
            Log.d(TAG, "observado sourceStationServiceList")
            stationServiceList.value = it
        }
    }

    fun queryByFilterAgain() {
        Log.d(TAG, "queryByFilterAgain")
        stationServiceList.removeSource(sourceStationServiceList)
        sourceStationServiceList = stationServiceRepository
            .getHistoryStationServiceByFilter(filterModel = filterModel, pageSize = 20)
        stationServiceList.addSource(sourceStationServiceList) {
            stationServiceList.value = it
        }
    }

    fun queryByName(nameQuery: String) {
        Log.d(TAG, "queryByName $nameQuery")
        stationServiceList.removeSource(sourceStationServiceList)
        sourceStationServiceList = stationServiceRepository
            .getHistoryStationServiceByName(nameQuery, filterModel = filterModel, pageSize = 20)
        stationServiceList.addSource(sourceStationServiceList) {
            stationServiceList.value = it
        }
    }

    fun resetQueryByName() {
        Log.d(TAG, "resetQueryByName")
        stationServiceList.removeSource(sourceStationServiceList)
        sourceStationServiceList = stationServiceRepository
            .getHistoryStationServiceByFilter(filterModel = filterModel, pageSize = 20)
        stationServiceList.addSource(sourceStationServiceList) {
            stationServiceList.value = it
        }
    }
}