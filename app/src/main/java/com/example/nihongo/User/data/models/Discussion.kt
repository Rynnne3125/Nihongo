package com.example.nihongo.User.data.models

data class Discussion(
    var id: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorImageUrl: String = "",
    val commentCount: Int = 0,
    val createdAt: Long = 0,
    val tags: List<String> = emptyList()
)