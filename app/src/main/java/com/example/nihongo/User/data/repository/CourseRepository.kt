package com.example.nihongo.User.data.repository

import com.example.nihongo.R
import com.example.nihongo.User.data.models.Course
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CourseRepository(private val dao: CourseDao) {
    val allCourses: Flow<List<Course>> = dao.getAllCourses() // Flow for observing all courses

    suspend fun insertCourse(course: Course) {
        dao.insertCourse(course) // Insert a single course
    }

    // Inside your repository or ViewModel
    suspend fun insertSampleData() {
        // Collect the Flow to get the list of courses
        val courses = dao.getAllCourses().first() // Collects the first value emitted by the Flow

        if (courses.isEmpty()) {
            val sampleCourses = listOf(
                Course(1, "Mastering Hiragana", "Beginner to Pro", 4.5, 1234, 890, R.drawable.course_hiragana, isVip = false),
                Course(2, "Katakana Advance", "Writing Practice", 4.7, 768, 654, R.drawable.course_katakana, isVip = true),
                Course(3, "JLPT N5 Preparation", "Standard Course", 4.8, 980, 720, R.drawable.course_n5, isVip = true)
            )
            dao.insertAll(sampleCourses)
        }
    }



    suspend fun getCourseById(courseId: Int): Course? {
        return dao.getCourseById(courseId) // Fetch course by ID from the DAO
    }
}
