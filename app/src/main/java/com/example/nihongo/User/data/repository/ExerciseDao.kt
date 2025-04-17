package com.example.nihongo.User.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nihongo.User.data.models.Exercise
import java.util.UUID

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<Exercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Query("DELETE FROM exercises")
    suspend fun clearExercises()

    @Query("SELECT * FROM exercises WHERE lessonId = :lessonId")
    suspend fun getExercisesByLessonId(lessonId: UUID): List<Exercise>
}
