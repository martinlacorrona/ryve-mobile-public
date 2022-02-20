package com.martinlacorrona.ryve.mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.martinlacorrona.ryve.mobile.entity.SubscribeNotificationEntity
import com.martinlacorrona.ryve.mobile.repository.NotificationRepository
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import io.objectbox.android.ObjectBoxLiveData
import javax.inject.Inject

@HiltViewModel
class SubscriptionListViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : BaseViewModel() {

    private val _subscriptionList = notificationRepository.getSubscribeNotification()
    val subscriptionList: ObjectBoxLiveData<SubscribeNotificationEntity> = _subscriptionList

    private val _removeSubscriptionResource =
        MediatorLiveData<Resource<List<SubscribeNotificationEntity>>>()
    val removeSubscriptionResource: LiveData<Resource<List<SubscribeNotificationEntity>>> =
        _removeSubscriptionResource

    var numberOfSuscriptions = MutableLiveData<Int>().apply { value = 0 }

    fun unsubscribe(subscribeNotificationId: Long) {
        notificationRepository.unsubscribeNotification(
            subscribeNotificationId,
            _removeSubscriptionResource
        )
    }
}