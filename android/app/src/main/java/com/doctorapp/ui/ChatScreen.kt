package com.doctorapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doctorapp.network.MessageDto
import com.doctorapp.network.NewMessageBody
import com.doctorapp.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(requestId: Int) {
    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        try {
            messages = RetrofitClient.api.getMessages(requestId)
        } catch (e: Exception) {
            status = "خطا در دریافت پیام‌ها: ${e.message}"
        }
    }

    LaunchedEffect(requestId) { reload() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("گفتگو — درخواست #$requestId", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        if (status.isNotEmpty()) Text(status, color = MaterialTheme.colorScheme.error)

        LazyColumn(Modifier.weight(1f)) {
            items(messages) { m ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(m.body, Modifier.padding(10.dp))
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text("پیام") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (draft.isBlank()) return@Button
                scope.launch {
                    try {
                        RetrofitClient.api.sendMessage(requestId, NewMessageBody(draft))
                        draft = ""
                        reload()
                    } catch (e: Exception) {
                        status = "خطا در ارسال پیام: ${e.message}"
                    }
                }
            }) {
                Text("ارسال")
            }
        }
    }
}
