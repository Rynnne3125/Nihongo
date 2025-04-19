package com.example.nihongo.User.data.repository

import com.example.nihongo.User.data.models.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class ExerciseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val exercisesCollection = firestore.collection("exercises")

    // Lấy tất cả bài tập thuộc bài học
    suspend fun getExercisesByLessonId(lessonId: String): List<Exercise> {
        val querySnapshot = exercisesCollection
            .whereEqualTo("lessonId", lessonId)  // Lọc theo lessonId
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { it.toObject<Exercise>() }
    }

    // Lấy bài tập theo ID
    suspend fun getExerciseById(exerciseId: String): Exercise? {
        val documentSnapshot = exercisesCollection.document(exerciseId).get().await()
        return if (documentSnapshot.exists()) {
            documentSnapshot.toObject<Exercise>()
        } else null
    }

    // Thêm một bài tập mới
    suspend fun addExercise(exercise: Exercise) {
        val exerciseRef = exercisesCollection.document(exercise.id.ifEmpty { exercisesCollection.document().id })
        exerciseRef.set(exercise).await()
    }
}
