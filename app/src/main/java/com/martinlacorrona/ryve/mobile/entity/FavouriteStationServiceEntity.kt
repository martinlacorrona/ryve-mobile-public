package com.martinlacorrona.ryve.mobile.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.util.*

@Entity
class FavouriteStationServiceEntity {
    @Id(assignable = true) var id: Long = 0

    var name: String = ""

    var longitude: Double = 0.0
    var latitude: Double = 0.0

    var schedule: String = ""

    var postalCode: String? = null
    var address: String? = null

    var town: String? = null
    var municipality: String? = null
    var district: String? = null

    var idMunicipality: Int? = null
    var idDistrict: Int? = null
    var idCCAA: Int? = null
    var idStationApi: Long? = null

    var price: Double? = null

    lateinit var stationServiceType: ToOne<StationServiceTypeEntity>

    fun getPriceString(): String {
        price?.let { 
            return it.toString()
        }
        return "-.--"
    }
}