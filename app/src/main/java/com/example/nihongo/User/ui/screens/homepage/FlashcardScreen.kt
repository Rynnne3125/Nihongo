package com.example.nihongo.User.ui.screens.homepage

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.nihongo.User.data.models.User
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.ui.components.BottomNavigationBar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(navController: NavController, user_email: String, initialTab: String? = null) {
    val tabs = listOf("Hiragana", "Katakana", "Kanji", "Từ vựng")
    val context = LocalContext.current
    
    // Xác định tab ban đầu dựa trên tham số
    val initialTabIndex = when (initialTab) {
        "hiragana" -> 0
        "katakana" -> 1
        "kanji" -> 2
        "vocabulary" -> 3
        else -> 0 // Mặc định là Hiragana
    }
    
    var selectedTab by remember { mutableStateOf(initialTabIndex) }
    val selectedItem = "vocabulary"
    var showStartSessionDialog by remember { mutableStateOf(false) }
    var studySessionConfig by remember { mutableStateOf<StudySessionConfig?>(null) }
    
    // Tap-to-Explain states
    var showTapToExplain by remember { mutableStateOf(false) }
    var selectedWord by remember { mutableStateOf("") }
    var selectedContext by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Flashcard Nhật Ngữ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "Học hiệu quả - Nhớ lâu hơn",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showStartSessionDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Bắt đầu học",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedItem = selectedItem,
                userEmail = user_email,
                onItemSelected = { selectedRoute ->
                    navController.navigate("$selectedRoute/$user_email") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF4CAF50),
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = Color(0xFF4CAF50)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Nội dung flashcard, chuyển động mượt mà khi đổi tab
            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) { targetState ->
                when (targetState) {
                    0 -> HiraganaFlashcard(
                        onWordTapped = { word, context ->
                            selectedWord = word
                            selectedContext = context
                            showTapToExplain = true
                        }
                    )
                    1 -> KatakanaFlashcard(
                        onWordTapped = { word, context ->
                            selectedWord = word
                            selectedContext = context
                            showTapToExplain = true
                        }
                    )
                    2 -> KanjiFlashcard(
                        onWordTapped = { word, context ->
                            selectedWord = word
                            selectedContext = context
                            showTapToExplain = true
                        }
                    )
                    3 -> VocabularyFlashcard(
                        onWordTapped = { word, context ->
                            selectedWord = word
                            selectedContext = context
                            showTapToExplain = true
                        }
                    )
                    else -> Unit
                }
            }
        }

        // StudyMode Selection Dialog
        if (showStartSessionDialog) {
            StudySetupDialog(
                onDismiss = { showStartSessionDialog = false },
                onStartSession = { config ->
                    studySessionConfig = config
                    showStartSessionDialog = false

                    // Update the selected tab based on card types
                    // Chọn tab đầu tiên trong danh sách các loại thẻ được chọn
                    // Nếu chọn "mixed" thì giữ nguyên tab hiện tại
                    if (!config.cardTypes.contains("mixed")) {
                        val firstType = config.cardTypes.firstOrNull()
                        when (firstType) {
                            "hiragana" -> selectedTab = 0
                            "katakana" -> selectedTab = 1
                            "kanji" -> selectedTab = 2
                            "vocabulary" -> selectedTab = 3
                        }
                    }
                }
            )
        }
        
        // Study Session Screen
        studySessionConfig?.let { config ->
            PracticeSessionDialog(
                config = config,
                user_email = user_email,
                onDismiss = { studySessionConfig = null }
            )
        }
    }
}

@Composable
fun StudySetupDialog(
    onDismiss: () -> Unit,
    onStartSession: (StudySessionConfig) -> Unit
) {
    // Thay đổi từ selectedCardType thành selectedCardTypes (danh sách loại thẻ được chọn)
    var selectedCardTypes by remember { mutableStateOf(setOf<String>()) }
    var selectedCardCount by remember { mutableStateOf(10) }

    // Thêm một biến để kiểm tra nếu không có loại thẻ nào được chọn
    val isSelectionValid = selectedCardTypes.isNotEmpty()

    val cardTypeOptions = listOf(
        "hiragana" to "Hiragana",
        "katakana" to "Katakana",
        "kanji" to "Kanji",
        "vocabulary" to "Từ vựng",
        "mixed" to "Trộn tất cả"
    )

    val cardCountOptions = listOf(5, 10, 15, 20, 30, 50)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Thiết lập học tập",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF388E3C),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Card Type Selection
                Text(
                    text = "Chọn loại thẻ (có thể chọn nhiều)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    maxItemsInEachRow = 3,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cardTypeOptions.forEach { (value, text) ->
                        val isSelected = selectedCardTypes.contains(value)

                        // Nếu "mixed" được chọn, hủy chọn các loại khác
                        // Nếu một loại khác được chọn, hủy chọn "mixed"
                        val onClick = {
                            selectedCardTypes = when {
                                value == "mixed" && !isSelected -> setOf("mixed")
                                value != "mixed" && !isSelected -> {
                                    val newSelection = selectedCardTypes.toMutableSet()
                                    newSelection.add(value)
                                    newSelection.remove("mixed")
                                    newSelection
                                }
                                else -> {
                                    val newSelection = selectedCardTypes.toMutableSet()
                                    newSelection.remove(value)
                                    newSelection
                                }
                            }
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = onClick,
                            label = { Text(text, fontSize = 14.sp) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF81C784),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Thêm dòng text hiển thị loại thẻ được chọn
                if (selectedCardTypes.isNotEmpty()) {
                    val selectedTypesText = if (selectedCardTypes.contains("mixed")) {
                        "Tất cả loại thẻ"
                    } else {
                        selectedCardTypes.joinToString(", ") { type ->
                            cardTypeOptions.find { it.first == type }?.second ?: ""
                        }
                    }

                    Text(
                        text = "Đã chọn: $selectedTypesText",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Card Count Selection
                Text(
                    text = "Số lượng thẻ: $selectedCardCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    cardCountOptions.forEach { count ->
                        OutlinedButton(
                            onClick = { selectedCardCount = count },
                            shape = CircleShape,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (selectedCardCount == count) Color(0xFF4CAF50) else Color.LightGray
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedCardCount == count) Color(0xFF4CAF50) else Color.Transparent,
                                contentColor = if (selectedCardCount == count) Color.White else Color.DarkGray
                            ),
                            modifier = Modifier.size(46.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Hủy")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (isSelectionValid) {
                                onStartSession(
                                    StudySessionConfig(
                                        // Truyền danh sách loại thẻ được chọn thay vì một chuỗi
                                        cardTypes = selectedCardTypes.toList(),
                                        cardCount = selectedCardCount
                                    )
                                )
                            }
                        },
                        enabled = isSelectionValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bắt đầu học")
                    }
                }
            }
        }
    }
}

// Cập nhật lớp StudySessionConfig để hỗ trợ nhiều loại thẻ
data class StudySessionConfig(
    // Thay đổi từ cardType thành cardTypes
    val cardTypes: List<String>,
    val cardCount: Int
)

@Composable
fun PracticeSessionDialog(
    config: StudySessionConfig,
    user_email: String,
    onDismiss: () -> Unit
) {
    var flashcards by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentCardIndex by remember { mutableStateOf(0) }
    var isCardFlipped by remember { mutableStateOf(false) }
    var userAnswer by remember { mutableStateOf("") }
    var remainingAttempts by remember { mutableStateOf(2) }
    var score by remember { mutableStateOf(0) }
    var isGameComplete by remember { mutableStateOf(false) }
    var correctAnswers by remember { mutableStateOf(0) }
    val userRepository = remember { UserRepository() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    

    var currentUser by remember { mutableStateOf<User?>(null) }
    
    // Load user data with logging
    LaunchedEffect(user_email) {
        Log.d("FlashcardScreen", "Attempting to load user with email: '$user_email'")
        
        if (user_email.isNotEmpty()) {
            try {
                val user = userRepository.getUserByEmail(user_email)
                currentUser = user
                
                if (user != null) {
                    Log.d("FlashcardScreen", "Successfully loaded user: id=${user.id}, username=${user.username}, activityPoints=${user.activityPoints}")
                } else {
                    Log.e("FlashcardScreen", "Failed to load user: getUserByEmail returned null")
                }
            } catch (e: Exception) {
                Log.e("FlashcardScreen", "Error loading user data", e)
            }
        } else {
            Log.w("FlashcardScreen", "Empty user email, cannot load user data")
        }
    }
    

    // Hiển thị tiêu đề phù hợp với các loại thẻ đã chọn
    val sessionTitle = when {
        config.cardTypes.contains("mixed") -> "Luyện tập trộn"
        config.cardTypes.size == 1 -> when(config.cardTypes.first()) {
            "hiragana" -> "Luyện tập Hiragana"
            "katakana" -> "Luyện tập Katakana"
            "kanji" -> "Luyện tập Kanji"
            "vocabulary" -> "Luyện tập Từ vựng"
            else -> "Luyện tập"
        }
        else -> {
            val typeNames = config.cardTypes.map { type ->
                when(type) {
                    "hiragana" -> "Hiragana"
                    "katakana" -> "Katakana"
                    "kanji" -> "Kanji"
                    "vocabulary" -> "Từ vựng"
                    else -> ""
                }
            }
            "Flashcard Game"
        }
    }

    // Load flashcards based on selected types
    LaunchedEffect(config) {
        // Nếu chọn "mixed", lấy tất cả các loại thẻ
        if (config.cardTypes.contains("mixed")) {
            val hiragana = getFlashcardsByExerciseId("hiragana_basic")
            val katakana = getFlashcardsByExerciseId("katakana_basic")
            val kanji = getFlashcardsByExerciseId("kanji_n5")
            val vocabulary = getFlashcardsByExerciseId("vocabulary_n5")
            flashcards = (hiragana + katakana + kanji + vocabulary).shuffled()
        } else {
            // Nếu không, lấy các loại thẻ được chọn
            val selectedFlashcards = mutableListOf<Pair<String, String>>()

            if (config.cardTypes.contains("hiragana")) {
                selectedFlashcards.addAll(getFlashcardsByExerciseId("hiragana_basic"))
            }
            if (config.cardTypes.contains("katakana")) {
                selectedFlashcards.addAll(getFlashcardsByExerciseId("katakana_basic"))
            }
            if (config.cardTypes.contains("kanji")) {
                selectedFlashcards.addAll(getFlashcardsByExerciseId("kanji_n5"))
            }
            if (config.cardTypes.contains("vocabulary")) {
                selectedFlashcards.addAll(getFlashcardsByExerciseId("vocabulary_n5"))
            }

            flashcards = selectedFlashcards.shuffled()
        }

        // Limit the number of cards based on config
        if (flashcards.size > config.cardCount) {
            flashcards = flashcards.take(config.cardCount).shuffled()
        }

        isLoading = false
    }

    // Check answer function
    fun checkAnswer() {
        if (currentCardIndex < flashcards.size) {
            val currentCard = flashcards[currentCardIndex]
            val correctAnswer = currentCard.second.trim().lowercase()
            val userInputAnswer = userAnswer.trim().lowercase()

            if (correctAnswer == userInputAnswer) {
                // Correct answer
                score += 10
                correctAnswers++

                // Move to next card
                if (currentCardIndex < flashcards.size - 1) {
                    currentCardIndex++
                    isCardFlipped = false
                    userAnswer = ""
                    remainingAttempts = 2
                } else {
                    // Game complete
                    isGameComplete = true
                }
            } else {
                // Wrong answer
                remainingAttempts--

                if (remainingAttempts <= 0) {
                    // Show answer
                    isCardFlipped = true
                }
            }
        }
    }

    // Next card function
    fun nextCard() {
        if (currentCardIndex < flashcards.size - 1) {
            currentCardIndex++
            isCardFlipped = false
            userAnswer = ""
            remainingAttempts = 2
        } else {
            // Game complete
            isGameComplete = true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sessionTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Điểm: $score",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }

                // Progress
                if (!isGameComplete) {
                    LinearProgressIndicator(
                        progress = (currentCardIndex + 1).toFloat() / flashcards.size.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFE0E0E0)
                    )

                    Text(
                        text = "Thẻ ${currentCardIndex + 1}/${flashcards.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }


                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(32.dp),
                        color = Color(0xFF4CAF50)
                    )
                } else if (flashcards.isEmpty()) {
                    Text("Không tìm thấy thẻ flashcard cho loại này")
                } else if (isGameComplete) {
                    // Game Complete Screen
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (correctAnswers >= flashcards.size / 2)
                                Icons.Default.EmojiEvents else Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (correctAnswers >= flashcards.size / 2)
                                Color(0xFFFFD700) else Color(0xFF4CAF50),
                            modifier = Modifier.size(72.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Hoàn thành!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Điểm của bạn: $score",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Trả lời đúng: $correctAnswers/${flashcards.size}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                // Add detailed logging to diagnose issues
                                Log.d("FlashcardScreen", "Kết thúc button clicked, score=$score, currentUser=${currentUser?.id ?: "null"}")
                                
                                // Add activity points to user when they click "Kết thúc"
                                if (score > 0 && currentUser != null) {
                                    try {
                                        // Thực hiện việc thêm điểm trong một coroutine riêng biệt
                                        // không phụ thuộc vào rememberCoroutineScope
                                        val userId = currentUser!!.id
                                        val pointsToAdd = score
                                        
                                        // Sử dụng GlobalScope để đảm bảo coroutine tiếp tục chạy
                                        // ngay cả khi composition bị hủy
                                        GlobalScope.launch(Dispatchers.IO) {
                                            try {
                                                Log.d("FlashcardScreen", "GlobalScope: Adding $pointsToAdd points to user $userId")
                                                val repo = UserRepository()
                                                val success = repo.addActivityPoints(userId, pointsToAdd)
                                                
                                                if (success) {
                                                    Log.d("FlashcardScreen", "GlobalScope: Successfully added points")
                                                    // Hiển thị toast trên Main thread
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(
                                                            context,
                                                            "Đã thêm $pointsToAdd điểm năng động!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                } else {
                                                    Log.e("FlashcardScreen", "GlobalScope: Failed to add points")
                                                }
                                            } catch (e: Exception) {
                                                Log.e("FlashcardScreen", "GlobalScope: Error adding points", e)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("FlashcardScreen", "Error starting GlobalScope coroutine", e)
                                    }
                                } else {
                                    if (score <= 0) {
                                        Log.w("FlashcardScreen", "Not adding points: score=$score is not positive")
                                    }
                                    if (currentUser == null) {
                                        Log.w("FlashcardScreen", "Not adding points: currentUser is null")
                                    }
                                }
                                
                                // Đóng dialog ngay lập tức - không cần delay
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.width(200.dp)
                        ) {
                            Text("Kết thúc")
                        }
                    }
                } else {
                    // Current card
                    val currentCard = flashcards[currentCardIndex]

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(vertical = 24.dp)
                    ) {
                        // Card with flip animation
                        FlippableCard(
                            frontContent = {
                                // Front of card (question)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = Color(0xFFE8F5E9),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFBDBDBD),
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentCard.first,
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            backContent = {
                                // Back of card (answer)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = Color(0xFFF1F8E9),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFBDBDBD),
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Đáp án:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = currentCard.second,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            },
                            isFlipped = isCardFlipped
                        )
                    }

                    // Attempts remaining
                    if (!isCardFlipped) {
                        Row(
                            modifier = Modifier.padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Lượt còn lại: ",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            repeat(remainingAttempts) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = Color(0xFFE57373),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Answer input
                    if (!isCardFlipped) {
                        OutlinedTextField(
                            value = userAnswer,
                            onValueChange = { userAnswer = it },
                            label = { Text("Nhập đáp án") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { checkAnswer() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { checkAnswer() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Kiểm tra")
                        }
                    } else {
                        // When card is flipped, show next button
                        Button(
                            onClick = { nextCard() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Tiếp tục")
                        }
                    }

                    // Control buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                // Skip current card
                                if (!isCardFlipped) {
                                    isCardFlipped = true
                                } else {
                                    nextCard()
                                }
                            },
                            border = BorderStroke(1.dp, Color(0xFFBDBDBD)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF757575)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Skip"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bỏ qua")
                        }

                        OutlinedButton(
                            onClick = {
                                // End session
                                isGameComplete = true
                            },
                            border = BorderStroke(1.dp, Color(0xFFBDBDBD)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF757575)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "End"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dừng")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlippableCard(
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit,
    isFlipped: Boolean
) {
    val rotation = animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 12f * density
            }
    ) {
        // Back content - visible when flipped
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Only show back when card is flipped
                    alpha = if (rotation.value > 90f) 1f else 0f
                    // Apply 180 rotation to make text readable after flip
                    rotationY = 180f
                }
        ) {
            backContent()
        }

        // Front content - visible initially
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Only show front when card is not flipped
                    alpha = if (rotation.value < 90f) 1f else 0f
                }
        ) {
            frontContent()
        }
    }
}

@Composable
fun HiraganaFlashcard(onWordTapped: ((String, String) -> Unit)? = null) {
    var flashcards by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        flashcards = getFlashcardsByExerciseId("hiragana_basic")
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4CAF50))
        }
    } else if (flashcards.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy thẻ Hiragana")
        }
    } else {
        ImprovedFlashcardGrid(flashcards, onWordTapped)
    }
}

@Composable
fun KatakanaFlashcard(onWordTapped: ((String, String) -> Unit)? = null) {
    var flashcards by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        flashcards = getFlashcardsByExerciseId("katakana_basic")
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4CAF50))
        }
    } else if (flashcards.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy thẻ Katakana")
        }
    } else {
        ImprovedFlashcardGrid(flashcards, onWordTapped)
    }
}

@Composable
fun KanjiFlashcard(onWordTapped: ((String, String) -> Unit)? = null) {
    var flashcards by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        flashcards = getFlashcardsByExerciseId("kanji_n5")
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4CAF50))
        }
    } else if (flashcards.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy thẻ Kanji")
        }
    } else {
        ImprovedFlashcardGrid(flashcards, onWordTapped)
    }
}

@Composable
fun VocabularyFlashcard(onWordTapped: ((String, String) -> Unit)? = null) {
    var flashcards by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        flashcards = getFlashcardsByExerciseId("vocabulary_n5")
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4CAF50))
        }
    } else if (flashcards.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy thẻ Từ vựng")
        }
    } else {
        ImprovedFlashcardGrid(flashcards, onWordTapped)
    }
}

@Composable
fun ImprovedFlashcardGrid(
    flashcards: List<Pair<String, String>>,
    onWordTapped: ((String, String) -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        // Bỏ modifier height cố định
        modifier = Modifier.fillMaxSize()
    ) {
        items(flashcards.size) { index ->
            FlashcardGridItem(
                flashcard = flashcards[index],
                onWordTapped = onWordTapped
            )
        }
    }
}

@Composable
fun FlashcardGridItem(
    flashcard: Pair<String, String>,
    onWordTapped: ((String, String) -> Unit)? = null
) {
    var isFlipped by remember { mutableStateOf(false) }

    Card(
        onClick = { isFlipped = !isFlipped },
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isFlipped) Color(0xFFF1F8E9) else Color(0xFFE8F5E9)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isFlipped) {
                // Hiển thị định nghĩa - có thể tap để giải thích
                Text(
                    text = flashcard.second,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            onWordTapped?.invoke(flashcard.second, flashcard.first)
                        }
                )
            } else {
                // Hiển thị thuật ngữ - có thể tap để giải thích
                Text(
                    text = flashcard.first,
                    textAlign = TextAlign.Center,
                    fontSize = when {
                        flashcard.first.length <= 2 -> 24.sp
                        flashcard.first.length <= 4 -> 20.sp
                        else -> 16.sp
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            onWordTapped?.invoke(flashcard.first, flashcard.second)
                        }
                )
            }
        }
    }
}

val hiraganaOrder = listOf(
    "あ", "い", "う", "え", "お",
    "か", "き", "く", "け", "こ",
    "さ", "し", "す", "せ", "そ",
    "た", "ち", "つ", "て", "と",
    "な", "に", "ぬ", "ね", "の",
    "は", "ひ", "ふ", "へ", "ほ",
    "ま", "み", "む", "め", "も",
    "や",       "ゆ",       "よ",
    "ら", "り", "る", "れ", "ろ",
    "わ",                   "を",
    "ん"
)

val katakanaOrder = listOf(
    "ア", "イ", "ウ", "エ", "オ",
    "カ", "キ", "ク", "ケ", "コ",
    "サ", "シ", "ス", "セ", "ソ",
    "タ", "チ", "ツ", "テ", "ト",
    "ナ", "ニ", "ヌ", "ネ", "ノ",
    "ハ", "ヒ", "フ", "ヘ", "ホ",
    "マ", "ミ", "ム", "メ", "モ",
    "ヤ",       "ユ",       "ヨ",
    "ラ", "リ", "ル", "レ", "ロ",
    "ワ",                   "ヲ",
    "ン"
)

suspend fun getFlashcardsByExerciseId(exerciseId: String): List<Pair<String, String>> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val snapshot = firestore.collection("flashcards")
            .whereEqualTo("exerciseId", exerciseId)
            .get()
            .await()

        val rawList = snapshot.documents.mapNotNull { doc ->
            val term = doc.getString("term")
            val definition = doc.getString("definition")
            if (term != null && definition != null) term to definition else null
        }

        // Sắp xếp Hiragana và Katakana
        when (exerciseId) {
            "hiragana_basic" -> {
                hiraganaOrder.mapNotNull { orderChar -> rawList.find { it.first == orderChar } }
            }
            "katakana_basic" -> {
                katakanaOrder.mapNotNull { orderChar -> rawList.find { it.first == orderChar } }
            }
            else -> rawList // nếu không phải hiragana hay katakana, trả lại danh sách không sắp xếp
        }

    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}
