package com.example.nihongo.User.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nihongo.User.data.repository.UserRepository
import com.example.nihongo.User.utils.NavigationRoutes
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    userRepo: UserRepository,
    email: String // Nhận email từ OTP Screen
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9))
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
            Text("Đặt lại mật khẩu", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF2E7D32))

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Mật khẩu mới") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Nhập lại mật khẩu") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (newPassword.length < 6) {
                        message = "Mật khẩu phải từ 6 ký tự trở lên"
                    } else if (newPassword != confirmPassword) {
                        message = "Mật khẩu không khớp"
                    } else {
                        scope.launch {
                            // Cần thêm hàm updatePassword vào UserRepository
                            val success = userRepo.updatePassword(email, newPassword)
                            if (success) {
                                message = "Đổi mật khẩu thành công!"
                                kotlinx.coroutines.delay(1000)
                                navController.navigate(NavigationRoutes.LOGIN) {
                                    popUpTo(0)
                                }
                            } else {
                                message = "Lỗi cập nhật mật khẩu. Vui lòng thử lại."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) {
                Text("Cập nhật mật khẩu")
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, color = if(message.contains("thành công")) Color.Green else Color.Red)
            }
        }
    }
}