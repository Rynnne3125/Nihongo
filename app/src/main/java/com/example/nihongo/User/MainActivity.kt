package com.example.nihongo.User

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.ExerciseRepository
import com.example.nihongo.User.data.repository.LessonRepository
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.utils.AppNavGraph
import com.example.nihongo.ui.theme.NihongoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        val userRepo = UserRepository()
        val courseRepo = CourseRepository()
        val lessonRepo = LessonRepository()
        val exerciseRepo = ExerciseRepository()

        CoroutineScope(Dispatchers.IO).launch {
            val courses = courseRepo.getAllCourses()
            // xử lý dữ liệu nếu cần
            Log.d("MainActivity", "Loaded ${courses.size} courses")
        }
        setContent {
            NihongoTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController, userRepo = userRepo, courseRepo =  courseRepo, lessonRepo =  lessonRepo, exerciseRepo = exerciseRepo)
                }
            }
        }
    }
}
