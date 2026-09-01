package com.jobhunter.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobhunter.app.models.Education
import com.jobhunter.app.models.UserProfile
import com.jobhunter.app.models.WorkExperience
import com.jobhunter.app.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
) {
    val profile by viewModel.profile.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    var firstName by remember(profile) { mutableStateOf(profile?.firstName ?: "") }
    var lastName by remember(profile) { mutableStateOf(profile?.lastName ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var location by remember(profile) { mutableStateOf(profile?.location ?: "") }
    var linkedinUrl by remember(profile) { mutableStateOf(profile?.linkedinUrl ?: "") }
    var githubUrl by remember(profile) { mutableStateOf(profile?.githubUrl ?: "") }
    var portfolioUrl by remember(profile) { mutableStateOf(profile?.portfolioUrl ?: "") }
    var summary by remember(profile) { mutableStateOf(profile?.summary ?: "") }

    // Skill management
    var newSkill by remember { mutableStateOf("") }
    var showAddSkill by remember { mutableStateOf(false) }

    // Experience management
    var showAddExperience by remember { mutableStateOf(false) }
    var expCompany by remember { mutableStateOf("") }
    var expPosition by remember { mutableStateOf("") }
    var expStartDate by remember { mutableStateOf("") }
    var expEndDate by remember { mutableStateOf("") }
    var expDescription by remember { mutableStateOf("") }

    // Education management
    var showAddEducation by remember { mutableStateOf(false) }
    var eduSchool by remember { mutableStateOf("") }
    var eduDepartment by remember { mutableStateOf("") }
    var eduDegree by remember { mutableStateOf("") }
    var eduStartDate by remember { mutableStateOf("") }
    var eduEndDate by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤 Özgeçmişim",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = {
                        if (isEditing) {
                            viewModel.updateBasicInfo(
                                firstName, lastName, email, phone, location,
                                linkedinUrl, githubUrl, portfolioUrl, summary
                            )
                        } else {
                            viewModel.startEditing()
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isEditing) "Kaydet" else "Düzenle")
                }
            }
        }

        // Profile card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${firstName.take(1)}${lastName.take(1)}".ifEmpty { "?" },
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "$firstName $lastName".ifEmpty { "Adınız Soyadınız" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Personal Info
        item {
            SectionTitle(title = "📋 Kişisel Bilgiler")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "Ad",
                        icon = Icons.Default.Person,
                        enabled = isEditing
                    )
                    ProfileTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Soyad",
                        icon = Icons.Default.Person,
                        enabled = isEditing
                    )
                    ProfileTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "E-posta",
                        icon = Icons.Default.Email,
                        enabled = isEditing
                    )
                    ProfileTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Telefon",
                        icon = Icons.Default.Phone,
                        enabled = isEditing
                    )
                    ProfileTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = "Konum",
                        icon = Icons.Default.LocationOn,
                        enabled = isEditing
                    )
                }
            }
        }

        // Social Links
        item {
            SectionTitle(title = "🔗 Sosyal Bağlantılar")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileTextField(
                        value = linkedinUrl,
                        onValueChange = { linkedinUrl = it },
                        label = "LinkedIn URL",
                        icon = Icons.Default.Link,
                        enabled = isEditing
                    )
                    ProfileTextField(
                        value = githubUrl,
                        onValueChange = { githubUrl = it },
                        label = "GitHub URL",
                        icon = Icons.Default.Code,
                        enabled = isEditing
                    )
                    ProfileTextField(
                        value = portfolioUrl,
                        onValueChange = { portfolioUrl = it },
                        label = "Portfolio URL",
                        icon = Icons.Default.Language,
                        enabled = isEditing
                    )
                }
            }
        }

        // Summary
        item {
            SectionTitle(title = "📝 Özet")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    label = { Text("Kendinizi tanıtın") },
                    enabled = isEditing,
                    minLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Experience
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(title = "💼 Deneyimler")
                if (isEditing) {
                    IconButton(onClick = { showAddExperience = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Deneyim Ekle")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (profile?.experiences?.isEmpty() != false) {
                    Text(
                        text = "Henüz deneyim eklenmemiş",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    profile?.experiences?.forEach { exp ->
                        ExperienceCard(exp = exp, isEditing = isEditing) {
                            viewModel.deleteExperience(exp.id)
                        }
                    }
                }
            }
        }

        // Education
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(title = "🎓 Eğitim")
                if (isEditing) {
                    IconButton(onClick = { showAddEducation = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Eğitim Ekle")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (profile?.educations?.isEmpty() != false) {
                    Text(
                        text = "Henüz eğitim eklenmemiş",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    profile?.educations?.forEach { edu ->
                        EducationCard(edu = edu, isEditing = isEditing) {
                            viewModel.deleteEducation(edu.id)
                        }
                    }
                }
            }
        }

        // Skills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(title = "🛠️ Yetenekler")
                if (isEditing) {
                    IconButton(onClick = { showAddSkill = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Yetenek Ekle")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (profile?.skills?.isEmpty() != false) {
                        Text(
                            text = "Henüz yetenek eklenmemiş",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            profile?.skills?.chunked(3)?.forEach { row ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    row.forEach { skill ->
                                        SkillChip(
                                            skill = skill,
                                            isEditing = isEditing,
                                            onRemove = { viewModel.removeSkill(skill) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    // Add Skill Dialog
    if (showAddSkill) {
        AlertDialog(
            onDismissRequest = { showAddSkill = false },
            title = { Text("Yetenek Ekle") },
            text = {
                OutlinedTextField(
                    value = newSkill,
                    onValueChange = { newSkill = it },
                    label = { Text("Yetenek adı") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addSkill(newSkill)
                    newSkill = ""
                    showAddSkill = false
                }) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSkill = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Add Experience Dialog
    if (showAddExperience) {
        AlertDialog(
            onDismissRequest = { showAddExperience = false },
            title = { Text("Deneyim Ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = expCompany, onValueChange = { expCompany = it }, label = { Text("Şirket") }, singleLine = true)
                    OutlinedTextField(value = expPosition, onValueChange = { expPosition = it }, label = { Text("Pozisyon") }, singleLine = true)
                    OutlinedTextField(value = expStartDate, onValueChange = { expStartDate = it }, label = { Text("Başlangıç (YYYY-MM)") }, singleLine = true)
                    OutlinedTextField(value = expEndDate, onValueChange = { expEndDate = it }, label = { Text("Bitiş (YYYY-MM veya devam)") }, singleLine = true)
                    OutlinedTextField(value = expDescription, onValueChange = { expDescription = it }, label = { Text("Açıklama") }, minLines = 2)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addExperience(
                        WorkExperience(
                            company = expCompany,
                            position = expPosition,
                            startDate = expStartDate,
                            endDate = expEndDate,
                            description = expDescription
                        )
                    )
                    expCompany = ""; expPosition = ""; expStartDate = ""; expEndDate = ""; expDescription = ""
                    showAddExperience = false
                }) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExperience = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Add Education Dialog
    if (showAddEducation) {
        AlertDialog(
            onDismissRequest = { showAddEducation = false },
            title = { Text("Eğitim Ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = eduSchool, onValueChange = { eduSchool = it }, label = { Text("Okul") }, singleLine = true)
                    OutlinedTextField(value = eduDepartment, onValueChange = { eduDepartment = it }, label = { Text("Bölüm") }, singleLine = true)
                    OutlinedTextField(value = eduDegree, onValueChange = { eduDegree = it }, label = { Text("Derece") }, singleLine = true)
                    OutlinedTextField(value = eduStartDate, onValueChange = { eduStartDate = it }, label = { Text("Başlangıç (YYYY)") }, singleLine = true)
                    OutlinedTextField(value = eduEndDate, onValueChange = { eduEndDate = it }, label = { Text("Bitiş (YYYY)") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addEducation(
                        Education(
                            school = eduSchool,
                            department = eduDepartment,
                            degree = eduDegree,
                            startDate = eduStartDate,
                            endDate = eduEndDate
                        )
                    )
                    eduSchool = ""; eduDepartment = ""; eduDegree = ""; eduStartDate = ""; eduEndDate = ""
                    showAddEducation = false
                }) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEducation = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}

@Composable
fun ExperienceCard(exp: WorkExperience, isEditing: Boolean, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exp.position, fontWeight = FontWeight.Bold)
                Text(
                    text = exp.company,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${exp.startDate} - ${exp.endDate}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isEditing) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", modifier = Modifier.size(20.dp))
                }
            }
        }
        if (exp.description.isNotEmpty()) {
            Text(
                text = exp.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun EducationCard(edu: Education, isEditing: Boolean, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = edu.school, fontWeight = FontWeight.Bold)
                Text(
                    text = "${edu.department} - ${edu.degree}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${edu.startDate} - ${edu.endDate}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isEditing) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", modifier = Modifier.size(20.dp))
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun SkillChip(skill: String, isEditing: Boolean, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = skill,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (isEditing) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Kaldır",
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
