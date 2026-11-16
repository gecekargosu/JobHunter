package com.jobhunter.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobhunter.app.models.Document
import com.jobhunter.app.models.DocumentType
import com.jobhunter.app.models.getDisplayName
import com.jobhunter.app.models.getEmoji

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentManagementScreen(
    documents: List<Document>,
    onUploadDocument: (Document) -> Unit,
    onDeleteDocument: (Document) -> Unit,
    onToggleDefault: (Document) -> Unit
) {
    var showUploadDialog by remember { mutableStateOf(false) }
    var showTypeSelector by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = "📁 Belgelerim",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${documents.size} belge yüklü",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(onClick = { showTypeSelector = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Belge Ekle")
                }
            }
        }

        // Bilgi Kartı
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "📌 Belgeler Ne İşe Yarar?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "CV ve sertifikalarınız toplu başvuru sırasında otomatik olarak e-postaya eklenir. Varsayılan belgeler otomatik seçilir.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Belge Tipleri Özeti
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DocumentType.values().take(4).forEach { type ->
                    val count = documents.count { it.type == type }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(type.getEmoji(), fontSize = 20.sp)
                            Text("$count", fontWeight = FontWeight.Bold)
                            Text(type.getDisplayName(), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Belge Listesi
        items(documents) { document ->
            DocumentCard(
                document = document,
                onDelete = { onDeleteDocument(document) },
                onToggleDefault = { onToggleDefault(document) }
            )
        }

        // Boş durum
        if (documents.isEmpty()) {
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
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Henüz belge eklenmemiş",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "CV, sertifika veya diğer belgelerinizi ekleyin",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Belge Türü Seçim Dialogu
    if (showTypeSelector) {
        AlertDialog(
            onDismissRequest = { showTypeSelector = false },
            title = { Text("Belge Türü Seçin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DocumentType.values().forEach { type ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showTypeSelector = false
                                    showUploadDialog = true
                                },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(type.getEmoji(), fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(type.getDisplayName(), fontWeight = FontWeight.Medium)
                                    Text(
                                        getDocumentTypeDescription(type),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTypeSelector = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Yükleme Dialogu (Basitleştirilmiş)
    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Belge Yükle") },
            text = {
                Column {
                    Text("Dosya seçici açılacak. PDF, DOC, JPG formatları desteklenir.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Not: Gerçek uygulamada dosya seçici açılır. Bu demo sürümüdür.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Demo: Örnek belge ekle
                    val demoDoc = Document(
                        name = "CV_Ahmet_Yilmaz_2026.pdf",
                        type = DocumentType.CV,
                        filePath = "/storage/emulated/0/Documents/CV_Ahmet_Yilmaz_2026.pdf",
                        fileSize = 245000,
                        isDefault = true
                    )
                    onUploadDocument(demoDoc)
                    showUploadDialog = false
                }) {
                    Text("Yükle (Demo)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun DocumentCard(
    document: Document,
    onDelete: () -> Unit,
    onToggleDefault: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // İkon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(document.type.getEmoji(), fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Bilgiler
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(document.name, fontWeight = FontWeight.Medium)
                    if (document.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ) {
                            Text(
                                "Varsayılan",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
                Text(
                    "${document.type.getDisplayName()} • ${formatFileSize(document.fileSize)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Aksiyonlar
            Row {
                IconButton(onClick = onToggleDefault) {
                    Icon(
                        if (document.isDefault) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Varsayılan Yap",
                        tint = if (document.isDefault) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun getDocumentTypeDescription(type: DocumentType): String {
    return when (type) {
        DocumentType.CV -> "Özgeçmiş/CV dosyası"
        DocumentType.CERTIFICATE -> "Sertifika veya belge"
        DocumentType.COVER_LETTER -> "Ön yazı kalıbı"
        DocumentType.REFERENCE -> "Referans mektubu"
        DocumentType.TRANSCRIPT -> "Not dökümü"
        DocumentType.PORTFOLIO -> "Portföy dosyası"
        DocumentType.OTHER -> "Diğer belgeler"
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}
