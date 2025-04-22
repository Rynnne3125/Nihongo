package com.example.nihongo.User.utils

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nihongo.Admin.AdminLoginScreen
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.ExerciseRepository
import com.example.nihongo.User.data.repository.LessonRepository
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.ui.screens.homepage.CourseDetailScreen
import com.example.nihongo.User.ui.screens.homepage.CoursesScreen
import com.example.nihongo.User.ui.screens.homepage.ExerciseScreen
import com.example.nihongo.User.ui.screens.homepage.HomeScreen
import com.example.nihongo.User.ui.screens.login.LoginScreen
import com.example.nihongo.User.ui.screens.login.OTPScreen
import com.example.nihongo.User.ui.screens.login.RegisterScreen
import com.example.nihongo.ui.screens.homepage.LessonsScreen

@Composable
fun AppNavGraph(navController: NavHostController, userRepo: UserRepository, courseRepo: CourseRepository, lessonRepo: LessonRepository, exerciseRepo: ExerciseRepository) {
    NavHost(navController = navController, startDestination = NavigationRoutes.LOGIN) {

        composable(NavigationRoutes.LOGIN) {
            LoginScreen(navController, userRepo)
        }

        composable(NavigationRoutes.REGISTER) {
            RegisterScreen(navController, userRepo)
        }

        composable("home/{user_email}") { backStackEntry ->
            val user_email = backStackEntry.arguments?.getString("user_email") ?: ""
            HomeScreen(navController = navController, user_email = user_email, userRepository = userRepo, courseRepository = courseRepo)
        }

        composable("admin_login") {
            AdminLoginScreen(navController)
        }

        composable("otp_screen") {
            val expectedOtp = navController.previousBackStackEntry?.savedStateHandle?.get<String>("expectedOtp") ?: ""
            val user_email = navController.previousBackStackEntry?.savedStateHandle?.get<String>("user_email") ?: ""
            OTPScreen(navController = navController, expectedOtp = expectedOtp, user_email = user_email)
        }

        composable("courses") { // Định nghĩa route cho courses
            CoursesScreen(navController = navController, courseRepository = courseRepo)
        }
        composable("courses/{courseId}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") // courseId now as String
            if (courseId != null) {
                CourseDetailScreen(courseId = courseId, navController = navController, courseRepository = courseRepo, userRepository = userRepo)
            } else {
                InvalidCourseScreen() // If courseId is invalid
            }
        }

        composable("lessons/{courseId}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") // courseId now as String
//            val courseId = "3"

            if (courseId != null) {
                LessonsScreen(courseId = courseId, navController = navController, lessonRepository = lessonRepo, courseRepository = courseRepo, userRepository = userRepo)
            } else {
                InvalidCourseScreen() // If courseId is invalid
            }
        }
        composable("exercise/{lessonId}/{sublessonId}") { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId")
            val sublessonId = backStackEntry.arguments?.getString("sublessonId")

            if (!lessonId.isNullOrBlank() && !sublessonId.isNullOrBlank()) {
                ExerciseScreen(
                    navController = navController,
                    lessonId = lessonId,
                    sublessonId = sublessonId,
                    exerciseRepository = exerciseRepo
                )
            } else {
                // Có thể show 1 màn hình báo lỗi:
                Text("Không tìm thấy bài tập, vui lòng thử lại.")
            }
        }

    }
}




@Composable
fun InvalidCourseScreen() {
    Text("Invalid course ID.")
}
