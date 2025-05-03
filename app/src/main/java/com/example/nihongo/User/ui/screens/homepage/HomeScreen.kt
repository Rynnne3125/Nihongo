package com.example.nihongo.User.ui.screens.homepage

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nihongo.User.data.models.Course
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.models.UserProgress
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.ui.components.BottomNavItem
import com.example.nihongo.User.ui.components.BottomNavigationBar
import com.example.nihongo.User.ui.components.TopBarIcon
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    user_email: String,
    userRepository: UserRepository,
    courseRepository: CourseRepository
) {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var courseList by remember { mutableStateOf<List<Course>>(emptyList()) }
    val selectedItem = "home"
    var searchQuery by remember { mutableStateOf("") }
    val filteredCourses = courseList.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }
    var userProgressList by remember { mutableStateOf<List<UserProgress>>(emptyList()) }


    LaunchedEffect(user_email) {
        currentUser = userRepository.getUserByEmail(user_email)
        userProgressList = currentUser?.let {
            userRepository.getAllUserProgress(it.id)
        } ?: emptyList()
        courseList = courseRepository.getAllCourses()
    }

    val imageUrls = courseList.map { it.imageRes }
    var currentImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(imageUrls) {
        while (true) {
            delay(3000)
            currentImageIndex = (currentImageIndex + 1) % imageUrls.size
        }
    }

    Scaffold(
        containerColor = Color(0xFFEEEEEE),
        topBar = {
            TopAppBar(
                title = {
                    currentUser?.let {
                        Column {
                            Text("\uD83D\uDC4B こんにちわ ${it.username} さん", style = MaterialTheme.typography.bodyLarge)
                            if (it.isVip) {
                                Text("\u2B50 VIP です!", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFC107))
                            }
                        }
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
                        navController.navigate("profile/$user_email") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Avatar")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedItem = selectedItem,
                userEmail = user_email,
                onItemSelected = { selectedRoute ->
                    navController.navigate("$selectedRoute/$user_email") {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        model = imageUrls.getOrNull(currentImageIndex),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Brush.verticalGradient(listOf(Color(0x80000000), Color.Transparent)))
                            .padding(16.dp)
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("What course are you looking for?") }
                        ,
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    val course = filteredCourses.firstOrNull()
                                    course?.let {
                                        navController.navigate("courses/${course.id}/$user_email")
                                    }
                                }) {
                                    Icon(Icons.Default.Search, contentDescription = "Go to Course")
                                }
                            }
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (searchQuery.isNotBlank()) {
                items(filteredCourses) { course ->
                    CourseCard(course = course, onClick = {
                        navController.navigate("courses/${course.id}/$user_email")
                    })
                }
            } else {
                items(courseList) { course ->
                    val progress = userProgressList.find { it.courseId == course.id }
                    if (progress != null) {
                        CourseProgressCard(
                            course = course,
                            userProgress = progress,
                            onContinueClick = {
                                navController.navigate("lessons/${course.id}/$user_email")
                            }
                        )
                    }
                }
                item {
                    Text("Trending Course", style = MaterialTheme.typography.titleMedium)
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        courseList.forEach { course ->
                            CourseCard(course = course, onClick = {
                                navController.navigate("courses/${course.id}/$user_email")
                            })
                        }
                    }
                }
                item {
                    Text("Your Learning Tools", style = MaterialTheme.typography.titleMedium)
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { MiniFeatureCard("Progress", Icons.Filled.HourglassEmpty) }
                        item {
                            MiniFeatureCard("Courses", Icons.Filled.School, modifier = Modifier.clickable {
                                navController.navigate(BottomNavItem.Courses.routePattern)
                            })
                        }
                        item { MiniFeatureCard("Flashcards", Icons.Filled.ViewAgenda) }
                        item { MiniFeatureCard("Exercise", Icons.Filled.FitnessCenter) }
                    }
                }
//                item {
//                    Text("Flashcard of the Day", style = MaterialTheme.typography.titleMedium)
//                    Spacer(modifier = Modifier.height(8.dp))
//                    FlashcardPager(
//                        listOf(
//                            "日本語" to "Japanese language",
//                            "ありがとう" to "Thank you",
//                            "学校" to "School",
//                            "学生" to "Student",
//                            "先生" to "Teacher"
//                        )
//                    )
//                }
            }
        }
    }
}



@Composable
fun FlashcardPager(vocabList: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            vocabList.forEach { (term, definition) ->
                FlipFlashcard(term = term, definition = definition)
            }
        }
    }
}



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FlipFlashcard(term: String, definition: String) {
    var isFlipped by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(65.dp)
            .height(60.dp)
            .clickable { isFlipped = !isFlipped },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = isFlipped,
                transitionSpec = {
                    fadeIn() + scaleIn() with fadeOut() + scaleOut()
                },
                label = "Flip Flashcard"
            ) { flipped ->
                if (flipped) {
                    Text(
                        text = definition,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF333333)
                    )
                } else {
                    Text(
                        text = term,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}



@Composable
fun CourseCard(
    course: Course,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Log.d("CourseCard", "imageRes ID: ${course.imageRes}")
    Box(
        modifier = modifier
            .width(250.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.LightGray)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = course.imageRes,
            contentDescription = "Course Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        if (course.isVip) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "VIP Course",
                tint = Color(0xFFFFD700),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                    .padding(6.dp)
            )
        }

        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                .padding(8.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = course.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = course.description,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text("${course.likes}", color = Color.White, fontSize = 12.sp)

                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text("${course.reviews}", color = Color.White, fontSize = 12.sp)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${course.rating}",
                    style = MaterialTheme.typography.bodySmall
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
@Composable
fun CourseProgressCard(
    course: Course,
    userProgress: UserProgress,
    onContinueClick: () -> Unit
) {
    val progressPercent = (userProgress.progress * 100).toInt()
    val completed = userProgress.completedLessons.size
    val total = userProgress.totalLessons

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = course.imageRes,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "$completed/$total lesson",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(48.dp) // Đảm bảo Box có cùng kích thước với CircularProgressIndicator
                ) {
                    CircularProgressIndicator(
                        progress = (userProgress.progress),  // Chuyển đổi sang giá trị từ 0 đến 1
                        modifier = Modifier.size(48.dp),
                        color = Color(0xFF4CAF50),
                        strokeWidth = 4.dp
                    )

                    Text(
                        text = "$progressPercent%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinueClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent // Dùng nền trong suốt
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF006FD1), // Xanh biển (turquoise)
                                Color(0xFF01A810)  // Xanh lá
                            )
                        )
                        ,
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clip(RoundedCornerShape(30.dp)), // để không vượt ra khỏi góc bo
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = "Continue",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun MiniFeatureCard(title: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(50),
        tonalElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(16.dp),
                tint = Color.Black.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
