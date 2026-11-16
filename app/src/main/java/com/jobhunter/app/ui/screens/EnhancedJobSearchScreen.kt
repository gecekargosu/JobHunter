package com.jobhunter.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobhunter.app.models.*
import com.jobhunter.app.services.JobSource
import com.jobhunter.app.viewmodels.JobSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedJobSearchScreen(
    viewModel: JobSearchViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val selectedJobIds by viewModel.selectedJobIds.collectAsState()
    val selectedSources by viewModel.selectedSources.collectAsState()
    val isApplying by viewModel.isApplying.collectAsState()
    val applicationProgress by viewModel.applicationProgress.collectAsState()
    val applicationResult by viewModel.applicationResult.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val selectedDocuments by viewModel.selectedDocuments.collectAsState()
    val jobTypeFilter by viewModel.jobTypeFilter.collectAsState()
    val experienceFilter by viewModel.experienceFilter.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var showDocuments by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }

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
                text = "🔍 Akıllı İş Arama",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Kariyer.net, Google, LinkedIn ve Indeed'de ara",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Örn: yapay zeka, part time yazılım, senior developer...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                                contentDescription = "Filtrele"
                            )
                        }
                        IconButton(onClick = { viewModel.searchJobs() }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Ara")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Kaynak Seçimi
        item {
            Text(
                text = "📡 Arama Kaynakları",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(JobSource.values().toList()) { source ->
                    FilterChip(
                        selected = selectedSources.contains(source),
                        onClick = { viewModel.toggleSource(source) },
                        label = { Text("${source.emoji} ${source.displayName}") }
                    )
                }
            }
        }

        // Filtreler
        if (showFilters) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎯 Filtreler", fontWeight = FontWeight.Bold)

                        // İş Tipi
                        Text("İş Tipi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("" to "Tümü", "full-time" to "Tam Zamanlı", "part-time" to "Yarı Zamanlı", "internship" to "Staj")) { (value, label) ->
                                FilterChip(
                                    selected = jobTypeFilter == value,
                                    onClick = { viewModel.updateJobTypeFilter(value) },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }

                        // Deneyim Seviyesi
                        Text("Deneyim Seviyesi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("" to "Tümü", "junior" to "Junior", "mid" to "Mid", "senior" to "Senior")) { (value, label) ->
                                FilterChip(
                                    selected = experienceFilter == value,
                                    onClick = { viewModel.updateExperienceFilter(value) },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Popüler Aramalar
        if (searchResults.isEmpty() && !isSearching) {
            item {
                Text(
                    text = "🔥 Popüler Aramalar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.getPopularSearches().take(8)) { suggestion ->
                        SuggestionChip(
                            onClick = {
                                viewModel.updateSearchQuery(suggestion)
                                viewModel.searchJobs()
                            },
                            label = { Text(suggestion, fontSize = 12.sp) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "⏰ Part Time Aramalar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.getPartTimeSearches().take(6)) { suggestion ->
                        SuggestionChip(
                            onClick = {
                                viewModel.updateSearchQuery(suggestion)
                                viewModel.searchJobs()
                            },
                            label = { Text(suggestion, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Loading
        if (isSearching) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("İş ilanları taranıyor...")
                    }
                }
            }
        }

        // Error
        if (searchError != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(searchError ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearResult() }) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat")
                        }
                    }
                }
            }
        }

        // Seçim Durumu ve Toplu Başvuru
        if (selectedJobIds.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 ${selectedJobIds.size} iş ilanı seçildi",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            TextButton(onClick = { viewModel.clearSelection() }) {
                                Text("Temizle")
                            }
                        }

                        // Belge Seçimi
                        OutlinedButton(
                            onClick = { showDocuments = !showDocuments },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📎 Ekler (${selectedDocuments.size} belge seçildi)")
                        }

                        // Başvuru Butonu
                        Button(
                            onClick = { viewModel.applyToSelectedJobs() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isApplying && selectedDocuments.isNotEmpty()
                        ) {
                            if (isApplying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Başvurular yapılıyor... ${(applicationProgress * 100).toInt()}%")
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("🚀 Toplu Başvuru Yap")
                            }
                        }
                    }
                }
            }
        }

        // Belge Seçimi Paneli
        if (showDocuments && selectedJobIds.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📎 Eklenecek Belgeler", fontWeight = FontWeight.Bold)

                        if (documents.isEmpty()) {
                            Text(
                                text = "Henüz belge yüklenmemiş. Ayarlardan belge ekleyebilirsiniz.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            documents.forEach { doc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleDocumentSelection(doc.id) }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedDocuments.contains(doc.id),
                                        onCheckedChange = { viewModel.toggleDocumentSelection(doc.id) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = doc.type.getEmoji())
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = doc.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            text = doc.type.getDisplayName(),
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
        }

        // Sonuç Sayısı
        if (searchResults.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 ${searchResults.size} iş ilanı bulundu",
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(onClick = { viewModel.toggleSelectAll() }) {
                        Text(
                            if (selectedJobIds.size == searchResults.size) "Tümünü Kaldır"
                            else "Tümünü Seç"
                        )
                    }
                }
            }
        }

        // İş İlanları
        items(searchResults) { job ->
            JobListingCard(
                job = job,
                isSelected = selectedJobIds.contains(job.id),
                onSelectionToggle = { viewModel.toggleJobSelection(job.id) }
            )
        }

        // Başvuru Sonucu
        if (applicationResult != null) {
            item {
                ApplicationResultCard(
                    result = applicationResult!!,
                    onDismiss = { viewModel.clearResult() }
                )
            }
        }
    }
}

@Composable
fun JobListingCard(
    job: JobListing,
    isSelected: Boolean,
    onSelectionToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionToggle() }
                )

                // Kaynak ikonu
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (job.source) {
                            "kariyer.net" -> "🏢"
                            "linkedin" -> "💼"
                            "google" -> "🔍"
                            "indeed" -> "📋"
                            else -> "📌"
                        },
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = job.companyName,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Detaylar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (job.location.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(job.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (job.jobType.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            job.jobType,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                if (job.experienceLevel.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            job.experienceLevel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Durum
            if (job.isApplied) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Text(
                        "✅ Başvuruldu",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun ApplicationResultCard(
    result: com.jobhunter.app.models.BatchApplicationResult,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📊 Başvuru Sonucu", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResultStat("Toplam", "${result.totalJobs}", MaterialTheme.colorScheme.primary)
                ResultStat("Başarılı", "${result.successCount}", Color(0xFF4CAF50))
                ResultStat("Başarısız", "${result.failCount}", MaterialTheme.colorScheme.error)
                ResultStat("Atlandı", "${result.skipCount}", Color(0xFFFF9800))
            }

            // Detaylı sonuçlar
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(result.results) { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (r.success) "✅" else "❌",
                            modifier = Modifier.width(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(r.jobTitle, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(r.companyName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(r.message, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultStat(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
