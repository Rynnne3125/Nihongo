package com.example.nihongo.Admin.ui

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.nihongo.Admin.viewmodel.AdminCourseViewModel
import com.example.nihongo.R
import com.example.nihongo.User.data.models.Course
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import java.util.UUID


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CoursePage(viewModel: AdminCourseViewModel = viewModel()) {

    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchCourses()
    }

    AnimatedContent(
        targetState = selectedCourse?.takeIf { !isEditing },
        transitionSpec = {
            if (targetState != null) {
                // Chuyển tiếp từ danh sách sang chi tiết: vuốt từ phải sang trái
                slideInHorizontally { it } + fadeIn() with
                        slideOutHorizontally { -it } + fadeOut()
            } else {
                // Trở về danh sách: vuốt từ trái sang phải
                slideInHorizontally { -it } + fadeIn() with
                        slideOutHorizontally { it } + fadeOut()
            }
        },
        label = "CourseContentTransition"
    ) { targetCourse ->
        if (targetCourse != null) {
            LessonPage(
                courseId = targetCourse.id,
                onBack = {
                    selectedCourse = null
                    UIVisibilityController.enableDisplayTopBarAndBottom()
                }
            )
            UIVisibilityController.disableDisplayTopBarAndBottom()
        } else {
            CourseListContent(
                courses = courses,
                isLoading = isLoading,
                onAddCourse = {
                    selectedCourse = null
                    showAddDialog = true
                },
                onViewCourse = {
                    selectedCourse = it
                    isEditing = false
                },
                onEditCourse = {
                    selectedCourse = it
                    isEditing = true
                    showAddDialog = true
                },
                onDeleteCourse = {
                    Log.d("CoursePage", "Deleting course with ID: ${it.id}")
                    viewModel.deleteCourse(it)
                },
                showAddDialog = showAddDialog,
                courseToEdit = selectedCourse,
                onDismissDialog = { showAddDialog = false },
                onSaveDB = { course ->
                    if (selectedCourse == null) {
                        viewModel.addCourse(course)
                    } else {
                        viewModel.updateCourse(course)
                    }
                    viewModel.fetchCourses() // ✅ refresh lại sau khi thêm/sửa
                },
                onSaveUI = {
                    showAddDialog = false
                    selectedCourse = null
                    isEditing = false
                }
            )
        }
    }
}

@Composable
fun CourseListContent(
    courses: List<Course>,
    isLoading: Boolean,
    onAddCourse: () -> Unit,
    onViewCourse: (Course) -> Unit,
    onEditCourse: (Course) -> Unit,
    onDeleteCourse: (Course) -> Unit,
    showAddDialog: Boolean,
    courseToEdit: Course?,
    onDismissDialog: () -> Unit,
    onSaveDB: (Course) -> Unit,
    onSaveUI: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text("📚 Course Manager", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddCourse,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Course")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Danh sách khóa học:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(courses) { _, course ->
                    CourseCard(
                        course = course,
                        onView = { onViewCourse(course) },
                        onEdit = { onEditCourse(course) },
                        onDelete = { onDeleteCourse(course) }
                    )
                }

                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        }

        if (showAddDialog) {
            AddCourseDialog(
                showDialog = true,
                courseToEdit = courseToEdit,
                onDismiss = onDismissDialog,
                onSaveDB = onSaveDB,
                onSaveUI = onSaveUI
            )
        }
    }
}


@Composable
fun CourseCard(
    course: Course,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val defaultPainter = rememberAsyncImagePainter("https://drive.google.com/uc?export=view&id=1uyNSW54w4stVjixb9ke_rOFhiGaeekEN")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background image
                AsyncImage(
                    model = course.imageRes,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    placeholder = defaultPainter,
                    error = defaultPainter,
                    fallback = defaultPainter
                )

                // Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                )

                // Main content area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = course.description,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row {
                                Text("⭐ ${course.rating}", color = Color.White)
                                Spacer(Modifier.width(12.dp))
                                Text("👍 ${course.likes}", color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp)) // Space between content and icons

                        // Icon buttons ở cuối cột bên phải
                        Column(
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .fillMaxHeight()
                                .offset(y = (20).dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { // độ rộng của text
                                IconButton(onClick = onView) {
                                    Icon(Icons.Default.RemoveRedEye, contentDescription = "View", tint = Color.White)
                                }
                                IconButton(onClick = onEdit) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                                }
                                IconButton(onClick = onDelete) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                }
                            }
                        }
                    }

                    // Optional: Spacer here if you want some padding between row and bottom
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // VIP badge
                if (course.vip) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFD700), shape = CircleShape)
                                .size(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("VIP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}





@Composable
fun AddCourseDialog(
    showDialog: Boolean,
    courseToEdit: Course?,
    onDismiss: () -> Unit,
    onSaveDB: (Course) -> Unit,
    onSaveUI: () -> Unit
) {
    if (!showDialog) return
    val id = remember { mutableStateOf(courseToEdit?.id ?: "") }
    val title = remember { mutableStateOf(courseToEdit?.title ?: "") }
    val description = remember { mutableStateOf(courseToEdit?.description ?: "") }
//    val rating = remember { mutableStateOf(courseToEdit?.rating ?: 0.0) }
//    val reviews = remember { mutableStateOf(courseToEdit?.reviews ?: 0) }
//    val likes = remember { mutableStateOf(courseToEdit?.likes ?: 0) }
    val rating = remember { mutableStateOf( 0.0 ) }
    val reviews = remember { mutableStateOf( 0 ) }
    val likes = remember { mutableStateOf( 0 ) }
    val imageRes = remember { mutableStateOf(courseToEdit?.imageRes ?: "https://drive.google.com/uc?export=view&id=1uyNSW54w4stVjixb9ke_rOFhiGaeekEN") } // URL mặc định
    val isVip = remember { mutableStateOf(courseToEdit?.vip ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (courseToEdit == null) "Add New Course" else "Edit Course")
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                TextField(
                    value = title.value,
                    onValueChange = { title.value = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                TextField(
                    value = description.value,
                    onValueChange = { description.value = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

//                // Rating
//                TextField(
//                    value = rating.value.toString(),
//                    onValueChange = { rating.value = it.toDoubleOrNull() ?: 0.0 },
//                    label = { Text("Rating (0-5)") },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
//                )
//
//                // Reviews
//                TextField(
//                    value = reviews.value.toString(),
//                    onValueChange = { reviews.value = it.toIntOrNull() ?: 0 },
//                    label = { Text("Reviews") },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
//                )
//
//                // Likes
//                TextField(
//                    value = likes.value.toString(),
//                    onValueChange = { likes.value = it.toIntOrNull() ?: 0 },
//                    label = { Text("Likes") },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
//                )

                // Image resource (URL)
                TextField(
                    value = imageRes.value,
                    onValueChange = { imageRes.value = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Display Image (URL)
                val painter = rememberImagePainter(imageRes.value)
                Image(
                    painter = painter,
                    contentDescription = "Course Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 8.dp),
                    contentScale = ContentScale.Crop
                )
                // VIP status toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("VIP", modifier = Modifier.padding(end = 8.dp))
                    Checkbox(
                        checked = isVip.value,
                        onCheckedChange = { isVip.value = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val course = Course(
                        id = id.value, // giữ lại ID nếu đang chỉnh sửa
                        title = title.value,
                        description = description.value,
                        rating = rating.value,
                        reviews = reviews.value,
                        likes = likes.value,
                        imageRes = imageRes.value,
                        vip = isVip.value
                    )


                    onSaveDB(course)
                    onSaveUI()  // Save on UI after DB operation
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}







