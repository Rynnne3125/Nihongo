package com.example.nihongo.User.data.repository

import com.example.nihongo.User.data.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private var currentUser: User? = null
    private val usersCollection = firestore.collection("users")

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
}
