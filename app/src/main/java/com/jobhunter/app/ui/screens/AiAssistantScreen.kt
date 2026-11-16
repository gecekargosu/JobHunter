package com.jobhunter.app.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobhunter.app.services.GoogleGeminiService
import com.jobhunter.app.services.NvidiaNimService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nvidiaService = remember { NvidiaNimService(context) }
    val geminiService = remember { GoogleGeminiService(context) }
    val dataStore = context.dataStore

    // DataStore'dan profil ve CV bilgilerini yukle
    val profileData by dataStore.data.collectAsState(initial = null)

    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    var apiConnected by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }

    // Profil bilgileri - DataStore'dan otomatik yukle
    var jobTitle by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userSkills by remember { mutableStateOf("") }
    var cvText by remember { mutableStateOf("") }
    var profileLoaded by remember { mutableStateOf(false) }

    // Profil bilgilerini DataStore'dan yukle
    LaunchedEffect(profileData) {
        if (!profileLoaded && profileData != null) {
            userName = profileData!![ProfileKeys.FULL_NAME] ?: ""
            userSkills = profileData!![ProfileKeys.SKILLS] ?: ""
            cvText = profileData!![ProfileKeys.COVER_LETTER_TEMPLATE] ?: ""
            profileLoaded = true
        }
    }

    // Gemini API durumunu kontrol et (NVIDIA degil)
    LaunchedEffect(Unit) {
        apiConnected = geminiService.isConfigured()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E21))
            .padding(16.dp)
    ) {
        // Baslik
        Text(
            text = "AI Asistan",
            color = Color(0xFF00D4FF),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // API Durumu + Key Girisi
        val geminiReady = geminiService.isConfigured()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (geminiReady) Color(0xFF00FF88).copy(alpha = 0.1f)
                else Color(0xFFFF4444).copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (geminiReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (geminiReady) Color(0xFF00FF88) else Color(0xFFFF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (geminiReady) "Gemini API Bagli ✅" else "API Baglantisi Yok",
                            color = if (geminiReady) Color(0xFF00FF88) else Color(0xFFFF4444),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!geminiReady) {
                            Text(
                                text = "AI ozellikleri icin API key gerekli",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        } else {
                            Text(
                                text = "1500 istek/gun ucretsiz",
                                color = Color(0xFF00FF88).copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Button(
                        onClick = {
                            apiKeyInput = geminiService.getApiKey()
                            showApiKeyDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (geminiReady) Color(0xFF00FF88) else Color(0xFFFF4444)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("API Key", color = Color.Black, fontSize = 12.sp)
                    }
                }
                // Profil durumu
                if (userName.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (userName.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF00FF88).copy(alpha = 0.2f)) {
                                Text("👤 $userName", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color(0xFF00FF88), fontSize = 10.sp)
                            }
                        }
                        if (userSkills.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF00D4FF).copy(alpha = 0.2f)) {
                                Text("🛠️ ${userSkills.split(",").size} Yetenek", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color(0xFF00D4FF), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Secimi
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1A1F36),
            contentColor = Color(0xFF00D4FF)
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("On Yazi", modifier = Modifier.padding(8.dp), fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Eslestirme", modifier = Modifier.padding(8.dp), fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Sirket", modifier = Modifier.padding(8.dp), fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                Text("CV Analiz", modifier = Modifier.padding(8.dp), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Icerik
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                when (selectedTab) {
                    0 -> CoverLetterForm(
                        jobTitle = jobTitle, onJobTitleChange = { jobTitle = it },
                        company = company, onCompanyChange = { company = it },
                        jobDescription = jobDescription, onJobDescriptionChange = { jobDescription = it },
                        userName = userName, onUserNameChange = { userName = it },
                        userSkills = userSkills, onUserSkillsChange = { userSkills = it },
                        onGenerate = {
                            scope.launch {
                                isLoading = true
                                resultText = try {
                                    nvidiaService.generateCoverLetter(
                                        jobTitle = jobTitle,
                                        company = company,
                                        jobDescription = jobDescription,
                                        userSkills = userSkills.split(",").map { it.trim() },
                                        userName = userName
                                    )
                                } catch (e: Exception) {
                                    "Hata: ${e.message}"
                                }
                                isLoading = false
                            }
                        }
                    )
                    1 -> JobMatchForm(
                        jobTitle = jobTitle, onJobTitleChange = { jobTitle = it },
                        jobDescription = jobDescription, onJobDescriptionChange = { jobDescription = it },
                        userSkills = userSkills, onUserSkillsChange = { userSkills = it },
                        onMatch = {
                            scope.launch {
                                isLoading = true
                                val result = try {
                                    nvidiaService.scoreJobMatch(
                                        jobTitle = jobTitle,
                                        jobDescription = jobDescription,
                                        userSkills = userSkills.split(",").map { it.trim() },
                                        userExperience = profileData?.get(ProfileKeys.EXPERIENCE) ?: "Deneyim bilgisi mevcut degil"
                                    )
                                } catch (e: Exception) {
                                    NvidiaNimService.JobMatchResult(0, "Hata: ${e.message}", emptyList(), emptyList())
                                }
                                resultText = "Eslesme Puani: ${result.score}/100\n\nNeden: ${result.reason}"
                                isLoading = false
                            }
                        }
                    )
                    2 -> CompanyResearchForm(
                        company = company, onCompanyChange = { company = it },
                        onResearch = {
                            scope.launch {
                                isLoading = true
                                resultText = try {
                                    nvidiaService.researchCompany(company)
                                } catch (e: Exception) {
                                    "Hata: ${e.message}"
                                }
                                isLoading = false
                            }
                        }
                    )
                    3 -> CvAnalysisForm(
                        cvText = cvText, onCvTextChange = { cvText = it },
                        onAnalyze = {
                            scope.launch {
                                isLoading = true
                                resultText = try {
                                    nvidiaService.analyzeCv(cvText)
                                } catch (e: Exception) {
                                    "Hata: ${e.message}"
                                }
                                isLoading = false
                            }
                        }
                    )
                }
            }

            if (resultText.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Sonuc", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(resultText, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00D4FF))
            }
        }
    }

    // API Key Dialog - Sadece Gemini
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Gemini API Key", color = Color(0xFF4285F4)) },
            text = {
                Column {
                    Text("aistudio.google.com'dan alinan API key'i girin.", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("1500 istek/gun ucretsiz!", color = Color(0xFF00FF88), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Gemini API Key", color = Color(0xFF4285F4)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4285F4),
                            unfocusedBorderColor = Color(0xFF3A3F52),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        geminiService.setApiKey(apiKeyInput)
                        showApiKeyDialog = false
                        apiConnected = geminiService.isConfigured()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                ) {
                    Text("Kaydet", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Iptal", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun CoverLetterForm(
    jobTitle: String, onJobTitleChange: (String) -> Unit,
    company: String, onCompanyChange: (String) -> Unit,
    jobDescription: String, onJobDescriptionChange: (String) -> Unit,
    userName: String, onUserNameChange: (String) -> Unit,
    userSkills: String, onUserSkillsChange: (String) -> Unit,
    onGenerate: () -> Unit
) {
    Column {
        OutlinedTextField(value = jobTitle, onValueChange = onJobTitleChange, label = { Text("Is Unvani", color = Color(0xFF00D4FF)) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00D4FF), unfocusedBorderColor = Color(0xFF3A3F52)))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = company, onValueChange = onCompanyChange, label = { Text("Sirket Adi", color = Color(0xFF00D4FF)) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00D4FF), unfocusedBorderColor = Color(0xFF3A3F52)))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = jobDescription, onValueChange = onJobDescriptionChange, label = { Text("Is Aciklamasi", color = Color(0xFF00D4FF)) }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00D4FF), unfocusedBorderColor = Color(0xFF3A3F52)))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = userName, onValueChange = onUserNameChange, label = { Text("Adiniz Soyadiniz", color = Color(0xFF00D4FF)) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00D4FF), unfocusedBorderColor = Color(0xFF3A3F52)))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = userSkills, onValueChange = onUserSkillsChange, label = { Text("Yetenekleriniz (virgulle)", color = Color(0xFF00D4FF)) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00D4FF), unfocusedBorderColor = Color(0xFF3A3F52)))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF))) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI ile On Yazi Olustur")
        }
    }
}

@Composable
fun JobMatchForm(
    jobTitle: String, onJobTitleChange: (String) -> Unit,
    jobDescription: String, onJobDescriptionChange: (String) -> Unit,
    userSkills: String, onUserSkillsChange: (String) -> Unit,
    onMatch: () -> Unit
) {
    Column {
        OutlinedTextField(value = jobTitle, onValueChange = onJobTitleChange, label = { Text("Is Unvani", color = Color(0xFF00D4FF)) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00D4FF), unfocusedBorderColor = Color(0xFF3A3F52)))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = jobDescription, onValueChange = onJobDescriptionChange, label = { Text("Is Aciklamasi", color = Color(0xFF00D4FF)) }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00D4FF), unfocusedBorderColor = Color(0xFF3A3F52)))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = userSkills, onValueChange = onUserSkillsChange, label = { Text("Yetenekleriniz (virgulle)", color = Color(0xFF00D4FF)) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00D4FF), unfocusedBorderColor = Color(0xFF3A3F52)))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onMatch, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88))) {
            Icon(Icons.Default.Psychology, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI ile Eslestirme Yap", color = Color.Black)
        }
    }
}

@Composable
fun CompanyResearchForm(
    company: String, onCompanyChange: (String) -> Unit,
    onResearch: () -> Unit
) {
    Column {
        OutlinedTextField(value = company, onValueChange = onCompanyChange, label = { Text("Sirket Adi", color = Color(0xFF00D4FF)) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00D4FF), unfocusedBorderColor = Color(0xFF3A3F52)))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onResearch, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFAA00))) {
            Icon(Icons.Default.Business, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sirketi Arastirma Yap", color = Color.Black)
        }
    }
}

@Composable
fun CvAnalysisForm(
    cvText: String, onCvTextChange: (String) -> Unit,
    onAnalyze: () -> Unit
) {
    Column {
        if (cvText.isNotEmpty()) {
            // CV yuklu - otomatik kullan
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00FF88).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00FF88), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CV yuklu - otomatik kullanilacak", color = Color(0xFF00FF88), fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = cvText,
            onValueChange = onCvTextChange,
            label = { Text(if (cvText.isNotEmpty()) "CV Metni (duzenleyebilirsiniz)" else "CV Metnini Yapistirin", color = Color(0xFF00D4FF)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00D4FF), unfocusedBorderColor = Color(0xFF3A3F52))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAnalyze, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4488))) {
            Icon(Icons.Default.Description, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI ile CV Analiz Et")
        }
    }
}
