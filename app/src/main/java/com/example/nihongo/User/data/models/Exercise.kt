package com.example.nihongo.User.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(entity = Lesson::class, parentColumns = ["id"], childColumns = ["lessonId"])
    ],
    indices = [Index("lessonId")]
)
data class Exercise(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val lessonId: UUID,
    val question: String,
    val answer: String,
    val type: ExerciseType
)

enum class ExerciseType {
    MULTIPLE_CHOICE, TRANSLATION, LISTENING
}

