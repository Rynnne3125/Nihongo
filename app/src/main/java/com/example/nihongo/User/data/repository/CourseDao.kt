package com.example.nihongo.User.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nihongo.User.data.models.Course
import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.Flashcard
import com.example.nihongo.User.data.models.Lesson
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<Course>> // Flow for observing the list of courses

    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    suspend fun getCourseById(courseId: Int): Course? // Suspended function to fetch course by ID

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>)
}

@Dao
interface LessonDao {

    @Query("SELECT * FROM lessons WHERE id = :lessonId LIMIT 1")
    suspend fun getLessonById(lessonId: UUID): Lesson?

    // Thêm phương thức này để lấy tất cả bài học theo khóa học
    @Query("SELECT * FROM lessons WHERE courseId = :courseId")
    suspend fun getLessonsByCourseId(courseId: Int): List<Lesson>
}


@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises WHERE lessonId = :lessonId")
    suspend fun getExercisesByLessonId(lessonId: UUID): List<Exercise>
}

@Dao
interface FlashcardDao {

    @Query("SELECT * FROM flashcards WHERE lessonId = :lessonId")
    suspend fun getFlashcardsByLessonId(lessonId: UUID): List<Flashcard>
}

