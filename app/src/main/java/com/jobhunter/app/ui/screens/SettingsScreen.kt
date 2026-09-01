package com.jobhunter.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobhunter.app.services.AiService
import com.jobhunter.app.services.GmailService

@Composable
fun SettingsScreen(
    gmailService: GmailService,
    aiService: AiService
) {
    var showGmailDialog by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }
    var showGmailHelp by remember { mutableStateOf(false) }
    var showAiHelp by remember { mutableStateOf(false) }
    var gmailAddress by remember { mutableStateOf(gmailService.getConfig().gmailAddress) }
    var apiKey by remember { mutableStateOf(aiService.getApiKey()) }
    var apiProvider by remember { mutableStateOf(aiService.getProvider()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Text(
                text = "⚙️ Ayarlar",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // ===== GMAIL SECTION =====
        item {
            SectionHeader(title = "📧 Gmail Hesabı")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (gmailService.isConnected()) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (gmailService.isConnected()) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (gmailService.isConnected()) "Gmail Bağlı" else "Gmail Bağlı Değil",
                                    fontWeight = FontWeight.Medium
                                )
                                if (gmailService.isConnected()) {
                                    Text(
                                        text = gmailService.getConfig().gmailAddress,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (gmailService.isConnected()) {
                            TextButton(
                                onClick = { gmailService.disconnect() },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Kes")
                            }
                        } else {
                            Button(onClick = { showGmailDialog = true }) {
                                Text("Bağla")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gmail Help Info Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gmail Hakkında",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Gmail hesabınızı bağlayarak e-postaları doğrudan uygulamadan gönderebilirsiniz.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "📌 Ne işe yarar?",
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "• Bulduğunuz şirketlere otomatik e-posta gönderir\n• Ön yazı ve CV ekler\n• Gönderim geçmişini tutar",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "📌 Nasıl bağlanır?",
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "1. Gmail adresinizi girin\n2. \"Bağla\" butonuna basın\n3. Google hesabınızla giriş yapın\n4. İzinleri onaylayın",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "⚠️ Güvenli mi?",
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Evet! Sadece e-posta gönderme izni istenir. Şifreniz saklanmaz, sadece Google tarafından verilen erişim anahtarı kullanılır.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "💡 İpucu: Gmail bağlamadan da e-posta gönderebilirsiniz — cihazın kendi e-posta uygulaması açılır.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Alternative: Just use device email
                    if (!gmailService.isConnected()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Alternatif: Cihaz E-postası",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Gmail bağlamadan da e-posta göndermek mümkündür. Cihazınızda kurulu e-posta uygulaması (Gmail, Outlook vb.) açılacaktır.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ===== AI SECTION =====
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "🤖 AI Ayarları (Yapay Zeka)")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (aiService.isConfigured()) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (aiService.isConfigured()) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (aiService.isConfigured()) "AI Yapılandırıldı" else "AI Yapılandırılmadı",
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (aiService.isConfigured()) {
                                        "Sağlayıcı: ${if (aiService.getProvider() == "openai") "OpenAI" else "Anthropic"}"
                                    } else {
                                        "AI ile otomatik e-posta oluşturmak için API anahtarı gerekli"
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showAiDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (aiService.isConfigured()) "API Anahtarını Güncelle" else "API Anahtarı Gir")
                    }
                }
            }
        }

        // AI Help Info Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Nedir ve Ne İşe Yarar?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "📌 AI ile ne yapabilirsiniz?",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Otomatik ön yazı oluşturur (İngilizce/Türkçe)\n• Şirkete özel e-posta yazar\n• CV'nizi analiz eder\n• Profesyonel içerik üretir",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "📌 API Anahtarı Nedir?",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AI servislerini kullanmak için bir \"anahtar\" gerekir. Bu anahtar, hesabınızı doğrular. Ücretsiz deneme hakları vardır.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // OpenAI Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🟢 OpenAI (ChatGPT) ile Kullanım",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "1. https://platform.openai.com adresine gidin\n2. \"Sign Up\" ile hesap oluşturun (Google ile giriş yapabilirsiniz)\n3. Sol menüden \"API Keys\" tıklayın\n4. \"Create new secret key\" butonuna basın\n5. Bir isim verin ve \"Create\" diyin\n6. Oluşan anahtarı kopyalayın (sk-... ile başlar)\n7. Buraya yapıştırın",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "💰 Fiyat: İlk $5 ücretsiz deneme kredisi var. E-posta oluşturmak için aylık ~$0.50-1.00 yeterli.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "📋 Ne yazmalıyım? → \"sk-\" ile başlayan anahtarı buraya yapıştırın",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Anthropic Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🟣 Anthropic (Claude) ile Kullanım",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "1. https://console.anthropic.com adresine gidin\n2. Hesap oluşturun veya giriş yapın\n3. Sol menüden \"API Keys\" tıklayın\n4. \"Create Key\" butonuna basın\n5. Bir isim verin ve \"Create\" diyin\n6. Oluşan anahtarı kopyalayın\n7. Buraya yapıştırın",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "💰 Fiyat: Ücretsiz deneme kredisi var. Claude 3 Haiku en ucuz model (~$0.25/1M token).",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "📋 Ne yazmalıyım? → \"sk-ant-\" ile başlayan anahtarı buraya yapıştırın",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Important notes
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚠️ Önemli Notlar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "• API anahtarınız sadece bu telefonda saklanır\n• Hiçbir sunucuya gönderilmez\n• İstediğiniz zaman silebilirsiniz\n• API anahtarını paylaşmayın!\n• AI olmadan da uygulama çalışır (varsayılan şablonlar kullanılır)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick tip
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "💡 Başlangıç Önerisi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Henüz API anahtarınız yoksa bile uygulamayı kullanabilirsiniz. AI olmadan varsayılon şablonlarla e-posta oluşturur. Sonra istediğiniz zaman API anahtarı ekleyebilirsiniz!",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // ===== ABOUT SECTION =====
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "ℹ️ Hakkında")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "JobHunter v1.0.0",
                        subtitle = "İş Arama Asistanı"
                    )
                    SettingsItem(
                        icon = Icons.Default.Code,
                        title = "Açık Kaynak",
                        subtitle = "GPL v3.0 lisansı ile ücretsiz"
                    )
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Gizlilik",
                        subtitle = "Tüm veriler sadece cihazınızda saklanır"
                    )
                    SettingsItem(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Teknoloji",
                        subtitle = "Kotlin + Jetpack Compose + Material 3"
                    )
                }
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    // ===== GMAIL CONNECT DIALOG =====
    if (showGmailDialog) {
        AlertDialog(
            onDismissRequest = { showGmailDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gmail Bağla")
                }
            },
            text = {
                Column {
                    // Step-by-step guide
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📱 Adım Adım Bağlantı",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Gmail adresinizi girin\n2. \"Bağla\" butonuna basın\n3. Google hesabınızla giriş yapın\n4. \"E-posta gönderme\" iznini onaylayın\n5. Tamamlandı!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Gmail Adresiniz",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Örnek: ornek@gmail.com",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = gmailAddress,
                        onValueChange = { gmailAddress = it },
                        placeholder = { Text("ornek@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "💡 Sadece @gmail.com adresleri desteklenir. Diğer e-posta sağlayıcıları için cihaz e-posta uygulaması kullanılır.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        gmailService.saveConfig(
                            com.jobhunter.app.models.EmailConfig(
                                gmailAddress = gmailAddress,
                                isConnected = false // Would need OAuth flow
                            )
                        )
                        showGmailDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Bağla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGmailDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // ===== AI API DIALOG =====
    if (showAiDialog) {
        AlertDialog(
            onDismissRequest = { showAiDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI API Ayarları")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Provider selection
                    Text(
                        text = "📌 Adım 1: Sağlayıcı Seçin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Hangi AI servisini kullanmak istersiniz?",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = apiProvider == "openai",
                            onClick = { apiProvider = "openai" },
                            label = { Text("🟢 OpenAI") },
                            leadingIcon = if (apiProvider == "openai") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = apiProvider == "anthropic",
                            onClick = { apiProvider = "anthropic" },
                            label = { Text("🟣 Anthropic") },
                            leadingIcon = if (apiProvider == "anthropic") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }

                    HorizontalDivider()

                    // API Key input
                    Text(
                        text = "📌 Adım 2: API Anahtarınızı Girin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    if (apiProvider == "openai") {
                        Text(
                            text = "OpenAI API anahtarınızı girin. sk- ile başlar.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Nereden alınır: https://platform.openai.com/api-keys",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    } else {
                        Text(
                            text = "Anthropic API anahtarınızı girin. sk-ant- ile başlar.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Nereden alınır: https://console.anthropic.com/settings/keys",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Anahtarı") },
                        placeholder = {
                            Text(
                                if (apiProvider == "openai") "sk-..." else "sk-ant-..."
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    HorizontalDivider()

                    // Test info
                    Text(
                        text = "📌 Adım 3: Kaydedin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "\"Kaydet\" butonuna basın. Anahtarınız sadece bu telefonada saklanır.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Security note
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Anahtarınız şifrelenerek sadece bu cihazda saklanır. Hiçbir yere gönderilmez.",
                                fontSize = 11.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        aiService.setApiKey(apiKey, apiProvider)
                        showAiDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
