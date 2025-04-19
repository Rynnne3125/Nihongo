package com.example.nihongo.User.ui.screens.homepage

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nihongo.User.data.models.Course
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.ui.components.BottomNavigationBar

@Composable
fun HomeScreen(navController: NavController, user_email: String, userRepository: UserRepository, courseRepository: CourseRepository) {

    var currentUser by remember { mutableStateOf<User?>(null) }
    var courseList by remember { mutableStateOf<List<Course>>(emptyList()) }

    // Lấy thông tin người dùng và danh sách khóa học từ Firestore
    LaunchedEffect(user_email) {
        currentUser = userRepository.getUserByEmail(user_email)
        courseList = courseRepository.getAllCourses() // Dùng repository để lấy khóa học
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")

                if (currentUser != null) {
                    Text("👋 Hello ${currentUser!!.username}")
                    if (currentUser!!.isVip) {
                        Text(text = "⭐ Bạn là VIP!")
                    }
                }

                Icon(imageVector = Icons.Default.Person, contentDescription = "Avatar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFA8E6CF), Color(0xFFDCEDC1))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text("NihonGo Study", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEDEDED), shape = RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("What course are you looking for?", color = Color.Gray)
                        }
                    }
                }
            }

            Text("Recent Lessons")
            LessonCard(title = "Lesson 1: Greetings", difficulty = 1)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Your Learning Tools", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    MiniFeatureCard("Progress", Icons.Filled.HourglassEmpty)
                }
                item {
                    MiniFeatureCard("Courses", Icons.Filled.School, modifier = Modifier.clickable {
                        navController.navigate("courses")
                    })
                }
                item {
                    MiniFeatureCard("Flashcards", Icons.Filled.ViewAgenda)
                }
                item {
                    MiniFeatureCard("Exercise", Icons.Filled.FitnessCenter)
                }
            }

            Text("Flashcard of the Day")
            FlashcardCard(term = "日本語", definition = "Japanese language")

            Spacer(modifier = Modifier.height(24.dp))

            Text("Trending Course", style = MaterialTheme.typography.titleMedium)

            // Hiển thị danh sách các khóa học
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                courseList.forEach { course ->
                    CourseCard(
                        course = course,
                        onClick = {
                            navController.navigate("courses/${course.id}") // Điều hướng đến chi tiết khóa học
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FlashcardCard(term: String, definition: String) {

}

@Composable
fun LessonCard(title: String, difficulty: Int) {

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
            .background(Color.LightGray) // fallback color
            .clickable { onClick() }
    ) {

        // Background Image
        Image(
            painter = painterResource(id = course.imageRes), // Assuming the resource is PNG/JPG
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
        )



        // Top-left: VIP Icon (Star) if course is VIP
        if (course.isVip) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "VIP Course",
                tint = Color(0xFFFFD700), // Vàng gold đậm hơn, dễ nhận biết VIP
                modifier = Modifier
                    .align(Alignment.TopStart) // Align top-left
                    .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                    .padding(6.dp)
            )
        }

        // Top-right: Open icon (OpenInNew)
        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd) // Align top-right
                .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                .padding(8.dp)
        )

        // Gradient overlay + Text
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

        // Bottom-right: Rating
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
fun MiniFeatureCard(title: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(50),
        tonalElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
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
                maxLines = 1, // prevent wrapping
                overflow = TextOverflow.Ellipsis // optional: add "..." if too long
            )
        }
    }
}
