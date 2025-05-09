package com.example.nihongo.User.data.repository

import android.util.Log
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.models.UserProgress
import com.example.nihongo.User.utils.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val sessionManager: SessionManager? = null
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
            sessionManager?.createLoginSession(hashedUser)
            true
        } else {
            false
        }
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
        user?.let { sessionManager?.createLoginSession(it) }
        return user
    }

    suspend fun getUserByEmail(email: String): User? {
        val querySnapshot = usersCollection
            .whereEqualTo("email", email)
            .get()
            .await()

        val document = querySnapshot.documents.firstOrNull()

        // Log raw Firestore document data
        Log.d("UserRepository", "Raw document data: ${document?.data}")

        val user = document?.toObject<User>()

        // Log mapped User object
        Log.d("UserRepository", "Mapped User object: $user")

        return user
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val querySnapshot = usersCollection.get().await()
            querySnapshot.documents.mapNotNull { it.toObject<User>() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun isVip(): Boolean {
        return getCurrentUser()?.vip == true
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

    fun logout() {
        currentUser = null
        sessionManager?.logoutUser()
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

    // Thêm hàm updateUser để cập nhật thông tin người dùng
    suspend fun updateUser(user: User) {
        try {
            // Cập nhật thông tin người dùng trong Firestore
            usersCollection.document(user.id).set(user).await()
            
            // Cập nhật currentUser trong repository
            currentUser = user
            
            // Cập nhật session nếu có SessionManager
            sessionManager?.createLoginSession(user)
            
            Log.d("UserRepository", "User updated successfully: ${user.id}")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user: ${e.message}")
            throw e
        }
    }

    // Lấy thông tin người dùng theo ID
    suspend fun getUserById(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            document.toObject<User>()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user by ID", e)
            null
        }
    }

    // Cập nhật trạng thái online của người dùng
    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean) {
        try {
            val user = getUserById(userId) ?: return
            val updatedUser = user.updateOnlineStatus(isOnline)
            usersCollection.document(userId).update("online", isOnline).await()
            
            // Cập nhật currentUser nếu đó là người dùng hiện tại
            if (currentUser?.id == userId) {
                currentUser = updatedUser
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating online status", e)
        }
    }

    // Thêm đối tác học tập
    suspend fun addStudyPartner(userId: String, partnerId: String): Boolean {
        return try {
            // Lấy thông tin người dùng
            val user = getUserById(userId) ?: return false
            
            // Kiểm tra xem người dùng có phải là VIP không
            if (!user.vip) {
                Log.d("UserRepository", "User is not VIP, cannot add partner")
                return false
            }
            
            // Cập nhật danh sách đối tác của người dùng
            val updatedUser = user.addPartner(partnerId)
            usersCollection.document(userId).update("partners", updatedUser.partners).await()
            
            // Cập nhật currentUser nếu đó là người dùng hiện tại
            if (currentUser?.id == userId) {
                currentUser = updatedUser
            }
            
            // Thêm điểm năng động cho người dùng
            addActivityPoints(userId, 10)
            
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding study partner", e)
            false
        }
    }

    // Xóa đối tác học tập
    suspend fun removeStudyPartner(userId: String, partnerId: String): Boolean {
        return try {
            val user = getUserById(userId) ?: return false
            val updatedUser = user.removePartner(partnerId)
            usersCollection.document(userId).update("partners", updatedUser.partners).await()
            
            // Cập nhật currentUser nếu đó là người dùng hiện tại
            if (currentUser?.id == userId) {
                currentUser = updatedUser
            }
            
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error removing study partner", e)
            false
        }
    }

    // Thêm điểm năng động cho người dùng
    suspend fun addActivityPoints(userId: String, points: Int): Boolean {
        return try {
            val user = getUserById(userId) ?: return false
            val newPoints = user.activityPoints + points
            val newRank = user.calculateRank()
            
            usersCollection.document(userId)
                .update(
                    "activityPoints", newPoints,
                    "rank", newRank
                ).await()
            
            // Cập nhật currentUser nếu đó là người dùng hiện tại
            if (currentUser?.id == userId) {
                currentUser = currentUser?.copy(
                    activityPoints = newPoints,
                    rank = newRank
                )
            }
            
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding activity points", e)
            false
        }
    }
}
