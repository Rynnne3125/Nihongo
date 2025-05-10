package com.example.nihongo.Admin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.User.data.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminUserViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            db.collection("users").get()
                .addOnSuccessListener { result ->
                    Log.d("UserCardDebug", result.documents.toString())
                    val userList = result.mapNotNull { it.toObject(User::class.java) }
                    userList.forEach { user ->
                        Log.d("UserCardDebug", "Fetched user: $user") // In ra dạng data class
                    }
                    _users.value = userList
                }
                .addOnFailureListener { exception ->
                    // handle error (e.g., log)
                }
        }
    }
    fun addUser(user: User) {
        val userWithId = user.copy(id = user.id.ifEmpty { db.collection("users").document().id })
        db.collection("users").document(userWithId.id)
            .set(userWithId)
            .addOnSuccessListener {
                fetchUsers()
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    fun updateUser(user: User) {
        db.collection("users").document(user.id)
            .set(user)
            .addOnSuccessListener {
                fetchUsers()
            }
            .addOnFailureListener {
                // Handle error
            }
    }
    fun deleteUser(user: User) {
        db.collection("users").document(user.id)
            .delete()
            .addOnSuccessListener {
                fetchUsers()
            }
            .addOnFailureListener {
                // Handle error
            }
    }
}
