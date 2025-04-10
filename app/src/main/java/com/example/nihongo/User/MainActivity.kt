package com.example.nihongo.User

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.nihongo.User.data.repository.AppDatabase
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.utils.AppNavGraph
import com.example.nihongo.ui.theme.NihongoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo Room và UserRepository
        val db = AppDatabase.getDatabase(this)
        val userRepo = UserRepository(db.userDao())

        setContent {
            NihongoTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController, userRepo = userRepo)
                }
            }
        }
    }
}
