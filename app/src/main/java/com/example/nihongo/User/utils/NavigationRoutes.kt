package com.example.nihongo.User.utils


sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Courses : Screen("courses")
    object Lessons : Screen("lessons/{courseId}")
    object Flashcards : Screen("flashcards/{lessonId}")
    object Exercises : Screen("exercises/{lessonId}")
}

