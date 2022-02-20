package com.martinlacorrona.ryve.mobile.viewmodel.principal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.martinlacorrona.ryve.mobile.entity.FavouriteStationServiceEntity
import com.martinlacorrona.ryve.mobile.repository.UserRepository
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.objectbox.android.ObjectBoxLiveData
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    val TAG = "FavouritesViewModel"

    private val _stationServiceList = userRepository.getFavouriteStationQueryObserved()
    val stationServiceList: ObjectBoxLiveData<FavouriteStationServiceEntity> = _stationServiceList

    private val _removeFavouriteResource =
        MediatorLiveData<Resource<List<FavouriteStationServiceEntity>>>()
    val removeFavouriteResource: LiveData<Resource<List<FavouriteStationServiceEntity>>> =
        _removeFavouriteResource

    var numberOfFavourite = MutableLiveData<Int>().apply { value = 0 }

    fun deleteFavourite(stationServiceId: Long) {
        userRepository.removeFavouriteStation(stationServiceId, _removeFavouriteResource)
    }
}