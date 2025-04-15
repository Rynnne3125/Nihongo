package com.example.nihongo.User.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val rating: Double,
    val reviews: Int,
    val likes: Int,
    val imageRes: Int // drawable ID
)


