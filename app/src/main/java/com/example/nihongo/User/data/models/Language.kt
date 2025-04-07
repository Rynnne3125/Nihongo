package com.example.nihongo.User.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class Language : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString() // Chuyển UUID thành String
    var name: String = ""
    var code: String = ""
}
