package com.doctorapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doctorapp.network.LoginRequest
import com.doctorapp.network.RetrofitClient
import com.doctorapp.network.TokenStore
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoggedIn: (role: String) -> Unit) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("ورود به ویزیت آنلاین", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("شماره موبایل") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("رمز عبور") })
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            scope.launch {
                try {
                    val res = RetrofitClient.api.login(LoginRequest(phone, password))
                    TokenStore.accessToken = res.access_token
                    onLoggedIn(res.user.role)
                } catch (e: Exception) {
                    status = "خطا در ورود: ${e.message}"
                }
            }
        }) {
            Text("ورود")
        }
        Spacer(Modifier.height(12.dp))
        Text(status)
    }
}
