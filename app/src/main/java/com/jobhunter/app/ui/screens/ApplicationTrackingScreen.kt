package com.jobhunter.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobhunter.app.models.ApplicationStatus
import com.jobhunter.app.models.JobApplication
import com.jobhunter.app.viewmodels.ApplicationTrackingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationTrackingScreen(
    viewModel: ApplicationTrackingViewModel
) {
    val applications by viewModel.filteredApplications.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val totalCount by viewModel.totalApplicationCount.collectAsState()
    val appliedCount by viewModel.appliedCount.collectAsState()
    val interviewCount by viewModel.interviewCount.collectAsState()
    val offerCount by viewModel.offerCount.collectAsState()
    val rejectedCount by viewModel.rejectedCount.collectAsState()

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
                text = "📊 Başvuru Takibi",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Toplam",
                    value = "$totalCount",
                    color = MaterialTheme.colorScheme.primary
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Başvuruldu",
                    value = "$appliedCount",
                    color = Color(0xFF2196F3)
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Görüşme",
                    value = "$interviewCount",
                    color = Color(0xFF4CAF50)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Teklif",
                    value = "$offerCount",
                    color = Color(0xFFFF9800)
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Red",
                    value = "$rejectedCount",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Status Filter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { viewModel.filterByStatus(null) },
                    label = { Text("Tümü") }
                )
                FilterChip(
                    selected = selectedStatus == ApplicationStatus.APPLIED,
                    onClick = { viewModel.filterByStatus(ApplicationStatus.APPLIED) },
                    label = { Text("Başvuruldu") }
                )
                FilterChip(
                    selected = selectedStatus == ApplicationStatus.INTERVIEW,
                    onClick = { viewModel.filterByStatus(ApplicationStatus.INTERVIEW) },
                    label = { Text("Görüşme") }
                )
            }
        }

        // Applications List
        items(applications) { application ->
            ApplicationCard(
                application = application,
                onClick = { viewModel.selectApplication(application) },
                onStatusChange = { status ->
                    viewModel.updateApplicationStatus(application.id, status)
                }
            )
        }

        // Empty state
        if (applications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Henüz başvuru yok",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Şirketler ekleyip başvuru başlatabilirsiniz",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ApplicationCard(
    application: JobApplication,
    onClick: () -> Unit,
    onStatusChange: (ApplicationStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Company icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = application.companyName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.companyName,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = application.position,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status chip
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = getStatusColor(application.status).copy(alpha = 0.1f),
                        modifier = Modifier.clickable { showStatusMenu = true }
                    ) {
                        Text(
                            text = getStatusText(application.status),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = getStatusColor(application.status)
                        )
                    }

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        ApplicationStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(getStatusText(status)) },
                                onClick = {
                                    onStatusChange(status)
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // ATS Score
            if (application.atsScore > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ATS Skoru: ${application.atsScore.toInt()}/100",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Expanded details
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (application.jobDescription.isNotEmpty()) {
                        Text(
                            text = application.jobDescription,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "Başvuru: ${formatDate(application.appliedAt)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (application.notes.isNotEmpty()) {
                        Text(
                            text = "Not: ${application.notes}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

fun getStatusColor(status: ApplicationStatus): Color {
    return when (status) {
        ApplicationStatus.DRAFT -> Color.Gray
        ApplicationStatus.APPLIED -> Color(0xFF2196F3)
        ApplicationStatus.REVIEWING -> Color(0xFFFF9800)
        ApplicationStatus.INTERVIEW -> Color(0xFF4CAF50)
        ApplicationStatus.OFFER -> Color(0xFF9C27B0)
        ApplicationStatus.ACCEPTED -> Color(0xFF4CAF50)
        ApplicationStatus.REJECTED -> Color(0xFFF44336)
        ApplicationStatus.WITHDRAWN -> Color.Gray
    }
}

fun getStatusText(status: ApplicationStatus): String {
    return when (status) {
        ApplicationStatus.DRAFT -> "Taslak"
        ApplicationStatus.APPLIED -> "Başvuruldu"
        ApplicationStatus.REVIEWING -> "İnceleniyor"
        ApplicationStatus.INTERVIEW -> "Görüşme"
        ApplicationStatus.OFFER -> "Teklif"
        ApplicationStatus.ACCEPTED -> "Kabul"
        ApplicationStatus.REJECTED -> "Red"
        ApplicationStatus.WITHDRAWN -> "Geri Çekildi"
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
