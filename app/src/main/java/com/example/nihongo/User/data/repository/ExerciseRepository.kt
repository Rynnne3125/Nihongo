package com.example.nihongo.User.data.repository

import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.ExerciseType
import com.example.nihongo.User.data.models.Lesson
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
    suspend fun getPracticeExercisesExcludingFirstSubLesson(lessonId: String): List<Exercise> {
        return try {
            // 1. Lấy tất cả các units cho lessonId
            val unitsSnapshot = firestore.collection("lessons")
                .document(lessonId)
                .get()
                .await()

            // 2. Lấy subLessons từ units
            val units = unitsSnapshot.toObject(Lesson::class.java)?.units ?: emptyList()

            // 3. Lọc ra các subLessonId không kết thúc bằng "-1"
            val filteredSubLessonIds = units
                .flatMap { it.subLessons }
                .filterNot { it.id.endsWith("-1") }
                .map { it.id }

            // 4. Lấy tất cả exercises thuộc các subLessonId đã lọc
            val exercisesSnapshot = firestore.collection("lessons")
                .document(lessonId)
                .collection("exercises")
                .get()
                .await()

            exercisesSnapshot.documents
                .mapNotNull { doc ->
                    // Trả về Exercise và sử dụng documentId tự động từ Firestore
                    doc.toObject(Exercise::class.java)?.apply {
                        id = doc.id // Lấy documentId và gán vào field id
                    }
                }
                .filter { it.subLessonId in filteredSubLessonIds && it.type == ExerciseType.PRACTICE }

        } catch (e: Exception) {
            emptyList()
        }
    }
    fun getQuizExerciseIds(lessonId: String) {
        val db = FirebaseFirestore.getInstance()
        val exercisesRef = db.collection("lessons").document(lessonId).collection("exercises")

        // Truy vấn các bài tập từ Firestore
        exercisesRef.get().addOnSuccessListener { querySnapshot ->
            // Duyệt qua các tài liệu và lấy ID của từng tài liệu
            val exerciseIds = querySnapshot.documents.map { it.id }

            // In ra tất cả ID của các bài tập
            println("Quiz Exercise IDs: $exerciseIds")
        }.addOnFailureListener { exception ->
            println("Error getting exercises: $exception")
        }
    }


}