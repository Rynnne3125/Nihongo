package com.example.nihongo.User.ui.screens.homepage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nihongo.User.utils.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Welcome to Nihongo App") })
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    navController.navigate(Screen.Courses.route)
                }) {
                    Text("Browse Courses")
                }
            }
            item {
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    // TODO: Navigate to VIP subscription
                }) {
                    Text("Subscribe VIP")
                }
            }
        }
    }
}
