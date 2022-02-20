package com.martinlacorrona.ryve.mobile.model

import com.martinlacorrona.ryve.mobile.entity.FuelTypeEntity

data class FuelTypeModel(
    var id: Long = 0,
    var name: String,
    var unit: String,
) {
    override fun toString() = name

    fun convertToEntity() : FuelTypeEntity {
        return FuelTypeEntity().also {
            it.id = id
            it.name = name
            it.unit = unit
        }
    }
}