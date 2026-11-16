package com.jobhunter.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.datastore.preferences.core.edit
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobhunter.app.services.CvReaderService
import com.jobhunter.app.ui.screens.dataStore
import com.jobhunter.app.ui.screens.ProfileKeys
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvImportScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cvReader = remember { CvReaderService(context) }
    val dataStore = context.dataStore

    var cvText by remember { mutableStateOf("") }
    var cvInfo by remember { mutableStateOf<CvReaderService.CvInfo?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    // Dosya secici
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            isProcessing = true
            scope.launch {
                try {
                    val mimeType = context.contentResolver.getType(it) ?: "text/plain"
                    val text = cvReader.extractText(it, mimeType)
                    
                    if (text.isNotEmpty()) {
                        cvText = text
                        cvInfo = cvReader.parseCvInfo(text)
                        showResult = true
                    } else {
                        cvText = "[Dosyadan metin cikarilamadi. Lutfen text/.txt dosyasi secin]"
                    }
                } catch (e: Exception) {
                    cvText = "Hata: ${e.message}"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CV Yukle & Analiz Et") },
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
            // Aciklama
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00D4FF).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00D4FF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("CV'nizi yukleyin, program sizin hakkınızda bilgi edinsin.", color = Color(0xFF00D4FF), fontSize = 13.sp)
                            Text("Yetenekleriniz, deneyimleriniz otomatik olarak cikarilacak.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Dosya sec butonu
            item {
                Button(
                    onClick = { filePicker.launch(arrayOf("application/pdf", "text/plain", "text/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CV Dosyasi Sec (PDF / TXT)", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            // Yukleniyor
            if (isProcessing) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF00D4FF), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("CV inceleniyor...", color = Color(0xFF00D4FF))
                        }
                    }
                }
            }

            // CV metni
            if (cvText.isNotEmpty() && !isProcessing) {
                item {
                    Text("CV Icerigi", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)), shape = RoundedCornerShape(12.dp)) {
                        Text(cvText.take(2000), modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }

            // Cikarilan bilgiler
            cvInfo?.let { info ->
                if (info.skills.isNotEmpty()) {
                    item {
                        Text("Yetenekleriniz (${info.skills.size})", color = Color(0xFF00FF88), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                info.skills.chunked(3).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                                        row.forEach { skill ->
                                            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF00FF88).copy(alpha = 0.15f)) {
                                                Text(skill, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF00FF88), fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (info.experiences.isNotEmpty()) {
                    item {
                        Text("Deneyimler (${info.experiences.size})", color = Color(0xFFFFAA00), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    items(info.experiences) { exp ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)), shape = RoundedCornerShape(8.dp)) {
                            Text(exp, modifier = Modifier.padding(8.dp), color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                if (info.educations.isNotEmpty()) {
                    item {
                        Text("Egitim (${info.educations.size})", color = Color(0xFFFF4488), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    items(info.educations) { edu ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)), shape = RoundedCornerShape(8.dp)) {
                            Text(edu, modifier = Modifier.padding(8.dp), color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                // Kaydetme basarili mesaji
                savedMessage?.let { msg ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(msg, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                }

                // Profile kaydet butonu
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                dataStore.edit { prefs ->
                                    prefs[ProfileKeys.SKILLS] = info.skills.joinToString(", ")
                                    prefs[ProfileKeys.EXPERIENCE] = info.experiences.joinToString("\n")
                                    prefs[ProfileKeys.EDUCATION] = info.educations.joinToString("\n")
                                    // CV ham metnini de kaydet (AI analizi icin)
                                    prefs[ProfileKeys.COVER_LETTER_TEMPLATE] = cvText.take(3000)
                                }
                                savedMessage = "✅ ${info.skills.size} yetenek, ${info.experiences.size} deneyim, ${info.educations.size} egitim kaydedildi!"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Profile Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bos durum
            if (cvText.isEmpty() && !isProcessing) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("CV'nizi yukleyin", color = Color.Gray, fontSize = 16.sp)
                            Text("PDF veya TXT dosyasi secin", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
