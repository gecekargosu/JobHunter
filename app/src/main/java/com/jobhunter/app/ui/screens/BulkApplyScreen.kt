package com.jobhunter.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobhunter.app.services.NvidiaNimService
import com.jobhunter.app.services.SmtpEmailService
import com.jobhunter.app.ui.screens.dataStore
import com.jobhunter.app.ui.screens.ProfileKeys
import kotlinx.coroutines.launch

data class SelectedJob(
    val title: String,
    val company: String,
    val platform: String,
    val url: String,
    val description: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkApplyScreen(
    selectedJobs: List<SelectedJob>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nvidiaService = remember { NvidiaNimService(context) }
    val dataStore = context.dataStore

    val smtpService = remember { SmtpEmailService(context) }
    
    var isProcessing by remember { mutableStateOf(false) }
    var currentJobIndex by remember { mutableIntStateOf(0) }
    var coverLetters by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var completedJobs by remember { mutableIntStateOf(0) }
    var statusMessage by remember { mutableStateOf("") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var isSending by remember { mutableStateOf(false) }
    var sendResults by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }

    // Kullanıcı profilini yükle
    var userName by remember { mutableStateOf("") }
    var userSkills by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        dataStore.data.collect { prefs ->
            userName = prefs[ProfileKeys.FULL_NAME] ?: ""
            userSkills = prefs[ProfileKeys.SKILLS] ?: ""
            userEmail = prefs[ProfileKeys.EMAIL] ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 Toplu Başvuru (${selectedJobs.size} ilan)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
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
        ) {
            // İlerleme çubuğu
            if (isProcessing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "🤖 AI Ön Yazı Oluşturuyor...",
                            color = Color(0xFF00D4FF),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = currentJobIndex.toFloat() / selectedJobs.size.coerceAtLeast(1),
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF00D4FF)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$currentJobIndex / ${selectedJobs.size} tamamlandı",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        if (statusMessage.isNotEmpty()) {
                            Text(statusMessage, fontSize = 11.sp, color = Color(0xFFFFAA00))
                        }
                    }
                }
            }

            // İlan listesi
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(selectedJobs) { index, job ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                coverLetters.containsKey(index) -> Color(0xFF00FF88).copy(alpha = 0.1f)
                                isProcessing && index == currentJobIndex -> Color(0xFFFFAA00).copy(alpha = 0.1f)
                                else -> Color(0xFF1A1F36)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Durum ikonu
                            Icon(
                                imageVector = when {
                                    coverLetters.containsKey(index) -> Icons.Default.CheckCircle
                                    isProcessing && index == currentJobIndex -> Icons.Default.Sync
                                    else -> Icons.Default.RadioButtonUnchecked
                                },
                                contentDescription = null,
                                tint = when {
                                    coverLetters.containsKey(index) -> Color(0xFF00FF88)
                                    isProcessing && index == currentJobIndex -> Color(0xFFFFAA00)
                                    else -> Color.Gray
                                },
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // İlan bilgileri
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    job.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${job.company} • ${job.platform}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            // Tarayıcıda aç butonu
                            IconButton(onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job.url))
                                    context.startActivity(intent)
                                } catch (e: Exception) { }
                            }) {
                                Icon(
                                    Icons.Default.OpenInNew,
                                    contentDescription = "Tarayıcıda Aç",
                                    tint = Color(0xFF00D4FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // On yazi varsa goster + duzenle butonu
                        if (coverLetters.containsKey(index)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "📝 On Yazi:",
                                        fontSize = 11.sp,
                                        color = Color(0xFF00FF88),
                                        fontWeight = FontWeight.Bold
                                    )
                                    // Duzenle butonu
                                    TextButton(
                                        onClick = { editingIndex = index },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Duzenle", fontSize = 11.sp)
                                    }
                                }
                                Text(
                                    coverLetters[index] ?: "",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Alt butonlar
            Column(
                modifier = Modifier.padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // AI ile ön yazı oluştur
                Button(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            selectedJobs.forEachIndexed { index, job ->
                                currentJobIndex = index + 1
                                statusMessage = "${job.company} için yazılıyor..."
                                try {
                                    val letter = nvidiaService.generateCoverLetter(
                                        jobTitle = job.title,
                                        company = job.company,
                                        jobDescription = job.description.ifEmpty { "${job.title} pozisyonu icin ${job.company} sirketinde calismak istiyorum." },
                                        userSkills = userSkills.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                        userName = userName.ifEmpty { "Kullanıcı" }
                                    )
                                    coverLetters = coverLetters + (index to letter)
                                } catch (e: Exception) {
                                    coverLetters = coverLetters + (index to "Ön yazı oluşturulamadı: ${e.message}")
                                }
                            }
                            isProcessing = false
                            statusMessage = "✅ Tüm ön yazılar hazır!"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing && selectedJobs.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🤖 AI ile Ön Yazı Oluştur", fontWeight = FontWeight.Bold)
                }

                // E-posta ile gonder (SMTP ile)
                Button(
                    onClick = {
                        scope.launch {
                            isSending = true
                            val results = mutableListOf<Pair<String, Boolean>>()
                            selectedJobs.forEachIndexed { index, job ->
                                val subject = "${userName.ifEmpty { "Basvuran" }} - ${job.title} Basvurusu - ${job.company}"
                                val body = coverLetters[index] ?: "Sayin ${job.company} Yetkilisi,\n\n${job.title} pozisyonu icin basvurmak istiyorum.\n\nSaygilarimla,\n$userName"
                                
                                if (smtpService.isConfigured()) {
                                    // Email adresi yoksa atla
                                    results.add(job.company to false)
                                } else {
                                    // SMTP yoksa email uygulamasini ac
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:")
                                            putExtra(Intent.EXTRA_SUBJECT, subject)
                                            putExtra(Intent.EXTRA_TEXT, body)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "E-posta Gonder"))
                                        results.add(job.company to true)
                                    } catch (e: Exception) {
                                        results.add(job.company to false)
                                    }
                                }
                            }
                            sendResults = results
                            isSending = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing && !isSending && coverLetters.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (smtpService.isConfigured()) "📧 SMTP ile Gonder (${coverLetters.size})"
                        else "📧 E-posta Uygulamasi ile Gonder (${coverLetters.size})",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // On yazi duzenleme dialogu
    editingIndex?.let { idx ->
        val currentLetter = coverLetters[idx] ?: ""
        var editedText by remember { mutableStateOf(currentLetter) }
        
        AlertDialog(
            onDismissRequest = { editingIndex = null },
            title = { Text("On Yazi Duzenle - ${selectedJobs.getOrNull(idx)?.company ?: ""}") },
            text = {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00D4FF),
                        unfocusedBorderColor = Color(0xFF3A3F52),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coverLetters = coverLetters + (idx to editedText)
                        editingIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF))
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingIndex = null }) {
                    Text("Iptal")
                }
            }
        )
    }
}
