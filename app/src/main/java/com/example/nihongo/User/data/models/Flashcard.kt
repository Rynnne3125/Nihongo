package com.example.nihongo.User.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(entity = Lesson::class, parentColumns = ["id"], childColumns = ["lessonId"])
    ],
    indices = [Index("lessonId")]
)
data class Flashcard(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val lessonId: UUID,
    val term: String,
    val definition: String
)
