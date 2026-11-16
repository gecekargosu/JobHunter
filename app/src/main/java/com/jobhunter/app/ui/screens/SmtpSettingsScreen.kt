package com.jobhunter.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobhunter.app.services.SmtpEmailService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmtpSettingsScreen(
    smtpService: SmtpEmailService
) {
    val scope = rememberCoroutineScope()

    var host by remember { mutableStateOf(smtpService.getConfig().host) }
    var port by remember { mutableStateOf(smtpService.getConfig().port) }
    var username by remember { mutableStateOf(smtpService.getConfig().username) }
    var password by remember { mutableStateOf(smtpService.getConfig().password) }
    var fromName by remember { mutableStateOf(smtpService.getConfig().fromName) }
    var useTls by remember { mutableStateOf(smtpService.getConfig().useTls) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📧 E-posta Ayarları") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bilgi kartı
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2196F3))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gmail SMTP Kurulumu", fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "E-posta göndermek için Gmail hesabında \"Uygulama Şifresi\" oluştur:\n" +
                        "1. https://myaccount.google.com/apppasswords adresine git\n" +
                        "2. \"Uygulama adı\" kısmına \"JobHunter\" yaz\n" +
                        "3. \"Oluştur\" butonuna bas\n" +
                        "4. Çıkan 16 haneli şifreyi yukarıdaki \"Şifre\" alanına gir",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            // Host
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("SMTP Sunucu") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null) },
                placeholder = { Text("smtp.gmail.com") }
            )

            // Port
            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text("Port") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                placeholder = { Text("587") }
            )

            // Username (Email)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("E-posta Adresin") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                placeholder = { Text("ornek@gmail.com") }
            )

            // Password (Uygulama Şifresi)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Uygulama Şifresi (16 haneli)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                placeholder = { Text("xxxx xxxx xxxx xxxx") }
            )

            // From Name
            OutlinedTextField(
                value = fromName,
                onValueChange = { fromName = it },
                label = { Text("Gönderen Adı") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                placeholder = { Text("Ad Soyad") }
            )

            // TLS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = useTls,
                    onCheckedChange = { useTls = it }
                )
                Text("TLS kullan (önerilen)")
            }

            // Test sonucu
            if (testResult != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (testResult!!.startsWith("✅")) Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else Color(0xFFFF5252).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = testResult!!,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
            }

            // Butonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Test Et
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isTesting = true
                            testResult = null
                            // Kaydet ve test et
                            smtpService.saveConfig(SmtpEmailService.SmtpConfig(
                                host = host, port = port, username = username,
                                password = password, fromName = fromName, useTls = useTls
                            ))
                            val result = smtpService.testConnection()
                            testResult = if (result.isSuccess) "✅ ${result.getOrNull()}"
                            else "❌ ${result.exceptionOrNull()?.message}"
                            isTesting = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isTesting && username.isNotBlank() && password.isNotBlank()
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.WifiFind, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Et")
                }

                // Kaydet
                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            smtpService.saveConfig(SmtpEmailService.SmtpConfig(
                                host = host, port = port, username = username,
                                password = password, fromName = fromName, useTls = useTls
                            ))
                            isSaving = false
                            testResult = "✅ Ayarlar kaydedildi!"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving && username.isNotBlank() && password.isNotBlank()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kaydet")
                }
            }

            // Durum
            if (smtpService.isConfigured()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("E-posta yapılandırıldı ✅", color = Color(0xFF4CAF50))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
