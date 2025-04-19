package com.example.nihongo.User.ui.screens.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.Flashcard
import com.example.nihongo.User.data.models.Lesson
import com.example.nihongo.User.data.repository.ExerciseRepository
import com.example.nihongo.User.data.repository.FlashcardRepository
import com.example.nihongo.User.data.repository.LessonRepository

@Composable
fun LessonsScreen(
    courseId: String, // Changed to String for courseId
    navController: NavController,
    lessonRepository: LessonRepository
) {
    val lessons = remember { mutableStateOf<List<Lesson>>(emptyList()) }

    LaunchedEffect(courseId) {
        lessons.value = lessonRepository.getLessonsByCourseId(courseId) // Fetch lessons by courseId (String)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Your Lessons",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(lessons.value) { lesson ->
                LessonCard(
                    lesson = lesson,
                    onClick = { navController.navigate("lessons/${lesson.id}") }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun LessonCard(lesson: Lesson, onClick: () -> Unit) {
    val cardColors = listOf(
        Color(0xFFD1C4E9), // Light purple
        Color(0xFFFFCCBC), // Light orange
        Color(0xFFB3E5FC)  // Light blue
    )
    val backgroundColor = remember { cardColors.random() }

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
            .fillMaxWidth()
            .background(color = backgroundColor, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(
            text = lesson.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        lesson.contentText?.let { content ->
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun LessonDetailScreen(
    lessonId: String, // Changed to String for lessonId
    navController: NavController,
    lessonRepository: LessonRepository,
    exerciseRepository: ExerciseRepository,
    flashcardRepository: FlashcardRepository
) {
    val lesson = remember { mutableStateOf<Lesson?>(null) }
    val exercises = remember { mutableStateOf<List<Exercise>>(emptyList()) }
    val flashcards = remember { mutableStateOf<List<Flashcard>>(emptyList()) }

    LaunchedEffect(lessonId) {
        lesson.value = lessonRepository.getLessonById(lessonId) // Fetch lesson by lessonId (String)
        lesson.value?.let {
            exercises.value = exerciseRepository.getExercisesByLessonId(lessonId) // Fetch exercises by lessonId (String)
            flashcards.value = flashcardRepository.getFlashcardsByLessonId(lessonId) // Fetch flashcards by lessonId (String)
        }
    }

    lesson.value?.let { lesson ->
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                SectionCard(title = "Lesson: ${lesson.title}") {
                    lesson.contentText?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray
                        )
                    }

                    lesson.videoUrl?.let {
                        Text(
                            text = "Video: $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (exercises.value.isNotEmpty()) {
                item {
                    SectionCard(title = "Exercises") {
                        exercises.value.forEach { exercise ->
                            Text(
                                text = "- ${exercise.type.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (flashcards.value.isNotEmpty()) {
                item {
                    SectionCard(title = "Flashcards") {
                        flashcards.value.forEach { card ->
                            Text(
                                text = "${card.term}: ${card.definition}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    } ?: run {
        Text(
            text = "Loading lesson details...",
            modifier = Modifier.padding(16.dp),
            color = Color.Gray
        )
    }
}

@Composable
fun SectionCard(
    title: String,
    backgroundColor: Color = listOf(
        Color(0xFFD1C4E9),
        Color(0xFFFFCCBC),
        Color(0xFFB3E5FC)
    ).random(),
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
