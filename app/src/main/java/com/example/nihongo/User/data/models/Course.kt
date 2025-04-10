package com.example.nihongo.User.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(entity = Language::class, parentColumns = ["id"], childColumns = ["sourceLanguageId"]),
        ForeignKey(entity = Language::class, parentColumns = ["id"], childColumns = ["targetLanguageId"])
    ],
    indices = [Index("sourceLanguageId"), Index("targetLanguageId")]
)
data class Course(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val sourceLanguageId: UUID,
    val targetLanguageId: UUID
)

