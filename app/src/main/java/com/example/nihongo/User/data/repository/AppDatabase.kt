package com.example.nihongo.User.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nihongo.User.data.models.Course
import com.example.nihongo.User.data.models.User

@Database(
    entities = [
        User::class,
        Course::class, // thêm entity mới
        // nếu có thêm UserInfo hoặc khác cũng add vào đây
    ],
    version = 2 // nhớ tăng version khi thêm entity mới
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao  // thêm DAO

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nihongo_app_db"
                ).fallbackToDestructiveMigration() // hoặc dùng migrate nếu dữ liệu cũ cần giữ
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
