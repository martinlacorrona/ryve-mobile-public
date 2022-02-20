package com.martinlacorrona.ryve.mobile.model

import com.martinlacorrona.ryve.mobile.entity.SubscribeNotificationEntity
import java.util.*

data class SubscribeNotificationModel(
    var id: Long = 0,

    var lastNotified: Date = Date(),
    var nextNotify: Date = Date(),
    var periodInDays: Int? = null,
    var status: Boolean = true,

    var stationService: StationServiceModel,
    var fuelType: FuelTypeModel
) {
    fun convertToEntity(): SubscribeNotificationEntity {
        return SubscribeNotificationEntity().also {
            it.id = id
            it.lastNotified = lastNotified
            it.nextNotify = nextNotify
            it.periodInDays = periodInDays
            it.status = status
            it.stationService.targetId = stationService.id
            it.fuelType.targetId = fuelType.id
        }
    }
}