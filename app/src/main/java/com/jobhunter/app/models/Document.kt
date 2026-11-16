package com.jobhunter.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Yüklenen belge modeli (CV, sertifika, referans mektubu vb.)
 */
@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // Belge adı (ör: "CV_Ahmet_2026.pdf")
    val type: DocumentType,              // Belge türü
    val filePath: String,                // Dosya yolu
    val fileSize: Long = 0,             // Dosya boyutu (byte)
    val mimeType: String = "application/pdf", // MIME türü
    val description: String = "",        // Açıklama
    val isDefault: Boolean = false,      // Varsayılan belge mi?
    val tags: List<String> = emptyList(), // Etiketler (arama için)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Belge türleri
 */
enum class DocumentType {
    CV,                 // Özgeçmiş
    CERTIFICATE,        // Sertifika
    COVER_LETTER,       // Ön yazı kalıbı
    REFERENCE,          // Referans mektubu
    TRANSCRIPT,         // Not dökümü
    PORTFOLIO,          // Portföy
    OTHER               // Diğer
}

/**
 * Belge türü için display bilgileri
 */
fun DocumentType.getDisplayName(): String {
    return when (this) {
        DocumentType.CV -> "Özgeçmiş"
        DocumentType.CERTIFICATE -> "Sertifika"
        DocumentType.COVER_LETTER -> "Ön Yazı Kalıbı"
        DocumentType.REFERENCE -> "Referans Mektubu"
        DocumentType.TRANSCRIPT -> "Not Dökümü"
        DocumentType.PORTFOLIO -> "Portföy"
        DocumentType.OTHER -> "Diğer"
    }
}

fun DocumentType.getEmoji(): String {
    return when (this) {
        DocumentType.CV -> "📄"
        DocumentType.CERTIFICATE -> "🏆"
        DocumentType.COVER_LETTER -> "✉️"
        DocumentType.REFERENCE -> "📋"
        DocumentType.TRANSCRIPT -> "🎓"
        DocumentType.PORTFOLIO -> "💼"
        DocumentType.OTHER -> "📁"
    }
}

/**
 * İş ilanı modeli (Kariyer.net ve diğer kaynaklardan)
 */
@Entity(tableName = "job_listings")
data class JobListing(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val source: String,                  // Kaynak (kariyer.net, linkedin, indeed)
    val externalId: String = "",         // Kaynaktaki ID
    val title: String,                   // İş unvanı
    val companyName: String,             // Şirket adı
    val companyLogo: String = "",        // Şirket logosu URL
    val location: String = "",            // Konum
    val jobType: String = "",            // Tam zamanlı, yarı zamanlı, staj
    val experienceLevel: String = "",    // Junior, Mid, Senior
    val salary: String = "",             // Maaş bilgisi
    val description: String = "",        // İş tanımı
    val requirements: List<String> = emptyList(), // Gereksinimler
    val benefits: List<String> = emptyList(),     // Yan haklar
    val url: String = "",                // İlan URL'si
    val email: String = "",              // Başvuru emaili
    val postedAt: Long = 0,             // Yayın tarihi
    val expiresAt: Long = 0,            // Bitiş tarihi
    val isApplied: Boolean = false,      // Başvuruldu mu?
    val appliedAt: Long = 0,            // Başvuru tarihi
    val isSelected: Boolean = false,     // Seçildi mi? (toplu başvuru için)
    val coverLetter: String = "",        // AI ile oluşturulan ön yazı
    val atsScore: Float = 0f,           // ATS uyumluluk skoru
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Toplu başvuru isteği
 */
data class BatchApplicationRequest(
    val jobIds: List<Long>,
    val profile: UserProfile,
    val documents: List<Document>,
    val customCoverLetter: String = "",
    val useAiCoverLetter: Boolean = true
)

/**
 * Toplu başvuru sonucu
 */
data class BatchApplicationResult(
    val totalJobs: Int,
    val successCount: Int,
    val failCount: Int,
    val skipCount: Int,
    val results: List<ApplicationResult>
)

data class ApplicationResult(
    val jobId: Long,
    val jobTitle: String,
    val companyName: String,
    val success: Boolean,
    val message: String,
    val coverLetterUsed: String = ""
)
