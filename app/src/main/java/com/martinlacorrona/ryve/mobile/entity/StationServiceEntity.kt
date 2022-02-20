package com.martinlacorrona.ryve.mobile.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.util.*

@Entity
class StationServiceEntity {
    @Id(assignable = true)  var id: Long = 0

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

    lateinit var stationServiceType: ToOne<StationServiceTypeEntity>

    override fun toString(): String {
        return "StationServiceEntity(id=$id, name='$name', longitude=$longitude, latitude=$latitude, schedule='$schedule', postalCode=$postalCode, address=$address, town=$town, municipality=$municipality, district=$district, idMunicipality=$idMunicipality, idDistrict=$idDistrict, idCCAA=$idCCAA, idStationApi=$idStationApi, stationServiceType=$stationServiceType)"
    }
}