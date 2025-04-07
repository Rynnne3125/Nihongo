package com.example.nihongo.User.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class Lesson : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString() // Chuyển UUID thành String
    var courseId: String = UUID.randomUUID().toString() // Chuyển UUID thành String
    var title: String = ""
    var difficultyLevel: Int = 1
}
