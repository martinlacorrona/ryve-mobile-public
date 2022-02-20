package com.martinlacorrona.ryve.mobile.model

import com.martinlacorrona.ryve.mobile.entity.HistoryStationServiceEntity
import java.util.*

data class HistoryStationServiceModel(
    var id: Long = 0,
    var date: Date,
    var datetime: Date,
    var price: Double,
    var stationService: StationServiceModel,
    var fuelType: FuelTypeModel,
) {
    fun convertToEntity() : HistoryStationServiceEntity {
        return HistoryStationServiceEntity().also {
            it.id = id
            it.datetime = datetime
            it.price = price
            it.stationServiceName = stationService.name
            it.idCCAA = stationService.idCCAA
            it.stationService.targetId = stationService.id
            it.fuelType.targetId = fuelType.id
        }
    }
}