package com.example.nihongo.User.data.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val createdAt: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()),
    val isVip: Boolean = false,
    val isLoggedIn: Boolean = false
)
