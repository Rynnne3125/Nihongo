package com.example.nihongo.User.ui.screens.homepage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.repository.AIRepository
import com.example.nihongo.User.data.repository.GroupChallenge
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeLeaderboardScreen(
    challengeId: String,
    currentUser: User,
    aiRepository: AIRepository,
    navController: NavController
) {
    val firestore = FirebaseFirestore.getInstance()
    var challenge by remember { mutableStateOf<GroupChallenge?>(null) }
    var isGeneratingQuiz by remember { mutableStateOf(false) }

    // Map lưu trữ dữ liệu User thực tế (để lấy Avatar và Tên)
    var participantsData by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    val scope = rememberCoroutineScope()

    // 1. Lắng nghe dữ liệu realtime của Challenge
    LaunchedEffect(challengeId) {
        firestore.collection("groupChallenges").document(challengeId)
            .addSnapshotListener { snapshot, _ ->
                challenge = snapshot?.toObject(GroupChallenge::class.java)?.copy(id = snapshot.id)
            }
    }

    // 2. Tải thông tin User mỗi khi danh sách participants thay đổi
    LaunchedEffect(challenge?.participants) {
        val userIds = challenge?.participants?.keys ?: emptySet()
        if (userIds.isNotEmpty()) {
            val fetchedUsers = mutableMapOf<String, User>()
            for (uid in userIds) {
                try {
                    // Tránh fetch lại nếu đã có trong map
                    if (!participantsData.containsKey(uid)) {
                        val doc = firestore.collection("users").document(uid).get().await()
                        doc.toObject(User::class.java)?.let { fetchedUsers[uid] = it }
                    } else {
                        fetchedUsers[uid] = participantsData[uid]!!
                    }
                } catch (e: Exception) {
                    Log.e("Leaderboard", "Lỗi tải thông tin user: $uid", e)
                }
            }
            participantsData = fetchedUsers
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(challenge?.title ?: "Bảng xếp hạng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (challenge == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2E7D32))
            }
            return@Scaffold
        }

        // Sắp xếp điểm số từ cao xuống thấp
        val sortedParticipants = challenge!!.participants.entries.sortedByDescending { it.value }.toList()

        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            // THẺ TRẠNG THÁI
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (challenge!!.status == "active") Color(0xFFE8F5E9) else Color(0xFFEEEEEE)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (challenge!!.status == "active") "🔴 Đang diễn ra..." else "✅ Đã kết thúc",
                        color = if (challenge!!.status == "active") Color(0xFF2E7D32) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${sortedParticipants.size} người tham gia")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DANH SÁCH BẢNG XẾP HẠNG
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (challenge!!.status == "completed") {
                    item {
                        RewardSummaryCard(
                            sortedParticipants = sortedParticipants,
                            participantsData = participantsData,
                            rewards = challenge!!.rewards
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                // BỤC VINH QUANG: TOP 3
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Top 2
                        if (sortedParticipants.size > 1) {
                            val uid = sortedParticipants[1].key
                            ChallengeTopUserCard(participantsData[uid], sortedParticipants[1].value, 2, Color(0xFFC0C0C0))
                        }
                        // Top 1
                        if (sortedParticipants.isNotEmpty()) {
                            val uid = sortedParticipants[0].key
                            ChallengeTopUserCard(participantsData[uid], sortedParticipants[0].value, 1, Color(0xFFFFD700))
                        }
                        // Top 3
                        if (sortedParticipants.size > 2) {
                            val uid = sortedParticipants[2].key
                            ChallengeTopUserCard(participantsData[uid], sortedParticipants[2].value, 3, Color(0xFFCD7F32))
                        }
                    }
                }

                // CÁC THỨ HẠNG CÒN LẠI (Từ hạng 4 trở đi)
                val remainingUsers = sortedParticipants.drop(3)
                itemsIndexed(remainingUsers) { index, entry ->
                    ChallengeLeaderboardRow(
                        user = participantsData[entry.key],
                        fallbackId = entry.key,
                        score = entry.value,
                        rank = index + 4
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // NÚT BẮT ĐẦU
            var isCompleting by remember { mutableStateOf(false) } // Thêm state này lên đầu column hoặc đầu hàm

            if (challenge!!.status == "active") {
                val hasParticipated = challenge!!.participants.containsKey(currentUser.id)

                if (!hasParticipated) {
                    // --- NÚT DÀNH CHO NGƯỜI CHƯA THI ---
                    Button(
                        onClick = {
                            if (!isGeneratingQuiz && challenge != null) {
                                scope.launch {
                                    isGeneratingQuiz = true
                                    try {
                                        val groupDoc = firestore.collection("studyGroups").document(challenge!!.groupId).get().await()
                                        val groupTitle = groupDoc.getString("title") ?: "Tiếng Nhật cơ bản"

                                        val generatedQuiz = aiRepository.generateQuizForGroup(groupTopic = groupTitle, numberOfQuestions = 5)

                                        if (generatedQuiz.isNotEmpty()) {
                                            navController.currentBackStackEntry?.savedStateHandle?.set("currentChallenge", challenge)
                                            navController.currentBackStackEntry?.savedStateHandle?.set("quizList", generatedQuiz)
                                            navController.navigate("challenge_quiz/${challengeId}/${currentUser.email}")
                                        } else {
                                            Log.e("Challenge", "Không thể tạo bộ câu hỏi")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("Challenge", "Lỗi tạo bài tập: ", e)
                                    } finally {
                                        isGeneratingQuiz = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                        enabled = !isGeneratingQuiz,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        if (isGeneratingQuiz) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("AI ĐANG TẠO ĐỀ...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        } else {
                            Text("BẮT ĐẦU THỬ THÁCH", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else {
                    // --- NÚT DÀNH CHO NGƯỜI ĐÃ THI XONG (CÓ QUYỀN CHỐT SỔ) ---
                    Button(
                        onClick = {
                            if (!isCompleting) {
                                scope.launch {
                                    isCompleting = true
                                    try {
                                        // Gọi hàm completeChallenge để chia điểm và đổi status
                                        aiRepository.completeChallenge(challengeId)
                                    } catch (e: Exception) {
                                        Log.e("Challenge", "Lỗi chốt sổ: ", e)
                                    } finally {
                                        isCompleting = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                        enabled = !isCompleting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)) // Đổi sang màu cam
                    ) {
                        if (isCompleting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("KẾT THÚC & TRAO GIẢI", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ================= UI COMPONENTS MỚI =================

@Composable
fun ChallengeTopUserCard(user: User?, score: Int, rank: Int, badgeColor: Color) {
    val avatarSize = if (rank == 1) 90.dp else 70.dp
    val crownOffset = if (rank == 1) (-20).dp else (-15).dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            AsyncImage(
                model = user?.imageUrl?.takeIf { it.isNotBlank() } ?: "https://ui-avatars.com/api/?name=${user?.username ?: "User"}&background=random",
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .border(3.dp, badgeColor, CircleShape)
            )

            // Icon vương miện
            Box(
                modifier = Modifier
                    .offset(y = crownOffset)
                    .background(badgeColor, CircleShape)
                    .padding(4.dp)
            ) {
                Text(
                    text = "$rank",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (rank == 1) 16.sp else 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user?.username ?: "Đang tải...",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        Text(
            text = "$score điểm",
            color = Color(0xFF2E7D32),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ChallengeLeaderboardRow(user: User?, fallbackId: String, score: Int, rank: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.width(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            AsyncImage(
                model = user?.imageUrl?.takeIf { it.isNotBlank() } ?: "https://ui-avatars.com/api/?name=${user?.username ?: fallbackId}&background=random",
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.username ?: "Thành viên (${fallbackId.take(4)}...)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                if (user?.rank != null) {
                    Text(text = "Rank: ${user.rank}", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Text(
                text = "$score",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFF2E7D32)
            )
        }
    }
}
@Composable
fun RewardSummaryCard(
    sortedParticipants: List<Map.Entry<String, Int>>,
    participantsData: Map<String, User>,
    rewards: Map<String, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), // Màu vàng cam nhạt chúc mừng
        elevation = CardDefaults.cardElevation(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎉 TỔNG KẾT TRAO THƯỞNG 🎉",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Lặp qua Top 3 để in giải thưởng
            sortedParticipants.take(3).forEachIndexed { index, entry ->
                val user = participantsData[entry.key]
                val rewardPoints = when (index) {
                    0 -> rewards["top1"] ?: 300
                    1 -> rewards["top2"] ?: 200
                    2 -> rewards["top3"] ?: 100
                    else -> rewards["others"] ?: 50
                }
                val rankMedal = when (index) {
                    0 -> "🥇 Hạng 1"
                    1 -> "🥈 Hạng 2"
                    2 -> "🥉 Hạng 3"
                    else -> ""
                }
                val color = when (index) {
                    0 -> Color(0xFFFBC02D) // Vàng
                    1 -> Color(0xFF9E9E9E) // Bạc
                    2 -> Color(0xFFD84315) // Đồng
                    else -> Color.Black
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = rankMedal, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.width(80.dp))
                        Text(
                            text = user?.username ?: "Thành viên",
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                    Text(
                        text = "+$rewardPoints điểm",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            // Dòng thông báo cho những người còn lại (Hạng 4 trở đi)
            if (sortedParticipants.size > 3) {
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFFFCC80))
                Text(
                    text = "Các thành viên còn lại nhận +${rewards["others"] ?: 50} điểm tham gia tích cực!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE65100),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}