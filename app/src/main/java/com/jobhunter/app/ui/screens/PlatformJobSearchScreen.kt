package com.jobhunter.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.jobhunter.app.services.PlatformJobSearchService
import com.jobhunter.app.viewmodels.PlatformJobSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformJobSearchScreen(
    viewModel: PlatformJobSearchViewModel,
    onNavigateToWebView: (String) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedJobs by viewModel.selectedJobs.collectAsState()
    val isApplying by viewModel.isApplying.collectAsState()
    val applicationProgress by viewModel.applicationProgress.collectAsState()
    val applicationResults by viewModel.applicationResults.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val jobTypeFilter by viewModel.jobTypeFilter.collectAsState()

    val context = LocalContext.current
    var showApplyDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showJobDetail by remember { mutableStateOf<PlatformJobSearchService.JobListing?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔍 Akıllı İş Arama") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // HATA MESAJI
            if (errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFFF5252))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage ?: "", color = Color(0xFFFF5252), modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(Icons.Default.Close, contentDescription = "Kapat")
                            }
                        }
                    }
                }
            }

            // ARAMA KUTUSU
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("İş ara... (ör: yapay zeka, flutter developer)") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        viewModel.updateSearchQuery("")
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Temizle")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // ARA BUTONU
                        Button(
                            onClick = { viewModel.search() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = searchQuery.isNotBlank() && !isLoading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Aranıyor...")
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tüm Platformlarda Ara")
                            }
                        }
                    }
                }
            }

            // GERÇEK MOD PLATFORM BUTONLARI
            if (searchResults.isEmpty() && !isLoading) {
                item {
                    Text(
                        "🌐 Gerçek Platformlarda Ara",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Doğrudan gerçek platform sitelerinde arama yapın. Scraping yok, gerçek sonuçlar!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Kariyer.net
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToWebView("kariyer") },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1976D2).copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎯", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Kariyer.net", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Gerçek Site", fontSize = 10.sp, color = Color(0xFF1976D2))
                            }
                        }
                        
                        // LinkedIn
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToWebView("linkedin") },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF0077B5).copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("💼", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("LinkedIn", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Gerçek Site", fontSize = 10.sp, color = Color(0xFF0077B5))
                            }
                        }
                        
                        // Indeed
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToWebView("indeed") },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2164F3).copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔍", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Indeed", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Gerçek Site", fontSize = 10.sp, color = Color(0xFF2164F3))
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "🔥 Popüler Aramalar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // POPÜLER ARAMALAR (Eski mod)
            if (searchResults.isEmpty() && !isLoading) {
                item {
                    Text(
                        "🔥 Popüler Aramalar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.popularSearches) { search ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.updateSearchQuery(search)
                                    viewModel.search()
                                },
                                label = { Text(search, fontSize = 13.sp) }
                            )
                        }
                    }
                }
            }

            // PLATFORM SEKME ÇUBUĞU
            if (searchResults.isNotEmpty()) {
                item {
                    Column {
                        // Platform butonları
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                PlatformFilterChip(
                                    label = "Tümü",
                                    count = searchResults.values.flatten().size,
                                    selected = selectedPlatform == "all",
                                    color = MaterialTheme.colorScheme.primary,
                                    onClick = { viewModel.updateSelectedPlatform("all") }
                                )
                            }
                            item {
                                PlatformFilterChip(
                                    label = "🎯 Kariyer.net",
                                    count = searchResults["Kariyer.net"]?.size ?: 0,
                                    selected = selectedPlatform == "Kariyer.net",
                                    color = Color(0xFF1976D2),
                                    onClick = {
                                        viewModel.updateSelectedPlatform("Kariyer.net")
                                        viewModel.searchPlatform("Kariyer.net")
                                    }
                                )
                            }
                            item {
                                PlatformFilterChip(
                                    label = "💼 LinkedIn",
                                    count = searchResults["LinkedIn"]?.size ?: 0,
                                    selected = selectedPlatform == "LinkedIn",
                                    color = Color(0xFF0077B5),
                                    onClick = {
                                        viewModel.updateSelectedPlatform("LinkedIn")
                                        viewModel.searchPlatform("LinkedIn")
                                    }
                                )
                            }
                            item {
                                PlatformFilterChip(
                                    label = "🔍 Indeed",
                                    count = searchResults["Indeed"]?.size ?: 0,
                                    selected = selectedPlatform == "Indeed",
                                    color = Color(0xFF2164F3),
                                    onClick = {
                                        viewModel.updateSelectedPlatform("Indeed")
                                        viewModel.searchPlatform("Indeed")
                                    }
                                )
                            }
                            item {
                                PlatformFilterChip(
                                    label = "🌐 Google Jobs",
                                    count = searchResults["Google Jobs"]?.size ?: 0,
                                    selected = selectedPlatform == "Google Jobs",
                                    color = Color(0xFF4285F4),
                                    onClick = {
                                        viewModel.updateSelectedPlatform("Google Jobs")
                                        viewModel.searchPlatform("Google Jobs")
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // İş tipi filtresi
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = jobTypeFilter == "all",
                                onClick = { viewModel.updateJobTypeFilter("all") },
                                label = { Text("Tümü", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = jobTypeFilter == "full-time",
                                onClick = { viewModel.updateJobTypeFilter("full-time") },
                                label = { Text("Tam Zamanlı", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = jobTypeFilter == "part-time",
                                onClick = { viewModel.updateJobTypeFilter("part-time") },
                                label = { Text("Yarı Zamanlı", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = jobTypeFilter == "remote",
                                onClick = { viewModel.updateJobTypeFilter("remote") },
                                label = { Text("Uzaktan", fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // SEÇİM DURUMU + TOPLU BAŞVURU
                if (selectedJobs.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${selectedJobs.size} ilan seçildi",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.toggleSelectAll() }) {
                                    Text("Tümünü Seç")
                                }
                                TextButton(onClick = { viewModel.clearSelection() }) {
                                    Text("Temizle")
                                }
                            }

                            // Toplu Başvuru Butonu
                            Button(
                                onClick = { showApplyDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("🚀 ${selectedJobs.size} İlanda Toplu Başvuru Yap")
                            }
                        }
                    }
                }

                // SONUÇLAR LİSTESİ
                val displayedJobs = if (selectedPlatform == "all") {
                    searchResults.values.flatten()
                } else {
                    searchResults[selectedPlatform] ?: emptyList()
                }

                // Toplam sonuç sayısı
                item {
                    Text(
                        "📋 ${displayedJobs.size} iş ilanı bulundu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tümünü Se checkbox
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleSelectAll() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedJobs.size == displayedJobs.size && displayedJobs.isNotEmpty(),
                            onCheckedChange = { viewModel.toggleSelectAll() }
                        )
                        Text("Tümünü Se", fontWeight = FontWeight.Medium)
                    }
                }

                // İş İlanları
                items(displayedJobs) { job ->
                    JobListingCard(
                        job = job,
                        isSelected = selectedJobs.contains(job.id),
                        onToggleSelection = { viewModel.toggleJobSelection(job.id) },
                        onOpenDetail = { showJobDetail = job }
                    )
                }
            }

            // YÜKLENİYOR
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Platformlar taranıyor...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // SONUÇ YOK
            if (!isLoading && searchResults.isEmpty() && searchQuery.isNotBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Sonuç bulunamadı", fontWeight = FontWeight.Bold)
                            Text(
                                "Farklı anahtar kelimelerle tekrar deneyin",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // BAŞVURU DİYALOGU
    if (showApplyDialog) {
        ApplyDialog(
            jobCount = selectedJobs.size,
            onDismiss = { showApplyDialog = false },
            onConfirm = { name, email, skills, coverLetter ->
                viewModel.applyToSelectedJobs(
                    senderName = name,
                    senderEmail = email,
                    senderSkills = skills,
                    coverLetterTemplate = coverLetter
                )
                showApplyDialog = false
                showResultDialog = true
            }
        )
    }

    // SONUÇ DİYALOGU
    if (showResultDialog) {
        ApplicationResultDialog(
            isApplying = isApplying,
            progress = applicationProgress,
            results = applicationResults,
            onDismiss = { showResultDialog = false }
        )
    }

    // İLAN DETAY DİYALOGU
    showJobDetail?.let { job ->
        JobDetailDialog(
            job = job,
            onDismiss = { showJobDetail = null },
            onOpenInBrowser = { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
                showJobDetail = null
            },
            onToggleSelection = {
                viewModel.toggleJobSelection(job.id)
            },
            isSelected = selectedJobs.contains(job.id)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text("$label ($count)", fontSize = 12.sp)
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.15f),
            selectedLabelColor = color
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListingCard(
    job: PlatformJobSearchService.JobListing,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onOpenDetail: () -> Unit = {}
) {
    val platformColor = when (job.source) {
        "Kariyer.net" -> Color(0xFF1976D2)
        "LinkedIn" -> Color(0xFF0077B5)
        "Indeed" -> Color(0xFF2164F3)
        "Google Jobs" -> Color(0xFF4285F4)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenDetail() }
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox (tıklanınca seçim değişir)
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(platformColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = job.company.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = platformColor,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Bilgiler
            Column(modifier = Modifier.weight(1f)) {
                // Başlık
                Text(
                    text = job.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Şirket
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.company,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Konum
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.location,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Platform badge
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = platformColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = job.source,
                            fontSize = 10.sp,
                            color = platformColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (job.salary.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = job.salary,
                                fontSize = 10.sp,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Git butonu
            if (job.url.isNotEmpty()) {
                IconButton(
                    onClick = { onOpenDetail() }
                ) {
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = "Platformda Gör",
                        tint = platformColor
                    )
                }
            }
        }
    }
}

@Composable
fun ApplyDialog(
    jobCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, skills: List<String>, coverLetter: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var skillsText by remember { mutableStateOf("") }
    var coverLetter by remember { mutableStateOf("") }
    var useAiCoverLetter by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🚀 Toplu Başvuru ($jobCount ilan)") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Seçili $jobCount ilana AI ile farklı ön yazılar oluşturularak gönderilecek.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Adın Soyadın") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-posta Adresin") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = skillsText,
                    onValueChange = { skillsText = it },
                    label = { Text("Yeteneklerin (virgülle ayır)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Kotlin, Python, React...") }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = useAiCoverLetter,
                        onCheckedChange = { useAiCoverLetter = it }
                    )
                    Text("AI ile otomatik ön yazı oluştur", fontSize = 13.sp)
                }

                if (!useAiCoverLetter) {
                    OutlinedTextField(
                        value = coverLetter,
                        onValueChange = { coverLetter = it },
                        label = { Text("Ön Yazı") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val skills = skillsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onConfirm(name, email, skills, coverLetter)
                },
                enabled = name.isNotBlank() && email.isNotBlank() && skillsText.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gönder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun JobDetailDialog(
    job: PlatformJobSearchService.JobListing,
    onDismiss: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
    onToggleSelection: () -> Unit,
    isSelected: Boolean
) {
    val platformColor = when (job.source) {
        "Kariyer.net" -> Color(0xFF1976D2)
        "LinkedIn" -> Color(0xFF0077B5)
        "Indeed" -> Color(0xFF2164F3)
        "Google Jobs" -> Color(0xFF4285F4)
        else -> MaterialTheme.colorScheme.primary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(job.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(job.company, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Platform badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = platformColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "📡 ${job.source}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = platformColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }

                // Konum
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(job.location, fontSize = 14.sp)
                }

                // Şirket
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(job.company, fontSize = 14.sp)
                }

                // Tarih
                if (job.date.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(job.date, fontSize = 14.sp)
                    }
                }

                // Maaş
                if (job.salary.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(job.salary, fontSize = 14.sp, color = Color(0xFF4CAF50))
                    }
                }

                // Açıklama
                if (job.description.isNotEmpty()) {
                    Divider()
                    Text("📋 Açıklama", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(job.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                }

                // Link
                if (job.url.isNotEmpty()) {
                    Divider()
                    Text("🔗 Link", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = job.url,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Seç/Kaldır
                OutlinedButton(onClick = onToggleSelection) {
                    Icon(
                        if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isSelected) "Seçimden Kaldır" else "Seç")
                }

                // Tarayıcıda Aç
                Button(
                    onClick = { if (job.url.isNotEmpty()) onOpenInBrowser(job.url) },
                    enabled = job.url.isNotEmpty()
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aç")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

@Composable
fun ApplicationResultDialog(
    isApplying: Boolean,
    progress: Float,
    results: List<PlatformJobSearchViewModel.ApplicationResult>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isApplying) onDismiss() },
        title = {
            if (isApplying) {
                Text("📧 Başvurular gönderiliyor...")
            } else {
                val successCount = results.count { it.success }
                Text("📊 Sonuç: $successCount/${results.size} başarılı")
            }
        },
        text = {
            Column {
                if (isApplying) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(progress * 100).toInt()}% tamamlandı", fontSize = 13.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results) { result ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (result.success) Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    else Color(0xFFFF5252).copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (result.success) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(result.job.company, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(result.job.title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isApplying) {
                Button(onClick = onDismiss) {
                    Text("Tamam")
                }
            }
        }
    )
}
