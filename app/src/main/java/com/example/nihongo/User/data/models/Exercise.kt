package com.example.nihongo.User.data.models

// Exercise model cho Firestore
data class Exercise(
    val id: String? = null,              // Firestore Document ID
    val subLessonId: String? = null,             // Firestore lessonId là String
    val question: String? = null,
    val answer: String? = null,
    val type: ExerciseType? = null,
    val options: List<String>? = null,
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val title: String? = null,
    val explanation: String? = null
)

enum class ExerciseType {
    FLASHCARD, MEMORY_GAME,VIDEO,
    MULTIPLE_CHOICE, DRAG_MATCH,
    LISTEN_CHOOSE, LISTEN_WRITE,
    FILL_IN_BLANK, HANDWRITING,
    MIXED, QUIZ
}

