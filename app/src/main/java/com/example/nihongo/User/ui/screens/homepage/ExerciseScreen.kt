package com.example.nihongo.User.ui.screens.homepage

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.ExerciseType
import com.example.nihongo.User.data.repository.ExerciseRepository
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    navController: NavController,
    sublessonId: String,
    exerciseRepository: ExerciseRepository,
    courseId: String,
    lessonId: String,
    userEmail: String
) {
    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var quizExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var shouldNavigateToQuiz by remember { mutableStateOf(false) }

    LaunchedEffect(sublessonId) {
        isLoading = true
        exercises = exerciseRepository.getExercisesBySubLessonId(sublessonId, lessonId)
        quizExercises = exerciseRepository.getPracticeExercisesExcludingFirstSubLesson(lessonId)
        isLoading = false

        // Nếu không có VIDEO thì trigger navigation
        val hasVideo = exercises.any { it.type == ExerciseType.VIDEO }
        if (!hasVideo) {
            shouldNavigateToQuiz = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exercises.firstOrNull()?.title ?: "Bài tập",
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Đang tải bài tập...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        } else if (shouldNavigateToQuiz) {
            // Navigation chỉ gọi đúng 1 lần
            LaunchedEffect(Unit) {
                navController.currentBackStackEntry?.savedStateHandle?.set("quizList", quizExercises)
                navController.navigate("quiz_screen/${Uri.encode(userEmail)}/$courseId/$lessonId")
            }
        } else {
            val videoExercise = exercises.find { it.type == ExerciseType.VIDEO }

            if (videoExercise != null) {
                // Parse explanation từ Firebase hoặc sử dụng sampleExplanation nếu không có
                val explanation = if (videoExercise.explanation.isNullOrBlank()) {
                    sampleExplanation
                } else {
                    parseExplanation(videoExercise.explanation)
                }

                VideoExerciseView(
                    navController = navController,
                    userEmail = userEmail,
                    courseId = courseId,
                    lessonId = lessonId,
                    title = videoExercise.title ?: "",
                    videoPath = videoExercise.videoUrl ?: "",
                    explanation = explanation,
                    quiz = quizExercises,
                    innerPadding = innerPadding
                )
            }
        }
    }
}

@Composable
fun VideoExerciseView(
    navController: NavController,
    userEmail: String,
    courseId: String,
    lessonId: String,
    title: String,
    videoPath: String,
    explanation: List<Pair<String, String>>,
    quiz: List<Exercise>,
    innerPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Video Title and Description
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Xem video và học các khái niệm cơ bản trong bài học này. Sau đó làm bài tập để củng cố kiến thức.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Video Player
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Video Player
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                    ) {
                        AndroidView(
                            factory = { context ->
                                val player = SimpleExoPlayer.Builder(context).build().apply {
                                    val mediaItem = MediaItem.fromUri(Uri.parse(videoPath))
                                    setMediaItem(mediaItem)
                                    prepare()
                                    playWhenReady = false
                                }
                                PlayerView(context).apply { 
                                    this.player = player
                                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                                    useController = true
                                    controllerAutoShow = true
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Video controls hint
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nhấn vào video để phát/tạm dừng",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Lesson Content
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "Nội dung bài học",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ExpandableExplanationCards(
                        navController = navController,
                        explanationItems = explanation,
                        userEmail = userEmail,
                        courseId = courseId,
                        lessonId = lessonId,
                        exercises = quiz
                    )
                }
            }
        }
        
        // Practice Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QuestionAnswer,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "Luyện tập",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Làm bài tập để củng cố kiến thức vừa học. Bạn cần hoàn thành bài tập để mở khóa bài học tiếp theo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            Log.d("QuizLog", "Quiz Exercises: ${quiz.map { it.question }}")
                            navController.currentBackStackEntry?.savedStateHandle?.set("quizList", quiz)
                            navController.navigate("quiz_screen/$userEmail/$courseId/$lessonId")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "BẮT ĐẦU LUYỆN TẬP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ExpandableExplanationCards(
    navController: NavController,
    explanationItems: List<Pair<String, String>>,
    userEmail: String,
    courseId: String,
    lessonId: String,
    exercises: List<Exercise>
) {
    val expandedCardIndex = remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        explanationItems.forEachIndexed { index, (title, content) ->
            val isExpanded = expandedCardIndex.value == index

            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    .clickable {
                        expandedCardIndex.value = if (isExpanded) -1 else index
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF9FAFB)
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = Color(0xFF4CAF50)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseExplanation(raw: String): List<Pair<String, String>> {
    val parts = raw.split("➤").filter { it.isNotBlank() }
    val result = mutableListOf<Pair<String, String>>()

    var i = 0
    while (i < parts.size - 1) {
        val title = parts[i].trim()
        val content = parts[i + 1].trim()
        result.add(title to content)
        i += 2
    }

    return result
}

val sampleExplanation = listOf(
    "I. Giới thiệu các loại chữ trong tiếng Nhật" to """
        Trong tiếng Nhật có 3 loại chữ:

        a. Kanji (chữ Hán): 日本
        - Chữ Kanji du nhập từ Trung Quốc vào Nhật Bản từ thế kỷ thứ 4.

        b. Hiragana (chữ mềm): にほん
        - Hiragana được tạo từ Kanji, dùng viết trợ từ, từ thuần Nhật.
        - VD: 世 ⇒ せ.

        c. Katakana (chữ cứng): 二ホン
        - Katakana dùng cho từ ngoại lai, tên nước, tên riêng.
        - VD: Orange ⇒ オレンジ.
    """.trimIndent(),

    "II. Giới thiệu bảng chữ cái Hiragana" to """
        - Bảng Hiragana gồm 46 chữ cái.
        - Hàng あ: あ(a), い(i), う(u), え(e), お(o).
        - Hàng か: か(ka), き(ki), く(ku), け(ke), こ(ko).
        - Hàng さ: さ(sa), し(shi), す(su), せ(se), そ(so).
        - Hàng た: た(ta), ち(chi), つ(tsu), て(te), と(to).
        - Hàng な: な(na), に(ni), ぬ(nu), ね(ne), の(no).
        - Hàng は: は(ha), ひ(hi), ふ(fu), へ(he), ほ(ho).
        - Hàng ま: ま(ma), み(mi), む(mu), め(me), も(mo).
        - Hàng や: や(ya), ゆ(yu), よ(yo).
        - Hàng ら: ら(ra), り(ri), る(ru), れ(re), ろ(ro).
        - Hàng わ: わ(wa), を(wo), ん(n).
    """.trimIndent()
)
