
package com.example.nihongo.User.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nihongo.User.data.models.Course
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    // Lấy tất cả các khóa học
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<Course>> // Flow để quan sát danh sách các khóa học

    // Lấy khóa học theo ID
    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    suspend fun getCourseById(courseId: Int): Course? // Hàm suspended để lấy khóa học theo ID

    // Thêm một khóa học, thay thế nếu đã tồn tại
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(course: List<Course>)

    // Thêm nhiều khóa học, thay thế nếu đã tồn tại
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>)

    // Xóa tất cả khóa học
    @Query("DELETE FROM courses")
    suspend fun clearCourses()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)
}

