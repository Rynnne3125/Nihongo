package com.example.nihongo.User.data.repository

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nihongo.User.data.models.Course
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).courseDao()
    private val repository = CourseRepository(dao)

    val courses: LiveData<List<Course>> = repository.allCourses.asLiveData()

    fun addSampleCourses() {
        viewModelScope.launch {
            repository.insertSampleData()
        }
    }
}
