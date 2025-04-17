package com.example.nihongo.User

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.nihongo.User.data.repository.AppDatabase
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.LessonRepository
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.utils.AppNavGraph
import com.example.nihongo.data.repository.SyncManager
import com.example.nihongo.ui.theme.NihongoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo Room và UserRepository
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            // Kiểm tra xem Room có dữ liệu chưa, nếu chưa có thì tải từ Firestore
            if (db.userDao().getAllUsers().isEmpty()) {
                SyncManager.loadAllFromFirestoreToRoom(this@MainActivity)
            }

            // Sau khi chắc chắn Room có dữ liệu, tiến hành đồng bộ từ Room lên Firestore
            SyncManager.syncAllToFirestore(this@MainActivity)
        }
        val userRepo = UserRepository(db.userDao())
        val courseRepo = CourseRepository(db.courseDao())
        val lessonRepo = LessonRepository(db.lessonDao(), db.exerciseDao(), db.flashcardDao()) // Khởi tạo LessonRepository

        setContent {
            NihongoTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController, userRepo = userRepo, courseRepo =  courseRepo, lessonRepo =  lessonRepo)
                }
            }
        }
    }
}
