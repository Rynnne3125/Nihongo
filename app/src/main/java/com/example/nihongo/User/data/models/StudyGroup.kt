package com.example.nihongo.User.data.models

import com.google.firebase.Timestamp
import java.util.UUID

data class StudyGroup(
    var id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val memberCount: Int = 0,
    val createdBy: String = "",
    val createdAt: Long? = null,
    val lastActivity: Timestamp? = null,
    val members: List<String> = emptyList(),
    val imageUrl: String = ""
)
