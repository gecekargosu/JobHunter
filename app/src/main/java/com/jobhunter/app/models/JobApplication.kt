package com.jobhunter.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * İş başvurusu takip modeli
 */
@Entity(tableName = "job_applications")
data class JobApplication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: Long,
    val companyName: String,
    val position: String = "",
    val jobDescription: String = "",
    val jobUrl: String = "",
    val appliedAt: Long = System.currentTimeMillis(),
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    val coverLetter: String = "",
    val atsScore: Float = 0f,
    val notes: String = "",
    val followUpDate: Long = 0,
    val responseDate: Long = 0,
    val interviewDate: Long = 0,
    val rejectionReason: String = ""
)

/**
 * Başvuru durumu
 */
enum class ApplicationStatus {
    DRAFT,          // Taslak
    APPLIED,        // Başvuruldu
    REVIEWING,      // İnceleniyor
    INTERVIEW,      // Görüşme
    OFFER,          // Teklif
    ACCEPTED,       // Kabul edildi
    REJECTED,       // Reddedildi
    WITHDRAWN       // Geri çekildi
}

/**
 * Başvuru istatistikleri
 */
data class ApplicationStats(
    val totalApplications: Int,
    val appliedCount: Int,
    val reviewingCount: Int,
    val interviewCount: Int,
    val offerCount: Int,
    val rejectedCount: Int,
    val successRate: Float,
    val averageAtsScore: Float,
    val applicationsByMonth: Map<String, Int>
)

/**
 * Başvuru filtresi
 */
data class ApplicationFilter(
    val status: ApplicationStatus? = null,
    val companyId: Long? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val minAtsScore: Float? = null
)
