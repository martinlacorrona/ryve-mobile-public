package com.martinlacorrona.ryve.mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.martinlacorrona.ryve.mobile.entity.FavouriteStationServiceEntity
import com.martinlacorrona.ryve.mobile.entity.HistoryStationServiceEntity
import com.martinlacorrona.ryve.mobile.entity.StationServiceEntity
import com.martinlacorrona.ryve.mobile.entity.SubscribeNotificationEntity
import com.martinlacorrona.ryve.mobile.repository.NotificationRepository
import com.martinlacorrona.ryve.mobile.repository.StationServiceRepository
import com.martinlacorrona.ryve.mobile.repository.UserRepository
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val stationServiceRepository: StationServiceRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : BaseViewModel() {

    val TAG = "DetailViewModel"

    val stationServiceId = MutableLiveData<Long>()

    val fuelId = MutableLiveData<Long>()
    val fuelName = MutableLiveData<String>()
    val fuelPrice = MutableLiveData<String>()

    val stationService = MediatorLiveData<StationServiceEntity>()

    val isFavourite = MediatorLiveData<Boolean>()

    private val _addFavouriteResource =
        MediatorLiveData<Resource<List<FavouriteStationServiceEntity>>>()
    val addFavouriteResource: LiveData<Resource<List<FavouriteStationServiceEntity>>> =
        _addFavouriteResource

    private val _removeFavouriteResource =
        MediatorLiveData<Resource<List<FavouriteStationServiceEntity>>>()
    val removeFavouriteResource: LiveData<Resource<List<FavouriteStationServiceEntity>>> =
        _removeFavouriteResource

    private val _addAlertResource = MediatorLiveData<Resource<List<SubscribeNotificationEntity>>>()
    val addAlertResource: LiveData<Resource<List<SubscribeNotificationEntity>>> = _addAlertResource

    init {
        //Carga datos de la estacion de la base de datos
        stationService.addSource(stationServiceId) {
            stationServiceId.value?.let {
                stationServiceRepository.getStationService(it).let { returnedStationService ->
                    stationService.value = returnedStationService
                }
            }
        }

        //Carga si es favorito o no para que el boton muestre un texto o otro
        isFavourite.addSource(stationServiceId) {
            stationServiceId.value?.let {
                isFavourite.value = userRepository.isStationFavouriteByStationServiceId(it)
            }
        }
    }

    fun addFavourite() {
        stationService.value?.let {
            userRepository.addFavouriteStation(it, _addFavouriteResource)
        }
    }

    fun deleteFavourite() {
        stationService.value?.let {
            userRepository.removeFavouriteStation(it.id, _removeFavouriteResource)
        }
    }

    fun addAlert(days: Int) {
        stationService.value?.let {
            notificationRepository.subscribeNotification(
                it.id,
                fuelId.value,
                days,
                _addAlertResource
            )
        }
    }

    fun getHistoryStationServiceEntity(): HistoryStationServiceEntity? {
        return stationServiceId.value?.let { stationServiceRepository.getHistoryStationService(it) }
    }
}