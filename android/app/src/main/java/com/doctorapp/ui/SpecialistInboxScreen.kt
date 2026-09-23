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
import com.doctorapp.network.StatusUpdateBody
import kotlinx.coroutines.launch

@Composable
fun SpecialistInboxScreen(onOpenChat: (Int) -> Unit) {
    var requests by remember { mutableStateOf<List<RequestDto>>(emptyList()) }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        try {
            requests = RetrofitClient.api.getInbox()
        } catch (e: Exception) {
            status = "خطا در دریافت کارتابل: ${e.message}"
        }
    }

    LaunchedEffect(Unit) { reload() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("کارتابل درخواست‌ها", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        if (status.isNotEmpty()) Text(status, color = MaterialTheme.colorScheme.error)

        LazyColumn {
            items(requests) { r ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("درخواست #${r.id} — وضعیت: ${statusLabel(r.status)}")
                        if (!r.note.isNullOrBlank()) Text("یادداشت: ${r.note}")
                        Spacer(Modifier.height(6.dp))
                        Row {
                            if (r.status == "pending") {
                                Button(onClick = {
                                    scope.launch {
                                        RetrofitClient.api.updateStatus(r.id, StatusUpdateBody("accepted"))
                                        reload()
                                    }
                                }) { Text("پذیرش") }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(onClick = {
                                    scope.launch {
                                        RetrofitClient.api.updateStatus(r.id, StatusUpdateBody("rejected"))
                                        reload()
                                    }
                                }) { Text("رد") }
                            }
                            if (r.status == "accepted") {
                                Button(onClick = { onOpenChat(r.id) }) { Text("گفتگو") }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(onClick = {
                                    scope.launch {
                                        RetrofitClient.api.updateStatus(r.id, StatusUpdateBody("completed"))
                                        reload()
                                    }
                                }) { Text("پایان ویزیت") }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun statusLabel(status: String): String = when (status) {
    "pending" -> "در انتظار"
    "accepted" -> "پذیرفته‌شده"
    "rejected" -> "رد شده"
    "completed" -> "پایان یافته"
    else -> status
}
