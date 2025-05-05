package com.example.nihongo.ui.screens.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nihongo.User.data.models.Course
import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.ExerciseType
import com.example.nihongo.User.data.models.Lesson
import com.example.nihongo.User.data.models.SubLesson
import com.example.nihongo.User.data.models.UnitItem
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.models.UserProgress
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.LessonRepository
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.ui.components.BottomNavigationBar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
    val selectedItem = "home"
    val hasAddedExercises = remember { mutableStateOf(false) }
    var userProgress by remember { mutableStateOf<List<UserProgress>>(emptyList()) }
    var isLessonCompleted by remember { mutableStateOf(false) }
    var user by remember { mutableStateOf<User?>(null) }


    LaunchedEffect(courseId) {
        lessons.value = lessonRepository.getLessonsByCourseId(courseId)
        course.value = courseRepository.getCourseById(courseId)
        isUserVip.value = userRepository.isVip()
        user = userRepository.getUserByEmail(userEmail) // Lấy thông tin người dùng từ userRepository
        userProgress = user?.let {
            listOfNotNull(userRepository.getUserProgressForCourse(it.id, courseId))
        } ?: emptyList()

        if (!hasAddedExercises.value) {
            val sampleLessons = createSampleLessons()
            sampleLessons.forEach { addExercisesToLesson(it) }
            hasAddedExercises.value = true
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Background Image full width
            course.value?.let { course ->
                AsyncImage(
                    model = course.imageRes,
                    contentDescription = "Course Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
            ) {
                Spacer(modifier = Modifier.height(240.dp)) // Push white card under image

                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        course.value?.let { course ->
                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${course.rating}", style = MaterialTheme.typography.bodyMedium)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(course.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                            if (course.vip) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Icon(Icons.Default.Star, contentDescription = "VIP", tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Chapters List", style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        lessons.value.forEach { lesson ->
                            user?.let {
                                // Lấy userProgress cho khóa học cụ thể từ userProgressList
                                val userProgressForCourse = userProgress.firstOrNull { progress ->
                                    progress.courseId == courseId // Điều kiện này đảm bảo lấy đúng khóa học
                                }
                                // Kiểm tra xem có userProgress cho khóa học không
                                userProgressForCourse?.let { userProgress ->
                                    isLessonCompleted = userProgress.completedLessons.contains(lesson.id)
                                }
                            }
                            LessonCard(
                                lesson = lesson,
                                isLessonCompleted = isLessonCompleted,
                                isExpanded = expandedLessons[lesson.id] == true,
                                onToggleExpand = {
                                    expandedLessons[lesson.id] = !(expandedLessons[lesson.id] ?: false)
                                },
                                expandedUnits = expandedUnits,
                                onSubLessonClick = { sub ->
                                    navController.navigate("exercise/${course.value?.id}/${lesson.id}/${sub.id}/$userEmail")
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}





@Composable
fun LessonCard(
    lesson: Lesson,
    isLessonCompleted : Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    expandedUnits: MutableMap<String, Boolean>,
    onSubLessonClick: (SubLesson) -> Unit
) {


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(20.dp)) // trắng tinh
            .clickable { onToggleExpand() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp)
            ) {
                Text(
                    text = lesson.stepTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Box(contentAlignment = Alignment.Center) {
                if (isLessonCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))

            lesson.units.forEachIndexed { index, unit ->
                val unitKey = "${lesson.id}_${unit.unitTitle}"
                UnitCard(
                    unit = unit,
                    isExpanded = expandedUnits[unitKey] == true,
                    onClick = { expandedUnits[unitKey] = !(expandedUnits[unitKey] ?: false) },
                    onSubLessonClick = onSubLessonClick
                )
                if (index != lesson.units.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp)) // khoảng cách giữa các UnitCard
                }
            }

        }
    }
}

@Composable
fun UnitCard(
    unit: UnitItem,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onSubLessonClick: (SubLesson) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(20.dp)) // nền trắng
            .border(
                1.dp,
                Color.Black.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            ) // viền đen nhẹ
            .padding(12.dp)
            .clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = unit.unitTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                color = Color.Black
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(6.dp))
            unit.subLessons.forEach { sub ->
                SubLessonItem(subLesson = sub, onClick = { onSubLessonClick(sub) })
            }
        }
    }

}

@Composable
fun SubLessonItem(subLesson: SubLesson, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (subLesson.isCompleted) Color(0xFF4CAF50) // Green if completed
                else Color.White,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = subLesson.title,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            color = if (subLesson.isCompleted) Color.White else Color.Black
        )

        if (subLesson.isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = Color.White
            )
        } else {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.4f), shape = CircleShape)
                    .padding(8.dp)
            )
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

fun createSampleLessons(): List<Lesson> {
    return listOf(
        Lesson(
            id = "1",
            courseId = "1",
            step = 1,
            stepTitle = "Bước 1: Giới thiệu về Hiragana",
            overview = """
            Tổng quan bảng chữ Hiragana.

            2/2 | 3:20 minutes

            Cùng tìm hiểu nguồn gốc và vai trò của Hiragana trong tiếng Nhật.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Giới thiệu Hiragana",
                    progress = "0/2",
                    subLessons = listOf(
                        SubLesson(id = "1-1", title = "Hiragana là gì?", type = "Video", isCompleted = false),
                        SubLesson(id = "1-2", title = "Tại sao nên học Hiragana?", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "2",
            courseId = "1",
            step = 2,
            stepTitle = "Bước 2: Học hàng あ",
            overview = """
            Học các chữ cái: あ, い, う, え, お

            3/3 | 5:40 minutes

            Luyện cách viết và đọc.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Hàng あ",
                    progress = "0/3",
                    subLessons = listOf(
                        SubLesson(id = "2-1", title = "Phát âm và cách viết あ-い-う-え-お", type = "Video", isCompleted = false),
                        SubLesson(id = "2-2", title = "Từ vựng với あ hàng", type = "Practice", isCompleted = false),
                        SubLesson(id = "2-3", title = "Luyện viết hàng あ", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "3",
            courseId = "1",
            step = 3,
            stepTitle = "Bước 3: Học hàng か",
            overview = """
            Học các chữ cái: か, き, く, け, こ

            3/3 | 5:50 minutes

            Phát âm và luyện viết với ví dụ từ vựng.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Hàng か",
                    progress = "0/3",
                    subLessons = listOf(
                        SubLesson(id = "3-1", title = "Phát âm và cách viết か-き-く-け-こ", type = "Video", isCompleted = false),
                        SubLesson(id = "3-2", title = "Từ vựng với か hàng", type = "Practice", isCompleted = false),
                        SubLesson(id = "3-3", title = "Luyện viết hàng か", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "4",
            courseId = "1",
            step = 4,
            stepTitle = "Bước 4: Học hàng さ",
            overview = """
            Học các chữ cái: さ, し, す, せ, そ

            3/3 | 6:00 minutes

            Tập phát âm và luyện viết chữ rõ ràng.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Hàng さ",
                    progress = "0/3",
                    subLessons = listOf(
                        SubLesson(id = "4-1", title = "Phát âm và cách viết さ-し-す-せ-そ", type = "Video", isCompleted = false),
                        SubLesson(id = "4-2", title = "Từ vựng với さ hàng", type = "Practice", isCompleted = false),
                        SubLesson(id = "4-3", title = "Luyện viết hàng さ", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "5",
            courseId = "1",
            step = 5,
            stepTitle = "Bước 5: Học hàng た",
            overview = """
            Học các chữ cái: た, ち, つ, て, と

            3/3 | 6:05 minutes

            Hướng dẫn đọc và luyện từ vựng ứng dụng.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Hàng た",
                    progress = "0/3",
                    subLessons = listOf(
                        SubLesson(id = "5-1", title = "Phát âm và cách viết た-ち-つ-て-と", type = "Video", isCompleted = false),
                        SubLesson(id = "5-2", title = "Từ vựng với た hàng", type = "Practice", isCompleted = false),
                        SubLesson(id = "5-3", title = "Luyện viết hàng た", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "6",
            courseId = "1",
            step = 6,
            stepTitle = "Bước 6: Học hàng な",
            overview = """
            Học hàng な: な, に, ぬ, ね, の

            3/3 | 6:10 minutes

            Tập phát âm và từ vựng ví dụ.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Hàng な",
                    progress = "0/3",
                    subLessons = listOf(
                        SubLesson(id = "6-1", title = "Phát âm và cách viết な-に-ぬ-ね-の", type = "Video", isCompleted = false),
                        SubLesson(id = "6-2", title = "Từ vựng với な hàng", type = "Practice", isCompleted = false),
                        SubLesson(id = "6-3", title = "Luyện viết hàng な", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "7",
            courseId = "1",
            step = 7,
            stepTitle = "Bước 7: Học hàng は",
            overview = """
            Học hàng は: は, ひ, ふ, へ, ほ

            3/3 | 6:45 minutes

            Luyện phát âm, ví dụ từ vựng.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Hàng は",
                    progress = "0/3",
                    subLessons = listOf(
                        SubLesson(id = "7-1", title = "Phát âm và cách viết は-ひ-ふ-へ-ほ", type = "Video", isCompleted = false),
                        SubLesson(id = "7-2", title = "Từ vựng với は hàng", type = "Practice", isCompleted = false),
                        SubLesson(id = "7-3", title = "Luyện viết hàng は", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "8",
            courseId = "1",
            step = 8,
            stepTitle = "Bước 8: Học hàng ま",
            overview = """
            Học hàng ま: ま, み, む, め, も

            3/3 | 6:20 minutes

            Phát âm rõ ràng và luyện từ vựng thực hành.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Hàng ま",
                    progress = "0/3",
                    subLessons = listOf(
                        SubLesson(id = "8-1", title = "Phát âm và cách viết ま-み-む-め-も", type = "Video", isCompleted = false),
                        SubLesson(id = "8-2", title = "Từ vựng với ま hàng", type = "Practice", isCompleted = false),
                        SubLesson(id = "8-3", title = "Luyện viết hàng ま", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "9",
            courseId = "1",
            step = 9,
            stepTitle = "Bước 9: Học hàng や",
            overview = """
            Học hàng や: や, ゆ, よ

            3/3 | 5:00 minutes

            Ít ký tự, dễ học nhanh và luyện viết.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Hàng や",
                    progress = "0/3",
                    subLessons = listOf(
                        SubLesson(id = "9-1", title = "Phát âm và cách viết や-ゆ-よ", type = "Video", isCompleted = false),
                        SubLesson(id = "9-2", title = "Từ vựng với や hàng", type = "Practice", isCompleted = false),
                        SubLesson(id = "9-3", title = "Luyện viết hàng や", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "10",
            courseId = "1",
            step = 10,
            stepTitle = "Bước 10: Học hàng ら",
            overview = """
            Học hàng ら: ら, り, る, れ, ろ

            3/3 | 6:15 minutes

            Thực hành phát âm chuẩn và ví dụ cơ bản.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Hàng ら",
                    progress = "0/3",
                    subLessons = listOf(
                        SubLesson(id = "10-1", title = "Phát âm và cách viết ら-り-る-れ-ろ", type = "Video", isCompleted = false),
                        SubLesson(id = "10-2", title = "Từ vựng với ら hàng", type = "Practice", isCompleted = false),
                        SubLesson(id = "10-3", title = "Luyện viết hàng ら", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "11",
            courseId = "1",
            step = 11,
            stepTitle = "Bước 11: Học hàng わ",
            overview = """
            Học hàng わ: わ, を, ん

            3/3 | 5:40 minutes

            Ký tự đặc biệt và âm kết thúc câu.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Hàng わ",
                    progress = "0/3",
                    subLessons = listOf(
                        SubLesson(id = "11-1", title = "Phát âm và cách viết わ-を-ん", type = "Video", isCompleted = false),
                        SubLesson(id = "11-2", title = "Từ vựng với わ hàng", type = "Practice", isCompleted = false),
                        SubLesson(id = "11-3", title = "Luyện viết hàng わ", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "12",
            courseId = "1",
            step = 12,
            stepTitle = "Bước 12: Luyện tập tổng hợp",
            overview = """
            Tổng hợp toàn bộ Hiragana đã học.

            2/2 | 8:00 minutes

            Nghe - đọc - viết ứng dụng thực tế.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Tổng ôn tập",
                    progress = "0/2",
                    subLessons = listOf(
                        SubLesson(id = "12-1", title = "Luyện nghe và đọc", type = "Video", isCompleted = false),
                        SubLesson(id = "12-2", title = "Kiểm tra từ vựng & chữ viết", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "13",
            courseId = "1",
            step = 13,
            stepTitle = "Bước 13: Thử thách mini game",
            overview = """
            Game hóa kiểm tra kiến thức Hiragana.

            1/1 | 5:30 minutes

            Kết hợp âm thanh, hình ảnh và phản xạ.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Mini game luyện phản xạ",
                    progress = "0/1",
                    subLessons = listOf(
                        SubLesson(id = "13-1", title = "Game phản xạ chữ Hiragana", type = "Practice", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "14",
            courseId = "1",
            step = 14,
            stepTitle = "Bước 14: Bài kiểm tra tổng kết",
            overview = """
            Kiểm tra đánh giá tổng hợp Hiragana.

            1/1 | 10:00 minutes

            Làm bài test và nhận kết quả.
        """.trimIndent(),
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Bài kiểm tra tổng hợp",
                    progress = "0/1",
                    subLessons = listOf(
                        SubLesson(id = "14-1", title = "Làm bài kiểm tra tổng kết", type = "Practice", isCompleted = false)
                    )
                )
            )
        )
    )
}


fun addExercisesToLesson(lesson: Lesson) {
    val db = FirebaseFirestore.getInstance()
    val exercisesRef = db.collection("lessons").document(lesson.id).collection("exercises")

    // Bước 1: Xóa toàn bộ exercise cũ
    exercisesRef.get().addOnSuccessListener { querySnapshot ->
        val batch = db.batch()

        // Duyệt qua tất cả các tài liệu trong collection để xóa chúng
        for (doc in querySnapshot.documents) {
            batch.delete(doc.reference)
        }

        // Sau khi batch delete xong, commit để thực thi xóa
        batch.commit().addOnSuccessListener {
            println("🧹 All old exercises in lesson '${lesson.id}' deleted.")

            // Bước 2: Thêm mới exercise sau khi đã xóa xong
            lesson.units.forEach { unitItem ->
                unitItem.subLessons.forEach { subLesson ->
                    // Lọc exercise theo type của subLesson (ví dụ: "Video" hoặc "Quiz")
                    val exercises = generateExercisesForSubLesson(subLesson, subLesson.id)

                    exercises.forEach { exercise ->
                        // Kiểm tra nếu exercise type phù hợp với subLesson type và subLessonId khớp
                        if (exercise.type == ExerciseType.from(subLesson.type) && exercise.subLessonId == subLesson.id) {
                            // Tạo exerciseData mà không cần id vì Firestore sẽ tự tạo ID
                            val exerciseData = hashMapOf(
                                "subLessonId" to subLesson.id,
                                "question" to exercise.question,
                                "answer" to exercise.answer,
                                "type" to exercise.type?.toString(),
                                "options" to exercise.options,
                                "videoUrl" to exercise.videoUrl,
                                "audioUrl" to exercise.audioUrl,
                                "imageUrl" to exercise.imageUrl,
                                "title" to exercise.title,
                                "explanation" to exercise.explanation,
                                "romanji" to exercise.romanji,
                                "kana" to exercise.kana
                            )

                            // Thêm exercise vào Firestore
                            exercisesRef.add(exerciseData)
                                .addOnSuccessListener { documentReference ->
                                    // Tài liệu đã được thêm vào Firestore, lấy document ID
                                    val exerciseId = documentReference.id
                                    println("✅ Added exercise to subLesson: ${subLesson.id} with ID: $exerciseId")
                                }
                                .addOnFailureListener { e ->
                                    println("❌ Failed to add exercise: ${e.message}")
                                }
                        }
                    }
                }
            }
        }.addOnFailureListener { e ->
            println("❌ Failed to delete old exercises: ${e.message}")
        }
    }
}



fun generateExercisesForSubLesson(subLesson: SubLesson, subLessonId: String): List<Exercise> {
    return when (subLesson.type) {
        "Video" -> listOf(
            Exercise(
                subLessonId = "1-1",
                title = "Giới thiệu bảng chữ cái Hiragana",
                videoUrl = "https://drive.google.com/uc?id=1G2tiFAR1HRFKsNT8fLqri1Ucxim7GNo2&export=download",
                type = ExerciseType.VIDEO
            ),
            Exercise(
                subLessonId = "2-1",
                title = "Phát âm và cách viết あ-い-う-え-お",
                videoUrl = "https://drive.google.com/uc?id=1Tkj5cMdfP2GtR60Usfbf0bLqNlRjzz6w&export=download",
                type = ExerciseType.VIDEO
            ),
            Exercise(
                subLessonId = "3-1",
                title = "Phát âm và cách viết か-き-く-け-こ",
                videoUrl = "https://drive.google.com/uc?id=1Fjp7jImlYbvmKvEwxIrAqdbzygsqYI9Q&export=download",
                type = ExerciseType.VIDEO
            ),
            Exercise(
                subLessonId = "4-1",
                title = "Phát âm và cách viết さ-し-す-せ-そ",
                videoUrl = "https://drive.google.com/uc?id=1Jq9LkHhOZjklfg1c5Q3LQ_yvQK_aP9_c&export=download",
                type = ExerciseType.VIDEO
            ),
            Exercise(
                subLessonId = "5-1",
                title = "Phát âm và cách viết た-ち-つ-て-と",
                videoUrl = "https://drive.google.com/uc?id=1LqTzPS9HZc7Mptx1eFz7kC8kpghjgfV9&export=download",
                type = ExerciseType.VIDEO
            ),
            Exercise(
                subLessonId = "6-1",
                title = "Phát âm và cách viết な-に-ぬ-ね-の",
                videoUrl = "https://drive.google.com/uc?id=1jdtqF9PrO3hNl7Bzm5bl6yLv0AY0seXy&export=download",
                type = ExerciseType.VIDEO
            ),
            Exercise(
                subLessonId = "7-1",
                title = "Phát âm và cách viết は-ひ-ふ-へ-ほ",
                videoUrl = "https://drive.google.com/uc?id=1Hp3ixuWiNY9-GxiFfGf0GR0yTlzi4uCK&export=download",
                type = ExerciseType.VIDEO
            ),
            Exercise(
                subLessonId = "8-1",
                title = "Phát âm và cách viết ま-み-む-め-も",
                videoUrl = "https://drive.google.com/uc?id=1RtwjYelHX5tLHR2k6iVs8woAjQttN8Dh&export=download",
                type = ExerciseType.VIDEO
            ),
            Exercise(
                subLessonId = "9-1",
                title = "Phát âm và cách viết や-ゆ-よ",
                videoUrl = "https://drive.google.com/uc?id=1PnQyP2X5cHZSHJlNHXrkxEgt7bVqu7W9&export=download",
                type = ExerciseType.VIDEO
            ),
            Exercise(
                subLessonId = "10-1",
                title = "Phát âm và cách viết ら-り-る-れ-ろ",
                videoUrl = "https://drive.google.com/uc?id=1jZa4zE1Fw91of6B5iFSPp-mMl58gftlx&export=download",
                type = ExerciseType.VIDEO
            ),
            Exercise(
                subLessonId = "11-1",
                title = "Phát âm và cách viết わ-を-ん",
                videoUrl = "https://drive.google.com/uc?id=1NeRPwpcwHt1a7aeSmHf7zAdEY_HQ7dfV&export=download",
                type = ExerciseType.VIDEO
            ),
        )

        "Practice" -> listOf(
            Exercise(
                subLessonId = "1-2",
                question = "Chữ 'あ' thuộc loại nào trong hệ thống chữ cái Nhật Bản?",
                answer = "Hiragana",
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                type = ExerciseType.PRACTICE,
                options = listOf("Hiragana", "Katakana", "Kanji", "Romaji"),
                title = "Hiragana Knowledge",
                explanation = "Chữ 'あ' là một ký tự trong bảng chữ cái Hiragana.",
                romanji = "a",
                kana = "あ"
            ),
            Exercise(
                subLessonId = "1-2",
                question = "Chữ \"い\" được sử dụng trong từ nào dưới đây?",
                answer = "いえ",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("いえ", "いく", "いま", "いち"),
                title = "Hiragana Vocabulary",
                explanation = "Chữ 'い' có thể xuất hiện trong từ 'いえ' (nhà).",
                romanji = "i",
                kana = "い"
            ),
            Exercise(
                subLessonId = "2-2",
                question = "Từ vựng nào sau đây có chữ 'あ' và có nghĩa 'bạn'?",
                answer = "あなた",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("あなた", "あき", "あさ", "あら"),
                title = "Hiragana Vocabulary",
                explanation = "'あなた' có nghĩa là 'bạn' trong tiếng Nhật.",
                romanji = "anata",
                kana = "あなた"
            ),
            Exercise(
                subLessonId = "3-2",
                question = "Chữ \"か\" có thể được sử dụng trong từ nào sau đây?",
                answer = "かばん",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("かばん", "かさ", "きもの", "きょう"),
                title = "Hiragana Vocabulary",
                explanation = "'かばん' có nghĩa là 'cái cặp'.",
                romanji = "kaban",
                kana = "かばん"
            ),
            Exercise(
                subLessonId = "3-3",
                question = "Chữ 'き' có thể kết hợp với từ nào sau đây để có nghĩa 'bài học'?",
                answer = "きょうか",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("きょうか", "きんきょう", "きんこう", "きょうし"),
                title = "Hiragana Vocabulary",
                explanation = "'きょうか' có nghĩa là 'bài học'.",
                romanji = "kyouka",
                kana = "きょうか"
            ),
            Exercise(
                subLessonId = "4-2",
                question = "Chữ 'さ' thường gặp trong từ nào sau đây?",
                answer = "さくら",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("さくら", "さよなら", "すし", "せんせい"),
                title = "Hiragana Vocabulary",
                explanation = "'さくら' có nghĩa là 'hoa anh đào'.",
                romanji = "sakura",
                kana = "さくら"
            ),
            Exercise(
                subLessonId = "5-2",
                question = "Chữ 'た' có thể dùng trong từ nào dưới đây để có nghĩa 'một'?",
                answer = "たったいま",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("たったいま", "たべる", "たちまち", "たけやま"),
                title = "Hiragana Vocabulary",
                explanation = "'たったいま' có nghĩa là 'vừa mới'.",
                romanji = "tattaima",
                kana = "たったいま"
            ),
            Exercise(
                subLessonId = "5-3",
                question = "Khi viết chữ 'た', bạn cần phải chú ý điều gì để viết đúng?",
                answer = "Viết nét ngang trước rồi mới viết nét đứng.",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("Viết nét ngang trước rồi mới viết nét đứng", "Viết nét đứng trước rồi mới viết nét ngang", "Viết từ trên xuống dưới", "Viết từ trái qua phải"),
                title = "Writing Hiragana",
                explanation = "Để viết đúng chữ 'た', bạn cần viết nét ngang trước rồi viết nét đứng.",
                romanji = "ta",
                kana = "た"
            ),
            Exercise(
                subLessonId = "6-2",
                question = "Chữ 'な' trong từ 'なま' có nghĩa là gì?",
                answer = "Sống",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("Sống", "Người", "Nước", "Nay"),
                title = "Hiragana Vocabulary",
                explanation = "'なま' có nghĩa là 'sống', ví dụ như 'なまもの' (thực phẩm tươi).",
                romanji = "nama",
                kana = "なま"
            ),
            Exercise(
                subLessonId = "6-3",
                question = "Chữ 'ぬ' trong từ 'ぬいぐるみ' có nghĩa là gì?",
                answer = "Búp bê",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("Búp bê", "Cửa", "Điều", "Gối"),
                title = "Hiragana Vocabulary",
                explanation = "'ぬいぐるみ' có nghĩa là 'búp bê'.",
                romanji = "nuigurumi",
                kana = "ぬいぐるみ"
            ),
            Exercise(
                subLessonId = "7-2",
                question = "Chữ 'は' được sử dụng trong từ nào dưới đây?",
                answer = "はたけ (ruộng)",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("はな (hoa)", "はたけ (ruộng)", "ひと (người)", "ふる (cũ)"),
                title = "Flashcard Hiragana",
                explanation = "Chọn từ đúng chứa chữ 'は'.",
                romanji = "ha",
                kana = "は"
            ),
            Exercise(
                subLessonId = "7-3",
                question = "Chữ 'ひ' là chữ thuộc hàng nào trong bảng Hiragana?",
                answer = "Hàng ひ",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("Hàng ほ", "Hàng ひ", "Hàng ふ", "Hàng へ"),
                title = "Flashcard Hiragana",
                explanation = "Chọn hàng đúng của chữ 'ひ'.",
                romanji = "hi",
                kana = "ひ"
            ),
            Exercise(
                subLessonId = "8-2",
                question = "Từ nào sau đây chứa chữ 'ま'?",
                answer = "まど (cửa sổ)",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("みず (nước)", "まど (cửa sổ)", "むし (côn trùng)", "めがね (kính)"),
                title = "Flashcard Hiragana",
                explanation = "Chọn từ đúng chứa chữ 'ま'.",
                romanji = "ma",
                kana = "ま"
            ),
            Exercise(
                subLessonId = "8-3",
                question = "Chữ 'む' phát âm là gì trong từ 'むし' (côn trùng)?",
                answer = "mu",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("mi", "mu", "me", "mo"),
                title = "Flashcard Hiragana",
                explanation = "Chọn nghĩa đúng với chữ 'む' trong từ 'むし'.",
                romanji = "mu",
                kana = "む"
            ),
            Exercise(
                subLessonId = "9-2",
                question = "Chữ 'や' thường xuất hiện trong các từ vựng nào sau đây?",
                answer = "やさい (rau)",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("やすみ (nghỉ ngơi)", "やさい (rau)", "ゆめ (giấc mơ)", "よる (ban đêm)"),
                title = "Flashcard Hiragana",
                explanation = "Chọn từ đúng chứa chữ 'や'.",
                romanji = "ya",
                kana = "や"
            ),
            Exercise(
                subLessonId = "9-3",
                question = "Chữ 'ゆ' được phát âm là gì trong từ 'ゆめ' (giấc mơ)?",
                answer = "yu",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("ya", "yu", "yo", "yi"),
                title = "Flashcard Hiragana",
                explanation = "Chọn nghĩa đúng với chữ 'ゆ' trong từ 'ゆめ'.",
                romanji = "yu",
                kana = "ゆ"
            ),
            Exercise(
                subLessonId = "10-2",
                question = "Chữ 'ら' trong từ 'らく' (dễ chịu) được phát âm là gì?",
                answer = "ra",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("ra", "ri", "ru", "re"),
                title = "Flashcard Hiragana",
                explanation = "Chọn nghĩa đúng với chữ 'ら' trong từ 'らく'.",
                romanji = "ra",
                kana = "ら"
            ),
            Exercise(
                subLessonId = "10-3",
                question = "Chữ 'り' được sử dụng trong từ nào dưới đây?",
                answer = "りんご (táo)",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("りんご (táo)", "るす (vắng nhà)", "れんしゅう (luyện tập)", "れいぞうこ (tủ lạnh)"),
                title = "Flashcard Hiragana",
                explanation = "Chọn từ đúng chứa chữ 'り'.",
                romanji = "ri",
                kana = "り"
            ),
            Exercise(
                subLessonId = "11-2",
                question = "Chữ 'わ' xuất hiện trong từ nào dưới đây?",
                answer = "わたし (tôi)",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("わたし (tôi)", "を (biểu tượng chỉ đối tượng trong câu)", "ん (âm cuối của từ)", "よ (chỉ sự chú ý)"),
                title = "Flashcard Hiragana",
                explanation = "Chọn từ đúng chứa chữ 'わ'.",
                romanji = "wa",
                kana = "わ"
            ),
            Exercise(
                subLessonId = "11-3",
                question = "Chữ 'ん' trong từ 'きんぎょ' (cá vàng) phát âm là gì?",
                answer = "n",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("n", "m", "ng", "no"),
                title = "Flashcard Hiragana",
                explanation = "Chọn nghĩa đúng với chữ 'ん' trong từ 'きんぎょ'.",
                romanji = "n",
                kana = "ん"
            ),
            Exercise(
                subLessonId = "12-2",
                question = "Chữ nào thuộc hàng 'た' trong bảng Hiragana?",
                answer = "た",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("た", "ち", "つ", "て"),
                title = "Hiragana Review",
                explanation = "Chọn chữ đúng thuộc hàng 'た'.",
                romanji = "ta",
                kana = "た"
            ),
            Exercise(
                subLessonId = "12-2",
                question = "Chữ 'し' trong từ 'しごと' (công việc) phát âm là gì?",
                answer = "shi",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("shi", "sa", "su", "se"),
                title = "Flashcard Hiragana",
                explanation = "Chọn nghĩa đúng với chữ 'し' trong từ 'しごと'.",
                romanji = "shi",
                kana = "し"
            ),
            Exercise(
                subLessonId = "13-1",
                question = "Viết chữ cái 'ち' theo đúng thứ tự.",
                answer = "ち",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("ち", "つ", "て", "と"),
                title = "Practice Hiragana Writing",
                explanation = "Luyện viết chữ cái 'ち' theo đúng thứ tự.",
                romanji = "chi",
                kana = "ち"
            ),
            Exercise(
                subLessonId = "13-1",
                question = "Chữ 'し' trong từ 'しゅくだい' (bài tập) phát âm là gì?",
                answer = "shi",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("su", "shi", "sa", "se"),
                title = "Practice Hiragana Writing",
                explanation = "Chọn nghĩa đúng với chữ 'し' trong từ 'しゅくだい'.",
                romanji = "shi",
                kana = "し"
            ),
            Exercise(
                subLessonId = "14-1",
                question = "Chữ nào thuộc hàng 'は' trong từ 'はな' (hoa)?",
                answer = "は",
                options = listOf("は", "ひ", "ふ", "へ"),
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                title = "Final Quiz Hiragana",
                explanation = "Chọn chữ cái đúng thuộc hàng 'は'.",
                romanji = "ha",
                kana = "は"
            ),
            Exercise(
                subLessonId = "14-1",
                question = "Chữ 'き' trong từ 'きもの' (kimono) phát âm là gì?",
                answer = "ki",
                type = ExerciseType.PRACTICE,
                imageUrl = "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh",
                options = listOf("ki", "ka", "ku", "ke"),
                title = "Final Quiz Hiragana",
                explanation = "Chọn nghĩa đúng với chữ 'き' trong từ 'きもの'.",
                romanji = "ki",
                kana = "き"
            ),
            )



        else -> emptyList()
    }
}
