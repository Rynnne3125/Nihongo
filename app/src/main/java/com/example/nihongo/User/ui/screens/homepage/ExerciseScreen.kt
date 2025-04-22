package com.example.nihongo.User.ui.screens.homepage

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
    lessonId: String
) {
    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }

    LaunchedEffect(sublessonId) {
        exercises = exerciseRepository.getExercisesBySubLessonId(sublessonId, lessonId)
    }

    NeonBackground {
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Đang tải bài tập...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                exercises.forEach { exercise ->
                    when (exercise.type) {
                        ExerciseType.VIDEO -> {
                            VideoExerciseView(
                                navController,
                                sublessonId,
                                exercise.title ?: "",
                                exercise.videoUrl ?: "",
                                sampleExplanation
                            )
                        }
                        ExerciseType.FLASHCARD -> {
                            FlashcardExerciseView(
                                question = exercise.question ?: "",
                                answer = exercise.answer ?: ""
                            )
                        }
                        ExerciseType.MEMORY_GAME -> {
                            MemoryGameExerciseView(
                                question = exercise.question ?: "",
                                answer = exercise.answer ?: ""
                            )
                        }
                        ExerciseType.MULTIPLE_CHOICE -> {
                            MultipleChoiceExerciseView(
                                question = exercise.question ?: "",
                                options = exercise.options ?: emptyList(),
                                correctAnswer = exercise.answer ?: ""
                            )
                        }
                        ExerciseType.QUIZ -> {
                            QuizExerciseView(
                                question = exercise.question ?: "",
                                options = exercise.options ?: emptyList(),
                                correctAnswer = exercise.answer ?: ""
                            )
                        }
                        else -> {
                            Text("Không xác định loại bài tập.")
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}


@Composable
fun NeonBackground(content: @Composable () -> Unit) {
    val gradientBrush = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF00CC00).copy(alpha = 0.5f),  // Xanh lá đậm
            0.2f to Color(0xFF00CC00).copy(alpha = 0.3f),  // Xanh lá nhạt
            0.5f to Color.White.copy(alpha = 0.2f),         // Trắng mờ dần
            0.7f to Color(0xFF0099CC).copy(alpha = 0.3f),  // Xanh biển nhạt
            1.0f to Color(0xFF0099CC).copy(alpha = 0.5f)   // Xanh biển đậm
        ),
        start = Offset(0f, 0f),    // Tạo điểm bắt đầu
        end = Offset(1000f, 1000f) // Tạo điểm kết thúc
    )

    // Radial Gradient toả sáng từ giữa
    val radialBrush = Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF00CC00).copy(alpha = 0.6f),  // Xanh lá đậm ở trung tâm
            0.3f to Color(0xFF00CC00).copy(alpha = 0.4f),  // Xanh lá nhẹ hơn khi ra ngoài
            0.6f to Color.White.copy(alpha = 0.3f),         // Pha trắng nhẹ hơn nữa
            1.0f to Color(0xFF0099CC).copy(alpha = 0.2f)   // Xanh biển pha loãng gần biên
        ),
        center = Offset(500f, 1000f),  // Canh giữa màn hình
        radius = 1500f                  // Phạm vi tỏa sáng
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = radialBrush) // Tạo background radial gradient
            .background(brush = gradientBrush) // Đồng thời áp dụng linear gradient
    ) {
        content() // Nội dung của bạn ở đây
    }
}







@Composable
fun VideoExerciseView(
    navController: NavController,
    sublessonId: String,
    title: String,
    videoPath: String,
    explanation: List<Pair<String, String>>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 👇 Video full width, sát viền ứng dụng
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
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 👇 Phần nội dung còn lại có padding như thường
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            ExpandableExplanationCards(
                explanationItems = explanation,
                subLessonId = sublessonId,
                onPracticeClick = { id, type ->
                    navController.navigate("exercise/${id}/${type.name}")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun ExpandableExplanationCards(
    explanationItems: List<Pair<String, String>>,
    subLessonId: String,
    onPracticeClick: (subLessonId: String, exerciseType: ExerciseType) -> Unit
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
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .animateContentSize()
                    .clickable {
                        expandedCardIndex.value = if (isExpanded) -1 else index
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF9FAFB) // Nền mềm mại
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B) // Xanh đậm hiện đại
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

        // Nút "Bắt đầu luyện tập" căn giữa
        Box(
            modifier = Modifier.fillMaxWidth(), // Đảm bảo Box chiếm toàn bộ chiều rộng
            contentAlignment = Alignment.Center // Căn giữa Button trong Box
        ) {
            Button(
                onClick = {
                    onPracticeClick(subLessonId, ExerciseType.FLASHCARD)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981), // Màu xanh lá chuyên nghiệp
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(top = 16.dp) // Căn chỉnh và tạo khoảng cách
            ) {
                Text("Bắt đầu luyện tập")
            }
        }
    }
}



@Composable
fun FlashcardExerciseView(question: String, answer: String) {
    var isAnswerVisible by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Flashcard", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(question, fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = { isAnswerVisible = !isAnswerVisible }) {
            Text(text = if (isAnswerVisible) "Ẩn câu trả lời" else "Hiện câu trả lời")
        }
        AnimatedVisibility(visible = isAnswerVisible) {
            Text(answer, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun MemoryGameExerciseView(question: String, answer: String) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Memory Game", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(question, fontSize = 18.sp)
        // Placeholder cho trò chơi memory game
    }
}

@Composable
fun MultipleChoiceExerciseView(question: String, options: List<String>, correctAnswer: String) {
    var selectedOption by remember { mutableStateOf("") }
    var isAnswerCorrect by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Multiple Choice", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(question, fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))
        options.forEach { option ->
            Row(modifier = Modifier.fillMaxWidth().clickable {
                selectedOption = option
                isAnswerCorrect = option == correctAnswer
            }) {
                Text(option, fontSize = 18.sp, modifier = Modifier.padding(8.dp))
            }
        }
        if (selectedOption.isNotEmpty()) {
            Text(
                text = if (isAnswerCorrect) "Chính xác!" else "Sai rồi!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAnswerCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun QuizExerciseView(question: String, options: List<String>, correctAnswer: String) {
    var selectedOption by remember { mutableStateOf("") }
    var isAnswerCorrect by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Quiz", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(question, fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))
        options.forEach { option ->
            Row(modifier = Modifier.fillMaxWidth().clickable {
                selectedOption = option
                isAnswerCorrect = option == correctAnswer
            }) {
                Text(option, fontSize = 18.sp, modifier = Modifier.padding(8.dp))
            }
        }
        if (selectedOption.isNotEmpty()) {
            Text(
                text = if (isAnswerCorrect) "Chính xác!" else "Sai rồi!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAnswerCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
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
