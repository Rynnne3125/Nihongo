package com.example.nihongo.Admin.ui

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nihongo.Admin.viewmodel.AdminExerciseViewModel
import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.ExerciseType
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePage(
    lessonId: String,
    subLessonId: String,
    onBack: () -> Unit,
    viewModel: AdminExerciseViewModel = viewModel()
) {
    BackHandler {
        onBack()
    }

    val exercises by viewModel.exercises.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentExercise by viewModel.currentExercise.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedExerciseId by remember { mutableStateOf<String?>(null) }

    // Load exercises when component is first created
    LaunchedEffect(lessonId, subLessonId) {
        viewModel.loadExercises(lessonId, subLessonId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercises Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.setCurrentExercise(viewModel.createEmptyExercise(subLessonId))
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Exercise")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if (exercises.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No exercises found",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.setCurrentExercise(viewModel.createEmptyExercise(subLessonId))
                                showAddDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Exercise")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(exercises) { exercise ->
                            ExerciseItem(
                                exercise = exercise,
                                onEdit = {
                                    viewModel.setCurrentExercise(exercise)
                                    showEditDialog = true
                                },
                                onDelete = {
                                    viewModel.setCurrentExercise(exercise)
                                    selectedExerciseId = exercise.id
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // Error message
            errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss", color = Color.White)
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }

    // Add Exercise Dialog
    if (showAddDialog) {
        currentExercise?.let { exercise ->
            ExerciseDialog(
                exercise = exercise,
                isNew = true,
                onDismiss = { showAddDialog = false },
                onSave = { updatedExercise ->
                    viewModel.createExercise(
                        lessonId = lessonId,
                        exercise = updatedExercise,
                        onSuccess = { showAddDialog = false },
                        onError = { /* Error is handled by viewModel */ }
                    )
                }
            )
        }
    }

    // Edit Exercise Dialog
    if (showEditDialog) {
        currentExercise?.let { exercise ->
            ExerciseDialog(
                exercise = exercise,
                isNew = false,
                onDismiss = { showEditDialog = false },
                onSave = { updatedExercise ->
                    viewModel.updateExercise(
                        lessonId = lessonId,
                        exercise = updatedExercise,
                        onSuccess = { showEditDialog = false },
                        onError = { /* Error is handled by viewModel */ }
                    )
                }
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Exercise") },
            text = { Text("Are you sure you want to delete this exercise? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedExerciseId?.let { exerciseId ->
                            viewModel.deleteExercise(
                                lessonId = lessonId,
                                exerciseId = exerciseId,
                                subLessonId = subLessonId,
                                onSuccess = { showDeleteDialog = false },
                                onError = { /* Error is handled by viewModel */ }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ExerciseItem(
    exercise: Exercise,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showVideo by remember { mutableStateOf(false) }
    var showAudio by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Exercise type badge
            Box(
                modifier = Modifier
                    .background(
                        when (exercise.type) {
                            ExerciseType.VIDEO -> Color(0xFF2196F3)
                            ExerciseType.PRACTICE -> Color(0xFF4CAF50)
                            else -> Color.Gray
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = exercise.type?.name ?: "UNKNOWN",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title if available
            exercise.title?.let {
                if (it.isNotEmpty()) {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Question
            Text(
                text = exercise.question ?: "No question",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Image if available
            exercise.imageUrl?.let {
                if (it.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(it)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Exercise Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Video preview and player
            exercise.videoUrl?.let {
                if (it.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showVideo = !showVideo },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Video",
                            tint = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showVideo) "Hide Video" else "Show Video",
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (showVideo) {
                        Spacer(modifier = Modifier.height(8.dp))
                        VideoPlayer(url = it)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Audio preview and player
            exercise.audioUrl?.let {
                if (it.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAudio = !showAudio },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AudioFile,
                            contentDescription = "Audio",
                            tint = Color(0xFF673AB7)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showAudio) "Hide Audio" else "Show Audio",
                            color = Color(0xFF673AB7),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (showAudio) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AudioPlayer(url = it)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Kana and Romanji if available
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                exercise.kana?.let {
                    if (it.isNotEmpty()) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Kana:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = it,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                exercise.romanji?.let {
                    if (it.isNotEmpty()) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Romanji:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = it,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Answer if available
            exercise.answer?.let {
                if (it.isNotEmpty()) {
                    Text(
                        text = "Answer: $it",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Options if available
            exercise.options?.let {
                if (it.isNotEmpty()) {
                    Text(
                        text = "Options:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                    it.forEachIndexed { index, option ->
                        Text(
                            text = "• $option",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Explanation if available
            exercise.explanation?.let {
                if (it.isNotEmpty()) {
                    Text(
                        text = "Explanation:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF2196F3)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE53935)
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(url: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun AudioPlayer(url: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(1000)
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(1L)
        }
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration = exoPlayer.duration
                }
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Audio player controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = {
                if (isPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color(0xFF673AB7)
                )
            }

            Text(
                text = formatDuration(currentPosition),
                color = Color.DarkGray,
                fontSize = 12.sp,
                modifier = Modifier.width(50.dp)
            )

            Slider(
                value = currentPosition.toFloat(),
                onValueChange = {
                    currentPosition = it.toLong()
                    exoPlayer.seekTo(it.toLong())
                },
                valueRange = 0f..duration.toFloat(),
                modifier = Modifier.weight(1f)
            )

            Text(
                text = formatDuration(duration),
                color = Color.DarkGray,
                fontSize = 12.sp,
                modifier = Modifier.width(50.dp)
            )
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDialog(
    exercise: Exercise,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit
) {
    // State for VIDEO type fields
    var title by remember { mutableStateOf(exercise.title ?: "") }
    var videoUrl by remember { mutableStateOf(exercise.videoUrl ?: "") }
    var videoQuestion by remember { mutableStateOf(exercise.question ?: "") }
    var explanation by remember { mutableStateOf(exercise.explanation ?: "") }

    // State for PRACTICE type fields
    var practiceQuestion by remember { mutableStateOf(exercise.question ?: "") }
    var practiceAnswer by remember { mutableStateOf(exercise.answer ?: "") }
    var optionsList = remember { exercise.options?.toMutableList() ?: mutableListOf<String>() }
    var optionsState by remember { mutableStateOf(optionsList) } // For triggering recomposition
    var romanji by remember { mutableStateOf(exercise.romanji ?: "") }
    var kana by remember { mutableStateOf(exercise.kana ?: "") }
    var audioUrl by remember { mutableStateOf(exercise.audioUrl ?: "") }
    var imageUrl by remember { mutableStateOf(exercise.imageUrl ?: "") }

    // Exercise type selection
    var type by remember { mutableStateOf(exercise.type ?: ExerciseType.PRACTICE) }

    // Preview states
    var showVideoPreview by remember { mutableStateOf(false) }
    var showAudioPreview by remember { mutableStateOf(false) }
    // Selected default image index
    val defaultImages = listOf(
        "https://img.freepik.com/premium-vector/man-wearing-hakama-with-crest-thinking-while-scratching-his-face_180401-12331.jpg",
        "https://thumb.ac-illust.com/95/95d92d1467ccf189f05af2b503a3e5a0_t.jpeg",
        "https://drive.google.com/uc?export=view&id=1TWpes3nKYbwSWyUj0uG0v5p-_a_zaVVh"
    )
    // Image selection mode
    var useDefaultImage by remember { mutableStateOf(exercise.imageUrl?.let { url ->
        defaultImages.contains(url)
    } ?: false) }



    var selectedDefaultImageIndex by remember {
        mutableStateOf(
            if (useDefaultImage) defaultImages.indexOf(exercise.imageUrl).takeIf { it >= 0 } ?: 0 else 0
        )
    }

    // Option being edited
    var newOption by remember { mutableStateOf("") }

    // Track if any field has changed
    var hasChanges by remember { mutableStateOf(isNew) }

    // Show different fields based on exercise type
    val isVideoType = type == ExerciseType.VIDEO
    val isPracticeType = type == ExerciseType.PRACTICE

    // Validate fields based on type
    val isSaveEnabled = hasChanges && when (type) {
        ExerciseType.VIDEO -> videoUrl.isNotBlank() && title.isNotBlank()
        ExerciseType.PRACTICE -> practiceQuestion.isNotBlank() && practiceAnswer.isNotBlank()
        else -> false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "Add New Exercise" else "Edit Exercise",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                // Exercise Type Selector
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Exercise Type",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExerciseType.values()
                                .filter { it == ExerciseType.PRACTICE || it == ExerciseType.VIDEO }
                                .forEach { exerciseType ->
                                    FilterChip(
                                        selected = type == exerciseType,
                                        onClick = {
                                            type = exerciseType
                                            hasChanges = true
                                        },
                                        label = {
                                            Text(
                                                text = when(exerciseType) {
                                                    ExerciseType.PRACTICE -> "Practice"
                                                    ExerciseType.VIDEO -> "Video"
                                                    else -> exerciseType.name
                                                }
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when(exerciseType) {
                                                    ExerciseType.PRACTICE -> Icons.Default.Assignment
                                                    ExerciseType.VIDEO -> Icons.Default.VideoLibrary
                                                    else -> Icons.Default.Check
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                        }
                    }
                }

                // Main content scrollable area
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // VIDEO TYPE FIELDS
                    if (isVideoType) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Title (required for VIDEO)
                                    OutlinedTextField(
                                        value = title,
                                        onValueChange = {
                                            title = it
                                            hasChanges = true
                                        },
                                        label = { Text("Title *") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = title.isBlank(),
                                        supportingText = {
                                            if (title.isBlank()) {
                                                Text("Title is required for video exercises")
                                            }
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Question (optional for VIDEO)
                                    OutlinedTextField(
                                        value = videoQuestion,
                                        onValueChange = {
                                            videoQuestion = it
                                            hasChanges = true
                                        },
                                        label = { Text("Question (Optional)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Video URL with preview
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Video Content",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = videoUrl,
                                        onValueChange = {
                                            videoUrl = it
                                            hasChanges = true
                                        },
                                        label = { Text("Video URL *") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = videoUrl.isBlank(),
                                        supportingText = {
                                            if (videoUrl.isBlank()) {
                                                Text("Video URL is required")
                                            }
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { showVideoPreview = !showVideoPreview }) {
                                                Icon(
                                                    imageVector = if (showVideoPreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = if (showVideoPreview) "Hide preview" else "Show preview"
                                                )
                                            }
                                        }
                                    )

                                    AnimatedVisibility(
                                        visible = showVideoPreview && videoUrl.isNotBlank(),
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        if (videoUrl.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                                    .padding(top = 16.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                            ) {
                                                VideoPlayer(videoUrl)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    OutlinedTextField(
                                        value = explanation,
                                        onValueChange = {
                                            explanation = it
                                            hasChanges = true
                                        },
                                        label = { Text("Explanation") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )
                                }
                            }
                        }
                    }

                    // PRACTICE TYPE FIELDS
                    if (isPracticeType) {
                        // Basic info
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = practiceQuestion,
                                        onValueChange = {
                                            practiceQuestion = it
                                            hasChanges = true
                                        },
                                        label = { Text("Question *") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = practiceQuestion.isBlank(),
                                        supportingText = {
                                            if (practiceQuestion.isBlank()) {
                                                Text("Question is required")
                                            }
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

//                                    OutlinedTextField(
//                                        value = practiceAnswer,
//                                        onValueChange = {
//                                            practiceAnswer = it
//                                            hasChanges = true
//                                        },
//                                        label = { Text("Answer *") },
//                                        modifier = Modifier.fillMaxWidth(),
//                                        isError = practiceAnswer.isBlank(),
//                                        supportingText = {
//                                            if (practiceAnswer.isBlank()) {
//                                                Text("Answer is required")
//                                            }
//                                        }
//                                    )
                                }
                            }
                        }

                        // Japanese content
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Japanese Content",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = romanji,
                                            onValueChange = {
                                                romanji = it
                                                hasChanges = true
                                            },
                                            label = { Text("Romanji") },
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = kana,
                                            onValueChange = {
                                                kana = it
                                                hasChanges = true
                                            },
                                            label = { Text("Kana") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Multiple choice options with checkboxes
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Multiple Choice Options",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Add options and check the correct answer",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Display existing options with checkboxes
                                    optionsList.forEachIndexed { index, option ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = option == practiceAnswer,
                                                onCheckedChange = { isChecked ->
                                                    if (isChecked) {
                                                        practiceAnswer = option
                                                        hasChanges = true
                                                    }
                                                }
                                            )

                                            Text(
                                                text = option,
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.bodyMedium
                                            )

                                            IconButton(
                                                onClick = {
                                                    optionsList.removeAt(index)
                                                    optionsState = ArrayList(optionsList) // Create new list reference to trigger recomposition
                                                    hasChanges = true
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remove option",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }

                                    // Add new option
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = newOption,
                                            onValueChange = { newOption = it },
                                            label = { Text("New Option") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )

                                        IconButton(
                                            onClick = {
                                                if (newOption.isNotBlank()) {
                                                    optionsList.add(newOption)
                                                    optionsState = ArrayList(optionsList) // Create new list reference to trigger recomposition
                                                    newOption = ""
                                                    hasChanges = true
                                                }
                                            },
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add option"
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Media content with improved image selection
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Media Content",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Image selection - TabRow for selection mode
                                    Text(
                                        text = "Exercise Image",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    TabRow(
                                        selectedTabIndex = if (useDefaultImage) 1 else 0,
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Tab(
                                            selected = !useDefaultImage,
                                            onClick = {
                                                useDefaultImage = false
                                                hasChanges = true
                                            },
                                            text = { Text("Custom URL") }
                                        )
                                        Tab(
                                            selected = useDefaultImage,
                                            onClick = {
                                                useDefaultImage = true
                                                imageUrl = defaultImages[selectedDefaultImageIndex]
                                                hasChanges = true
                                            },
                                            text = { Text("Default Images") }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Show different content based on tab selection
                                    if (!useDefaultImage) {
                                        OutlinedTextField(
                                            value = imageUrl,
                                            onValueChange = {
                                                imageUrl = it
                                                hasChanges = true
                                            },
                                            label = { Text("Custom Image URL") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    } else {
                                        LazyRow(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            itemsIndexed(defaultImages) { index, defaultImage ->
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(90.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .border(
                                                                width = 2.dp,
                                                                color = if (selectedDefaultImageIndex == index)
                                                                    MaterialTheme.colorScheme.primary
                                                                else
                                                                    Color.Transparent,
                                                                shape = RoundedCornerShape(8.dp)
                                                            )
                                                            .clickable {
                                                                selectedDefaultImageIndex = index
                                                                imageUrl = defaultImage
                                                                hasChanges = true
                                                            }
                                                    ) {
                                                        AsyncImage(
                                                            model = defaultImage,
                                                            contentDescription = "Default image ${index + 1}",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )

                                                        if (selectedDefaultImageIndex == index) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .background(Color(0x66000000)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.CheckCircle,
                                                                    contentDescription = "Selected",
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.size(32.dp)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    Text(
                                                        text = "Image ${index + 1}",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Audio URL with preview
                                    OutlinedTextField(
                                        value = audioUrl,
                                        onValueChange = {
                                            audioUrl = it
                                            hasChanges = true
                                        },
                                        label = { Text("Audio URL") },
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(onClick = { showAudioPreview = !showAudioPreview }) {
                                                Icon(
                                                    imageVector = if (showAudioPreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = if (showAudioPreview) "Hide preview" else "Show preview"
                                                )
                                            }
                                        }
                                    )

                                    AnimatedVisibility(
                                        visible = showAudioPreview && audioUrl.isNotBlank(),
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        if (audioUrl.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 16.dp)
                                            ) {
                                                AudioPlayer(audioUrl)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            // Create updated exercise object based on the current type
                            val updatedExercise = when (type) {
                                ExerciseType.VIDEO -> exercise.copy(
                                    type = ExerciseType.VIDEO,
                                    title = title,
                                    videoUrl = videoUrl,
                                    question = videoQuestion,
                                    explanation = explanation,
                                    // Reset PRACTICE specific fields
                                    answer = null,
                                    options = null,
                                    romanji = null,
                                    kana = null,
                                    audioUrl = null,
                                    imageUrl = null
                                )
                                ExerciseType.PRACTICE -> exercise.copy(
                                    type = ExerciseType.PRACTICE,
                                    question = practiceQuestion,
                                    answer = practiceAnswer,
                                    options = optionsList,
                                    romanji = romanji.ifBlank { null },
                                    kana = kana.ifBlank { null },
                                    audioUrl = audioUrl.ifBlank { null },
                                    imageUrl = imageUrl.ifBlank { null },
                                    // Reset VIDEO specific fields
                                    title = null,
                                    videoUrl = null,
                                    explanation = null
                                )
                                else -> exercise // Shouldn't happen
                            }
                            onSave(updatedExercise)
                        },
                        enabled = isSaveEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}