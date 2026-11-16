package com.jobhunter.app.ui.screens

import android.content.Context
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
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "user_profile")

object ProfileKeys {
    val FULL_NAME = stringPreferencesKey("full_name")
    val EMAIL = stringPreferencesKey("email")
    val PHONE = stringPreferencesKey("phone")
    val SKILLS = stringPreferencesKey("skills")
    val EXPERIENCE = stringPreferencesKey("experience")
    val EDUCATION = stringPreferencesKey("education")
    val COVER_LETTER_TEMPLATE = stringPreferencesKey("cover_letter_template")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = context.dataStore

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var coverLetterTemplate by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    // Her ekranda acildiginda veriyi yukle (CV kaydedildikten sonra geri donulunce gorulmeli)
    val savedPrefs by dataStore.data.collectAsState(initial = null)
    LaunchedEffect(savedPrefs) {
        if (savedPrefs != null) {
            fullName = savedPrefs!![ProfileKeys.FULL_NAME] ?: fullName
            email = savedPrefs!![ProfileKeys.EMAIL] ?: email
            phone = savedPrefs!![ProfileKeys.PHONE] ?: phone
            skills = savedPrefs!![ProfileKeys.SKILLS] ?: skills
            experience = savedPrefs!![ProfileKeys.EXPERIENCE] ?: experience
            education = savedPrefs!![ProfileKeys.EDUCATION] ?: education
            coverLetterTemplate = savedPrefs!![ProfileKeys.COVER_LETTER_TEMPLATE] ?: coverLetterTemplate
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👤 Profil & CV Bilgileri") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0E21)
                )
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
            // Info card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF00D4FF).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00D4FF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Bu bilgiler AI tarafından iş ilanlarıyla eşleştirme ve ön yazı oluşturmak için kullanılacak.",
                            fontSize = 12.sp,
                            color = Color(0xFF00D4FF),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Kişisel Bilgiler
            item {
                Text("📋 Kişisel Bilgiler", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                ProfileTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Ad Soyad",
                    icon = Icons.Default.Person,
                    singleLine = true
                )
            }

            item {
                ProfileTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-posta Adresi",
                    icon = Icons.Default.Email,
                    singleLine = true
                )
            }

            item {
                ProfileTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Telefon Numarası",
                    icon = Icons.Default.Phone,
                    singleLine = true
                )
            }

            // Yetenekler
            item {
                Text("🛠️ Yetenekler", color = Color(0xFF00FF88), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "Her yeteneği virgülle ayırın (örn: Kotlin, Java, Python, React)",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            item {
                ProfileTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = "Yetenekler (virgülle ayırın)",
                    icon = Icons.Default.Star,
                    singleLine = false,
                    minLines = 3
                )
            }

            // Deneyim
            item {
                Text("💼 Deneyim", color = Color(0xFFFFAA00), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                ProfileTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = "İş deneyimlerinizi yazın (şirket, pozisyon, tarih)",
                    icon = Icons.Default.Work,
                    singleLine = false,
                    minLines = 4
                )
            }

            // Eğitim
            item {
                Text("🎓 Eğitim", color = Color(0xFFFF4488), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                ProfileTextField(
                    value = education,
                    onValueChange = { education = it },
                    label = "Eğitim bilgilerinizi yazın (okul, bölüm, yıl)",
                    icon = Icons.Default.School,
                    singleLine = false,
                    minLines = 3
                )
            }

            // Ön Yazı Şablonu
            item {
                Text("📝 Ön Yazı Şablonu (Opsiyonel)", color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "AI'ın kullanacağı bir şablon yazabilirsiniz. Boş bırakırsanız AI kendi şablonunu oluşturur.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            item {
                ProfileTextField(
                    value = coverLetterTemplate,
                    onValueChange = { coverLetterTemplate = it },
                    label = "Ön yazı şablonu yazın...",
                    icon = Icons.Default.Description,
                    singleLine = false,
                    minLines = 5
                )
            }

            // Kaydet butonu
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            dataStore.edit { prefs ->
                                prefs[ProfileKeys.FULL_NAME] = fullName
                                prefs[ProfileKeys.EMAIL] = email
                                prefs[ProfileKeys.PHONE] = phone
                                prefs[ProfileKeys.SKILLS] = skills
                                prefs[ProfileKeys.EXPERIENCE] = experience
                                prefs[ProfileKeys.EDUCATION] = education
                                prefs[ProfileKeys.COVER_LETTER_TEMPLATE] = coverLetterTemplate
                            }
                            saved = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        if (saved) Icons.Default.CheckCircle else Icons.Default.Save,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (saved) "✅ Kaydedildi!" else "💾 Kaydet", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF00D4FF),
            unfocusedBorderColor = Color(0xFF3A3F52),
            focusedContainerColor = Color(0xFF1A1F36),
            unfocusedContainerColor = Color(0xFF1A1F36),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color(0xFF00D4FF),
            unfocusedLabelColor = Color.Gray
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
