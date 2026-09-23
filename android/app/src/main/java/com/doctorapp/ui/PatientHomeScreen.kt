package com.doctorapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.doctorapp.network.NewRequestBody
import com.doctorapp.network.RetrofitClient
import com.doctorapp.network.SpecialistDto
import kotlinx.coroutines.launch

@Composable
fun PatientHomeScreen() {
    var specialists by remember { mutableStateOf<List<SpecialistDto>>(emptyList()) }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            specialists = RetrofitClient.api.getSpecialists()
        } catch (e: Exception) {
            status = "خطا در دریافت لیست متخصصین: ${e.message}"
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("انتخاب پزشک یا متخصص تغذیه", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        if (status.isNotEmpty()) Text(status, color = MaterialTheme.colorScheme.error)

        LazyColumn {
            items(specialists) { sp ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(sp.full_name, style = MaterialTheme.typography.titleMedium)
                        Text(sp.specialty ?: "", style = MaterialTheme.typography.bodySmall)
                        if (!sp.bio.isNullOrBlank()) Text(sp.bio, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(6.dp))
                        Button(onClick = {
                            scope.launch {
                                try {
                                    RetrofitClient.api.createRequest(NewRequestBody(sp.id, null))
                                    status = "درخواست برای ${sp.full_name} ارسال شد"
                                } catch (e: Exception) {
                                    status = "خطا در ارسال درخواست: ${e.message}"
                                }
                            }
                        }) {
                            Text("درخواست ویزیت")
                        }
                    }
                }
            }
        }
    }
}
