package com.jobhunter.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jobhunter.app.data.JobHunterDatabase
import com.jobhunter.app.models.*
import com.jobhunter.app.services.AtsService
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ApplicationTrackingViewModel(application: Application) : AndroidViewModel(application) {

    private val logger = AppLogger.getInstance(application)
    private val database = JobHunterDatabase.getDatabase(application)
    private val applicationDao = database.jobApplicationDao()
    private val atsService = AtsService(application)

    init {
        logger.logScreenOpen("Başvuru Takibi")
    }

    // Tüm başvurular
    val allApplications: StateFlow<List<JobApplication>> = applicationDao.getAllApplications()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Duruma göre başvurular
    private val _selectedStatus = MutableStateFlow<ApplicationStatus?>(null)
    val selectedStatus: StateFlow<ApplicationStatus?> = _selectedStatus.asStateFlow()

    val filteredApplications: StateFlow<List<JobApplication>> = _selectedStatus.flatMapLatest { status ->
        if (status != null) {
            applicationDao.getApplicationsByStatus(status)
        } else {
            applicationDao.getAllApplications()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // İstatistikler
    val totalApplicationCount: StateFlow<Int> = applicationDao.getTotalApplicationCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val appliedCount: StateFlow<Int> = applicationDao.getApplicationCountByStatus(ApplicationStatus.APPLIED)
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val interviewCount: StateFlow<Int> = applicationDao.getApplicationCountByStatus(ApplicationStatus.INTERVIEW)
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val offerCount: StateFlow<Int> = applicationDao.getApplicationCountByStatus(ApplicationStatus.OFFER)
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val rejectedCount: StateFlow<Int> = applicationDao.getApplicationCountByStatus(ApplicationStatus.REJECTED)
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val averageAtsScore: StateFlow<Float> = applicationDao.getAverageAtsScore()
        .map { it ?: 0f }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    // Aylık başvurular
    val monthlyApplications: StateFlow<List<MonthlyApplications>> = applicationDao.getApplicationsByMonth()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Başvuru detayı
    private val _selectedApplication = MutableStateFlow<JobApplication?>(null)
    val selectedApplication: StateFlow<JobApplication?> = _selectedApplication.asStateFlow()

    /**
     * Yeni başvuru oluştur
     */
    fun createApplication(
        companyId: Long,
        companyName: String,
        position: String,
        jobDescription: String = "",
        jobUrl: String = "",
        coverLetter: String = ""
    ) {
        logger.logUserAction("Başvuru Oluştur", "$companyName - $position")
        
        viewModelScope.launch {
            val application = JobApplication(
                companyId = companyId,
                companyName = companyName,
                position = position,
                jobDescription = jobDescription,
                jobUrl = jobUrl,
                coverLetter = coverLetter,
                status = ApplicationStatus.DRAFT
            )
            val id = applicationDao.insertApplication(application)
            logger.logDatabase("INSERT", "job_applications", "ID: $id")
        }
    }

    /**
     * Başvuru durumunu güncelle
     */
    fun updateApplicationStatus(applicationId: Long, newStatus: ApplicationStatus, notes: String = "") {
        viewModelScope.launch {
            val application = applicationDao.getApplicationById(applicationId) ?: return@launch
            
            logger.logUserAction("Başvuru Durumu Güncelleme", "${application.companyName}: ${application.status} -> $newStatus")
            
            val updatedApplication = application.copy(
                status = newStatus,
                notes = if (notes.isNotEmpty()) notes else application.notes,
                responseDate = if (newStatus == ApplicationStatus.INTERVIEW || 
                    newStatus == ApplicationStatus.OFFER || 
                    newStatus == ApplicationStatus.REJECTED) {
                    System.currentTimeMillis()
                } else application.responseDate,
                interviewDate = if (newStatus == ApplicationStatus.INTERVIEW) {
                    System.currentTimeMillis()
                } else application.interviewDate
            )
            
            applicationDao.updateApplication(updatedApplication)
            logger.logDatabase("UPDATE", "job_applications", "Durum güncellendi: $newStatus")
        }
    }

    /**
     * ATS skoru hesapla
     */
    fun calculateAtsScore(applicationId: Long, profile: UserProfile) {
        viewModelScope.launch {
            val application = applicationDao.getApplicationById(applicationId) ?: return@launch
            
            logger.i("ATS", "Başvuru için ATS skoru hesaplanıyor: ${application.companyName}")
            
            val result = atsService.calculateAtsScore(
                jobDescription = application.jobDescription,
                requiredSkills = emptyList(), // İş ilanından çıkarma yapılabilir
                requiredExperience = 2,
                profile = profile
            )
            
            val updatedApplication = application.copy(
                atsScore = result.score
            )
            
            applicationDao.updateApplication(updatedApplication)
            logger.i("ATS", "ATS skoru güncellendi: ${result.score} (${result.grade})")
        }
    }

    /**
     * Takip tarihi ayarla
     */
    fun setFollowUpDate(applicationId: Long, followUpDate: Long) {
        viewModelScope.launch {
            val application = applicationDao.getApplicationById(applicationId) ?: return@launch
            
            logger.logUserAction("Takip Tarihi Ayarlama", "${application.companyName}")
            
            val updatedApplication = application.copy(followUpDate = followUpDate)
            applicationDao.updateApplication(updatedApplication)
        }
    }

    /**
     * Başvuruyu sil
     */
    fun deleteApplication(application: JobApplication) {
        viewModelScope.launch {
            logger.logUserAction("Başvuru Silme", "${application.companyName}")
            applicationDao.deleteApplication(application)
            logger.logDatabase("DELETE", "job_applications", application.companyName)
        }
    }

    /**
     * Başvuru detayını seç
     */
    fun selectApplication(application: JobApplication) {
        _selectedApplication.value = application
    }

    /**
     * Durum filtresini değiştir
     */
    fun filterByStatus(status: ApplicationStatus?) {
        _selectedStatus.value = status
    }

    /**
     * Takip edilmesi gereken başvuruları getir
     */
    suspend fun getApplicationsNeedingFollowUp(): List<JobApplication> {
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        return applicationDao.getApplicationsNeedingFollowUp(oneWeekAgo)
    }

    /**
     * Başvuru istatistiklerini hesapla
     */
    fun calculateStats(): ApplicationStats {
        val total = totalApplicationCount.value
        val applied = appliedCount.value
        val interview = interviewCount.value
        val offer = offerCount.value
        val rejected = rejectedCount.value

        val successRate = if (total > 0) {
            ((interview + offer).toFloat() / total) * 100
        } else 0f

        return ApplicationStats(
            totalApplications = total,
            appliedCount = applied,
            reviewingCount = total - applied - interview - offer - rejected,
            interviewCount = interview,
            offerCount = offer,
            rejectedCount = rejected,
            successRate = successRate,
            averageAtsScore = averageAtsScore.value,
            applicationsByMonth = monthlyApplications.value.associate { it.month to it.count }
        )
    }
}
