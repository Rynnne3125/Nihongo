package com.example.nihongo.User.ui.screens.homepage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.repository.AIRepository
import com.example.nihongo.User.data.repository.GroupChallenge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChallengeHubScreen(
    groupId: String,
    aiRepository: AIRepository,
    navController: NavController,
    currentUser: User
) {
    var activeChallenges by remember { mutableStateOf<List<GroupChallenge>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Lấy danh sách thử thách đang active
    LaunchedEffect(groupId) {
        activeChallenges = aiRepository.getActiveChallengesForGroup(groupId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thử thách nhóm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            // AI CŨNG CÓ THỂ TẠO THỬ THÁCH
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        val newChallenge = GroupChallenge(
                            groupId = groupId,
                            title = "Thi đấu ${System.currentTimeMillis() % 1000}",
                            description = "Tham gia trả lời câu hỏi để giành điểm số!",
                            targetType = "quiz",
                            targetValue = 10,
                            status = "active"
                        )
                        val challengeId = aiRepository.createGroupChallenge(newChallenge)
                        if (challengeId != null) {
                            // Chuyển thẳng vào phòng chờ (Leaderboard) của thử thách vừa tạo
                            navController.navigate("challenge_leaderboard/$challengeId/${currentUser.email}")                        }
                    }
                },
                containerColor = Color(0xFFE65100),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo mới")
                Spacer(Modifier.width(8.dp))
                Text("Tạo thử thách", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2E7D32))
            }
        } else if (activeChallenges.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Chưa có thử thách nào đang diễn ra.\nHãy là người đầu tiên tạo nhé!", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                items(activeChallenges) { challenge ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        onClick = {
                            navController.navigate("challenge_leaderboard/${challenge.id}/${currentUser.email}")                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(challenge.title, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(challenge.description, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Bấm để tham gia thi đấu 🚀", color = Color(0xFFE65100), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}