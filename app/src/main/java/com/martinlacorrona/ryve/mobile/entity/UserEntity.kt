package com.martinlacorrona.ryve.mobile.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*

@Entity
class UserEntity {
    @Id(assignable = true) var id: Long = 0
    var mail: String = ""
    var name: String = ""
    var surname: String = ""
    var regdate: Date? = Date()
    var lastlogin: Date? = Date()
}