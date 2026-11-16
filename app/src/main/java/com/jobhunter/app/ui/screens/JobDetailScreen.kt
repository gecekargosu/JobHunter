package com.jobhunter.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.launch

/**
 * Is Detay Ekrani
 * Secilen ilanin tum detaylarini gosterir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobTitle: String,
    company: String,
    location: String = "",
    description: String = "",
    url: String = "",
    source: String = "",
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val researchService = remember { CompanyResearchService(context) }

    var companyInfo by remember { mutableStateOf<CompanyResearchService.CompanyInfo?>(null) }
    var isResearching by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Is Detayi", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    // Tarayicida ac
                    IconButton(onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) { }
                    }) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "Tarayicida Ac")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0E21))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0E21))
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Is basligi
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(jobTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF00D4FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(company, color = Color(0xFF00D4FF), fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(location.ifEmpty { "Belirtilmemis" }, color = Color.Gray, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(source, color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Is aciklamasi
            Text("Is Aciklamasi", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = description.ifEmpty { "Is aciklamasi mevcut degil. Detaylar icin web sitesini ziyaret edin." },
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sirket arastirmasi
            Text("Sirket Arastirmasi", color = Color(0xFFFFAA00), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        isResearching = true
                        companyInfo = researchService.researchCompany(company)
                        isResearching = false
                    }
                },
                enabled = company.isNotBlank() && !isResearching,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFAA00)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isResearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                } else {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sirketi Arastir: $company", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            // Sirket sonuclari
            companyInfo?.let { info ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Web siteleri
                        if (info.webSites.isNotEmpty()) {
                            Text("🌐 Web Siteleri", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            info.webSites.take(3).forEach { site ->
                                Text(
                                    text = site,
                                    color = Color(0xFF00D4FF),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                        .background(Color(0xFF00D4FF).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Email adresleri
                        if (info.emails.isNotEmpty()) {
                            Text("📧 Email Adresleri", color = Color(0xFF00FF88), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            info.emails.take(3).forEach { email ->
                                Row(
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(email, color = Color(0xFF00FF88), fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:$email")
                                        }
                                        try { context.startActivity(intent) } catch (e: Exception) { }
                                    }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Email, contentDescription = "Email Gonder", tint = Color(0xFF00FF88), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // LinkedIn
                        if (info.linkedinUrl.isNotEmpty()) {
                            Text("💼 LinkedIn", color = Color(0xFF0077B5), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = info.linkedinUrl,
                                color = Color(0xFF0077B5),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    .background(Color(0xFF0077B5).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Aciklama
                        if (info.description.isNotEmpty()) {
                            Text("📋 Hakkinda", color = Color(0xFFFFAA00), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(info.description, color = Color.White, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
