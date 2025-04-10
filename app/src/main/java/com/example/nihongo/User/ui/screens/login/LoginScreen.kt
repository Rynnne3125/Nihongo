package com.example.nihongo.User.ui.screens.login

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.utils.EmailSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(navController: NavController, userRepo: UserRepository) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isSendingOtp by remember { mutableStateOf(false) }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                message = ""
            },
            label = { Text("Email") },
            isError = email.isNotBlank() && !isValidEmail(email),
            singleLine = true
        )
        if (email.isNotBlank() && !isValidEmail(email)) {
            Text("Email không hợp lệ", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                message = ""
            },
            label = { Text("Password") },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                    )
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    message = ""
                    if (!isValidEmail(email)) {
                        message = "Vui lòng nhập email hợp lệ."
                        return@launch
                    }
                    if (password.length < 6) {
                        message = "Mật khẩu phải có ít nhất 6 ký tự."
                        return@launch
                    }

                    isSendingOtp = true
                    val user = userRepo.loginUserByEmail(email, password)
                    if (user != null) {
                        val otp = EmailSender.generateOTP()
                        withContext(Dispatchers.IO) {
                            EmailSender.sendOTP(
                                recipientEmail = email,
                                otp = otp,
                                onSuccess = {
                                    scope.launch {
                                        navController.currentBackStackEntry?.savedStateHandle?.set("expectedOtp", otp)
                                        navController.navigate("otp_screen")
                                        isSendingOtp = false
                                    }
                                },
                                onFailure = {
                                    it.printStackTrace()
                                    Log.e("EmailSender", "Lỗi gửi OTP: ${it.message}", it)
                                    scope.launch {
                                        message = "Không thể gửi OTP: ${it.localizedMessage ?: "Lỗi không xác định"}"
                                        isSendingOtp = false
                                    }
                                }
                            )
                        }
                    } else {
                        message = "Sai email hoặc mật khẩu!"
                        isSendingOtp = false
                    }
                }
            },
            enabled = !isSendingOtp
        ) {
            Text(if (isSendingOtp) "Đang gửi OTP..." else "Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("Chưa có tài khoản? Đăng ký")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (message.isNotEmpty()) {
            Text(message, color = MaterialTheme.colorScheme.error)
        }
    }
}
