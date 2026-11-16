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
import com.jobhunter.app.services.SmtpEmailService
import kotlinx.coroutines.launch

/**
 * On Yazi Duzeltme Ekrani
 * AI'in yazdigi on yaziyi duzeltme, gonderme
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverLetterEditScreen(
    coverLetter: String,
    jobTitle: String,
    company: String,
    recipientEmail: String = "",
    senderName: String = "",
    senderEmail: String = "",
    onSend: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val smtpService = remember { SmtpEmailService(context) }

    var editedLetter by remember { mutableStateOf(coverLetter) }
    var isSending by remember { mutableStateOf(false) }
    var showSendDialog by remember { mutableStateOf(false) }
    var sendStatus by remember { mutableStateOf<String?>(null) }
    var recipientEmailState by remember { mutableStateOf(recipientEmail) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📝 On Yazi Duzenle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    // Kaydet butonu
                    IconButton(onClick = {
                        showSendDialog = true
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "Gonder")
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
            // Is bilgileri
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "🏢 $company",
                        color = Color(0xFF00D4FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "💼 $jobTitle",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Alici email
            OutlinedTextField(
                value = recipientEmailState,
                onValueChange = { recipientEmailState = it },
                label = { Text("Alici Email", color = Color(0xFF00D4FF)) },
                placeholder = { Text("ornek@sirket.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF00D4FF)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00D4FF),
                    unfocusedBorderColor = Color(0xFF3A3F52),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // On yazi metni - duzenlenebilir
            Text(
                "📝 On Yazi Metni (duzenleyebilirsiniz):",
                color = Color(0xFF00D4FF),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedLetter,
                onValueChange = { editedLetter = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00D4FF),
                    unfocusedBorderColor = Color(0xFF3A3F52),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF00D4FF)
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gonder butonu
            Button(
                onClick = { showSendDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSending && editedLetter.isNotBlank() && recipientEmailState.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gonderiliyor...")
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Email Gonder")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Durum mesaji
            sendStatus?.let { status ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (status.contains("Basarili")) Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else Color(0xFFFF5252).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (status.contains("Basarili")) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (status.contains("Basarili")) Color(0xFF4CAF50) else Color(0xFFFF5252)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(status, color = Color.White, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Gonderme onay dialogu
    if (showSendDialog) {
        AlertDialog(
            onDismissRequest = { showSendDialog = false },
            title = { Text("Email Gonder") },
            text = {
                Column {
                    Text("Bu on yaziyi gondermek istediginize emin misiniz?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Alici: $recipientEmailState", fontSize = 13.sp, color = Color.Gray)
                    Text("Sirket: $company", fontSize = 13.sp, color = Color.Gray)
                    Text("Pozisyon: $jobTitle", fontSize = 13.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSendDialog = false
                        isSending = true
                        scope.launch {
                            try {
                                val result = smtpService.sendEmail(
                                    to = recipientEmailState,
                                    subject = "$senderName - $jobTitle Basvurusu - $company",
                                    body = editedLetter
                                )
                                if (result.isSuccess) {
                                    sendStatus = "✅ Basarili! Email $recipientEmailState adresine gonderildi."
                                    onSend(editedLetter)
                                } else {
                                    sendStatus = "❌ Hata: ${result.exceptionOrNull()?.message}"
                                }
                            } catch (e: Exception) {
                                sendStatus = "❌ Hata: ${e.message}"
                            } finally {
                                isSending = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF))
                ) {
                    Text("Gonder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendDialog = false }) {
                    Text("Iptal")
                }
            }
        )
    }
}
