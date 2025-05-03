package com.example.nihongo.User.ui.screens.homepage

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Đang tải bài tập...")
        }
    } else if (shouldNavigateToQuiz) {
        // Navigation chỉ gọi đúng 1 lần
        LaunchedEffect(Unit) {
            navController.currentBackStackEntry?.savedStateHandle?.set("quizList", quizExercises)
            navController.navigate("quiz_screen/${Uri.encode(userEmail)}/$courseId/$lessonId")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFEEEEEE)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val videoExercise = exercises.find { it.type == ExerciseType.VIDEO }

            if (videoExercise != null) {
                VideoExerciseView(
                    navController = navController,
                    userEmail = userEmail,
                    courseId = courseId,
                    lessonId = lessonId,
                    title = videoExercise.title ?: "",
                    videoPath = videoExercise.videoUrl ?: "",
                    explanation = sampleExplanation,
                    quiz = quizExercises
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
    quiz: List<Exercise>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(24.dp))

        AndroidView(
            factory = { context ->
                val player = SimpleExoPlayer.Builder(context).build().apply {
                    val mediaItem = MediaItem.fromUri(Uri.parse(videoPath))
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = false
                }
                PlayerView(context).apply { this.player = player }
            },
            modifier = Modifier.fillMaxWidth().aspectRatio(16 / 9f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(16.dp)) {
            ExpandableExplanationCards(
                navController,
                explanationItems = explanation,
                userEmail = userEmail,
                courseId = courseId,
                lessonId = lessonId,
                exercises = quiz
            )
            Spacer(modifier = Modifier.height(16.dp))
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
    exercises: List<Exercise> // <-- Thêm danh sách bài tập
) {
    val expandedCardIndex = remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        explanationItems.forEachIndexed { index, (title, content) ->
            val isExpanded = expandedCardIndex.value == index

            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .animateContentSize()
                    .clickable {
                        expandedCardIndex.value = if (isExpanded) -1 else index
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF9FAFB)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    )

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 20.sp,
                                    color = Color(0xFF334155)
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        // Nút "Bắt đầu luyện tập"
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {

                        Log.d("QuizLog", "Quiz Exercises: ${exercises.map { it.question }}")

                    // Lưu dữ liệu vào SavedStateHandle
                    navController.currentBackStackEntry?.savedStateHandle?.set("quizList", exercises)
                    navController.navigate("quiz_screen/$userEmail/$courseId/$lessonId")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Bắt đầu luyện tập")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

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
