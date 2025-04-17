package com.example.nihongo.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.example.nihongo.User.data.models.Course
import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.Flashcard
import com.example.nihongo.User.data.models.Lesson
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.repository.AppDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

object SyncManager {


    private fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun syncAllToFirestore(context: Context) {
        if (!isConnected(context)) {
            // Hiển thị thông báo nếu không có kết nối
            Toast.makeText(context, "Vui lòng kết nối mạng", Toast.LENGTH_SHORT).show()
            return
        }
        val db = AppDatabase.getDatabase(context)
        val firestore = Firebase.firestore

        try {
            // Users
            db.userDao().getAllUsers().forEach {
                firestore.collection("users").document(it.id).set(it).await()
            }

            // Courses
            db.courseDao().getAllCourses().first().forEach {
                firestore.collection("courses").document(it.id.toString()).set(it).await()
            }

            // Lessons
            db.lessonDao().getAllLessons().forEach {
                firestore.collection("lessons").document(it.id.toString()).set(it).await()
            }

            // Flashcards
            db.flashcardDao().getAllFlashcards().forEach {
                firestore.collection("flashcards").document(it.id.toString()).set(it).await()
            }

            // Exercises
            db.exerciseDao().getAllExercises().forEach {
                firestore.collection("exercises").document(it.id.toString()).set(it).await()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun loadAllFromFirestoreToRoom(context: Context) {
        val db = AppDatabase.getDatabase(context)
        val firestore = Firebase.firestore

        try {
            // Load Users
            val usersSnapshot = firestore.collection("users").get().await()
            val users = usersSnapshot.toObjects(User::class.java)
            db.userDao().clearUsers()
            db.userDao().insertUsers(users)

            // Load Courses
            val coursesSnapshot = firestore.collection("courses").get().await()
            val courses = coursesSnapshot.toObjects(Course::class.java)
            db.courseDao().clearCourses()
            db.courseDao().insertCourses(courses)

            // Load Lessons
            val lessonsSnapshot = firestore.collection("lessons").get().await()
            val lessons = lessonsSnapshot.toObjects(Lesson::class.java)
            db.lessonDao().clearLessons()
            db.lessonDao().insertLessons(lessons)

            // Load Flashcards
            val flashcardsSnapshot = firestore.collection("flashcards").get().await()
            val flashcards = flashcardsSnapshot.toObjects(Flashcard::class.java)
            db.flashcardDao().clearFlashcards()
            db.flashcardDao().insertFlashcards(flashcards)

            // Load Exercises
            val exercisesSnapshot = firestore.collection("exercises").get().await()
            val exercises = exercisesSnapshot.toObjects(Exercise::class.java)
            db.exerciseDao().clearExercises()
            db.exerciseDao().insertExercises(exercises)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



}
