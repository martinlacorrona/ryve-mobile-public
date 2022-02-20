package com.martinlacorrona.ryve.mobile.entity

import com.martinlacorrona.ryve.mobile.model.FuelTypeModel
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class FuelTypeEntity {
    @Id(assignable = true)
    var id: Long = 0
    var name: String = ""
    var unit: String = ""

    fun convertToModel(): FuelTypeModel {
        return FuelTypeModel(
            id,
            name,
            unit,
        )
    }
}