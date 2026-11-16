package com.jobhunter.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobhunter.app.services.CompanyResearchService
import com.jobhunter.app.services.NvidiaNimService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyResearchScreen(
    initialCompanyName: String = "",
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val researchService = remember { CompanyResearchService(context) }
    val nvidiaService = remember { NvidiaNimService(context) }
    
    var companyName by remember { mutableStateOf(initialCompanyName) }
    var companyInfo by remember { mutableStateOf<CompanyResearchService.CompanyInfo?>(null) }
    var aiSummary by remember { mutableStateOf("") }
    var isResearching by remember { mutableStateOf(false) }
    var isAiLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sirket Arastirmasi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0E21))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0E21))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Arama kutusu
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Sirket Adi", color = Color(0xFF00D4FF)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00D4FF), unfocusedBorderColor = Color(0xFF3A3F52), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (companyName.isNotBlank()) {
                                        scope.launch {
                                            isResearching = true
                                            companyInfo = researchService.researchCompany(companyName)
                                            isResearching = false
                                        }
                                    }
                                },
                                enabled = companyName.isNotBlank() && !isResearching,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ara")
                            }
                            Button(
                                onClick = {
                                    if (companyName.isNotBlank()) {
                                        scope.launch {
                                            isAiLoading = true
                                            aiSummary = nvidiaService.researchCompany(companyName)
                                            isAiLoading = false
                                        }
                                    }
                                },
                                enabled = companyName.isNotBlank() && !isAiLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFAA00)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Ozet", color = Color.Black)
                            }
                        }
                    }
                }
            }

            // Yukleniyor
            if (isResearching) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF00D4FF), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Sirket arastiriliyor...", color = Color(0xFF00D4FF))
                        }
                    }
                }
            }

            // Sonuclar
            companyInfo?.let { info ->
                // Sirket basligi
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF00D4FF).copy(alpha = 0.1f)), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🏢", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(info.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                if (info.description.isNotEmpty()) {
                                    Text(info.description.take(200), color = Color.Gray, fontSize = 12.sp, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }

                // Web siteleri
                if (info.webSites.isNotEmpty()) {
                    item { Text("🌐 Web Siteleri (${info.webSites.size})", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold) }
                    items(info.webSites.take(5)) { url ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {}
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF00D4FF), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(url.take(60), color = Color(0xFF00D4FF), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Email adresleri
                if (info.emails.isNotEmpty()) {
                    item { Text("📧 Email Adresleri (${info.emails.size})", color = Color(0xFF00FF88), fontWeight = FontWeight.Bold) }
                    items(info.emails.take(5)) { email ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$email")
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF00FF88), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(email, color = Color(0xFF00FF88), fontSize = 12.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Send, contentDescription = "Email Gonder", tint = Color(0xFF00FF88), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // LinkedIn
                if (info.linkedinUrl.isNotEmpty()) {
                    item { Text("💼 LinkedIn", color = Color(0xFF0077B5), fontWeight = FontWeight.Bold) }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.linkedinUrl))) } catch (_: Exception) {}
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0077B5).copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF0077B5), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(info.linkedinUrl.take(60), color = Color(0xFF0077B5), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // AI Ozet
            if (aiSummary.isNotEmpty()) {
                item { Text("🤖 AI Sirket Ozeti", color = Color(0xFFFFAA00), fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)), shape = RoundedCornerShape(12.dp)) {
                        Text(aiSummary, modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }
            }

            if (isAiLoading) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFFFAA00), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("AI sirketi arastiriyor...", color = Color(0xFFFFAA00))
                        }
                    }
                }
            }
        }
    }
}
