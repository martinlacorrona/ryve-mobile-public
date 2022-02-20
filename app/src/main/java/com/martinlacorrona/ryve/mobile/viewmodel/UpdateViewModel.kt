package com.martinlacorrona.ryve.mobile.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.martinlacorrona.ryve.mobile.app.Properties
import com.martinlacorrona.ryve.mobile.entity.SubscribeNotificationEntity
import com.martinlacorrona.ryve.mobile.model.*
import com.martinlacorrona.ryve.mobile.repository.NotificationRepository
import com.martinlacorrona.ryve.mobile.repository.StationServiceRepository
import com.martinlacorrona.ryve.mobile.repository.UserRepository
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val stationServiceRepository: StationServiceRepository,
    notificationRepository: NotificationRepository,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel() {

    private val _user = userRepository.downloadUser()
    val user: LiveData<Resource<UserModel>> = _user

    private val _fuelTypes = stationServiceRepository.updateFuelTypes()
    val fuelTypes: LiveData<Resource<List<FuelTypeModel>>> = _fuelTypes

    private val _userPreferences = MediatorLiveData<Resource<UserPreferencesModel>>()
    var userPreferences: LiveData<Resource<UserPreferencesModel>> = _userPreferences

    private val _stationsService = MediatorLiveData<Resource<List<StationServiceModel>>>()
    var stationsService: LiveData<Resource<List<StationServiceModel>>> = _stationsService

    private val _userFavouriteStation = MediatorLiveData<Resource<List<FavouriteStationServiceModel>>>()
    val userFavouriteStation: LiveData<Resource<List<FavouriteStationServiceModel>>> = _userFavouriteStation

    private val _historyStationsService = MediatorLiveData<Resource<List<HistoryStationServiceModel>>>()
    val historyStationsService: LiveData<Resource<List<HistoryStationServiceModel>>> = _historyStationsService

    private val _subscribeNotification = MediatorLiveData<Resource<List<SubscribeNotificationEntity>>>()
    val subscribeNotification: LiveData<Resource<List<SubscribeNotificationEntity>>> = _subscribeNotification

    var forceUpdate = false

    init {
        _userPreferences.addSource(_fuelTypes) {
            if (it.status == Resource.Status.SUCCESS)
                userRepository.downloadUserPreferences(_userPreferences)
        }

        _userFavouriteStation.addSource(_fuelTypes) {
            if (it.status == Resource.Status.SUCCESS)
                userRepository.downloadUserFavouriteStation(_userFavouriteStation)
        }

        _historyStationsService.addSource(_stationsService) {
            if (it.status == Resource.Status.SUCCESS) {
                stationServiceRepository.updateHistoryStationService(
                    _historyStationsService,
                    _userPreferences.value?.data?.favouriteFuel?.id
                )
                notificationRepository
                    .getSubscribeNotificationResource(_subscribeNotification)
            }
        }

        stationServiceRepository.updateStationService(_stationsService)
    }

    fun getLoaded(): Boolean = sharedPreferences.getBoolean(Properties.LOADED, false)

    fun saveLoaded() {
        with(sharedPreferences.edit()) {
            putBoolean(Properties.LOADED, true)
            putLong(Properties.LAST_LOADED, Date().time)
            commit()
        }
    }

    fun logout() {
        with(sharedPreferences.edit()) {
            clear()
            commit()
        }
    }
}