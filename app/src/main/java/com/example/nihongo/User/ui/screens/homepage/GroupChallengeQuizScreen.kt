package com.example.nihongo.User.ui.screens.homepage

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.repository.AIRepository
import com.example.nihongo.User.data.repository.GroupChallenge
import com.example.nihongo.User.ui.components.FloatingAISensei
import kotlinx.coroutines.launch
import kotlin.collections.getOrNull
import kotlin.collections.lastIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChallengeQuizScreen(
    challenge: GroupChallenge,
    quizExercises: List<Exercise>,
    currentUser: User,
    aiRepository: AIRepository,
    navController: NavController
) {
    var currentIndex by remember { mutableStateOf(0) }
    var selectedWords by remember { mutableStateOf(listOf<String>()) }
    var result by remember { mutableStateOf<String?>(null) }

    // THEO DÕI ĐIỂM SỐ & ANIMATION
    var currentScore by remember { mutableStateOf(0) }
    var shakeOffset by remember { mutableStateOf(0f) }
    var showConfetti by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var isAnswerChecked by remember { mutableStateOf(false) }

    val currentExercise = quizExercises.getOrNull(currentIndex)
    val correctAnswer = currentExercise?.answer ?: ""

    // TÍNH TOÁN TIẾN ĐỘ THÀNH PHẦN
    val progress = if (quizExercises.isNotEmpty()) {
        (currentIndex + 1).toFloat() / quizExercises.size.toFloat()
    } else 0f

    // LOGIC KIỂM TRA ĐÁP ÁN
    val onCheckAnswerClick: () -> Unit = {
        val answer = selectedWords.joinToString(" ")

        if (answer == correctAnswer) {
            result = "Đúng rồi!"
            currentScore++
            showConfetti = true // Bắn pháo hoa

            // Cập nhật điểm lên Leaderboard nhóm
            scope.launch {
                aiRepository.updateChallengeProgress(
                    challengeId = challenge.id,
                    userId = currentUser.id,
                    progress = currentScore
                )
            }
        } else {
            result = "Sai rồi!"
            // Rung màn hình khi sai
            scope.launch {
                repeat(3) {
                    shakeOffset = 12f
                    kotlinx.coroutines.delay(50)
                    shakeOffset = -12f
                    kotlinx.coroutines.delay(50)
                }
                shakeOffset = 0f
            }
        }
        isAnswerChecked = true
    }

    // LOGIC CHUYỂN CÂU HOẶC HOÀN THÀNH
    val onNextOrFinishClick: () -> Unit = {
        if (currentIndex < quizExercises.lastIndex) {
            currentIndex++
            selectedWords = emptyList()
            result = null
            isAnswerChecked = false
        } else {
            // Quay về màn hình nhóm
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thử thách: ${challenge.title}", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // 1. THANH TIẾN ĐỘ (Giữ nguyên)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Câu hỏi ${currentIndex + 1}/${quizExercises.size}",
                                fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF4CAF50), trackColor = Color(0xFFE8F5E9)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. KHU VỰC CÂU HỎI HOẶC KẾT QUẢ
                if (!isAnswerChecked) {
                    currentExercise?.let {
                        val density = LocalDensity.current
                        val animatedOffset by animateFloatAsState(targetValue = shakeOffset, animationSpec = tween(100))

                        Box(modifier = Modifier.offset(x = with(density) { animatedOffset.toDp() }).fillMaxWidth().weight(1f)) {
                            QuestionCard(
                                question = it.question ?: "", imageUrl = it.imageUrl ?: "",
                                romaji = it.romanji ?: "", kana = it.kana ?: "", options = it.options ?: emptyList(),
                                selectedWords = selectedWords,
                                onWordSelected = { word -> if (!selectedWords.contains(word)) selectedWords = selectedWords + word },
                                onWordRemoved = { index -> selectedWords = selectedWords.toMutableList().apply { removeAt(index) } }
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (result == "Đúng rồi!") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.size(80.dp).background(if (result == "Đúng rồi!") Color(0xFF4CAF50) else Color.Red, CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text(text = if (result == "Đúng rồi!") "✓" else "✗", fontSize = 40.sp, color = Color.White, fontWeight = FontWeight.Bold) }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(text = result ?: "", color = if (result == "Đúng rồi!") Color(0xFF4CAF50) else Color.Red, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row {
                                Text("Đáp án đúng: ", fontSize = 16.sp, color = Color.DarkGray)
                                Text(correctAnswer, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. FOOTER
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = {
                                if (!isAnswerChecked) onCheckAnswerClick() else onNextOrFinishClick()
                            },
                            enabled = if (!isAnswerChecked) selectedWords.isNotEmpty() else true,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentIndex < quizExercises.lastIndex || !isAnswerChecked) Color(0xFF2E7D32) else Color(0xFF1565C0),
                                disabledContainerColor = Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Text(
                                text = if (!isAnswerChecked) "KIỂM TRA" else if (currentIndex < quizExercises.lastIndex) "TIẾP TỤC" else "KẾT THÚC BÀI THI",
                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // HIỆU ỨNG BẮN PHÁO HOA
            if (showConfetti) {
                ConfettiOverlay(onComplete = { showConfetti = false })
            }
        }
    }
}