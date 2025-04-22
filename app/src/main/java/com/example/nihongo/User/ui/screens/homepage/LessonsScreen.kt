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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.nihongo.User.data.repository.CourseRepository
import com.example.nihongo.User.data.repository.LessonRepository
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.ui.screens.homepage.NeonBackground
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun LessonsScreen(
    courseId: String,
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

    LaunchedEffect(courseId) {
        lessons.value = lessonRepository.getLessonsByCourseId(courseId)
        course.value = courseRepository.getCourseById(courseId)
        isUserVip.value = userRepository.isVip()
    }

    NeonBackground { // Lồng vào nền
        // Scrollable Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Cho phép scroll
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            course.value?.let { course ->

                Column {
                    AsyncImage(
                        model = course.imageRes, // URL tải về hình ảnh từ Google Drive
                        contentDescription = "Course Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )


                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = course.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rating: ${course.rating}", style = MaterialTheme.typography.bodyMedium)
                        Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Comment, contentDescription = "Reviews", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${course.reviews} Reviews", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ThumbUp, contentDescription = "Likes", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${course.likes} Likes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (course.isVip) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "VIP",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            } ?: run {
                Text("Loading course...", color = Color.Gray)
            }

            lessons.value.forEach { lesson ->
                LessonCard(
                    lesson = lesson,
                    isExpanded = expandedLessons[lesson.id] == true,
                    onToggleExpand = {
                        expandedLessons[lesson.id] = !(expandedLessons[lesson.id] ?: false)
                    },
                    expandedUnits = expandedUnits,
                    onSubLessonClick = { sub ->
                        navController.navigate("exercise/${lesson.id}/${sub.id}")
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}



@Composable
fun LessonCard(
    lesson: Lesson,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    expandedUnits: MutableMap<String, Boolean>,
    onSubLessonClick: (SubLesson) -> Unit
) {
    val progress = if (lesson.totalUnits != 0) lesson.completedUnits.toFloat() / lesson.totalUnits else 0f

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
                Text(
                    text = "    ${lesson.completedUnits}/${lesson.totalUnits}    Units đã hoàn thành",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(36.dp),
                    color = Color(0xFF4CAF50),
                    strokeWidth = 4.dp,
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
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
    val progressParts = unit.progress.split("/")
    val progressValue = if (progressParts.size == 2) {
        val completed = progressParts[0].toFloatOrNull() ?: 0f
        val total = progressParts[1].toFloatOrNull() ?: 1f
        (completed / total) * 100f
    } else {
        0f
    }

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

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progressValue / 100f },
                    modifier = Modifier.size(28.dp),
                    color = Color(0xFF81C784),
                    strokeWidth = 3.dp,
                )
                Text(
                    text = "${progressValue.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
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
            courseId = "3",
            step = 1,
            stepTitle = "Bước 1: Cùng tìm hiểu về Bảng chữ cái trong tiếng Nhật",
            overview = "Tổng quan về chữ cái tiếng Nhật (1 lesson)\n\n2/2 | 3:20 minutes\n\nCùng tìm hiểu về Bảng chữ cái trong tiếng Nhật.\n\nLuyện tập",
            totalUnits = 1,
            completedUnits = 1,
            units = listOf(
                UnitItem(
                    unitTitle = "Tổng quan về chữ cái",
                    progress = "2/2",
                    subLessons = listOf(
                        SubLesson(id = "", title = "Cùng tìm hiểu về Bảng chữ cái trong tiếng Nhật", type = "Video", isCompleted = true),
                        SubLesson(id = "", title = "Luyện tập", type = "Practice", isCompleted = true)
                    )
                )
            )
        ),
        Lesson(
            id = "",
            courseId = "3",
            step = 2,
            stepTitle = "Bước 2: Nắm vững bảng chữ cái Hiragana (phần 1)",
            overview = "Unit 1：Hiragana 1\n\n6/9 |",
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Unit 1：Hiragana 1",
                    progress = "6/9",
                    subLessons = listOf(
                        SubLesson(id = "", title = "[B1][Video] Hàng あ và hàng か", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[B1][Video] Hàng さ và hàng た", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[B1] Luyện tập ghi nhớ hàng あ〜た", type = "Practice", isCompleted = false),
                        SubLesson(id = "", title = "[B1][Video] Hàng な và hàng は", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[B1][Video] Hàng ま và hàng や", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[U1] Bài luyện tập nhận diện mặt chữ", type = "Practice", isCompleted = false),
                        SubLesson(id = "", title = "[U1] Bài luyện tập viết", type = "Practice", isCompleted = false),
                        SubLesson(id = "", title = "[U1] Bài luyện tập nghe", type = "Practice", isCompleted = false),
                        SubLesson(id = "", title = "[B1] Kiểm tra viết", type = "Quiz", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "",
            courseId = "3",
            step = 3,
            stepTitle = "Bước 3: Nắm vững bảng chữ cái Hiragana (phần 2)",
            overview = "Unit 3：Hiragana 3 (1 lesson)\n\n1/6 |",
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Unit 3：Hiragana 3",
                    progress = "1/6",
                    subLessons = listOf(
                        SubLesson(id = "", title = "[B3][Video] Âm ghép", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[B3] Luyện tập nghe", type = "Practice", isCompleted = false),
                        SubLesson(id = "", title = "[B3][Video] Trường âm và âm ngắt", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[B3] Luyện tập viết", type = "Practice", isCompleted = false),
                        SubLesson(id = "", title = "[B3] Luyện tập tổng hợp", type = "Practice", isCompleted = false),
                        SubLesson(id = "", title = "[B3] Kiểm tra viết", type = "Quiz", isCompleted = false)
                    )
                )
            )
        ),
        Lesson(
            id = "",
            courseId = "3",
            step = 4,
            stepTitle = "Unit 4：Katakana",
            overview = "Unit 4：Katakana (1 lesson)\n\n1/12 |",
            totalUnits = 1,
            completedUnits = 0,
            units = listOf(
                UnitItem(
                    unitTitle = "Unit 4：Katakana",
                    progress = "1/12",
                    subLessons = listOf(
                        SubLesson(id = "", title = "[B4][Video] Giới thiệu bảng Katakana", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[B4][Video] Hàng ア và hàng カ", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[B4][Video] Hàng サ và hàng タ", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[B4] Ôn tập hàng サ〜タ", type = "Practice", isCompleted = false),
                        SubLesson(id = "", title = "[B4][Video] Hàng ナ và hàng ハ", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[B4][Video] Hàng マ và hàng ヤ", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[B4][Video] Hàng ラ và hàng ワ", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[B1][Video] Âm đục, âm ghép, âm ngắt, trường âm", type = "Video", isCompleted = false),
                        SubLesson(id = "", title = "[B4] Luyện tập nhận diện mặt chữ", type = "Practice", isCompleted = false),
                        SubLesson(id = "", title = "[B1] Luyện tập nghe và đọc chữ", type = "Practice", isCompleted = false),
                        SubLesson(id = "", title = "[B4] Luyện tập viết", type = "Practice", isCompleted = false),
                        SubLesson(id = "", title = "[B4] Kiểm tra viết", type = "Quiz", isCompleted = false)
                    )
                )
            )
        )
    )
}


// Call the function to add exercises to this lesson
fun addExercisesToLesson(lesson: Lesson) {
    val db = FirebaseFirestore.getInstance()

    lesson.units.forEach { unitItem ->
        unitItem.subLessons.forEach { subLesson ->
            val exercises = generateExercisesForSubLesson(subLesson, subLesson.id)

            exercises.forEach { exercise ->
                val exerciseData = hashMapOf(
                    "id" to exercise.id,
                    "subLessonId" to subLesson.id,
                    "question" to exercise.question,
                    "answer" to exercise.answer,
                    "type" to exercise.type?.toString(), // convert enum to string
                    "options" to exercise.options,
                    "videoUrl" to exercise.videoUrl,
                    "audioUrl" to exercise.audioUrl,
                    "imageUrl" to exercise.imageUrl,
                    "title" to exercise.title,
                    "explanation" to exercise.explanation
                )


                db.collection("lessons")
                    .document(lesson.id)
                    .collection("exercises")
                    .add(exerciseData)
                    .addOnSuccessListener {
                        println("✅ Exercise added to subLesson: ${subLesson.id}")
                    }
                    .addOnFailureListener { e ->
                        println("❌ Failed to add exercise: ${e.message}")
                    }
            }
        }
    }
}

fun generateExercisesForSubLesson(subLesson: SubLesson, subLessonId: String): List<Exercise> {
    return when (subLesson.type) {
        "Video" -> listOf(
            Exercise(
                id = "",
                subLessonId = subLessonId,
                title = "Giới thiệu bảng chữ cái Hiragana",
                videoUrl = "https://drive.google.com/uc?id=1G2tiFAR1HRFKsNT8fLqri1Ucxim7GNo2&export=download",
                type = ExerciseType.VIDEO
            )
        )

        "Practice" -> listOf(
            Exercise(
                id = "",
                subLessonId = subLessonId,
                question = "Luyện tập ghi nhớ chữ \"あ\" — trong bài: \"${subLesson.title}\".",
                answer = "あ",
                type = ExerciseType.FLASHCARD,
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/6/68/Hiragana_ka.svg"
            ),
            Exercise(
                id = "",
                subLessonId = subLessonId,
                question = "Kéo chữ đúng vào hình quả táo (りんご) trong bài: \"${subLesson.title}\".",
                answer = "あ",
                type = ExerciseType.MEMORY_GAME,
                imageUrl = "https://img.freepik.com/free-photo/red-apple-isolated-white_2829-10305.jpg"
            ),
            Exercise(
                id = "",
                subLessonId = subLessonId,
                question = "Trong bài \"${subLesson.title}\", chữ \"う\" nằm ở đâu?",
                answer = "う",
                options = listOf("え", "う", "い", "あ"),
                type = ExerciseType.MULTIPLE_CHOICE
            ),
            Exercise(
                id = "",
                subLessonId = subLessonId,
                question = "Flashcard: Chữ \"い\" nghĩa là gì?",
                answer = "i",
                type = ExerciseType.FLASHCARD,
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/6/65/Hiragana_i.svg"
            ),
            Exercise(
                id = "",
                subLessonId = subLessonId,
                question = "Chọn chữ tương ứng với âm \"え\"",
                answer = "え",
                options = listOf("お", "え", "い", "あ"),
                type = ExerciseType.MULTIPLE_CHOICE
            ),
            Exercise(
                id = "",
                subLessonId = subLessonId,
                question = "Kéo chữ \"お\" vào hình biểu tượng giọt nước (nước = みず).",
                answer = "お",
                type = ExerciseType.MEMORY_GAME,
                imageUrl = "https://img.freepik.com/free-vector/water-drop-icon_78370-2298.jpg"
            )
        )

        "Quiz" -> listOf(
            Exercise(
                id = "",
                subLessonId = subLessonId,
                question = "Kiểm tra cuối bài: Chữ \"か\" đọc là gì?",
                answer = "ka",
                options = listOf("ka", "ki", "ku", "ke"),
                type = ExerciseType.QUIZ
            ),
            Exercise(
                id = "",
                subLessonId = subLessonId,
                question = "Chữ \"き\" phát âm là?",
                answer = "ki",
                options = listOf("ka", "ke", "ki", "ko"),
                type = ExerciseType.QUIZ
            ),
            Exercise(
                id = "",
                subLessonId = subLessonId,
                question = "Chữ nào tương ứng với âm \"く\"?",
                answer = "く",
                options = listOf("け", "か", "く", "き"),
                type = ExerciseType.QUIZ
            ),
            Exercise(
                id = "",
                subLessonId = subLessonId,
                question = "Chữ \"け\" thường xuất hiện trong từ nào sau đây?",
                answer = "けむり (khói)",
                options = listOf("あさ", "けむり", "すし", "ねこ"),
                type = ExerciseType.QUIZ
            ),
            Exercise(
                id = "",
                subLessonId = subLessonId,
                question = "Hãy chọn đúng thứ tự phát âm của hàng あ: ",
                answer = "あ, い, う, え, お",
                options = listOf(
                    "あ, う, い, お, え",
                    "あ, い, う, え, お",
                    "い, あ, う, え, お",
                    "お, え, う, い, あ"
                ),
                type = ExerciseType.QUIZ
            )
        )

        else -> emptyList()
    }
}
