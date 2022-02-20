package com.martinlacorrona.ryve.mobile.repository

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.martinlacorrona.ryve.mobile.app.ObjectBox
import com.martinlacorrona.ryve.mobile.entity.*
import com.martinlacorrona.ryve.mobile.model.ErrorModel
import com.martinlacorrona.ryve.mobile.model.FuelTypeModel
import com.martinlacorrona.ryve.mobile.rest.NotificationService
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationService: NotificationService,
    private val gson: Gson
) {

    private val TAG = "NotificationRepository"

    private val subscribeNotificationBox = ObjectBox.boxStore.boxFor<SubscribeNotificationEntity>()
    private val notificationsBox = ObjectBox.boxStore.boxFor<NotificationEntity>()

    fun registerToken(
        token: String?,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificationService.registertoken(tokenFirebase = token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unregistertoken(
        token: String?,
        liveData: MutableLiveData<Resource<Void>>,
    ) {
        liveData.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = notificationService.unregistertoken(tokenFirebase = token)
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

    fun getSubscribeNotificationResource(result: MediatorLiveData<Resource<List<SubscribeNotificationEntity>>>) {
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = notificationService.getSubscribeNotification()
                //Guardar en local
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        subscribeNotificationBox.removeAll()
                        subscribeNotificationBox.put(
                            response.body()?.map { it.convertToEntity() })
                        result.value =
                            Resource.success(subscribeNotificationBox.all, response.code())
                    } else { //error convertido a objeto
                        val errorModel: ErrorModel = gson.fromJson(
                            response.errorBody()?.string(),
                            object : TypeToken<ErrorModel>() {}.type
                        )
                        result.value = Resource.error(errorModel.message, code = response.code())
                    }
                    Log.d(TAG, "subscribeNotificationBox: ${subscribeNotificationBox.all}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun subscribeNotification(
        stationServiceId: Long? = null,
        fuelTypeId: Long? = null,
        periodInDays: Int? = null,
        result: MediatorLiveData<Resource<List<SubscribeNotificationEntity>>>
    ) {
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = notificationService.subscribeNotification(
                    stationServiceId = stationServiceId,
                    fuelTypeId = fuelTypeId,
                    periodInDays = periodInDays
                )
                //Guardar en local
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        subscribeNotificationBox.removeAll()
                        subscribeNotificationBox.put(
                            response.body()?.map { it.convertToEntity() })
                        result.value =
                            Resource.success(subscribeNotificationBox.all, response.code())
                    } else { //error convertido a objeto
                        val errorModel: ErrorModel = gson.fromJson(
                            response.errorBody()?.string(),
                            object : TypeToken<ErrorModel>() {}.type
                        )
                        result.value = Resource.error(errorModel.message, code = response.code())
                    }
                    Log.d(TAG, "subscribeNotificationBox: ${subscribeNotificationBox.all}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun unsubscribeNotification(
        subscribeNotificationId: Long? = null,
        result: MediatorLiveData<Resource<List<SubscribeNotificationEntity>>>
    ) {
        result.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = notificationService.unsubscribeNotification(
                    subscribeNotificationId = subscribeNotificationId
                )
                //Guardar en local
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        subscribeNotificationBox.removeAll()
                        subscribeNotificationBox.put(
                            response.body()?.map { it.convertToEntity() })
                        result.value =
                            Resource.success(subscribeNotificationBox.all, response.code())
                    } else { //error convertido a objeto
                        val errorModel: ErrorModel = gson.fromJson(
                            response.errorBody()?.string(),
                            object : TypeToken<ErrorModel>() {}.type
                        )
                        result.value = Resource.error(errorModel.message, code = response.code())
                    }
                    Log.d(TAG, "subscribeNotificationBox: ${subscribeNotificationBox.all}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    result.value = Resource.error(e.toString(), null)
                }
            }
        }
    }

    fun getSubscribeNotification(): ObjectBoxLiveData<SubscribeNotificationEntity> {
        return ObjectBoxLiveData(subscribeNotificationBox.query()
            .order(SubscribeNotificationEntity_.id, QueryBuilder.DESCENDING)
            .build())
    }

    fun getNotifications(): List<NotificationEntity> {
        return notificationsBox.all
    }

    fun getNotificationsQueryObserved() : ObjectBoxLiveData<NotificationEntity> {
        return ObjectBoxLiveData(notificationsBox.query()
            .order(NotificationEntity_.date, QueryBuilder.DESCENDING)
            .build())
    }

    fun getUnviewedNotificationsQueryObserved() : ObjectBoxLiveData<NotificationEntity> {
        return ObjectBoxLiveData(notificationsBox.query()
            .equal(NotificationEntity_.viewed, false)
            .build())
    }

    fun deleteNotificationById(notificationId: Long) {
        notificationsBox.remove(notificationId)
    }

    fun markNotificationAsRead(notificationId: Long) {
        notificationsBox[notificationId].apply {
            viewed = true
            notificationsBox.put(this)
        }
    }

    fun markAllNotificationAsRead() {
        notificationsBox.all.forEach {
            it.viewed = true
            notificationsBox.put(it)
        }
    }
}