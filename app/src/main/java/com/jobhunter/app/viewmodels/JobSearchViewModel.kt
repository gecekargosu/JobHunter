package com.jobhunter.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jobhunter.app.data.JobHunterDatabase
import com.jobhunter.app.models.*
import com.jobhunter.app.services.*
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * İş Arama ve Toplu Başvuru ViewModel'i
 */
class JobSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val logger = AppLogger.getInstance(application)
    private val database = JobHunterDatabase.getDatabase(application)
    private val jobListingDao = database.jobListingDao()
    private val documentDao = database.documentDao()
    private val companyDao = database.companyDao()
    private val profileDao = database.userProfileDao()
    private val emailDao = database.emailDao()

    private val jobSearchService = JobSearchService(logger)
    private val aiService = AiService(application)
    private val emailAttachmentService = EmailAttachmentService(application)

    init {
        logger.logScreenOpen("İş Arama")
    }

    // ===== ARAMA DURUMU =====
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<JobListing>>(emptyList())
    val searchResults: StateFlow<List<JobListing>> = _searchResults.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private val _selectedSources = MutableStateFlow(listOf(
        JobSource.KARIYER_NET,
        JobSource.GOOGLE_JOBS
    ))
    val selectedSources: StateFlow<List<JobSource>> = _selectedSources.asStateFlow()

    // ===== SEÇİM DURUMU =====
    private val _selectedJobIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedJobIds: StateFlow<Set<Long>> = _selectedJobIds.asStateFlow()

    val selectedJobCount: StateFlow<Int> = _selectedJobIds.map { it.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // ===== BELGE DURUMU =====
    val documents: StateFlow<List<Document>> = documentDao.getAllDocuments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val selectedDocuments = MutableStateFlow<Set<Long>>(emptySet())

    // ===== BAŞVURU DURUMU =====
    private val _isApplying = MutableStateFlow(false)
    val isApplying: StateFlow<Boolean> = _isApplying.asStateFlow()

    private val _applicationProgress = MutableStateFlow(0f)
    val applicationProgress: StateFlow<Float> = _applicationProgress.asStateFlow()

    private val _applicationResult = MutableStateFlow<BatchApplicationResult?>(null)
    val applicationResult: StateFlow<BatchApplicationResult?> = _applicationResult.asStateFlow()

    // ===== KAYITLI İLANLAR =====
    val savedJobListings: StateFlow<List<JobListing>> = jobListingDao.getAllJobListings()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ===== FILTRE =====
    private val _jobTypeFilter = MutableStateFlow("")
    val jobTypeFilter: StateFlow<String> = _jobTypeFilter.asStateFlow()

    private val _experienceFilter = MutableStateFlow("")
    val experienceFilter: StateFlow<String> = _experienceFilter.asStateFlow()

    private val _locationFilter = MutableStateFlow("")
    val locationFilter: StateFlow<String> = _locationFilter.asStateFlow()

    // ===== METODLAR =====

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateJobTypeFilter(type: String) {
        _jobTypeFilter.value = type
    }

    fun updateExperienceFilter(level: String) {
        _experienceFilter.value = level
    }

    fun updateLocationFilter(location: String) {
        _locationFilter.value = location
    }

    fun toggleSource(source: JobSource) {
        val current = _selectedSources.value.toMutableList()
        if (current.contains(source)) {
            current.remove(source)
        } else {
            current.add(source)
        }
        _selectedSources.value = current
    }

    /**
     * İş ara
     */
    fun searchJobs() {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) {
            _searchError.value = "Lütfen bir arama kelimesi girin"
            return
        }

        logger.logUserAction("İş Arama", query)
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null
            _searchResults.value = emptyList()

            try {
                val results = jobSearchService.searchAllSources(
                    query = query,
                    location = _locationFilter.value,
                    jobType = _jobTypeFilter.value,
                    experienceLevel = _experienceFilter.value,
                    sources = _selectedSources.value,
                    limit = 50
                )

                val duration = System.currentTimeMillis() - startTime
                logger.i("JobSearch", "Arama tamamlandı: ${results.size} ilan (${duration}ms)")
                _searchResults.value = results

                if (results.isEmpty()) {
                    _searchError.value = "Sonuç bulunamadı. Farklı anahtar kelimeler deneyin."
                }

            } catch (e: Exception) {
                logger.logError("JobSearch", "İş Arama", e.message ?: "Hata", e)
                _searchError.value = "Arama sırasında hata: ${e.message}"
            } finally {
                _isSearching.value = false
            }
        }
    }

    /**
     * İlanı seç/kaldır
     */
    fun toggleJobSelection(jobId: Long) {
        val current = _selectedJobIds.value.toMutableSet()
        if (current.contains(jobId)) {
            current.remove(jobId)
        } else {
            current.add(jobId)
        }
        _selectedJobIds.value = current
    }

    /**
     * Tümünü seç/kaldır
     */
    fun toggleSelectAll() {
        if (_selectedJobIds.value.size == _searchResults.value.size) {
            _selectedJobIds.value = emptySet()
        } else {
            _selectedJobIds.value = _searchResults.value.map { it.id }.toSet()
        }
    }

    /**
     * Seçimi temizle
     */
    fun clearSelection() {
        _selectedJobIds.value = emptySet()
    }

    /**
     * Belgeyi seç/kaldır
     */
    fun toggleDocumentSelection(documentId: Long) {
        val current = selectedDocuments.value.toMutableSet()
        if (current.contains(documentId)) {
            current.remove(documentId)
        } else {
            current.add(documentId)
        }
        selectedDocuments.value = current
    }

    /**
     * Seçili ilanlara toplu başvuru yap
     */
    fun applyToSelectedJobs() {
        val selectedIds = _selectedJobIds.value
        if (selectedIds.isEmpty()) {
            _searchError.value = "Lütfen en az bir iş ilanı seçin"
            return
        }

        viewModelScope.launch {
            val profile = profileDao.getProfileSync()
            if (profile == null) {
                _searchError.value = "Profil bulunamadı. Önce Profilim ekranından bilgilerinizi girin."
                return@launch
            }

            val selectedDocs = documents.value.filter { doc ->
                selectedDocuments.value.contains(doc.id)
            }

            _isApplying.value = true
            _applicationProgress.value = 0f
            _isApplying.value = true
            _applicationProgress.value = 0f

            val results = mutableListOf<ApplicationResult>()
            var successCount = 0
            var failCount = 0
            var skipCount = 0

            val selectedJobs = _searchResults.value.filter { selectedIds.contains(it.id) }
            val totalJobs = selectedJobs.size

            for ((index, job) in selectedJobs.withIndex()) {
                try {
                    // Progress güncelle
                    _applicationProgress.value = (index + 1).toFloat() / totalJobs

                    // İlan zaten başvurulmuş mu kontrol et
                    if (job.isApplied) {
                        skipCount++
                        results.add(
                            ApplicationResult(
                                jobId = job.id,
                                jobTitle = job.title,
                                companyName = job.companyName,
                                success = false,
                                message = "Zaten başvurulmuş"
                            )
                        )
                        continue
                    }

                    // AI ile özelleştirilmiş ön yazı oluştur
                    val coverLetter = if (job.email.isNotEmpty()) {
                        generatePersonalizedCoverLetter(job, profile)
                    } else {
                        // Email yoksa atla
                        skipCount++
                        results.add(
                            ApplicationResult(
                                jobId = job.id,
                                jobTitle = job.title,
                                companyName = job.companyName,
                                success = false,
                                message = "E-posta adresi bulunamadı"
                            )
                        )
                        continue
                    }

                    // E-posta gönder
                    val success = if (job.email.isNotEmpty()) {
                        emailAttachmentService.sendJobApplicationEmail(
                            to = job.email,
                            company = job.companyName,
                            position = job.title,
                            coverLetter = coverLetter,
                            attachments = selectedDocs,
                            senderName = "${profile.firstName} ${profile.lastName}"
                        )
                    } else {
                        false
                    }

                    // Veritabanını güncelle
                    val updatedJob = job.copy(
                        isApplied = true,
                        appliedAt = System.currentTimeMillis(),
                        coverLetter = coverLetter
                    )
                    jobListingDao.updateJobListing(updatedJob)

                    // Şirketi kaydet (eğer yoksa)
                    val existingCompany = companyDao.getCompanyByDomain(job.companyName.lowercase())
                    if (existingCompany == null) {
                        companyDao.insertCompany(
                            Company(
                                name = job.companyName,
                                email = job.email,
                                website = job.url,
                                location = job.location,
                                description = job.description.take(500)
                            )
                        )
                    }

                    // Gönderilen e-postayı kaydet
                    emailDao.insertSentEmail(
                        SentEmail(
                            companyId = existingCompany?.id ?: 0,
                            companyName = job.companyName,
                            recipientEmail = job.email,
                            subject = "${profile.firstName} ${profile.lastName} - İş Başvurusu - ${job.title}",
                            body = coverLetter,
                            status = if (success) "sent" else "failed"
                        )
                    )

                    if (success) successCount++ else failCount++

                    results.add(
                        ApplicationResult(
                            jobId = job.id,
                            jobTitle = job.title,
                            companyName = job.companyName,
                            success = success,
                            message = if (success) "Başvuru gönderildi" else "Gönderme başarısız",
                            coverLetterUsed = coverLetter
                        )
                    )

                    logger.i("JobSearch", "Başvuru: ${job.title} @ ${job.companyName} [${if (success) "BAŞARILI" else "BAŞARISIZ"}]")

                } catch (e: Exception) {
                    failCount++
                    results.add(
                        ApplicationResult(
                            jobId = job.id,
                            jobTitle = job.title,
                            companyName = job.companyName,
                            success = false,
                            message = "Hata: ${e.message}"
                        )
                    )
                    logger.logError("JobSearch", "Başvuru Hatası", e.message ?: "Hata", e)
                }
            }

            _applicationResult.value = BatchApplicationResult(
                totalJobs = totalJobs,
                successCount = successCount,
                failCount = failCount,
                skipCount = skipCount,
                results = results
            )

            _isApplying.value = false
            _applicationProgress.value = 1f

            logger.i("JobSearch", "Toplu başvuru tamamlandı: $successCount başarılı, $failCount başarısız, $skipCount atlandı")
        }
    }

    /**
     * İlan için kişiselleştirilmiş ön yazı oluştur
     */
    private suspend fun generatePersonalizedCoverLetter(
        job: JobListing,
        profile: UserProfile
    ): String {
        // Şirket bilgisini Company modeline çevir
        val company = Company(
            name = job.companyName,
            industry = "",
            location = job.location,
            description = job.description,
            website = job.url
        )

        return aiService.generateCoverLetter(
            company = company,
            profile = profile,
            position = job.title
        )
    }

    /**
     * Sonucu temizle
     */
    fun clearResult() {
        _applicationResult.value = null
    }

    /**
     * Popüler aramaları getir
     */
    fun getPopularSearches(): List<String> {
        return jobSearchService.getPopularSearches()
    }

    /**
     * Popüler Part Time aramalar
     */
    fun getPartTimeSearches(): List<String> {
        return listOf(
            "part time yazılım",
            "part time developer",
            "yarı zamanlı programmer",
            "part time web geliştirme",
            "part time mobile developer",
            "hafta sonu yazılım",
            "akşam çalışma yazılım"
        )
    }
}
