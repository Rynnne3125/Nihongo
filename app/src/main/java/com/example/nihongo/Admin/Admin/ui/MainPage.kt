package com.example.nihongo.Admin.ui

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.nihongo.Admin.viewmodel.AdminMainPageViewModel
import com.example.nihongo.User.data.models.Course
import com.example.nihongo.User.data.models.User
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun MainPage(navController: NavHostController) {
    val innerNavController = rememberNavController()
    val selectedItemIndex = remember { mutableStateOf(0) }
    val adminViewModel: AdminMainPageViewModel = viewModel()

    val showTopBar = remember { mutableStateOf(true) }
    val showBottomBar = remember { mutableStateOf(true) }

    // Assign global state
    UIVisibilityController.showTopBarState = showTopBar
    UIVisibilityController.showBottomBarState = showBottomBar

//    FirebaseMessaging.getInstance().unsubscribeFromTopic("all")
//        .addOnCompleteListener { task ->
//            var msg = "Unsubscribed from topic"
//            if (!task.isSuccessful) {
//                msg = "Unsubscription failed"
//            }
//            Log.d("FCM", msg)
//        }


    Scaffold(
        topBar = { if (showTopBar.value) TopBar() },
        bottomBar = { if (showBottomBar.value) BottomNavigationBar(innerNavController, selectedItemIndex)}
    ) { padding ->
        NavHost(innerNavController, startDestination = "mainPage") {
            composable("mainPage") {
                AdminDashboard(
                    modifier = Modifier.padding(padding),
                    viewModel = adminViewModel
                )
                UIVisibilityController.enableDisplayTopBarAndBottom()
            }
            composable("CoursePage") {
                UIVisibilityController.enableDisplayTopBarAndBottom()
                CoursePage()
            }
            composable("CommunityPage") {
                UIVisibilityController.enableDisplayTopBarAndBottom()
                NotifyPage()
            }
            composable("FlashcardPage") {
                UIVisibilityController.disableDisplayTopBar()
                FlashcardPage()
            }
            composable("userPage") {
                UIVisibilityController.enableDisplayTopBarAndBottom()
                userPage()
            }
        }
    }
}

@Composable
fun AdminDashboard(modifier: Modifier = Modifier, viewModel: AdminMainPageViewModel) {
    val isLoading by viewModel.isLoading

    val monthlyUserCount by viewModel.monthlyUserCount.collectAsState()
    val topUsers by viewModel.topUsers.collectAsState()
    val topCourses by viewModel.topCourses.collectAsState()
    val totalCourseCount by viewModel.totalCourseCount.collectAsState()
    val vipMemberCount by viewModel.vipMemberCount.collectAsState()
    val mostEnrolledCourses by viewModel.mostEnrolledCourses.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SearchBar()
        }

        item {
            DashboardHeader(totalCourseCount, vipMemberCount)
        }

        item {
            Text(
                "Monthly User Growth",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            MonthlyUserChart(monthlyUserCount)
        }

        item {
            Text(
                "Top 5 Active Users",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            TopUsersSection(topUsers)
        }

        item {
            Text(
                "Most Liked Courses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            TopCoursesSection(topCourses)
        }

        item {
            Text(
                "Most Enrolled Courses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            MostEnrolledCoursesSection(mostEnrolledCourses)
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun DashboardHeader(totalCourseCount: Int, vipMemberCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.MenuBook,
            title = "Total Courses",
            value = totalCourseCount.toString(),
            backgroundColor = Color(0xFFE3F2FD)
        )

        Spacer(modifier = Modifier.width(16.dp))

        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Star,
            title = "VIP Members",
            value = vipMemberCount.toString(),
            backgroundColor = Color(0xFFFFF8E1)
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    backgroundColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MonthlyUserChart(monthlyData: Map<String, Int>) {
    val primaryColor = MaterialTheme.colorScheme.primary

    if (monthlyData.isEmpty()) {
        EmptyStateCard("No monthly user data available")
        return
    }

    val sortedData = monthlyData.toList()
    val maxValue = sortedData.maxOfOrNull { it.second } ?: 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Draw the chart
            Canvas(
                modifier = Modifier.fillMaxWidth().height(150.dp)
            ) {
                val barWidth = size.width / sortedData.size
                val heightRatio = size.height / maxValue.toFloat()

                // Draw horizontal grid lines
                val gridLineCount = 5
                val gridLineSpacing = size.height / gridLineCount

                for (i in 0..gridLineCount) {
                    val y = size.height - (i * gridLineSpacing)
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                // Draw bars
                sortedData.forEachIndexed { index, (_, value) ->
                    val barHeight = value * heightRatio
                    val x = index * barWidth + barWidth / 4

                    drawLine(
                        color = primaryColor,
                        start = Offset(x + barWidth / 2, size.height),
                        end = Offset(x + barWidth / 2, size.height - barHeight),
                        strokeWidth = barWidth / 2,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw month labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .offset(x = (10).dp,y = (16).dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                sortedData.forEach { (month, _) ->
                    Text(
                        text = month,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TopUsersSection(users: List<User>) {
    if (users.isEmpty()) {
        EmptyStateCard("No user data available")
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            users.forEachIndexed { index, user ->
                UserRankItem(
                    rank = index + 1,
                    user = user,
                    modifier = Modifier.fillMaxWidth()
                )

                if (index < users.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun UserRankItem(rank: Int, user: User, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(32.dp)
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.username.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (user.vip) "VIP Member" else "Standard Member",
                style = MaterialTheme.typography.bodySmall,
                color = if (user.vip) Color(0xFFFFB300) else Color.Gray
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${user.activityPoints}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Points",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun TopCoursesSection(courses: List<Course>) {
    if (courses.isEmpty()) {
        EmptyStateCard("No course data available")
        return
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(courses) { course ->
            CourseCard(
                course = course,
                modifier = Modifier.width(240.dp)
            )
        }
    }
}

@Composable
fun CourseCard(course: Course, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                // Thay Icon bằng hình ảnh từ Coil
                AsyncImage(
                    model = course.imageRes, // có thể là URL hoặc resource ID
                    contentDescription = "Course Image",
                    modifier = Modifier.size(240.dp), // bạn có thể điều chỉnh size theo ý
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = course.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFB300)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = course.rating.toString(),
                    style = MaterialTheme.typography.bodySmall,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "(${course.reviews})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Likes",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Red
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${course.likes} likes",
                    style = MaterialTheme.typography.bodySmall,
                )

                Spacer(modifier = Modifier.weight(1f))

                if (course.vip) {
                    Surface(
                        color = Color(0xFFFFB300),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.offset(y = (-5).dp)
                    ) {
                        Text(
                            text = "VIP",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MostEnrolledCoursesSection(courseEnrollments: List<AdminMainPageViewModel.CourseEnrollment>) {
    if (courseEnrollments.isEmpty()) {
        EmptyStateCard("No enrollment data available")
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            courseEnrollments.forEachIndexed { index, enrollment ->
                CourseEnrollmentItem(
                    rank = index + 1,
                    enrollment = enrollment,
                    modifier = Modifier.fillMaxWidth()
                )

                if (index < courseEnrollments.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun CourseEnrollmentItem(
    rank: Int,
    enrollment: AdminMainPageViewModel.CourseEnrollment,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(32.dp)
        )

        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = "Course",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = enrollment.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${enrollment.enrollmentCount}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "users",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("NIHONGO", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Row {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", modifier = Modifier.padding(end = 16.dp))
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    // Store TextField value
    var text by remember { mutableStateOf("") }

    // Animation for TextField color
    val textFieldColor by animateColorAsState(
        targetValue = if (text.isEmpty()) Color(0xFFF0F0F0) else Color(0xFFE0E0E0),
        animationSpec = tween(durationMillis = 500)
    )

    TextField(
        value = text,
        onValueChange = { newText -> text = newText },
        placeholder = { Text("Search...") },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(bottom = 8.dp),
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search Icon")
        },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = textFieldColor // Use animated color
        )
    )
}

@Composable
fun BottomNavigationBar(navController: NavController, selectedItemIndex: MutableState<Int>) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 10.sp) },
            selected = selectedItemIndex.value == 0,
            onClick = {
                navController.navigate("mainPage") {
                    launchSingleTop = true
                }
                selectedItemIndex.value = 0 // Update index when item is selected
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Folder, contentDescription = "Courses") },
            label = { Text("Courses" , fontSize = 10.sp) },
            selected = selectedItemIndex.value == 1,
            onClick = {
                navController.navigate("CoursePage") {
                    launchSingleTop = true
                }
                selectedItemIndex.value = 1 // Update index when item is selected
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LibraryBooks, contentDescription = "FlashCard") },
            label = { Text("FlashCard" , fontSize = 10.sp) },
            selected = selectedItemIndex.value == 2,
            onClick = {
                navController.navigate("FlashcardPage") {
                    launchSingleTop = true
                }
                selectedItemIndex.value = 2 // Update index when item is selected
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Notifications , contentDescription = "Notify") },
            label = { Text("Notify" , fontSize = 10.sp) },
            selected = selectedItemIndex.value == 3,
            onClick = {
                navController.navigate("CommunityPage") {
                    launchSingleTop = true
                }
                selectedItemIndex.value = 3 // Update index when item is selected
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Users") },
            label = { Text("Users" , fontSize = 10.sp) },
            selected = selectedItemIndex.value == 4,
            onClick = {
                navController.navigate("userPage") {
                    launchSingleTop = true
                }
                selectedItemIndex.value = 4 // Update index when item is selected
            }
        )
    }
}

// Global controller
object UIVisibilityController {
    var showTopBarState: MutableState<Boolean>? = null
    var showBottomBarState: MutableState<Boolean>? = null

    fun disableDisplayTopBar() {
        showTopBarState?.value = false
    }

    fun disableDisplayBottomBar() {
        showBottomBarState?.value = false
    }

    fun enableDisplayTopBar() {
        showTopBarState?.value = true
    }

    fun enableDisplayBottomBar() {
        showBottomBarState?.value = true
    }

    fun enableDisplayTopBarAndBottom() {
        showTopBarState?.value = true
        showBottomBarState?.value = true
    }

    fun disableDisplayTopBarAndBottom() {
        showTopBarState?.value = false
        showBottomBarState?.value = false
    }
}
