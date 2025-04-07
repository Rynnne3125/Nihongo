package com.example.nihongo.User.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class UserProgress : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString() // Chuyển UUID thành String
    var userId: String = UUID.randomUUID().toString() // Chuyển UUID thành String
    var lessonId: String = UUID.randomUUID().toString() // Chuyển UUID thành String
    var completedAt: String = ""
}
