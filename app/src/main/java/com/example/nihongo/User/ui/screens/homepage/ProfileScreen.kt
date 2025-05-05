package com.example.nihongo.User.ui.screens.homepage


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nihongo.User.data.models.Course
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.models.UserProgress
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.ui.components.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userEmail: String,
    userRepository: UserRepository,
    courseRepository: CourseRepository,
    onSignOut: () -> Unit,
) {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var userProgressList by remember { mutableStateOf<List<UserProgress>>(emptyList()) }
    var courseList by remember { mutableStateOf<List<Course>>(emptyList()) }
    val selectedItem = "profile" // xác định tab đang được chọn

    LaunchedEffect(Unit) {
        val user = userRepository.getUserByEmail(userEmail)
        currentUser = user
        user?.let {
            userProgressList = userRepository.getAllUserProgress(it.id)
        }
        courseList = courseRepository.getAllCourses()
    }

    Scaffold(
        containerColor = Color(0xFFEEEEEE),
        topBar = {
            TopAppBar(
                title = {
                    currentUser?.let {
                        Column {
                            Text("👋 こんにちわ ${it.username} さん", style = MaterialTheme.typography.bodyLarge)
                            if (it.vip) {
                                Text("⭐ VIP です!", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFC107))
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("community/$userEmail") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Icon(Icons.Default.Group, contentDescription = "Notifications")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedItem = selectedItem,
                userEmail = userEmail,
                onItemSelected = { selectedRoute ->
                    navController.navigate("$selectedRoute/$userEmail") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

        }
    ) { padding ->
        currentUser?.let { user ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .background(Color.Transparent)
                    .padding(16.dp)
            ) {
                // Avatar
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "User Icon",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                        .padding(20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // User Info
                Text(user.username, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(user.email, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                if (user.vip) {
                    Text("⭐ VIP Member", color = Color(0xFFFFC107), fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("My Courses", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                courseList.forEach { course ->
                    val progress = userProgressList.find { it.courseId == course.id }
                    if (progress != null) {
                        CourseProgressCard(
                            course = course,
                            userProgress = progress,
                            onContinueClick = {
                                navController.navigate("lessons/${course.id}/$userEmail")
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out", color = Color.White)
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
