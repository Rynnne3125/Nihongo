package com.example.nihongo.User.data.repository

import com.example.nihongo.User.data.models.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ExerciseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getExercisesBySubLessonId(subLessonId: String, lessonId: String): List<Exercise> {
         val exercisesCollection = firestore.collection("lessons")
            .document(lessonId)
            .collection("exercises")
        return try {
            val snapshot = exercisesCollection
                .whereEqualTo("subLessonId", subLessonId)  // Truy vấn theo subLessonId
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Exercise::class.java) }
        } catch (e: Exception) {
            emptyList() // Trả về list rỗng nếu lỗi
        }
    }


}
