package com.example.nihongo.User.utils

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nihongo.Admin.AdminLoginScreen
import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.ExerciseRepository
import com.example.nihongo.User.data.repository.LessonRepository
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.ui.components.BottomNavItem
import com.example.nihongo.User.ui.screens.homepage.CourseDetailScreen
import com.example.nihongo.User.ui.screens.homepage.CoursesScreen
import com.example.nihongo.User.ui.screens.homepage.ExerciseScreen
import com.example.nihongo.User.ui.screens.homepage.FlashcardScreen
import com.example.nihongo.User.ui.screens.homepage.HomeScreen
import com.example.nihongo.User.ui.screens.homepage.ProfileScreen
import com.example.nihongo.User.ui.screens.homepage.QuizScreen
import com.example.nihongo.User.ui.screens.login.LoginScreen
import com.example.nihongo.User.ui.screens.login.OTPScreen
import com.example.nihongo.User.ui.screens.login.RegisterScreen
import com.example.nihongo.ui.screens.homepage.LessonsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    userRepo: UserRepository,
    courseRepo: CourseRepository,
    lessonRepo: LessonRepository,
    exerciseRepo: ExerciseRepository
) {
    NavHost(navController = navController, startDestination = NavigationRoutes.LOGIN) {

        // Auth
        composable(NavigationRoutes.LOGIN) {
            LoginScreen(navController, userRepo)
        }

        composable(NavigationRoutes.REGISTER) {
            RegisterScreen(navController, userRepo)
        }

        composable("otp_screen") {
            val expectedOtp = navController.previousBackStackEntry?.savedStateHandle?.get<String>("expectedOtp") ?: ""
            val userEmail = navController.previousBackStackEntry?.savedStateHandle?.get<String>("user_email") ?: ""
            OTPScreen(navController, expectedOtp, userEmail)
        }

        // Bottom Nav Screens
        composable("${BottomNavItem.Home.route}/{user_email}",
            arguments = listOf(navArgument("user_email") { type = NavType.StringType })
        ) { backStackEntry ->
            val userEmail = backStackEntry.arguments?.getString("user_email") ?: ""
            HomeScreen(navController, userEmail, userRepo, courseRepo)
        }

        composable("${BottomNavItem.Courses.route}/{user_email}",
            arguments = listOf(navArgument("user_email") { type = NavType.StringType })
        ) { backStackEntry ->
            val userEmail = backStackEntry.arguments?.getString("user_email") ?: ""
            CoursesScreen(navController, courseRepo, userEmail)
        }

        composable("${BottomNavItem.Profile.route}/{user_email}",
            arguments = listOf(navArgument("user_email") { type = NavType.StringType })
        ) { backStackEntry ->
            val userEmail = backStackEntry.arguments?.getString("user_email") ?: ""
            ProfileScreen(
                navController = navController,
                userEmail = userEmail,
                userRepository = userRepo,
                courseRepository = courseRepo,
                onSignOut = {
                    navController.navigate(NavigationRoutes.LOGIN) {
                        popUpTo(0)
                    }
                }
            )
        }

        // Other Screens
        composable("admin_login") {
            AdminLoginScreen(navController)
        }

        composable("courses/{courseId}/{user_email}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            val userEmail = backStackEntry.arguments?.getString("user_email") ?: ""
            if (courseId != null) {
                CourseDetailScreen(courseId, navController, courseRepo, userRepo, lessonRepo, userEmail)
            } else {
                InvalidCourseScreen()
            }
        }

        composable("lessons/{courseId}/{user_email}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("user_email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val userEmail = backStackEntry.arguments?.getString("user_email") ?: ""
            LessonsScreen(courseId, userEmail, navController, lessonRepo, courseRepo, userRepo)
        }

        composable("exercise/{courseId}/{lessonId}/{sublessonId}/{user_email}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.StringType },
                navArgument("sublessonId") { type = NavType.StringType },
                navArgument("user_email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val lessonId = backStackEntry.arguments?.getString("lessonId")
            val sublessonId = backStackEntry.arguments?.getString("sublessonId")
            val userEmail = backStackEntry.arguments?.getString("user_email") ?: ""
            if (!lessonId.isNullOrBlank() && !sublessonId.isNullOrBlank()) {
                ExerciseScreen(navController, sublessonId, exerciseRepo, courseId,  lessonId, userEmail)
            } else {
                Text("Không tìm thấy bài tập, vui lòng thử lại.")
            }
        }

        composable("quiz_screen/{user_email}/{courseId}/{lessonId}",
            arguments = listOf(navArgument("user_email") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            val quizExercises = navController.previousBackStackEntry?.savedStateHandle?.get<List<Exercise>>("quizList") ?: emptyList()
            val userEmail = backStackEntry.arguments?.getString("user_email") ?: ""
            QuizScreen(quizExercises, userEmail,courseId,lessonId, navController)
        }
        composable("vocabulary/{user_email}") { backStackEntry ->
            val userEmail = backStackEntry.arguments?.getString("user_email") ?: ""
            FlashcardScreen(navController, userEmail)
        }

    }
}

@Composable
fun InvalidCourseScreen() {
    Text("Invalid course ID.")
}
