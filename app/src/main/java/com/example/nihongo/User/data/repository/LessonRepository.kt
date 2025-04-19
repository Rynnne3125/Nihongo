package com.example.nihongo.User.data.repository

import com.example.nihongo.User.data.models.Lesson
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class LessonRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val lessonsCollection = firestore.collection("lessons")

    // Lấy tất cả bài học thuộc khóa học
    suspend fun getLessonsByCourseId(courseId: String): List<Lesson> {
        val querySnapshot = lessonsCollection
            .whereEqualTo("courseId", courseId)  // Lọc theo courseId
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { it.toObject<Lesson>() }
    }

    // Lấy bài học theo ID
    suspend fun getLessonById(lessonId: String): Lesson? {
        val documentSnapshot = lessonsCollection.document(lessonId).get().await()
        return if (documentSnapshot.exists()) {
            documentSnapshot.toObject<Lesson>()
        } else null
    }

    // Thêm một bài học mới
    suspend fun addLesson(lesson: Lesson) {
        val lessonRef = lessonsCollection.document(lesson.id.ifEmpty { lessonsCollection.document().id })
        lessonRef.set(lesson).await()
    }
}
