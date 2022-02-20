package com.martinlacorrona.ryve.mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.martinlacorrona.ryve.mobile.model.FuelTypeModel
import com.martinlacorrona.ryve.mobile.model.UserPreferencesModel
import com.martinlacorrona.ryve.mobile.repository.StationServiceRepository
import com.martinlacorrona.ryve.mobile.repository.UserRepository
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    stationServiceRepository: StationServiceRepository,
) : BaseViewModel() {

    val TAG = "PreferencesViewModel"

    val favouriteFuel = MutableLiveData<FuelTypeModel>()
    val kmRange = MutableLiveData<String>()
    val carname = MutableLiveData<String>()
    val carcolor = MutableLiveData<String>()

    var mode = MediatorLiveData<PreferencesMode>().apply { value = PreferencesMode.CREATION }

    private val _fuelTypes = stationServiceRepository.updateFuelTypes()
    val fuelTypes: LiveData<Resource<List<FuelTypeModel>>> = _fuelTypes

    private val _userPreferences = MediatorLiveData<Resource<UserPreferencesModel>>()
    val userPreferences: LiveData<Resource<UserPreferencesModel>> = _userPreferences

    //Guardamos aqui el favorito en un inicio si esta en modo update
    private lateinit var fuelAtStart: FuelTypeModel

    init {
        _fuelTypes.addSource(mode) {
            if (it == PreferencesMode.UPDATE) {
                userRepository.loadUserPreferences().let {
                    kmRange.value = it.kmRange.toString()
                    carname.value = it.carname!!
                    carcolor.value = it.carcolor!!
                    favouriteFuel.value = it.favouriteFuel
                    fuelAtStart = it.favouriteFuel
                }
            }
        }
    }

    fun updateUserPreferences() {
        userRepository.updateUserPreferences(
            UserPreferencesModel(
                carcolor = carcolor.value,
                carname = carname.value,
                kmRange = kmRange.value?.toDouble(),
                favouriteFuel = favouriteFuel.value!!
            ), _userPreferences
        )
    }

    /**
     * Funcion para comprobar si es necesario actualizar de nuevo, solo ocurrira esto
     * cuando etemos en modo update el favourite fuel sea distinto del que estaba al principio
     */
    fun isNeedToUpdateAgain(): Boolean =
        mode.value == PreferencesMode.UPDATE && favouriteFuel.value != fuelAtStart

    enum class PreferencesMode {
        CREATION, UPDATE
    }
}