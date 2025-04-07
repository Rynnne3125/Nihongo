package com.example.nihongo.User.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

enum class ExerciseType {
    MULTIPLE_CHOICE,
    TRANSLATION,
    LISTENING
}

class Exercise : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString() // Lưu UUID dưới dạng String
    var lessonId: String = UUID.randomUUID().toString() // Lưu UUID dưới dạng String
    var question: String = ""
    var answer: String = ""

    // Lưu ExerciseType dưới dạng String
    var type: String = ExerciseType.MULTIPLE_CHOICE.name

    // Sử dụng phương thức này để chuyển đổi String thành enum khi cần
    fun getType(): ExerciseType {
        return ExerciseType.valueOf(type)
    }

    // Sử dụng phương thức này để chuyển đổi từ enum thành String khi cần lưu trữ
    fun setType(exerciseType: ExerciseType) {
        this.type = exerciseType.name
    }
}
