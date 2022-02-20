package com.martinlacorrona.ryve.mobile.model

import com.martinlacorrona.ryve.mobile.entity.FavouriteStationServiceEntity
import io.objectbox.relation.ToOne
import java.util.*

data class FavouriteStationServiceModel(
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
    var price: Double?,
    var stationServiceType: StationServiceTypeModel
) {
    fun convertToEntity(): FavouriteStationServiceEntity {
        return FavouriteStationServiceEntity().also {
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
            it.price = price
            it.stationServiceType.targetId = stationServiceType.id
        }
    }
}