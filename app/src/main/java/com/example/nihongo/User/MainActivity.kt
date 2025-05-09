package com.example.nihongo.User

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.ExerciseRepository
import com.example.nihongo.User.data.repository.LessonRepository
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.ui.components.BottomNavItem
import com.example.nihongo.User.utils.AppNavGraph
import com.example.nihongo.User.utils.NavigationRoutes
import com.example.nihongo.User.utils.SessionManager
import com.example.nihongo.ui.theme.NihongoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        val userRepo = UserRepository(sessionManager = sessionManager)
        val courseRepo = CourseRepository()
        val lessonRepo = LessonRepository()
        val exerciseRepo = ExerciseRepository()

        // Check if user is already logged in
        val loggedInUser = sessionManager.getUserDetails()
        val startDestination = if (loggedInUser != null) {
            // Set the current user in repository
            CoroutineScope(Dispatchers.IO).launch {
                // Cập nhật trạng thái online của người dùng
                userRepo.updateUserOnlineStatus(loggedInUser.id, true)
            }
            "home/${loggedInUser.email}"
        } else {
            NavigationRoutes.LOGIN
        }

        // Rest of your code...

        setContent {
            NihongoTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    // Handle back press for the entire app
                    BackHandler {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route

                        // If we're on a main screen (home, courses, profile, community)
                        if (currentRoute?.contains(BottomNavItem.Home.route) == true ||
                            currentRoute?.contains(BottomNavItem.Courses.route) == true ||
                            currentRoute?.contains(BottomNavItem.Profile.route) == true ||
                            currentRoute?.contains(BottomNavItem.Community.route) == true) {

                            // Implement double back press to exit
                            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                                finish()
                            } else {
                                Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                                backPressedTime = System.currentTimeMillis()
                            }
                        } else {
                            // For other screens, allow normal back navigation
                            navController.popBackStack()
                        }
                    }

                    AppNavGraph(
                        navController = navController,
                        userRepo = userRepo,
                        courseRepo = courseRepo,
                        lessonRepo = lessonRepo,
                        exerciseRepo = exerciseRepo,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
