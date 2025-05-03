package com.example.nihongo.User.ui.screens.homepage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nihongo.User.ui.components.BottomNavigationBar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(navController: NavController, user_email: String) {
    val tabs = listOf("Hiragana", "Katakana", "Kanji")
    var selectedTab by remember { mutableStateOf(0) }
    val selectedItem = "vocabulary"

    Scaffold(
        containerColor = Color(0xFFEEEEEE),
        topBar = {
            TopAppBar(
                title = {
                    Text("Cùng học Flashcard để nhớ lâu hơn nhé!", style = MaterialTheme.typography.titleLarge)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50), // Màu xanh lá mát
                    titleContentColor = Color.White
                ),
                modifier = Modifier.animateContentSize() // Thêm hiệu ứng chuyển động khi mở/đóng
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
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
        ) {
            // TabRow với hiệu ứng chuyển động mượt mà
            TabRow(
                selectedTabIndex = selectedTab,
                indicator = { tabPositions ->
                    // Tạo hiệu ứng chỉ báo (underline) mượt mà
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(4.dp)
                            .fillMaxWidth()
                            .background(Color(0xFF388E3C)) // Xanh lá đậm
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title, style = MaterialTheme.typography.bodyMedium) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier
                            .animateContentSize() // Mượt mà khi chọn
                            .padding(8.dp) // Tạo không gian cho các tab
                    )
                }
            }

            // Nội dung flashcard, chuyển động mượt mà khi đổi tab
            AnimatedContent(targetState = selectedTab) { targetState ->
                when (targetState) {
                    0 -> HiraganaFlashcard()
                    1 -> KatakanaFlashcard()
                    2 -> KanjiFlashcard()
                    else -> Unit
                }
            }
        }
    }
}



@Composable
fun HiraganaFlashcard() {
    var flashcards by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(Unit) {
        flashcards = getFlashcardsByExerciseId("hiragana_basic")
    }

    if (flashcards.isEmpty()) {
        CircularProgressIndicator()
    } else {
        FlashcardPager(flashcards)
    }
}

@Composable
fun KatakanaFlashcard() {
    var flashcards by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(Unit) {
        flashcards = getFlashcardsByExerciseId("katakana_basic")
    }

    if (flashcards.isEmpty()) {
        CircularProgressIndicator()
    } else {
        FlashcardPager(flashcards)
    }
}

@Composable
fun KanjiFlashcard() {
    var flashcards by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(Unit) {
        flashcards = getFlashcardsByExerciseId("kanji_n5")
    }

    if (flashcards.isEmpty()) {
        CircularProgressIndicator()
    } else {
        FlashcardPager(flashcards)
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
