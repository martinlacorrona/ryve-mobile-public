package com.martinlacorrona.ryve.mobile.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.text.SimpleDateFormat
import java.util.*

@Entity
class SubscribeNotificationEntity {
    @Id(assignable = true) var id: Long = 0

    var lastNotified: Date = Date()
    var nextNotify: Date = Date()
    var periodInDays: Int? = null
    var status: Boolean = true

    lateinit var stationService: ToOne<StationServiceEntity>
    lateinit var fuelType: ToOne<FuelTypeEntity>

    fun nextNotifyToString(): String {
        val sd = SimpleDateFormat("dd/MM/yyyy")
        return sd.format(nextNotify)
    }

    override fun toString(): String {
        return "SubscribeNotificationEntity(id=$id, lastNotified=$lastNotified, nextNotify=$nextNotify, periodInDays=$periodInDays, status=$status, stationService=$stationService, fuelType=$fuelType)"
    }
}