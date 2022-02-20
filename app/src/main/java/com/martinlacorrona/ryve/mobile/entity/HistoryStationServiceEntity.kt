package com.martinlacorrona.ryve.mobile.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.util.*

@Entity
class HistoryStationServiceEntity {
    @Id(assignable = true)  var id: Long = 0
    var datetime: Date = Date()
    var price: Double = 1.0
    var stationServiceName: String = ""
    var idCCAA: Int? = -1

    lateinit var stationService: ToOne<StationServiceEntity>
    lateinit var fuelType: ToOne<FuelTypeEntity>

    fun getPriceString() = price.toString()
}