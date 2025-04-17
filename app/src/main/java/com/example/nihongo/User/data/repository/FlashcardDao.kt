package com.example.nihongo.User.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nihongo.User.data.models.Flashcard
import java.util.UUID

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards")
    suspend fun getAllFlashcards(): List<Flashcard>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<Flashcard>)

    @Query("DELETE FROM flashcards")
    suspend fun clearFlashcards()

    @Query("SELECT * FROM flashcards WHERE lessonId = :lessonId")
    suspend fun getFlashcardsByLessonId(lessonId: UUID): List<Flashcard>
}
