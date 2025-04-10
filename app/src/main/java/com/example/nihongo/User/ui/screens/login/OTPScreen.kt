package com.example.nihongo.User.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun OTPScreen(navController: NavController, expectedOtp: String) {
    var enteredOtp by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nhập mã OTP", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = enteredOtp,
            onValueChange = { enteredOtp = it },
            label = { Text("OTP") }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (enteredOtp == expectedOtp) {
                navController.navigate("home")
            } else {
                errorMessage = "Mã OTP không đúng"
            }
        }) {
            Text("Xác nhận")
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}
