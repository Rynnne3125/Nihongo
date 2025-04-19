package com.example.nihongo.User.ui.screens.homepage

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nihongo.User.data.models.Course
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.UserRepository

@Composable
fun CoursesScreen(navController: NavController, courseRepository: CourseRepository) {
    val courses = remember { mutableStateOf<List<Course>>(emptyList()) } // Sử dụng mutableStateOf thay vì mutableStateListOf

    LaunchedEffect(true) {
        courses.value = courseRepository.getAllCourses() // ✅ gọi hàm suspend đúng cách
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(courses.value) { course ->
            CourseCard(course = course, onClick = {
                navController.navigate("courses/${course.id}") // Điều hướng đến khóa học chi tiết
            })
        }
    }
}

@Composable
fun CourseDetailScreen(
    courseId: String?,  // Now accepting courseId as String
    navController: NavController,
    courseRepository: CourseRepository,
    userRepository: UserRepository
) {
    val course = remember { mutableStateOf<Course?>(null) }
    val isUserVip = remember { mutableStateOf(false) }  // Biến để lưu trạng thái VIP

    val context = LocalContext.current

    // Lấy dữ liệu course từ repository
    LaunchedEffect(courseId) {
        // Ensure courseId is not null and fetch the course with String ID
        course.value = courseId?.let { courseRepository.getCourseById(it) }
    }

    // Lấy trạng thái VIP của user từ repository
    LaunchedEffect(Unit) {
        isUserVip.value = userRepository.isVip()
    }

    course.value?.let { course ->
        Column(modifier = Modifier.padding(16.dp)) {

            // Hình ảnh khóa học
            Image(
                painter = painterResource(id = course.imageRes),
                contentDescription = "Course Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tiêu đề khóa học
            Text(
                text = course.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mô tả khóa học
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Đánh giá khóa học
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rating: ${course.rating}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star Icon",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hiển thị số lượng đánh giá và lượt thích
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Reviews Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${course.reviews} Reviews",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Likes Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${course.likes} Likes",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hiển thị VIP nếu là khóa học VIP
            if (course.isVip) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "VIP Icon",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút tham gia khóa học
            Button(
                onClick = {
                    if (course.isVip && !isUserVip.value) {
                        Toast.makeText(context, "You need to be VIP to join this course", Toast.LENGTH_SHORT).show()
                    } else {
                        navController.navigate("lessons/${course.id}")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join Course")
            }
        }
    } ?: run {
        Text("Loading or course not found...", color = Color.Gray)
    }
}
