package com.example.nihongo.User.ui.screens.login

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            .background(Color(0xFFE8F5E9)) // Xanh nhạt
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Đăng nhập",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF2E7D32) // Xanh lá đậm
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    message = ""
                },
                label = { Text("Email") },
                isError = email.isNotBlank() && !isValidEmail(email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (email.isNotBlank() && !isValidEmail(email)) {
                Text(
                    "Email không hợp lệ",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    message = ""
                },
                label = { Text("Mật khẩu") },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSendingOtp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)) // Màu xanh lá đậm hơn nền
            ) {
                Text(if (isSendingOtp) "Đang gửi OTP..." else "Đăng nhập")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { navController.navigate("register") }) {
                Text("Chưa có tài khoản? Đăng ký", color = Color(0xFF2E7D32))
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { navController.navigate("admin_login") }) {
                Text(
                    "Đăng nhập với tư cách Admin",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
