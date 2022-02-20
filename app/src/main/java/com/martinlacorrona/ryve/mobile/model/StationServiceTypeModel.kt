package com.martinlacorrona.ryve.mobile.model

import com.martinlacorrona.ryve.mobile.entity.StationServiceTypeEntity

data class StationServiceTypeModel (
    var id: Long = 0,
    var name: String
) {
    fun convertToEntity() : StationServiceTypeEntity {
        return StationServiceTypeEntity().also {
            it.id = id
            it.name = name
        }
    }
}