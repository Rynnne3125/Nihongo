package com.example.nihongo.User.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class Course : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()

    var name: String = ""

    var sourceLanguageId: String = UUID.randomUUID().toString()
    var targetLanguageId: String = UUID.randomUUID().toString()
}
