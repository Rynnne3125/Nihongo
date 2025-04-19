package com.example.nihongo.User.data.models

// Exercise model cho Firestore
data class Exercise(
    val id: String = "",              // Firestore Document ID
    val lessonId: String,             // Firestore lessonId là String
    val question: String,
    val answer: String,
    val type: ExerciseType,
    val options: List<String>? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val hint: String? = null,
    val explanation: String? = null
)

enum class ExerciseType {
    MULTIPLE_CHOICE, TRANSLATION, LISTENING, WRITING
}
