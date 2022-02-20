package com.martinlacorrona.ryve.mobile.repository

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.martinlacorrona.ryve.mobile.app.ObjectBox
import com.martinlacorrona.ryve.mobile.entity.*
import com.martinlacorrona.ryve.mobile.model.ErrorModel
import com.martinlacorrona.ryve.mobile.model.FavouriteStationServiceModel
import com.martinlacorrona.ryve.mobile.model.UserModel
import com.martinlacorrona.ryve.mobile.model.UserPreferencesModel
import com.martinlacorrona.ryve.mobile.rest.UserFavouriteStationServiceService
import com.martinlacorrona.ryve.mobile.rest.UserPreferencesService
import com.martinlacorrona.ryve.mobile.rest.UserService
import com.martinlacorrona.ryve.mobile.rest.model.UserRestModel
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userService: UserService,
    private val userPreferencesService: UserPreferencesService,
    private val userFavouriteStationServiceService: UserFavouriteStationServiceService,
    private val gson: Gson
) {

    private val TAG = "UserRepository"

    private val userBox = ObjectBox.boxStore.boxFor<UserEntity>()
    private val userPreferencesBox = ObjectBox.boxStore.boxFor<UserPreferencesEntity>()
    private val favouriteStationServiceBox = ObjectBox.boxStore.boxFor<FavouriteStationServiceEntity>()

    fun login(
        email: String?,
        password: String?,
        liveData: MutableLiveData<Resource<UserModel>>,
    ) {
        liveData.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userService.getUser(Credentials.basic(email!!, password!!))
                withContext(Dispatchers.Main) {
                    liveData.value = Resource.success(response.body(), response.code())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    liveData.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun register(
        userRestModel: UserRestModel,
        liveData: MutableLiveData<Resource<UserModel>>,
    ) {
        liveData.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userService.register(userRegister = userRestModel)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        userBox.removeAll()
                        response.body()?.let { userBox.put(it.convertToEntity())}
                        liveData.value = Resource.success(response.body(), response.code())
                    } else { //error convertido a objeto
                        val errorModel: ErrorModel = gson.fromJson(response.errorBody()?.string(), object : TypeToken<ErrorModel>() {}.type)
                        liveData.value = Resource.error(errorModel.message, code = response.code())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    liveData.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun updateUser(
        userRestModel: UserRestModel,
        liveData: MutableLiveData<Resource<UserModel>>,
    ) {
        liveData.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userService.updateUser(userModel = userRestModel)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        userBox.removeAll()
                        response.body()?.let { userBox.put(it.convertToEntity())}
                        liveData.value = Resource.success(response.body(), response.code())
                    } else { //error convertido a objeto
                        val errorModel: ErrorModel = gson.fromJson(response.errorBody()?.string(), object : TypeToken<ErrorModel>() {}.type)
                        liveData.value = Resource.error(errorModel.message, code = response.code())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    liveData.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun downloadUser(): MediatorLiveData<Resource<UserModel>> {
        val result = MediatorLiveData<Resource<UserModel>>()
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userService.getUser()
                //Guardar en local
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        userBox.removeAll()
                        response.body()?.let { userBox.put(it.convertToEntity())}
                    }
                    result.value = Resource.success(response.body(), response.code())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
        return result
    }

    fun downloadUserPreferences(result: MediatorLiveData<Resource<UserPreferencesModel>>) {
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userPreferencesService.getUserPreferences()
                //Guardar en local
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        userPreferencesBox.removeAll()
                        response.body()?.let { userPreferencesBox.put(it.convertToEntity())}
                    }
                    Log.d(TAG, "userPreferencesBox: ${userPreferencesBox.all}")
                    result.value = Resource.success(response.body(), response.code())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun downloadUserFavouriteStation(result: MediatorLiveData<Resource<List<FavouriteStationServiceModel>>>) {
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userFavouriteStationServiceService.getFavouriteStations()
                //Guardar en local
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        favouriteStationServiceBox.removeAll()
                        favouriteStationServiceBox.put(response.body()?.map { it.convertToEntity() })
                    }
                    Log.d(TAG, "favouriteStationServiceBox: ${favouriteStationServiceBox.all}")
                    result.value = Resource.success(response.body(), response.code())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun updateUserPreferences(userPreferencesModel: UserPreferencesModel,
                              result: MediatorLiveData<Resource<UserPreferencesModel>>) {
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userPreferencesService
                    .updateUserPreferences(userPreferencesModel = userPreferencesModel)
                //Guardar en local
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        userPreferencesBox.removeAll()
                        response.body()?.let { userPreferencesBox.put(it.convertToEntity())}
                        Log.d(TAG, "userPreferencesBox: ${userPreferencesBox.all}")
                        result.value = Resource.success(response.body(), response.code())
                    } else { //error convertido a objeto
                        val errorModel: ErrorModel = gson.fromJson(response.errorBody()?.string(), object : TypeToken<ErrorModel>() {}.type)
                        result.value = Resource.error(errorModel.message, code = response.code())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun addFavouriteStation(stationServiceEntity: StationServiceEntity,
                            result: MediatorLiveData<Resource<List<FavouriteStationServiceEntity>>>) {
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userFavouriteStationServiceService
                    .addFavouriteStation(idStation = stationServiceEntity.id)
                //Guardar en local
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        favouriteStationServiceBox.removeAll()
                        favouriteStationServiceBox.put(response.body()?.map { it.convertToEntity() })
                    }
                    Log.d(TAG, "favouriteStationServiceBox: ${favouriteStationServiceBox.all}")
                    result.value = Resource.success(favouriteStationServiceBox.all, response.code())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun removeFavouriteStation(stationServiceId: Long,
                            result: MediatorLiveData<Resource<List<FavouriteStationServiceEntity>>>) {
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userFavouriteStationServiceService
                    .removeFavouriteStation(idStation = stationServiceId)
                //Guardar en local
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        favouriteStationServiceBox.removeAll()
                        favouriteStationServiceBox.put(response.body()?.map { it.convertToEntity() })
                    }
                    Log.d(TAG, "favouriteStationServiceBox: ${favouriteStationServiceBox.all}")
                    result.value = Resource.success(favouriteStationServiceBox.all, response.code())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun loadUserPreferences(): UserPreferencesModel {
        return userPreferencesBox.all[0].convertToModel()
    }

    fun getFavouriteStationQueryObserved() : ObjectBoxLiveData<FavouriteStationServiceEntity> {
        return ObjectBoxLiveData(favouriteStationServiceBox.query().order(
            FavouriteStationServiceEntity_.price).build())
    }

    fun isStationFavouriteByStationServiceId(stationServiceId: Long) : Boolean {
        return favouriteStationServiceBox[stationServiceId] != null
    }

    fun getUserEntity(): MutableLiveData<UserEntity> {
        return MutableLiveData<UserEntity>().apply { value = userBox.all[0] }
    }
}