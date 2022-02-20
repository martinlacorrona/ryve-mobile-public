package com.martinlacorrona.ryve.mobile.model

import com.martinlacorrona.ryve.mobile.entity.UserPreferencesEntity
import java.util.*

data class UserPreferencesModel(
    var id: Long = 0,
    var carname: String? = null,
    var carcolor: String? = null,
    var kmRange: Double? = 500.0,
    var favouriteFuel: FuelTypeModel,
) {
    fun convertToEntity(): UserPreferencesEntity {
        return UserPreferencesEntity().also {
            it.id = id
            it.carname = carname
            it.carcolor = carcolor
            it.kmRange = kmRange
            it.favouriteFuel.targetId = favouriteFuel.id
        }
    }
}