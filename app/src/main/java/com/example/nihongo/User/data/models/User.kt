package com.example.nihongo.User.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class User : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString() // Chuyển UUID thành String
    var username: String = ""
    var email: String = ""
    var password: String = ""
    var createdAt: String = ""
    var isVip: Boolean = false
}
