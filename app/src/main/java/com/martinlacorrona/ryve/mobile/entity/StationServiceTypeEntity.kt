package com.martinlacorrona.ryve.mobile.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class StationServiceTypeEntity {
    @Id(assignable = true) var id: Long = 0
    var name: String = ""
}