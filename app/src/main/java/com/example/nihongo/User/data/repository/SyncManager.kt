package com.example.nihongo.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.example.nihongo.User.data.repository.AppDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

object SyncManager {

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
            val users = db.userDao().getAllUsers() // Lấy Flow, dùng first() để lấy giá trị
            users.forEach {
                firestore.collection("users").document(it.id).set(it).await()
            }

            // Courses
            val courses = db.courseDao().getAllCourses().first() // Dùng first() cho Flow
            courses.forEach {
                firestore.collection("courses").document(it.id.toString()).set(it).await()
            }

            // Lessons
            val lessons = db.lessonDao().getAllLessons() // Dùng first() cho Flow
            lessons.forEach {
                firestore.collection("lessons").document(it.id.toString()).set(it).await()
            }

            // Flashcards
            val flashcards = db.flashcardDao().getAllFlashcards() // Dùng first() cho Flow
            flashcards.forEach {
                firestore.collection("flashcards").document(it.id.toString()).set(it).await()
            }

            // Exercises
            val exercises = db.exerciseDao().getAllExercises() // Dùng first() cho Flow
            exercises.forEach {
                firestore.collection("exercises").document(it.id.toString()).set(it).await()
            }

            // Hiển thị thông báo đồng bộ thành công
            Toast.makeText(context, "Đồng bộ hóa thành công", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            // Hiển thị thông báo nếu có lỗi xảy ra
            Toast.makeText(context, "Đồng bộ hóa thất bại", Toast.LENGTH_SHORT).show()
        }
    }

    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}