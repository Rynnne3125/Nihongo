package com.example.nihongo.User.data.repository

import com.example.nihongo.User.data.models.Course
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class CourseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val coursesCollection = firestore.collection("courses")

    suspend fun getAllCourses(): List<Course> {
        val snapshot = coursesCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject<Course>() }
    }

    suspend fun insertCourse(course: Course) {
        val courseId = if (course.id.isNotEmpty()) {
            course.id
        } else {
            coursesCollection.document().id  // Firestore tự sinh ID
        }

        val courseWithId = course.copy(id = courseId)
        coursesCollection.document(courseWithId.id).set(courseWithId).await()
    }

    suspend fun getCourseById(courseId: String): Course? {
        val documentSnapshot = coursesCollection.document(courseId).get().await()
        return if (documentSnapshot.exists()) {
            documentSnapshot.toObject<Course>()
        } else null
    }
}