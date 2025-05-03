package com.example.nihongo.User.data.repository

import android.util.Log
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.models.UserProgress
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private var currentUser: User? = null
    private val usersCollection = firestore.collection("users")

    // ===== USER AUTH =====
    suspend fun registerUser(user: User): Boolean {
        val existing = getUserByUsername(user.username)
        return if (existing == null) {
            val hashedUser = user.copy(password = hashPassword(user.password))
            usersCollection.document(hashedUser.id).set(hashedUser).await()
            currentUser = hashedUser
            true
        } else {
            false
        }
    }

    suspend fun loginUser(username: String, password: String): User? {
        val hashedPassword = hashPassword(password)
        val querySnapshot = usersCollection
            .whereEqualTo("username", username)
            .whereEqualTo("password", hashedPassword)
            .get()
            .await()

        val user = querySnapshot.documents.firstOrNull()?.toObject<User>()
        currentUser = user
        return user
    }

    suspend fun loginUserByEmail(email: String, password: String): User? {
        val hashedPassword = hashPassword(password)
        val querySnapshot = usersCollection
            .whereEqualTo("email", email)
            .whereEqualTo("password", hashedPassword)
            .get()
            .await()

        val user = querySnapshot.documents.firstOrNull()?.toObject<User>()
        currentUser = user
        return user
    }

    suspend fun getUserByEmail(email: String): User? {
        val querySnapshot = usersCollection
            .whereEqualTo("email", email)
            .get()
            .await()

        return querySnapshot.documents.firstOrNull()?.toObject<User>()
    }

    suspend fun isVip(): Boolean {
        return getCurrentUser()?.isVip == true
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    private suspend fun getUserByUsername(username: String): User? {
        val querySnapshot = usersCollection
            .whereEqualTo("username", username)
            .get()
            .await()

        return querySnapshot.documents.firstOrNull()?.toObject<User>()
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ===== USER PROGRESS =====
    suspend fun saveUserProgress(userId: String, userProgress: UserProgress) {
        try {
            val progressRef = firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(userProgress.courseId)

            progressRef.set(userProgress).await()
            Log.d("UserProgress", "User progress saved successfully")
        } catch (e: Exception) {
            Log.e("UserProgress", "Error saving user progress", e)
        }
    }

    suspend fun updateUserProgress(
        userId: String,
        courseId: String,
        exerciseId: String,
        passed: Boolean
    ) {
        try {
            val progressRef = firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId)

            val progressSnapshot = progressRef.get().await()

            if (progressSnapshot.exists()) {
                val userProgress = progressSnapshot.toObject(UserProgress::class.java) ?: return

                val updatedCompletedExercises = userProgress.completedExercises.toMutableList()
                val updatedPassedExercises = userProgress.passedExercises.toMutableList()

                if (!updatedCompletedExercises.contains(exerciseId)) {
                    updatedCompletedExercises.add(exerciseId)
                }

                if (passed && !updatedPassedExercises.contains(exerciseId)) {
                    updatedPassedExercises.add(exerciseId)
                }

                val updatedProgress = userProgress.copy(
                    completedExercises = updatedCompletedExercises,
                    passedExercises = updatedPassedExercises,
                    lastUpdated = System.currentTimeMillis()
                )

                progressRef.set(updatedProgress).await()
                Log.d("UserProgress", "User exercise progress updated")

            } else {
                Log.e("UserProgress", "Progress not found for this course")
            }

        } catch (e: Exception) {
            Log.e("UserProgress", "Error updating progress", e)
        }
    }

    suspend fun markLessonAsCompleted(userId: String, courseId: String, lessonId: String, totalLessons: Int) {
        try {
            val progressRef = firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId)

            val snapshot = progressRef.get().await()
            if (!snapshot.exists()) return

            val userProgress = snapshot.toObject(UserProgress::class.java) ?: return
            val completedLessons = userProgress.completedLessons.toMutableList()

            if (!completedLessons.contains(lessonId)) {
                completedLessons.add(lessonId)
            }

            val updatedProgress = userProgress.copy(
                completedLessons = completedLessons,
                progress = (completedLessons.size.toFloat() / totalLessons.toFloat()),
                lastUpdated = System.currentTimeMillis()
            )
            Log.d("UserProgress", "completedLessons.size: ${completedLessons.size}")
            Log.d("UserProgress", "totalLessons: $totalLessons")
            progressRef.set(updatedProgress).await()
            Log.d("UserProgress", "Lesson marked as completed")

        } catch (e: Exception) {
            Log.e("UserProgress", "Error marking lesson as completed", e)
        }
    }

    suspend fun getAllUserProgress(userId: String): List<UserProgress> {
        return try {
            val snapshot = firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(UserProgress::class.java) }
        } catch (e: Exception) {
            Log.e("UserProgress", "Error getting all user progress", e)
            emptyList()
        }
    }
    suspend fun getUserProgressForCourse(userId: String, courseId: String): UserProgress? {
        return try {
            // Lấy dữ liệu của khóa học cụ thể trong collection "courses"
            val snapshot = firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId) // Lấy document của khóa học cụ thể
                .get()
                .await()

            // Nếu tài liệu tồn tại, chuyển đổi nó thành đối tượng UserProgress
            snapshot.toObject(UserProgress::class.java)
        } catch (e: Exception) {
            Log.e("UserProgress", "Error getting user progress for course", e)
            null
        }
    }

    fun calculateProgress(completed: Int, total: Int): Double {
        return if (total > 0) (completed.toDouble() / total.toDouble()) else 0.0
    }
}
