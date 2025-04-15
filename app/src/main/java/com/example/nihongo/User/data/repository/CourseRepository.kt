package com.example.nihongo.User.data.repository

import com.example.nihongo.R
import com.example.nihongo.User.data.models.Course
import kotlinx.coroutines.flow.Flow

class CourseRepository(private val dao: CourseDao) {
    val allCourses: Flow<List<Course>> = dao.getAllCourses()

    suspend fun insertCourse(course: Course) {
        dao.insertCourse(course)
    }

    suspend fun insertSampleData() {
        dao.insertAll(
            listOf(
                Course(0, "Mastering Hiragana", "Beginner to Pro", 4.5, 1234, 890, R.drawable.course_hiragana),
                Course(1, "Katakana Advance", "Writing Practice", 4.7, 768, 654, R.drawable.course_katakana),
                Course(2, "JLPT N5 Preparation", "Standard Course", 4.8, 980, 720, R.drawable.course_n5)
            )
        )
    }
}
