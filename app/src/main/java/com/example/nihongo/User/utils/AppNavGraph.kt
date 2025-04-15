package com.example.nihongo.User.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nihongo.Admin.AdminLoginScreen
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.ui.screens.homepage.HomeScreen
import com.example.nihongo.User.ui.screens.login.LoginScreen
import com.example.nihongo.User.ui.screens.login.OTPScreen
import com.example.nihongo.User.ui.screens.login.RegisterScreen

@Composable
fun AppNavGraph(navController: NavHostController, userRepo: UserRepository) {
    NavHost(navController = navController, startDestination = NavigationRoutes.LOGIN) {
        composable(NavigationRoutes.LOGIN) {
            LoginScreen(navController, userRepo)
        }
        composable(NavigationRoutes.REGISTER) {
            RegisterScreen(navController, userRepo)
        }
        composable("home/{user_email}") { backStackEntry ->
            val user_email = backStackEntry.arguments?.getString("user_email") ?: ""
            HomeScreen(navController = navController, user_email = user_email, userRepository = userRepo)
        }

        composable("admin_login") {
            AdminLoginScreen(navController)
        }
        composable("otp_screen") {
            val expectedOtp = navController.previousBackStackEntry?.savedStateHandle?.get<String>("expectedOtp") ?: ""
            val user_email = navController.previousBackStackEntry?.savedStateHandle?.get<String>("user_email") ?: ""
            OTPScreen(navController = navController, expectedOtp = expectedOtp, user_email = user_email)
        }


    }
}
