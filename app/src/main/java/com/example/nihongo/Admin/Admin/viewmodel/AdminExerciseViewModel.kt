package com.example.nihongo.Admin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.ExerciseType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminExerciseViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // State holders
    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Current exercise being edited
    private val _currentExercise = MutableStateFlow<Exercise?>(null)
    val currentExercise: StateFlow<Exercise?> = _currentExercise.asStateFlow()

    // Load exercises for a specific sublesson
    fun loadExercises(lessonId: String, subLessonId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val exercisesCollection = firestore.collection("lessons")
                    .document(lessonId)
                    .collection("exercises")
                    .whereEqualTo("subLessonId", subLessonId)
                    .get()
                    .await()

                val exercisesList = exercisesCollection.documents.mapNotNull { doc ->
                    doc.toObject(Exercise::class.java)?.apply {
                        id = doc.id // Set the document ID
                    }
                }

                _exercises.value = exercisesList
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load exercises: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Set current exercise for editing
    fun setCurrentExercise(exercise: Exercise?) {
        _currentExercise.value = exercise
    }

    // Create new exercise
    fun createExercise(
        lessonId: String,
        exercise: Exercise,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1. Log kiểm tra dữ liệu đầu vào
                Log.d("FIREBASE_DEBUG", "--- Bắt đầu tạo Exercise ---")
                Log.d("FIREBASE_DEBUG", "LessonID: $lessonId")
                Log.d("FIREBASE_DEBUG", "VideoURL cần lưu: ${exercise.videoUrl}")
                // Nếu Log này hiện null -> Lỗi ở code Generator (AiCourseGenerate)
                // Nếu Log này hiện có link -> Lỗi do Firestore hoặc Data Class

                val exercisesCollection = firestore.collection("lessons")
                    .document(lessonId)
                    .collection("exercises")

                val docRef = exercisesCollection.document()

                // Add the ID to the exercise object
                val exerciseWithId = exercise.copy(id = docRef.id)

                // 2. Set the document data (Firestore tự map object)
                docRef.set(exerciseWithId).await()

                Log.d("FIREBASE_DEBUG", "✅ Đã lưu thành công Exercise ID: ${docRef.id}")

                // Reload exercises
                loadExercises(lessonId, exercise.subLessonId ?: "")
                onSuccess()
            } catch (e: Exception) {
                Log.e("FIREBASE_DEBUG", "❌ Lỗi khi lưu: ${e.message}")
                _errorMessage.value = "Failed to create exercise: ${e.message}"
                onError(_errorMessage.value ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update existing exercise
    fun updateExercise(
        lessonId: String,
        exercise: Exercise,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val exerciseId = exercise.id ?: throw IllegalArgumentException("Exercise ID cannot be null")

                val exerciseRef = firestore.collection("lessons")
                    .document(lessonId)
                    .collection("exercises")
                    .document(exerciseId)

                exerciseRef.set(exercise).await()

                // Reload exercises
                loadExercises(lessonId, exercise.subLessonId ?: "")
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update exercise: ${e.message}"
                onError(_errorMessage.value ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete exercise
    fun deleteExercise(
        lessonId: String,
        exerciseId: String,
        subLessonId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val exerciseRef = firestore.collection("lessons")
                    .document(lessonId)
                    .collection("exercises")
                    .document(exerciseId)

                exerciseRef.delete().await()

                // Reload exercises
                loadExercises(lessonId, subLessonId)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete exercise: ${e.message}"
                onError(_errorMessage.value ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Create a new empty exercise with defaults
    fun createEmptyExercise(subLessonId: String): Exercise {
        return Exercise(
            subLessonId = subLessonId,
            question = "",
            answer = "",
            type = ExerciseType.PRACTICE,
            options = emptyList(),
            passed = false
        )
    }

    // Reset error message
    fun clearError() {
        _errorMessage.value = null
    }
}

