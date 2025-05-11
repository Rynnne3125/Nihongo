package com.example.nihongo.ui.screens.homepage

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nihongo.User.data.models.Course
import com.example.nihongo.User.data.models.Lesson
import com.example.nihongo.User.data.models.SubLesson
import com.example.nihongo.User.data.models.UnitItem
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.models.UserProgress
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.LessonRepository
import com.example.nihongo.User.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    courseId: String,
    userEmail: String,
    navController: NavController,
    lessonRepository: LessonRepository,
    courseRepository: CourseRepository,
    userRepository: UserRepository,
) {
    val lessons = remember { mutableStateOf<List<Lesson>>(emptyList()) }
    val course = remember { mutableStateOf<Course?>(null) }
    val isUserVip = remember { mutableStateOf(false) }
    val expandedLessons = remember { mutableStateMapOf<String, Boolean>() }
    val expandedUnits = remember { mutableStateMapOf<String, Boolean>() }
    var userProgress by remember { mutableStateOf<List<UserProgress>>(emptyList()) }
    var user by remember { mutableStateOf<User?>(null) }
    
    // Add tabs for different sections
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Bài học", "Tiến độ", "Tài liệu")
    
    // Add refresh trigger for data updates
    var refreshTrigger by remember { mutableStateOf(0) }
    
    fun refreshData() {
        refreshTrigger += 1
    }

    LaunchedEffect(courseId, refreshTrigger) {
        lessons.value = lessonRepository.getLessonsByCourseId(courseId)
        course.value = courseRepository.getCourseById(courseId)
        isUserVip.value = userRepository.isVip()
        user = userRepository.getUserByEmail(userEmail)
        userProgress = user?.let {
            listOfNotNull(userRepository.getUserProgressForCourse(it.id, courseId))
        } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = course.value?.title ?: "Bài học",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.DarkGray
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.DarkGray,
                    actionIconContentColor = Color.Gray
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Course header with image and basic info
            CourseHeader(course.value)
            
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = Color(0xFF00C853)
                    )
                },
                containerColor = Color.White,
                contentColor = Color(0xFF00C853)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            // Content based on selected tab
            when (selectedTab) {
                0 -> LessonsTab(
                    lessons = lessons.value,
                    userProgress = userProgress,
                    expandedLessons = expandedLessons,
                    expandedUnits = expandedUnits,
                    onSubLessonClick = { sub, lesson ->
                        navController.navigate("exercise/${course.value?.id}/${lesson.id}/${sub.id}/$userEmail")
                    }
                )
                1 -> ProgressTab(userProgress, lessons.value)
                2 -> MaterialsTab(course.value)
            }
        }
    }
}

@Composable
fun CourseHeader(course: Course?) {
    if (course == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray)
        )
        return
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Background image
        AsyncImage(
            model = course.imageRes,
            contentDescription = "Course Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )
        
        // Course info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            // VIP badge if applicable
            if (course.vip) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = Color(0xFFFFD700),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "VIP",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "VIP",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text(
                text = course.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${course.rating}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun LessonsTab(
    lessons: List<Lesson>,
    userProgress: List<UserProgress>,
    expandedLessons: MutableMap<String, Boolean>,
    expandedUnits: MutableMap<String, Boolean>,
    onSubLessonClick: (SubLesson, Lesson) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(lessons) { lesson ->
            val isCompleted = userProgress.any { progress ->
                progress.completedLessons.contains(lesson.id)
            }
            
            ModernLessonCard(
                lesson = lesson,
                isLessonCompleted = isCompleted,
                isExpanded = expandedLessons[lesson.id] == true,
                onToggleExpand = {
                    expandedLessons[lesson.id] = !(expandedLessons[lesson.id] ?: false)
                },
                expandedUnits = expandedUnits,
                onSubLessonClick = { sub -> onSubLessonClick(sub, lesson) }
            )
        }
        
        // Add some space at the bottom
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ModernLessonCard(
    lesson: Lesson,
    isLessonCompleted: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    expandedUnits: MutableMap<String, Boolean>,
    onSubLessonClick: (SubLesson) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Lesson header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lesson number circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isLessonCompleted) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLessonCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White
                        )
                    } else {
                        Text(
                            text = "${lesson.step}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.stepTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${lesson.completedUnits}/${lesson.totalUnits} đã hoàn thành",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color(0xFF4CAF50)
                )
            }
            
            // Progress indicator
            LinearProgressIndicator(
                progress = { 
                    if (lesson.totalUnits > 0) 
                        lesson.completedUnits.toFloat() / lesson.totalUnits.toFloat() 
                    else 0f 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0)
            )
            
            // Expanded content
            if (isExpanded) {
                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                
                // Lesson overview
                if (lesson.overview.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Tổng quan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = lesson.overview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                    
                    Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                }
                
                // Units
                lesson.units.forEach { unit ->
                    val unitKey = "${lesson.id}_${unit.unitTitle}"
                    ModernUnitCard(
                        unit = unit,
                        isExpanded = expandedUnits[unitKey] == true,
                        onClick = { expandedUnits[unitKey] = !(expandedUnits[unitKey] ?: false) },
                        onSubLessonClick = onSubLessonClick
                    )
                }
            }
        }
    }
}

@Composable
fun ModernUnitCard(
    unit: UnitItem,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onSubLessonClick: (SubLesson) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Unit header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = unit.unitTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = unit.progress,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = Color(0xFF4CAF50)
            )
        }
        
        // Sub-lessons
        if (isExpanded) {
            unit.subLessons.forEach { subLesson ->
                ModernSubLessonItem(
                    subLesson = subLesson,
                    onClick = { onSubLessonClick(subLesson) }
                )
            }
        }
    }
}

@Composable
fun ModernSubLessonItem(
    subLesson: SubLesson,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 56.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon based on type
        Icon(
            imageVector = when (subLesson.type) {
                "Video" -> Icons.Default.PlayCircle
                "Practice" -> Icons.Default.Edit
                "Quiz" -> Icons.Default.QuestionAnswer
                else -> Icons.Default.Description
            },
            contentDescription = null,
            tint = if (subLesson.isCompleted) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = subLesson.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (subLesson.isCompleted) Color(0xFF4CAF50) else Color.DarkGray,
            modifier = Modifier.weight(1f)
        )
        
        if (subLesson.isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Start",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ProgressTab(userProgress: List<UserProgress>, lessons: List<Lesson>) {
    // Calculate overall progress
    val totalLessons = lessons.size
    val completedLessons = userProgress.flatMap { it.completedLessons }.distinct().size
    val progressPercentage = if (totalLessons > 0) {
        (completedLessons.toFloat() / totalLessons.toFloat()) * 100
    } else 0f
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Overall progress card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Tiến độ tổng quan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Progress circle
                        CircularProgressIndicator(
                            progress = { progressPercentage / 100 },
                            modifier = Modifier.size(120.dp),
                            strokeWidth = 12.dp,
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFFE0E0E0)
                        )
                        
                        // Percentage text
                        Text(
                            text = "${progressPercentage.toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProgressStat(
                            value = completedLessons,
                            label = "Bài học đã hoàn thành",
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF4CAF50)
                        )
                        
                        ProgressStat(
                            value = totalLessons - completedLessons,
                            label = "Bài học còn lại",
                            icon = Icons.Default.Schedule,
                            color = Color(0xFFFFA000)
                        )
                    }
                }
            }
        }
        
        // Lesson progress header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Chi tiết tiến độ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Lesson progress items
        items(lessons) { lesson ->
            val isCompleted = userProgress.any { progress ->
                progress.completedLessons.contains(lesson.id)
            }
            
            LessonProgressItem(
                lesson = lesson,
                isCompleted = isCompleted
            )
        }
        
        // Add some space at the bottom
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ProgressStat(
    value: Int,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        modifier = Modifier
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun LessonProgressItem(
    lesson: Lesson,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = lesson.stepTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCompleted) Color(0xFF4CAF50) else Color.DarkGray
        )
    }
}

@Composable
fun MaterialsTab(course: Course?) {
    if (course == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        )
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tài liệu khóa học",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Add your materials content here
        // For example, you can list documents, videos, or other resources
        // This is a placeholder implementation
        val materials = listOf(
            "Bảng chữ Hiragana",
            "Bảng chữ Katakana"
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(materials) { material ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Handle material click */ }
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = material,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                }
            }
            
            // Add some space at the bottom
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
// Hàm push Firestore
suspend fun addLessonToCourse(courseId: String, lesson: Lesson) {
    val firestore = FirebaseFirestore.getInstance()
    val lessonsCollection = firestore.collection("lessons")

    val generatedId = lesson.id.ifEmpty { lessonsCollection.document().id }
    val lessonWithId = lesson.copy(id = generatedId, courseId = courseId)

    // Sinh ID cho SubLesson
    val finalLesson = generateSubLessonIds(lessonWithId)

    // Push lesson lên Firestore
    lessonsCollection.document(generatedId).set(finalLesson).await()
}


fun generateSubLessonIds(lesson: Lesson): Lesson {
    val firestore = FirebaseFirestore.getInstance()

    val updatedUnits = lesson.units.map { unit ->
        val updatedSubLessons = unit.subLessons.map { subLesson ->
            val generatedId = subLesson.id.ifEmpty { firestore.collection("sublessons").document().id }
            subLesson.copy(id = generatedId)
        }
        unit.copy(subLessons = updatedSubLessons)
    }

    return lesson.copy(units = updatedUnits)
}

//fun createSampleLessons(): List<Lesson> {
//    return listOf(
//        Lesson(
//            id = "1",
//            courseId = "1",
//            step = 1,
//            stepTitle = "Bước 1: Giới thiệu về Hiragana",
//            overview = """
//            Tổng quan bảng chữ Hiragana.
//
//            2/2 | 3:20 minutes
//
//            Cùng tìm hiểu nguồn gốc và vai trò của Hiragana trong tiếng Nhật.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Giới thiệu Hiragana",
//                    progress = "0/2",
//                    subLessons = listOf(
//                        SubLesson(id = "1-1", title = "Hiragana là gì?", type = "Video", isCompleted = false),
//                        SubLesson(id = "1-2", title = "Tại sao nên học Hiragana?", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "2",
//            courseId = "1",
//            step = 2,
//            stepTitle = "Bước 2: Học hàng あ",
//            overview = """
//            Học các chữ cái: あ, い, う, え, お
//
//            3/3 | 5:40 minutes
//
//            Luyện cách viết và đọc.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Hàng あ",
//                    progress = "0/3",
//                    subLessons = listOf(
//                        SubLesson(id = "2-1", title = "Phát âm và cách viết あ-い-う-え-お", type = "Video", isCompleted = false),
//                        SubLesson(id = "2-2", title = "Từ vựng với あ hàng", type = "Practice", isCompleted = false),
//                        SubLesson(id = "2-3", title = "Luyện viết hàng あ", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "3",
//            courseId = "1",
//            step = 3,
//            stepTitle = "Bước 3: Học hàng か",
//            overview = """
//            Học các chữ cái: か, き, く, け, こ
//
//            3/3 | 5:50 minutes
//
//            Phát âm và luyện viết với ví dụ từ vựng.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Hàng か",
//                    progress = "0/3",
//                    subLessons = listOf(
//                        SubLesson(id = "3-1", title = "Phát âm và cách viết か-き-く-け-こ", type = "Video", isCompleted = false),
//                        SubLesson(id = "3-2", title = "Từ vựng với か hàng", type = "Practice", isCompleted = false),
//                        SubLesson(id = "3-3", title = "Luyện viết hàng か", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "4",
//            courseId = "1",
//            step = 4,
//            stepTitle = "Bước 4: Học hàng さ",
//            overview = """
//            Học các chữ cái: さ, し, す, せ, そ
//
//            3/3 | 6:00 minutes
//
//            Tập phát âm và luyện viết chữ rõ ràng.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Hàng さ",
//                    progress = "0/3",
//                    subLessons = listOf(
//                        SubLesson(id = "4-1", title = "Phát âm và cách viết さ-し-す-せ-そ", type = "Video", isCompleted = false),
//                        SubLesson(id = "4-2", title = "Từ vựng với さ hàng", type = "Practice", isCompleted = false),
//                        SubLesson(id = "4-3", title = "Luyện viết hàng さ", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "5",
//            courseId = "1",
//            step = 5,
//            stepTitle = "Bước 5: Học hàng た",
//            overview = """
//            Học các chữ cái: た, ち, つ, て, と
//
//            3/3 | 6:05 minutes
//
//            Hướng dẫn đọc và luyện từ vựng ứng dụng.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Hàng た",
//                    progress = "0/3",
//                    subLessons = listOf(
//                        SubLesson(id = "5-1", title = "Phát âm và cách viết た-ち-つ-て-と", type = "Video", isCompleted = false),
//                        SubLesson(id = "5-2", title = "Từ vựng với た hàng", type = "Practice", isCompleted = false),
//                        SubLesson(id = "5-3", title = "Luyện viết hàng た", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "6",
//            courseId = "1",
//            step = 6,
//            stepTitle = "Bước 6: Học hàng な",
//            overview = """
//            Học hàng な: な, に, ぬ, ね, の
//
//            3/3 | 6:10 minutes
//
//            Tập phát âm và từ vựng ví dụ.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Hàng な",
//                    progress = "0/3",
//                    subLessons = listOf(
//                        SubLesson(id = "6-1", title = "Phát âm và cách viết な-に-ぬ-ね-の", type = "Video", isCompleted = false),
//                        SubLesson(id = "6-2", title = "Từ vựng với な hàng", type = "Practice", isCompleted = false),
//                        SubLesson(id = "6-3", title = "Luyện viết hàng な", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "7",
//            courseId = "1",
//            step = 7,
//            stepTitle = "Bước 7: Học hàng は",
//            overview = """
//            Học hàng は: は, ひ, ふ, へ, ほ
//
//            3/3 | 6:45 minutes
//
//            Luyện phát âm, ví dụ từ vựng.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Hàng は",
//                    progress = "0/3",
//                    subLessons = listOf(
//                        SubLesson(id = "7-1", title = "Phát âm và cách viết は-ひ-ふ-へ-ほ", type = "Video", isCompleted = false),
//                        SubLesson(id = "7-2", title = "Từ vựng với は hàng", type = "Practice", isCompleted = false),
//                        SubLesson(id = "7-3", title = "Luyện viết hàng は", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "8",
//            courseId = "1",
//            step = 8,
//            stepTitle = "Bước 8: Học hàng ま",
//            overview = """
//            Học hàng ま: ま, み, む, め, も
//
//            3/3 | 6:20 minutes
//
//            Phát âm rõ ràng và luyện từ vựng thực hành.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Hàng ま",
//                    progress = "0/3",
//                    subLessons = listOf(
//                        SubLesson(id = "8-1", title = "Phát âm và cách viết ま-み-む-め-も", type = "Video", isCompleted = false),
//                        SubLesson(id = "8-2", title = "Từ vựng với ま hàng", type = "Practice", isCompleted = false),
//                        SubLesson(id = "8-3", title = "Luyện viết hàng ま", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "9",
//            courseId = "1",
//            step = 9,
//            stepTitle = "Bước 9: Học hàng や",
//            overview = """
//            Học hàng や: や, ゆ, よ
//
//            3/3 | 5:00 minutes
//
//            Ít ký tự, dễ học nhanh và luyện viết.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Hàng や",
//                    progress = "0/3",
//                    subLessons = listOf(
//                        SubLesson(id = "9-1", title = "Phát âm và cách viết や-ゆ-よ", type = "Video", isCompleted = false),
//                        SubLesson(id = "9-2", title = "Từ vựng với や hàng", type = "Practice", isCompleted = false),
//                        SubLesson(id = "9-3", title = "Luyện viết hàng や", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "10",
//            courseId = "1",
//            step = 10,
//            stepTitle = "Bước 10: Học hàng ら",
//            overview = """
//            Học hàng ら: ら, り, る, れ, ろ
//
//            3/3 | 6:15 minutes
//
//            Thực hành phát âm chuẩn và ví dụ cơ bản.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Hàng ら",
//                    progress = "0/3",
//                    subLessons = listOf(
//                        SubLesson(id = "10-1", title = "Phát âm và cách viết ら-り-る-れ-ろ", type = "Video", isCompleted = false),
//                        SubLesson(id = "10-2", title = "Từ vựng với ら hàng", type = "Practice", isCompleted = false),
//                        SubLesson(id = "10-3", title = "Luyện viết hàng ら", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "11",
//            courseId = "1",
//            step = 11,
//            stepTitle = "Bước 11: Học hàng わ",
//            overview = """
//            Học hàng わ: わ, を, ん
//
//            3/3 | 5:40 minutes
//
//            Ký tự đặc biệt và âm kết thúc câu.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Hàng わ",
//                    progress = "0/3",
//                    subLessons = listOf(
//                        SubLesson(id = "11-1", title = "Phát âm và cách viết わ-を-ん", type = "Video", isCompleted = false),
//                        SubLesson(id = "11-2", title = "Từ vựng với わ hàng", type = "Practice", isCompleted = false),
//                        SubLesson(id = "11-3", title = "Luyện viết hàng わ", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "12",
//            courseId = "1",
//            step = 12,
//            stepTitle = "Bước 12: Luyện tập tổng hợp",
//            overview = """
//            Tổng hợp toàn bộ Hiragana đã học.
//
//            2/2 | 8:00 minutes
//
//            Nghe - đọc - viết ứng dụng thực tế.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Tổng ôn tập",
//                    progress = "0/2",
//                    subLessons = listOf(
//                        SubLesson(id = "12-1", title = "Luyện nghe và đọc", type = "Video", isCompleted = false),
//                        SubLesson(id = "12-2", title = "Kiểm tra từ vựng & chữ viết", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "13",
//            courseId = "1",
//            step = 13,
//            stepTitle = "Bước 13: Thử thách mini game",
//            overview = """
//            Game hóa kiểm tra kiến thức Hiragana.
//
//            1/1 | 5:30 minutes
//
//            Kết hợp âm thanh, hình ảnh và phản xạ.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Mini game luyện phản xạ",
//                    progress = "0/1",
//                    subLessons = listOf(
//                        SubLesson(id = "13-1", title = "Game phản xạ chữ Hiragana", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        ),
//        Lesson(
//            id = "14",
//            courseId = "1",
//            step = 14,
//            stepTitle = "Bước 14: Bài kiểm tra tổng kết",
//            overview = """
//            Kiểm tra đánh giá tổng hợp Hiragana.
//
//            1/1 | 10:00 minutes
//
//            Làm bài test và nhận kết quả.
//        """.trimIndent(),
//            totalUnits = 1,
//            completedUnits = 0,
//            units = listOf(
//                UnitItem(
//                    unitTitle = "Bài kiểm tra tổng hợp",
//                    progress = "0/1",
//                    subLessons = listOf(
//                        SubLesson(id = "14-1", title = "Làm bài kiểm tra tổng kết", type = "Practice", isCompleted = false)
//                    )
//                )
//            )
//        )
//    )
//}
//
//
//fun addExercisesToLesson(lesson: Lesson) {
//    val db = FirebaseFirestore.getInstance()
//    val exercisesRef = db.collection("lessons").document(lesson.id).collection("exercises")
//
//    // Bước 1: Xóa toàn bộ exercise cũ
//    exercisesRef.get().addOnSuccessListener { querySnapshot ->
//        val batch = db.batch()
//
//        // Duyệt qua tất cả các tài liệu trong collection để xóa chúng
//        for (doc in querySnapshot.documents) {
//            batch.delete(doc.reference)
//        }
//
//        // Sau khi batch delete xong, commit để thực thi xóa
//        batch.commit().addOnSuccessListener {
//            println("🧹 All old exercises in lesson '${lesson.id}' deleted.")
//
//            // Bước 2: Thêm mới exercise sau khi đã xóa xong
//            lesson.units.forEach { unitItem ->
//                unitItem.subLessons.forEach { subLesson ->
//                    // Lọc exercise theo type của subLesson (ví dụ: "Video" hoặc "Quiz")
//                    val exercises = generateExercisesForSubLesson(subLesson, subLesson.id)
//
//                    exercises.forEach { exercise ->
//                        // Kiểm tra nếu exercise type phù hợp với subLesson type và subLessonId khớp
//                        if (exercise.type == ExerciseType.from(subLesson.type) && exercise.subLessonId == subLesson.id) {
//                            // Tạo exerciseData mà không cần id vì Firestore sẽ tự tạo ID
//                            val exerciseData = hashMapOf(
//                                "subLessonId" to subLesson.id,
//                                "question" to exercise.question,
//                                "answer" to exercise.answer,
//                                "type" to exercise.type?.toString(),
//                                "options" to exercise.options,
//                                "videoUrl" to exercise.videoUrl,
//                                "audioUrl" to exercise.audioUrl,
//                                "imageUrl" to exercise.imageUrl,
//                                "title" to exercise.title,
//                                "explanation" to exercise.explanation,
//                                "romanji" to exercise.romanji,
//                                "kana" to exercise.kana
//                            )
//
//                            // Thêm exercise vào Firestore
//                            exercisesRef.add(exerciseData)
//                                .addOnSuccessListener { documentReference ->
//                                    // Tài liệu đã được thêm vào Firestore, lấy document ID
//                                    val exerciseId = documentReference.id
//                                    println("✅ Added exercise to subLesson: ${subLesson.id} with ID: $exerciseId")
//                                }
//                                .addOnFailureListener { e ->
//                                    println("❌ Failed to add exercise: ${e.message}")
//                                }
//                        }
//                    }
//                }
//            }
//        }.addOnFailureListener { e ->
//            println("❌ Failed to delete old exercises: ${e.message}")
//        }
//    }
//}
//
//
//
//fun generateExercisesForSubLesson(subLesson: SubLesson, subLessonId: String): List<Exercise> {
//    return when (subLesson.type) {
//        "Video" -> listOf(
//            Exercise(
//                subLessonId = "1-1",
//                title = "Giới thiệu bảng chữ cái Hiragana",
//                videoUrl = "https://drive.google.com/uc?id=1G2tiFAR1HRFKsNT8fLqri1Ucxim7GNo2&export=download",
//                type = ExerciseType.VIDEO
//            ),
//            Exercise(
//                subLessonId = "2-1",
//                title = "Phát âm và cách viết あ-い-う-え-お",
//                videoUrl = "https://drive.google.com/uc?id=1Tkj5cMdfP2GtR60Usfbf0bLqNlRjzz6w&export=download",
//                type = ExerciseType.VIDEO
//            ),
//            Exercise(
//                subLessonId = "3-1",
//                title = "Phát âm và cách viết か-き-く-け-こ",
//                videoUrl = "https://drive.google.com/uc?id=1Fjp7jImlYbvmKvEwxIrAqdbzygsqYI9Q&export=download",
//                type = ExerciseType.VIDEO
//            ),
//            Exercise(
//                subLessonId = "4-1",
//                title = "Phát âm và cách viết さ-し-す-せ-そ",
//                videoUrl = "https://drive.google.com/uc?id=1Jq9LkHhOZjklfg1c5Q3LQ_yvQK_aP9_c&export=download",
//                type = ExerciseType.VIDEO
//            ),
//            Exercise(
//                subLessonId = "5-1",
//                title = "Phát âm và cách viết た-ち-つ-て-と",
//                videoUrl = "https://drive.google.com/uc?id=1LqTzPS9HZc7Mptx1eFz7kC8kpghjgfV9&export=download",
//                type = ExerciseType.VIDEO
//            ),
//            Exercise(
//                subLessonId = "6-1",
//                title = "Phát âm và cách viết な-に-ぬ-ね-の",
//                videoUrl = "https://drive.google.com/uc?id=1jdtqF9PrO3hNl7Bzm5bl6yLv0AY0seXy&export=download",
//                type = ExerciseType.VIDEO
//            ),
//            Exercise(
//                subLessonId = "7-1",
//                title = "Phát âm và cách viết は-ひ-ふ-へ-ほ",
//                videoUrl = "https://drive.google.com/uc?id=1Hp3ixuWiNY9-GxiFfGf0GR0yTlzi4uCK&export=download",
//                type = ExerciseType.VIDEO
//            ),
//            Exercise(
//                subLessonId = "8-1",
//                title = "Phát âm và cách viết ま-み-む-め-も",
//                videoUrl = "https://drive.google.com/uc?id=1RtwjYelHX5tLHR2k6iVs8woAjQttN8Dh&export=download",
//                type = ExerciseType.VIDEO
//            ),
//            Exercise(
//                subLessonId = "9-1",
//                title = "Phát âm và cách viết や-ゆ-よ",
//                videoUrl = "https://drive.google.com/uc?id=1PnQyP2X5cHZSHJlNHXrkxEgt7bVqu7W9&export=download",
//                type = ExerciseType.VIDEO
//            ),
//            Exercise(
//                subLessonId = "10-1",
//                title = "Phát âm và cách viết ら-り-る-れ-ろ",
//                videoUrl = "https://drive.google.com/uc?id=1jZa4zE1Fw91of6B5iFSPp-mMl58gftlx&export=download",
//                type = ExerciseType.VIDEO
//            ),
//            Exercise(
//                subLessonId = "11-1",
//                title = "Phát âm và cách viết わ-を-ん",
//                videoUrl = "https://drive.google.com/uc?id=1NeRPwpcwHt1a7aeSmHf7zAdEY_HQ7dfV&export=download",
//                type = ExerciseType.VIDEO
//            ),
//        )
//
//        "Practice" -> listOf(
//            Exercise(
//                subLessonId = "1-2",
//                question = "Chữ 'あ' thuộc loại nào trong hệ thống chữ cái Nhật Bản?",
//                answer = "Hiragana",
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                type = ExerciseType.PRACTICE,
//                options = listOf("Hiragana", "Katakana", "Kanji", "Romaji"),
//                title = "Hiragana Knowledge",
//                explanation = "Chữ 'あ' là một ký tự trong bảng chữ cái Hiragana.",
//                romanji = "a",
//                kana = "あ"
//            ),
//            Exercise(
//                subLessonId = "1-2",
//                question = "Chữ \"い\" được sử dụng trong từ nào dưới đây?",
//                answer = "いえ",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("いえ", "いく", "いま", "いち"),
//                title = "Hiragana Vocabulary",
//                explanation = "Chữ 'い' có thể xuất hiện trong từ 'いえ' (nhà).",
//                romanji = "i",
//                kana = "い"
//            ),
//            Exercise(
//                subLessonId = "2-2",
//                question = "Từ vựng nào sau đây có chữ 'あ' và có nghĩa 'bạn'?",
//                answer = "あなた",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("あなた", "あき", "あさ", "あら"),
//                title = "Hiragana Vocabulary",
//                explanation = "'あなた' có nghĩa là 'bạn' trong tiếng Nhật.",
//                romanji = "anata",
//                kana = "あなた"
//            ),
//            Exercise(
//                subLessonId = "3-2",
//                question = "Chữ \"か\" có thể được sử dụng trong từ nào sau đây?",
//                answer = "かばん",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("かばん", "かさ", "きもの", "きょう"),
//                title = "Hiragana Vocabulary",
//                explanation = "'かばん' có nghĩa là 'cái cặp'.",
//                romanji = "kaban",
//                kana = "かばん"
//            ),
//            Exercise(
//                subLessonId = "3-3",
//                question = "Chữ 'き' có thể kết hợp với từ nào sau đây để có nghĩa 'bài học'?",
//                answer = "きょうか",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("きょうか", "きんきょう", "きんこう", "きょうし"),
//                title = "Hiragana Vocabulary",
//                explanation = "'きょうか' có nghĩa là 'bài học'.",
//                romanji = "kyouka",
//                kana = "きょうか"
//            ),
//            Exercise(
//                subLessonId = "4-2",
//                question = "Chữ 'さ' thường gặp trong từ nào sau đây?",
//                answer = "さくら",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("さくら", "さよなら", "すし", "せんせい"),
//                title = "Hiragana Vocabulary",
//                explanation = "'さくら' có nghĩa là 'hoa anh đào'.",
//                romanji = "sakura",
//                kana = "さくら"
//            ),
//            Exercise(
//                subLessonId = "5-2",
//                question = "Chữ 'た' có thể dùng trong từ nào dưới đây để có nghĩa 'một'?",
//                answer = "たったいま",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("たったいま", "たべる", "たちまち", "たけやま"),
//                title = "Hiragana Vocabulary",
//                explanation = "'たったいま' có nghĩa là 'vừa mới'.",
//                romanji = "tattaima",
//                kana = "たったいま"
//            ),
//            Exercise(
//                subLessonId = "5-3",
//                question = "Khi viết chữ 'た', bạn cần phải chú ý điều gì để viết đúng?",
//                answer = "Viết nét ngang trước rồi mới viết nét đứng.",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("Viết nét ngang trước rồi mới viết nét đứng", "Viết nét đứng trước rồi mới viết nét ngang", "Viết từ trên xuống dưới", "Viết từ trái qua phải"),
//                title = "Writing Hiragana",
//                explanation = "Để viết đúng chữ 'た', bạn cần viết nét ngang trước rồi viết nét đứng.",
//                romanji = "ta",
//                kana = "た"
//            ),
//            Exercise(
//                subLessonId = "6-2",
//                question = "Chữ 'な' trong từ 'なま' có nghĩa là gì?",
//                answer = "Sống",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("Sống", "Người", "Nước", "Nay"),
//                title = "Hiragana Vocabulary",
//                explanation = "'なま' có nghĩa là 'sống', ví dụ như 'なまもの' (thực phẩm tươi).",
//                romanji = "nama",
//                kana = "なま"
//            ),
//            Exercise(
//                subLessonId = "6-3",
//                question = "Chữ 'ぬ' trong từ 'ぬいぐるみ' có nghĩa là gì?",
//                answer = "Búp bê",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("Búp bê", "Cửa", "Điều", "Gối"),
//                title = "Hiragana Vocabulary",
//                explanation = "'ぬいぐるみ' có nghĩa là 'búp bê'.",
//                romanji = "nuigurumi",
//                kana = "ぬいぐるみ"
//            ),
//            Exercise(
//                subLessonId = "7-2",
//                question = "Chữ 'は' được sử dụng trong từ nào dưới đây?",
//                answer = "はたけ (ruộng)",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("はな (hoa)", "はたけ (ruộng)", "ひと (người)", "ふる (cũ)"),
//                title = "Flashcard Hiragana",
//                explanation = "Chọn từ đúng chứa chữ 'は'.",
//                romanji = "ha",
//                kana = "は"
//            ),
//            Exercise(
//                subLessonId = "7-3",
//                question = "Chữ 'ひ' là chữ thuộc hàng nào trong bảng Hiragana?",
//                answer = "Hàng ひ",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("Hàng ほ", "Hàng ひ", "Hàng ふ", "Hàng へ"),
//                title = "Flashcard Hiragana",
//                explanation = "Chọn hàng đúng của chữ 'ひ'.",
//                romanji = "hi",
//                kana = "ひ"
//            ),
//            Exercise(
//                subLessonId = "8-2",
//                question = "Từ nào sau đây chứa chữ 'ま'?",
//                answer = "まど (cửa sổ)",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("みず (nước)", "まど (cửa sổ)", "むし (côn trùng)", "めがね (kính)"),
//                title = "Flashcard Hiragana",
//                explanation = "Chọn từ đúng chứa chữ 'ま'.",
//                romanji = "ma",
//                kana = "ま"
//            ),
//            Exercise(
//                subLessonId = "8-3",
//                question = "Chữ 'む' phát âm là gì trong từ 'むし' (côn trùng)?",
//                answer = "mu",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("mi", "mu", "me", "mo"),
//                title = "Flashcard Hiragana",
//                explanation = "Chọn nghĩa đúng với chữ 'む' trong từ 'むし'.",
//                romanji = "mu",
//                kana = "む"
//            ),
//            Exercise(
//                subLessonId = "9-2",
//                question = "Chữ 'や' thường xuất hiện trong các từ vựng nào sau đây?",
//                answer = "やさい (rau)",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("やすみ (nghỉ ngơi)", "やさい (rau)", "ゆめ (giấc mơ)", "よる (ban đêm)"),
//                title = "Flashcard Hiragana",
//                explanation = "Chọn từ đúng chứa chữ 'や'.",
//                romanji = "ya",
//                kana = "や"
//            ),
//            Exercise(
//                subLessonId = "9-3",
//                question = "Chữ 'ゆ' được phát âm là gì trong từ 'ゆめ' (giấc mơ)?",
//                answer = "yu",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("ya", "yu", "yo", "yi"),
//                title = "Flashcard Hiragana",
//                explanation = "Chọn nghĩa đúng với chữ 'ゆ' trong từ 'ゆめ'.",
//                romanji = "yu",
//                kana = "ゆ"
//            ),
//            Exercise(
//                subLessonId = "10-2",
//                question = "Chữ 'ら' trong từ 'らく' (dễ chịu) được phát âm là gì?",
//                answer = "ra",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("ra", "ri", "ru", "re"),
//                title = "Flashcard Hiragana",
//                explanation = "Chọn nghĩa đúng với chữ 'ら' trong từ 'らく'.",
//                romanji = "ra",
//                kana = "ら"
//            ),
//            Exercise(
//                subLessonId = "10-3",
//                question = "Chữ 'り' được sử dụng trong từ nào dưới đây?",
//                answer = "りんご (táo)",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("りんご (táo)", "るす (vắng nhà)", "れんしゅう (luyện tập)", "れいぞうこ (tủ lạnh)"),
//                title = "Flashcard Hiragana",
//                explanation = "Chọn từ đúng chứa chữ 'り'.",
//                romanji = "ri",
//                kana = "り"
//            ),
//            Exercise(
//                subLessonId = "11-2",
//                question = "Chữ 'わ' xuất hiện trong từ nào dưới đây?",
//                answer = "わたし (tôi)",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("わたし (tôi)", "を (biểu tượng chỉ đối tượng trong câu)", "ん (âm cuối của từ)", "よ (chỉ sự chú ý)"),
//                title = "Flashcard Hiragana",
//                explanation = "Chọn từ đúng chứa chữ 'わ'.",
//                romanji = "wa",
//                kana = "わ"
//            ),
//            Exercise(
//                subLessonId = "11-3",
//                question = "Chữ 'ん' trong từ 'きんぎょ' (cá vàng) phát âm là gì?",
//                answer = "n",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("n", "m", "ng", "no"),
//                title = "Flashcard Hiragana",
//                explanation = "Chọn nghĩa đúng với chữ 'ん' trong từ 'きんぎょ'.",
//                romanji = "n",
//                kana = "ん"
//            ),
//            Exercise(
//                subLessonId = "12-2",
//                question = "Chữ nào thuộc hàng 'た' trong bảng Hiragana?",
//                answer = "た",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("た", "ち", "つ", "て"),
//                title = "Hiragana Review",
//                explanation = "Chọn chữ đúng thuộc hàng 'た'.",
//                romanji = "ta",
//                kana = "た"
//            ),
//            Exercise(
//                subLessonId = "12-2",
//                question = "Chữ 'し' trong từ 'しごと' (công việc) phát âm là gì?",
//                answer = "shi",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("shi", "sa", "su", "se"),
//                title = "Flashcard Hiragana",
//                explanation = "Chọn nghĩa đúng với chữ 'し' trong từ 'しごと'.",
//                romanji = "shi",
//                kana = "し"
//            ),
//            Exercise(
//                subLessonId = "13-1",
//                question = "Viết chữ cái 'ち' theo đúng thứ tự.",
//                answer = "ち",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("ち", "つ", "て", "と"),
//                title = "Practice Hiragana Writing",
//                explanation = "Luyện viết chữ cái 'ち' theo đúng thứ tự.",
//                romanji = "chi",
//                kana = "ち"
//            ),
//            Exercise(
//                subLessonId = "13-1",
//                question = "Chữ 'し' trong từ 'しゅくだい' (bài tập) phát âm là gì?",
//                answer = "shi",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("su", "shi", "sa", "se"),
//                title = "Practice Hiragana Writing",
//                explanation = "Chọn nghĩa đúng với chữ 'し' trong từ 'しゅくだい'.",
//                romanji = "shi",
//                kana = "し"
//            ),
//            Exercise(
//                subLessonId = "14-1",
//                question = "Chữ nào thuộc hàng 'は' trong từ 'はな' (hoa)?",
//                answer = "は",
//                options = listOf("は", "ひ", "ふ", "へ"),
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                title = "Final Quiz Hiragana",
//                explanation = "Chọn chữ cái đúng thuộc hàng 'は'.",
//                romanji = "ha",
//                kana = "は"
//            ),
//            Exercise(
//                subLessonId = "14-1",
//                question = "Chữ 'き' trong từ 'きもの' (kimono) phát âm là gì?",
//                answer = "ki",
//                type = ExerciseType.PRACTICE,
//                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
//                options = listOf("ki", "ka", "ku", "ke"),
//                title = "Final Quiz Hiragana",
//                explanation = "Chọn nghĩa đúng với chữ 'き' trong từ 'きもの'.",
//                romanji = "ki",
//                kana = "き"
//            ),
//            )
//
//
//
//        else -> emptyList()
//    }
//}
