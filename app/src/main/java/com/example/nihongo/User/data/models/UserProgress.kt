package com.example.nihongo.User.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "user_progress",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"]),
        ForeignKey(entity = Lesson::class, parentColumns = ["id"], childColumns = ["lessonId"])
    ],
    indices = [Index("userId"), Index("lessonId")]
)
data class UserProgress(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val lessonId: UUID,
    val completedAt: LocalDateTime
)
