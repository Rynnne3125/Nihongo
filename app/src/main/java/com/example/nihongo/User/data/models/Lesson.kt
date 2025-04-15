package com.example.nihongo.User.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(entity = Course::class, parentColumns = ["id"], childColumns = ["courseId"])
    ],
    indices = [Index("courseId")]
)
data class Lesson(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val courseId: UUID,
    val title: String,
    val difficultyLevel: Int,
    val shortDescription: String? = null, // ✅ Hiển thị preview nhanh
    val contentText: String?,
    val videoUrl: String?,
    val audioUrl: String? = null,         // ✅ Dành cho nghe
    val imageUrl: String? = null,         // ✅ Minh họa
    val duration: Int? = null             // ✅ Thời lượng bài học (tính theo phút)
)

