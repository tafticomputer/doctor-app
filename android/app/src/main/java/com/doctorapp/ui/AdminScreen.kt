package com.doctorapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doctorapp.network.RequestDto
import com.doctorapp.network.RetrofitClient

@Composable
fun AdminScreen() {
    var requests by remember { mutableStateOf<List<RequestDto>>(emptyList()) }
    var status by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            requests = RetrofitClient.api.getAllRequests()
        } catch (e: Exception) {
            status = "خطا در دریافت لیست: ${e.message}"
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("پنل مدیریت — همه درخواست‌ها", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        if (status.isNotEmpty()) Text(status, color = MaterialTheme.colorScheme.error)

        LazyColumn {
            items(requests) { r ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("درخواست #${r.id} — وضعیت: ${statusLabel(r.status)}")
                        Text("بیمار: ${r.patient_id}   متخصص: ${r.specialist_id}")
                    }
                }
            }
        }
    }
}
