package com.martinlacorrona.ryve.mobile.model

import com.martinlacorrona.ryve.mobile.entity.UserEntity
import java.util.*

data class UserModel (
    var id: Long = 0,
    var mail: String,
    var name: String,
    var surname: String,
    var regdate: Date?,
    var lastlogin: Date?
) {
    fun convertToEntity() : UserEntity {
        return UserEntity().also {
            it.id = id
            it.mail = mail
            it.name = name
            it.surname = surname
            it.regdate = regdate
            it.lastlogin = lastlogin
        }
    }
}