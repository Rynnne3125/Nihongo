package com.example.nihongo.Admin

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nihongo.Admin.viewmodel.AdminUserViewModel
import com.onesignal.OneSignal

@Composable
fun AdminLoginScreen(navController: NavController) {
    // State cho các trường nhập liệu
    var email by remember { mutableStateOf("") } // Đổi tên biến thành email cho rõ nghĩa với ViewModel
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Sử dụng viewModel() để đảm bảo lifecycle tốt hơn là remember { AdminUserViewModel() }
    // Nếu chưa có dependency, bạn có thể giữ nguyên remember { AdminUserViewModel() }
    val viewModel: AdminUserViewModel = viewModel()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Admin Login",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF2E7D32)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                message = "" // Xóa thông báo lỗi khi người dùng gõ lại
            },
            label = { Text("Email Admin") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Input Password
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                message = ""
            },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Nút Đăng nhập
        Button(
            onClick = {
                // 1. Validate cơ bản: Không được để trống
                if (email.isBlank() || password.isBlank()) {
                    message = "Vui lòng nhập đầy đủ Email và Mật khẩu!"
                    return@Button
                }

                // 2. Bắt đầu loading
                isLoading = true
                message = ""

                // 3. Gọi ViewModel để kiểm tra (Firebase)
                viewModel.checkLogin(email, password) { success ->
                    isLoading = false // Tắt loading khi có kết quả

                    if (success) {
                        // --- ĐĂNG NHẬP THÀNH CÔNG ---
                        Log.d("AdminLogin", "Login Success: $email")

                        // Lưu session vào SharedPreferences
                        val sharedPref = context.getSharedPreferences("admin_session", Context.MODE_PRIVATE)
                        sharedPref.edit().putBoolean("isLoggedIn", true).apply()

                        // Thiết lập tag cho OneSignal (để gửi noti riêng cho admin)
                        OneSignal.User.addTag("admin", "true")

                        // Chuyển màn hình
                        navController.navigate("MainPage") {
                            popUpTo("admin_login") { inclusive = true }
                        }
                    } else {
                        // --- ĐĂNG NHẬP THẤT BẠI ---
                        Log.w("AdminLogin", "Login Failed")
                        message = "Sai email, mật khẩu hoặc tài khoản không phải Admin!"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
            enabled = !isLoading // Disable nút khi đang loading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Đăng nhập")
            }
        }

        // Hiển thị thông báo lỗi nếu có
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, color = Color.Red)
        }
    }
}