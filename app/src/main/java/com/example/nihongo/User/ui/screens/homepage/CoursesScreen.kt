package com.example.nihongo.User.ui.screens.homepage

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nihongo.User.data.models.Course
import com.example.nihongo.User.data.models.Lesson
import com.example.nihongo.User.data.models.UserProgress
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.LessonRepository
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.ui.components.BottomNavigationBar
import com.example.nihongo.User.ui.components.TopBarIcon
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    navController: NavController,
    courseRepository: CourseRepository,
    userEmail: String
) {
    val allCourses = remember { mutableStateOf<List<Course>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val selectedItem = "courses"
    val filteredCourses = remember(searchQuery, allCourses.value) {
        if (searchQuery.isBlank()) allCourses.value
        else allCourses.value.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(true) {
        allCourses.value = courseRepository.getAllCourses()
    }

    Scaffold(
        containerColor = Color(0xFFEEEEEE),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search your course?",
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Gray.copy(alpha = 0.1f), // Set the background color to a light gray
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = {
                                        val course = filteredCourses.firstOrNull()
                                        course?.let {
                                            navController.navigate("courses/${course.id}/$userEmail")
                                        }
                                    }) {
                                        Icon(Icons.Default.Search, contentDescription = "Search")
                                    }
                                }
                            }
                        )
                    }
                },
                navigationIcon = {
                    TopBarIcon(selectedItem = selectedItem)
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = {
                        navController.navigate("profile/$userEmail") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Avatar")
                    }
                },
                modifier = Modifier.height(100.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
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
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredCourses) { course ->
                CourseCard(course = course, onClick = {
                    navController.navigate("courses/${course.id}/$userEmail")
                })
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    navController: NavController,
    courseRepository: CourseRepository,
    userRepository: UserRepository,
    lessonRepository: LessonRepository,
    userEmail: String
) {
    val course = remember { mutableStateOf<Course?>(null) }
    val isUserVip = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val lessons = remember { mutableStateOf<List<Lesson>>(emptyList()) }
    val context = LocalContext.current
    val selectedItem = "courses"

    // Fetch data
    LaunchedEffect(courseId) {
        course.value = courseRepository.getCourseById(courseId)
        lessons.value = lessonRepository.getLessonsByCourseId(courseId)
    }

    LaunchedEffect(Unit) {
        isUserVip.value = userRepository.isVip()
    }

    Scaffold(
        containerColor = Color(0xFFEEEEEE),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Course Details",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.DarkGray
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.DarkGray,
                    actionIconContentColor = Color.Gray
                )
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
        course.value?.let { course ->
            Column(modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = course.imageRes,
                    contentDescription = "Course Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(course.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(course.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rating: ${course.rating}", style = MaterialTheme.typography.bodyMedium)
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp).padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Comment, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${course.reviews} Reviews", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${course.likes} Likes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (course.isVip) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "VIP Icon",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp).padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (course.isVip && !isUserVip.value) {
                            Toast.makeText(context, "You need to be VIP to join this course", Toast.LENGTH_SHORT).show()
                        } else {
                            coroutineScope.launch {
                                val user = userRepository.getUserByEmail(userEmail)
                                user?.let {
                                    val newProgress = UserProgress(
                                        userId = it.id,
                                        courseId = course.id,
                                        completedLessons = emptyList(),
                                        completedExercises = emptyList(),
                                        passedExercises = emptyList(),
                                        totalLessons = lessons.value.size,
                                        progress = 0.0f,
                                        lastUpdated = System.currentTimeMillis(),
                                        courseTitle = course.title,
                                        currentLessonId = null
                                    )
                                    userRepository.saveUserProgress(it.id, newProgress)
                                    navController.navigate("lessons/${course.id}/$userEmail")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Join Course")
                }
            }
        } ?: Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding), contentAlignment = Alignment.Center) {
            Text("Loading or course not found...", color = Color.Gray)
        }
    }
}
