package com.example.nihongo.User.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nihongo.User.data.models.Converters
import com.example.nihongo.User.data.models.Course
import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.Flashcard
import com.example.nihongo.User.data.models.Lesson
import com.example.nihongo.User.data.models.User

@Database(
    entities = [
        User::class,
        Course::class,
        Lesson::class,
        Flashcard::class,
        Exercise::class
    ],
    version = 10,  // ⚠️ Nhớ tăng version mỗi lần thêm entity mới!
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun exerciseDao(): ExerciseDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nihongo_app_db"
                )
                    .fallbackToDestructiveMigration() // Xoá data cũ nếu version khác
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
