package com.example.nihongo.User.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nihongo.User.data.models.Lesson
import java.util.UUID

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons")
    suspend fun getAllLessons(): List<Lesson>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Query("DELETE FROM lessons")
    suspend fun clearLessons()

    @Query("SELECT * FROM lessons WHERE id = :lessonId LIMIT 1")
    suspend fun getLessonById(lessonId: UUID): Lesson?

    // Thêm phương thức này để lấy tất cả bài học theo khóa học
    @Query("SELECT * FROM lessons WHERE courseId = :courseId")
    suspend fun getLessonsByCourseId(courseId: Int): List<Lesson>
}
