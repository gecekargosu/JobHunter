package com.jobhunter.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobhunter.app.services.AiService
import com.jobhunter.app.services.GmailService
import com.jobhunter.app.services.GoogleGeminiService
import com.jobhunter.app.services.SmtpEmailService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    gmailService: GmailService,
    aiService: AiService,
    smtpService: SmtpEmailService? = null,
    onNavigateToSmtpSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val geminiService = remember { GoogleGeminiService(context) }

    var showApiDialog by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf("gemini") }
    var apiKeyInput by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Text("⚙️ Ayarlar", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        // ===== SMTP BOLUMU =====
        item { SectionHeader("📧 E-posta Gönderimi") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (smtpService?.isConfigured() == true) Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToSmtpSettings() }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (smtpService?.isConfigured() == true) Icons.Default.CheckCircle else Icons.Default.Mail,
                        contentDescription = null,
                        tint = if (smtpService?.isConfigured() == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (smtpService?.isConfigured() == true) "SMTP E-posta Aktif" else "SMTP E-posta Ayarla", fontWeight = FontWeight.Medium)
                        Text(
                            if (smtpService?.isConfigured() == true) "E-postalar doğrudan gönderiliyor" else "Gmail uygulama şifresi ile e-posta gönder",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }

        // ===== AI AYARLARI =====
        item { Spacer(modifier = Modifier.height(8.dp)); SectionHeader("🤖 AI Ayarları (Yapay Zeka)") }

        // AI Durum karti
        item {
            val geminiReady = geminiService.isConfigured()
            val aiReady = aiService.isConfigured()
            val anyReady = geminiReady || aiReady

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (anyReady) Color(0xFF00FF88).copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (anyReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (anyReady) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    when {
                                        geminiReady && aiReady -> "Gemini + ${aiService.getProvider().uppercase()} Bagli"
                                        geminiReady -> "Gemini API Bagli ✅"
                                        aiReady -> "${aiService.getProvider().uppercase()} Bagli ✅"
                                        else -> "AI Yapilandirilmadi"
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    if (anyReady) "AI ile on yazi, eslestirme, sirket arastirmasi" else "API anahtari gerekli",
                                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            selectedProvider = if (geminiReady) "gemini" else aiService.getProvider()
                            apiKeyInput = if (geminiReady) geminiService.getApiKey() else aiService.getApiKey()
                            showApiDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (anyReady) "API Anahtarini Guncelle" else "API Anahtari Gir")
                    }
                }
            }
        }

        // Ucretsiz API rehberi
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🆓 Ücretsiz API Rehberi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Google Gemini
                    ApiProviderCard(
                        name = "🟢 Google Gemini (ÖNERİLEN)",
                        free = "1500 istek/gün ücretsiz",
                        url = "aistudio.google.com/apikey",
                        steps = "1. aistudio.google.com/apikey adresine gidin\n2. Google hesabınızla giriş yapın\n3. \"Create API Key\" butonuna basın\n4. Oluşan anahtarı kopyalayın\n5. Buraya yapıştırın",
                        color = Color(0xFF4285F4)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // NVIDIA NIM
                    ApiProviderCard(
                        name = "🟡 NVIDIA NIM",
                        free = "81+ model ücretsiz (izin gerekli)",
                        url = "build.nvidia.com",
                        steps = "1. build.nvidia.com adresine gidin\n2. \"Sign Up\" ile hesap oluşturun\n3. \"Get API Key\" butonuna basın\n4. nvapi-... ile başlayan anahtarı kopyalayın\n5. Not: \"Public API Endpoints\" izni gerekebilir",
                        color = Color(0xFF76B900)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // OpenAI
                    ApiProviderCard(
                        name = "🔵 OpenAI (ChatGPT)",
                        free = null,
                        url = "platform.openai.com/api-keys",
                        steps = "1. platform.openai.com adresine gidin\n2. Hesap oluşturun veya giriş yapın\n3. \"API Keys\" menüsünden \"Create\" deyin\n4. sk-... ile başlayan anahtarı kopyalayın",
                        color = Color(0xFF10A37F)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Anthropic
                    ApiProviderCard(
                        name = "🟣 Anthropic (Claude)",
                        free = null,
                        url = "console.anthropic.com/settings/keys",
                        steps = "1. console.anthropic.com adresine gidin\n2. Hesap oluşturun veya giriş yapın\n3. \"API Keys\" menüsünden \"Create Key\" deyin\n4. sk-ant-... ile başlayan anahtarı kopyalayın",
                        color = Color(0xFFD97757)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Guvenlik notu
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Anahtarlar sadece bu telefonda saklanır. Hiçbir yere gönderilmez.", fontSize = 11.sp, color = Color(0xFF4CAF50))
                        }
                    }
                }
            }
        }

        // ===== HAKKINDA =====
        item { Spacer(modifier = Modifier.height(8.dp)); SectionHeader("ℹ️ Hakkında") }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsItem(icon = Icons.Default.Info, title = "JobHunter v7.0.0", subtitle = "İş Arama Asistanı")
                    SettingsItem(icon = Icons.Default.Security, title = "Gizlilik", subtitle = "Tüm veriler sadece cihazınızda saklanır")
                    SettingsItem(icon = Icons.Default.PhoneAndroid, title = "Teknoloji", subtitle = "Kotlin + Jetpack Compose + Material 3")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    // ===== API KEY DIALOG =====
    if (showApiDialog) {
        AlertDialog(
            onDismissRequest = { showApiDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("API Ayarları")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Sağlayıcı Seçin", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    // 4 saglayici butonu - 2x2 grid
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedProvider == "gemini",
                            onClick = { selectedProvider = "gemini"; apiKeyInput = geminiService.getApiKey() },
                            label = { Text("🟢 Gemini", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedProvider == "nvidia",
                            onClick = { selectedProvider = "nvidia"; apiKeyInput = aiService.getApiKey() },
                            label = { Text("🟡 NVIDIA", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedProvider == "openai",
                            onClick = { selectedProvider = "openai"; apiKeyInput = aiService.getApiKey() },
                            label = { Text("🔵 OpenAI", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedProvider == "anthropic",
                            onClick = { selectedProvider = "anthropic"; apiKeyInput = aiService.getApiKey() },
                            label = { Text("🟣 Anthropic", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Divider()

                    Text("API Anahtarınızı Girin", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    val hint = when (selectedProvider) {
                        "gemini" -> "aistudio.google.com'dan alınan anahtar"
                        "nvidia" -> "build.nvidia.com'dan alınan nvapi-... anahtarı"
                        "openai" -> "platform.openai.com'dan alınan sk-... anahtarı"
                        else -> "console.anthropic.com'dan alınan sk-ant-... anahtarı"
                    }
                    Text(hint, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("API Anahtarı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Anahtarınız şifrelenerek sadece bu cihazda saklanır.", fontSize = 11.sp, color = Color(0xFF4CAF50))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (selectedProvider) {
                            "gemini" -> geminiService.setApiKey(apiKeyInput)
                            else -> aiService.setApiKey(apiKeyInput, selectedProvider)
                        }
                        showApiDialog = false
                        saved = true
                    },
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showApiDialog = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
fun ApiProviderCard(name: String, free: String?, url: String, steps: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
            if (free != null) {
                Text("💰 $free", fontSize = 12.sp, color = color.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(steps, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
