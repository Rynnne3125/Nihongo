package com.example.nihongo.User.ui.screens.homepage


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.models.UserProgress
import com.example.nihongo.User.data.repository.UserRepository
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun QuizScreen(quizExercises: List<Exercise>, userEmail: String, courseId: String, lessonId: String,  navController: NavController) {
    var currentIndex by remember { mutableStateOf(0) }
    val currentExercise = quizExercises.getOrNull(currentIndex)
    val correctAnswer = currentExercise?.answer ?: ""

    var selectedWords by remember { mutableStateOf(listOf<String>()) }
    var result by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var shakeOffset by remember { mutableStateOf(0f) }

    // Confetti state
    var showConfetti by remember { mutableStateOf(false) }

    // Track if the answer has been checked
    var isAnswerChecked by remember { mutableStateOf(false) }
    val userProgressRepository = UserRepository()

    var user by remember { mutableStateOf<User?>(null) }
    var userProgressList by remember { mutableStateOf<List<UserProgress>>(emptyList()) }

    // Load user profile when composable is launched
    LaunchedEffect(userEmail) {
        scope.launch {
            user = userProgressRepository.getUserByEmail(userEmail)
            userProgressList = user?.let {
                listOfNotNull(userProgressRepository.getUserProgressForCourse(it.id, courseId))
            } ?: emptyList()

        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color(0xFFF0FFF4))
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quiz nhanh dễ học",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isAnswerChecked) {
                currentExercise?.let {
                    val density = LocalDensity.current
                    val animatedOffset by animateFloatAsState(
                        targetValue = shakeOffset,
                        animationSpec = tween(100),
                        label = "shake"
                    )

                    Box(
                        modifier = Modifier
                            .offset(x = with(density) { animatedOffset.toDp() })
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        QuestionCard(
                            question = it.question ?: "",
                            imageUrl = it.imageUrl ?:"",
                            romaji = it.romanji ?:"",
                            kana = it.kana ?: "",
                            options = it.options ?: emptyList(),
                            selectedWords = selectedWords,
                            onWordSelected = { word ->
                                if (!selectedWords.contains(word)) {
                                    selectedWords = selectedWords + word
                                }
                            },
                            onWordRemoved = { index ->
                                selectedWords = selectedWords.toMutableList().apply { removeAt(index) }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer + Result
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            val answer = selectedWords.joinToString(" ")
                            if (answer == correctAnswer) {
                                result = "Đúng rồi!"
                                scope.launch {

                                    // Update user progress
                                    user?.let {
                                        // Here, we're passing the `exerciseId` to the repository function
                                        userProgressRepository.updateUserProgress(
                                            userId = it.id,
                                            courseId = courseId,
                                            exerciseId = currentExercise?.id ?: "",
                                            passed = true
                                        )
                                    }

                                    // If it's the last question, mark the lesson as completed
                                    if (currentIndex == quizExercises.lastIndex) {
                                        user?.let {
                                            // Lấy userProgress cho khóa học cụ thể từ userProgressList
                                            val userProgressForCourse = userProgressList.firstOrNull { progress ->
                                                progress.courseId == courseId // Điều kiện này đảm bảo lấy đúng khóa học
                                            }

                                            // Kiểm tra xem có userProgress cho khóa học không
                                            userProgressForCourse?.let { userProgress ->
                                                val totalLessons = userProgress.totalLessons // Lấy tổng số bài học của khóa học

                                                userProgressRepository.markLessonAsCompleted(
                                                    userId = it.id,
                                                    courseId = courseId,
                                                    lessonId = lessonId,
                                                    totalLessons = totalLessons
                                                )
                                            }
                                        }
                                    }

                                }
                                showConfetti = true
                            } else {
                                result = "Sai rồi!"
                                scope.launch {
                                    repeat(3) {
                                        shakeOffset = 12f
                                        delay(50)
                                        shakeOffset = -12f
                                        delay(50)
                                    }
                                    shakeOffset = 0f
                                }
                            }
                            isAnswerChecked = true
                        },
                        enabled = selectedWords.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("KIỂM TRA", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedResult(result)
                }
            }
        }

        // 💥 Vẽ pháo hoa phía trên, chỉ top màn hình
        if (showConfetti) {
            ConfettiOverlay(
                onComplete = {
                    showConfetti = false
                    if (currentIndex < quizExercises.lastIndex) {
                        currentIndex++
                        selectedWords = listOf()
                        result = null
                        isAnswerChecked = false
                    } else {
                        navController.navigate("lessons/${courseId}/$userEmail")
                    }
                }
            )
        }
    }
}





@Composable
fun QuestionCard(
    question: String,
    imageUrl: String,
    romaji: String,
    kana: String,
    options: List<String>,
    selectedWords: List<String>,
    onWordSelected: (String) -> Unit,
    onWordRemoved: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Animated Image
            AsyncImage(
                model = imageUrl,
                contentDescription = "Human Figure",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(80.dp))

            // RIGHT: Romaji & Kana (centered vertically)
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .height(80.dp)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = kana,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = romaji,
                    color = Color.Gray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Selected words
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp
        ) {
            selectedWords.forEachIndexed { index, word ->
                OutlinedButton(
                    onClick = { onWordRemoved(index) },
                    border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFC8E6C9))
                ) {
                    Text(word)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Options
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp
        ) {
            options.forEach { option ->
                val isSelected = selectedWords.contains(option)
                OutlinedButton(
                    onClick = { onWordSelected(option) },
                    enabled = !isSelected,
                    border = BorderStroke(1.dp, Color.LightGray),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) Color.LightGray else Color.Transparent
                    )
                ) {
                    Text(option)
                }
            }
        }
    }
}

@Composable
fun AnimatedResult(result: String?) {
    val isCorrect = result == "Đúng rồi!"
    val scale = remember { Animatable(0f) }

    LaunchedEffect(result) {
        if (result != null) {
            scale.snapTo(0f)
            scale.animateTo(1f, tween(1800, easing = EaseOutBack))
        }
    }

    AnimatedVisibility(
        visible = result != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .scale(scale.value)
                    .background(
                        color = if (isCorrect) Color(0xFF4CAF50) else Color.Red,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCorrect) "✅" else "❌",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result ?: "",
                fontSize = 18.sp,
                color = if (isCorrect) Color(0xFF2E7D32) else Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ConfettiOverlay(onComplete: () -> Unit) {
    val confettiParticles = remember { mutableStateListOf<ConfettiParticle>() }

    LaunchedEffect(Unit) {
        confettiParticles.clear()
        repeat(100) {
            confettiParticles.add(ConfettiParticle.random())
        }
        delay(2000)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp) // ✅ chỉ phủ phần đầu, tránh che kết quả
            .padding(top = 48.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        confettiParticles.forEach { particle ->
            ConfettiItem(particle)
        }
    }
}


@Composable
fun ConfettiItem(particle: ConfettiParticle) {
    val x = remember { Animatable(particle.startX) }
    val y = remember { Animatable(particle.startY) }
    val alpha = remember { Animatable(1f) }

    val shape = remember {
        listOf(CircleShape, RoundedCornerShape(3.dp), StarShape).random()
    }

    LaunchedEffect(Unit) {
        launch {
            x.animateTo(
                targetValue = particle.endX,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
        }
        launch {
            y.animateTo(
                targetValue = particle.endY,
                animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(x.value.toInt(), y.value.toInt()) }
            .size(10.dp)
            .graphicsLayer { this.alpha = alpha.value }
            .background(
                color = particle.color,
                shape = StarShape
            )
    )
}

data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val color: Color
) {
    companion object {
        fun random(): ConfettiParticle {
            val screenWidth = -100f
            val screenHeight = 1500f

            val centerX = screenWidth / 2
            val startX = centerX
            val startY = Random.nextFloat() * 50f // gần đỉnh

            // endX lệch ra trái hoặc phải từ tâm
            val maxOffsetX = 200f // có thể điều chỉnh để bay rộng hơn hoặc hẹp hơn
            val endX = centerX + (Random.nextFloat() * maxOffsetX * if (Random.nextBoolean()) 1 else -1)

            val endY = Random.nextFloat() * screenHeight

            return ConfettiParticle(
                startX = startX,
                startY = startY,
                endX = endX,
                endY = endY,
                color = Color(
                    red = Random.nextFloat(),
                    green = Random.nextFloat(),
                    blue = Random.nextFloat()
                )
            )
        }
    }

}

object StarShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val midX = size.width / 2
        val midY = size.height / 2
        val radius = size.minDimension / 2
        val spikes = 5
        val outerRadius = radius
        val innerRadius = radius / 2.5f
        val angle = Math.PI / spikes

        path.moveTo(midX, midY - outerRadius)
        for (i in 1 until spikes * 2) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val x = midX + (r * cos(i * angle)).toFloat()
            val y = midY - (r * sin(i * angle)).toFloat()
            path.lineTo(x, y)
        }
        path.close()

        return Outline.Generic(path)
    }
}
