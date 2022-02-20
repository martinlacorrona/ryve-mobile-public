package com.martinlacorrona.ryve.mobile.notification

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.app.ObjectBox
import com.martinlacorrona.ryve.mobile.app.Properties.CHANNEL_ID
import com.martinlacorrona.ryve.mobile.entity.NotificationEntity
import com.martinlacorrona.ryve.mobile.entity.NotificationEntity_
import com.martinlacorrona.ryve.mobile.view.HistoryActivity
import com.martinlacorrona.ryve.mobile.view.HistoryActivity.Companion.HISTORY_ACTIVITY_FUEL_MODE_NOTIFICATION
import io.objectbox.kotlin.boxFor
import java.util.*


class AppFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "AppFirebaseMessagingService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.from)
        Log.d(TAG, "Data: " + remoteMessage.data)

        //DATOS RECIBIDOS
        val data = remoteMessage.data

        var notificationEntity = NotificationEntity().apply {
            title = data["title"]
            body = data["body"]
            date = Date()
            stationServiceId = data["stationServiceId"]?.toLong() ?: 0
        }

        //INTENT
        val resultIntent = Intent(this, HistoryActivity::class.java).apply {
            putExtra(
                HistoryActivity.HISTORY_ACTIVITY_STATION_SERVICE_ID,
                notificationEntity.stationServiceId
            )
            putExtra(
                HistoryActivity.HISTORY_ACTIVITY_FUEL_MODE,
                HISTORY_ACTIVITY_FUEL_MODE_NOTIFICATION
            )
        }
        // Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(
                notificationEntity.stationServiceId.toInt(),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        var notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(resultPendingIntent)
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setContentTitle(notificationEntity.title)
            .setContentText(notificationEntity.body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        with(NotificationManagerCompat.from(this)) {
            val notificationsBox = ObjectBox.boxStore.boxFor<NotificationEntity>()
            notify(notificationsBox.put(notificationEntity).toInt(), notification)
            if (notificationsBox.all.size > 20) {
                notificationsBox.query().order(NotificationEntity_.id).build().findFirst()?.let {
                    notificationsBox.remove(it)
                }
            }
        }
    }
}