package com.example.nihongo.User.data.models

data class Course(
    val id: String = "",        // Firestore Document ID
    val title: String = "",
    val description: String = "",
    val rating: Double = 0.0,
    val reviews: Int = 0,
    val likes: Int = 0,
    val imageRes: Int = 0,      // vẫn giữ imageRes kiểu Int nếu ảnh local
    val isVip: Boolean = false
)
