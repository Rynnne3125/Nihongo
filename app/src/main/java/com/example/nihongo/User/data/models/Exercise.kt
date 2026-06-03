package com.example.nihongo.User.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

// Exercise model cho Firestore
@Parcelize
data class Exercise(
    var id: String? = null,              // Firestore Document ID
    val subLessonId: String? = null,             // Firestore lessonId là String
    val question: String? = null,
    val answer: String? = null,
    val type: ExerciseType? = null,
    val options: List<String>? = null,
    val videoUrl: String? = null,
    val romanji: String? = null,
    val kana: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val title: String? = null,
    val passed: Boolean = false,
    val explanation: String? = null
): Serializable, Parcelable

enum class ExerciseType {
    FLASHCARD, MEMORY_GAME,VIDEO,
    MULTIPLE_CHOICE, DRAG_MATCH,
    LISTEN_CHOOSE, LISTEN_WRITE,
    FILL_IN_BLANK, HANDWRITING,
    MIXED, PRACTICE;
    companion object {
        fun from(type: String): ExerciseType? {
            return values().find { it.name.equals(type, ignoreCase = true) }
        }
    }
}

