package com.martinlacorrona.ryve.mobile.viewmodel.principal

import androidx.lifecycle.MutableLiveData
import com.martinlacorrona.ryve.mobile.entity.NotificationEntity
import com.martinlacorrona.ryve.mobile.repository.NotificationRepository
import com.martinlacorrona.ryve.mobile.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.objectbox.android.ObjectBoxLiveData
import javax.inject.Inject

@HiltViewModel
class AlertViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : BaseViewModel() {

    private val _notificationList = notificationRepository.getNotificationsQueryObserved()
    val notificationList: ObjectBoxLiveData<NotificationEntity> = _notificationList

    var numberOfNotifications = MutableLiveData<Int>().apply { value = 0 }

    fun deleteNotification(notificationId: Long) {
        notificationRepository.deleteNotificationById(notificationId)
    }

    fun markNotificationAsRead(notificationId: Long) {
        notificationRepository.markNotificationAsRead(notificationId)
    }

    fun markAllNotificationAsRead() {
        notificationRepository.markAllNotificationAsRead()
    }
}