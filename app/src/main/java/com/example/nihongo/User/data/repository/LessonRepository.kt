package com.example.nihongo.User.data.repository

import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.Flashcard
import com.example.nihongo.User.data.models.Lesson
import java.util.UUID

class LessonRepository(private val lessonDao: LessonDao, private val exerciseDao: ExerciseDao, private val flashcardDao: FlashcardDao) {

    suspend fun getLessonsByCourseId(courseId: Int): List<Lesson> {
        // Lấy tất cả bài học thuộc khóa học
        return lessonDao.getLessonsByCourseId(courseId)
    }

    suspend fun getLessonById(lessonId: UUID): Lesson? {
        return lessonDao.getLessonById(lessonId)
    }

    suspend fun getExercisesByLessonId(lessonId: UUID): List<Exercise> {
        return exerciseDao.getExercisesByLessonId(lessonId)
    }

    suspend fun getFlashcardsByLessonId(lessonId: UUID): List<Flashcard> {
        return flashcardDao.getFlashcardsByLessonId(lessonId)
    }
}
