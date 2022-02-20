package com.martinlacorrona.ryve.mobile.entity

import com.martinlacorrona.ryve.mobile.model.UserPreferencesModel
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.util.*

@Entity
class UserPreferencesEntity {
    @Id(assignable = true)  var id: Long = 0
    var carname: String? = ""
    var carcolor: String? = ""
    var kmRange: Double? = 200.0

    lateinit var favouriteFuel: ToOne<FuelTypeEntity>

    fun convertToModel(): UserPreferencesModel {
        return UserPreferencesModel(
            id,
            carname,
            carcolor,
            kmRange,
            favouriteFuel.target.convertToModel()
        )
    }

    override fun toString(): String {
        return "UserPreferencesEntity(id=$id, carname=$carname, carcolor=$carcolor, kmRange=$kmRange, favouriteFuel=${favouriteFuel.targetId})"
    }
}