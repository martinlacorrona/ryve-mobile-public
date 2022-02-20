package com.martinlacorrona.ryve.mobile.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.martinlacorrona.ryve.mobile.entity.NotificationEntity
import com.martinlacorrona.ryve.mobile.repository.NotificationRepository
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import io.objectbox.android.ObjectBoxLiveData
import javax.inject.Inject

@HiltViewModel
class PrincipalViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val notificationRepository: NotificationRepository
) : BaseViewModel() {

    val TAG = "PrincipalViewModel"

    //Resource de unregistertoken
    private val _unregistertoken = MediatorLiveData<Resource<Void>>()
    val unregistertoken: LiveData<Resource<Void>> = _unregistertoken

    private val _unviewedNotificationList = notificationRepository.getUnviewedNotificationsQueryObserved()
    val unviewedNotificationList: ObjectBoxLiveData<NotificationEntity> = _unviewedNotificationList

    //Firebase Token
    private var token: String? = null

    init {
        getNotificationToken()
    }

    fun unregisterToken() {
        Log.d(TAG, "Borrando token guardado en las sharedPreferences: " +
                "$token")
        token?.let {
            notificationRepository.unregistertoken(it, _unregistertoken)
        } ?: run {
            _unregistertoken.value = Resource(Resource.Status.SUCCESS, null, null)
        }
    }

    fun deleteSharedPreferences() {
        with(sharedPreferences.edit()) {
            clear()
        }.apply { commit() }
    }

    private fun getNotificationToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            token = task.result
            Log.d(TAG, "Nuevo token recibido: $token")

            //Save backend
            notificationRepository.registerToken(token)
        })
    }
}