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
import com.jobhunter.app.models.Company
import com.jobhunter.app.viewmodels.CompanySearchViewModel
import com.jobhunter.app.viewmodels.EmailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompaniesScreen(
    companyViewModel: CompanySearchViewModel,
    emailViewModel: EmailViewModel
) {
    val companies by companyViewModel.savedCompanies.collectAsState()
    var selectedCompanies by remember { mutableStateOf(setOf<Long>()) }
    var showBulkActions by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCompanies = companies.filter {
        searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) ||
                it.industry.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🏢 Şirketlerim",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${companies.size} şirket kayıtlı",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedCompanies.isNotEmpty()) {
                    IconButton(onClick = { showBulkActions = !showBulkActions }) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("${selectedCompanies.size}")
                        }
                    }
                }
            }
        }

        // Search
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Şirket ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Bulk actions
        if (showBulkActions && selectedCompanies.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                // Filter companies that have emails
                                val companiesWithEmails = companies
                                    .filter { selectedCompanies.contains(it.id) && it.email.isNotEmpty() }
                                    .map { it.id }
                                
                                if (companiesWithEmails.isEmpty()) {
                                    emailViewModel.clearResult()
                                } else {
                                    emailViewModel.bulkSendEmails(companiesWithEmails)
                                }
                                selectedCompanies = emptySet()
                                showBulkActions = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Toplu Gönder", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                selectedCompanies = emptySet()
                                showBulkActions = false
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("İptal", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Company list
        items(filteredCompanies) { company ->
            CompanyCard(
                company = company,
                isSelected = selectedCompanies.contains(company.id),
                onSelectionToggle = {
                    selectedCompanies = if (selectedCompanies.contains(company.id)) {
                        selectedCompanies - company.id
                    } else {
                        selectedCompanies + company.id
                    }
                },
                onSendEmail = { emailViewModel.sendEmailToCompany(company.id) },
                onDelete = { companyViewModel.deleteCompany(company) }
            )
        }

        // Empty state
        if (filteredCompanies.isEmpty()) {
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
                            Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Henüz şirket eklenmemiş",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Şirket Ara ekranından şirket bulup kaydedin",
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
fun CompanyCard(
    company: Company,
    isSelected: Boolean,
    onSelectionToggle: () -> Unit,
    onSendEmail: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
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
                // Checkbox
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionToggle() }
                )

                // Company icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = company.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = company.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = company.email.ifEmpty { "E-posta yok" },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status icons
                Row {
                    if (company.email.isNotEmpty()) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "E-posta var",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (company.isContacted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "İletişime geçildi",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Expanded details
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (company.industry.isNotEmpty()) {
                        InfoRow(icon = Icons.Default.Work, text = company.industry)
                    }
                    if (company.location.isNotEmpty()) {
                        InfoRow(icon = Icons.Default.LocationOn, text = company.location)
                    }
                    if (company.description.isNotEmpty()) {
                        Text(
                            text = company.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSendEmail,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            enabled = company.email.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gönder", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
