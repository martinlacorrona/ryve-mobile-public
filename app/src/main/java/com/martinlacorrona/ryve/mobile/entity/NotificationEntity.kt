package com.martinlacorrona.ryve.mobile.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.text.SimpleDateFormat
import java.util.*

@Entity
class NotificationEntity {
    @Id
    var id: Long = 0
    var title: String? = ""
    var body: String? = ""
    var date: Date = Date()
    var stationServiceId: Long = 0
    var viewed: Boolean? = false

    fun dateToString(): String {
        val sd = SimpleDateFormat("dd/MM/yyyy")
        return sd.format(date)
    }
}