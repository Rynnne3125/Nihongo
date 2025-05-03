package com.example.nihongo.User.data.models

data class UserProgress(
    val userId: String = "",                         // ID người dùng
    val courseId: String = "",                       // ID khóa học
    val courseTitle: String = "",                    // Tiêu đề khóa học
    val currentLessonId: String? = null,             // Bài học hiện tại mà người dùng đang học
    val completedLessons: List<String> = emptyList(),     // Danh sách các bài học đã hoàn thành
    val completedExercises: List<String> = emptyList(),   // Danh sách các bài tập đã làm
    val passedExercises: List<String> = emptyList(),      // Danh sách các bài tập đã vượt qua
    val progress: Float = 0.0f,                      // Tiến độ học (%)
    val lastUpdated: Long = 0L,                      // Thời gian cập nhật
    val totalLessons: Int = 0                        // Tổng số bài học
)
