package com.martinlacorrona.ryve.mobile.model

import com.martinlacorrona.ryve.mobile.entity.StationServiceEntity
import java.util.*

data class StationServiceModel(
    var id: Long = 0,

    var name: String,

    var longitude: Double,
    var latitude: Double,

    var schedule: String,

    var postalCode: String?,
    var address: String?,

    var town: String?,
    var municipality: String?,
    var district: String?,

    var idMunicipality: Int?,
    var idDistrict: Int?,
    var idCCAA: Int?,
    var idStationApi: Long?,

    var stationServiceType: StationServiceTypeModel,
) {
    fun convertToEntity(): StationServiceEntity {
        return StationServiceEntity().also {
            it.id = id
            it.name = name
            it.longitude = longitude
            it.latitude = latitude
            it.schedule = schedule
            it.postalCode = postalCode
            it.address = address
            it.town = town
            it.municipality = municipality
            it.idMunicipality = idMunicipality
            it.idDistrict = idDistrict
            it.idCCAA = idCCAA
            it.idStationApi = idStationApi
            it.stationServiceType.targetId = stationServiceType.id
        }
    }
}