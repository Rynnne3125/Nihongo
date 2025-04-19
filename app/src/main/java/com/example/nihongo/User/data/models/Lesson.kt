package com.example.nihongo.User.data.models

data class Lesson(
    val id: String = "",               // Firestore Document ID
    val courseId: String,              // Thay UUID thành String vì Firestore ID là String
    val title: String,
    val difficultyLevel: Int,
    val shortDescription: String? = null,
    val contentText: String? = null,
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val duration: Int? = null
)
