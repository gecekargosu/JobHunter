package com.jobhunter.app.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jobhunter.app.services.PlatformJobSearchService
import com.jobhunter.app.services.SmtpEmailService
import com.jobhunter.app.services.AiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlatformJobSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val platformService = PlatformJobSearchService(application)
    private val smtpService = SmtpEmailService(application)
    private val aiService = AiService(application)

    // Arama durumu
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedPlatform = MutableStateFlow("all")
    val selectedPlatform: StateFlow<String> = _selectedPlatform.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchResults = MutableStateFlow<Map<String, List<PlatformJobSearchService.JobListing>>>(emptyMap())
    val searchResults: StateFlow<Map<String, List<PlatformJobSearchService.JobListing>>> = _searchResults.asStateFlow()

    // Tum sonuclar (filtrelenmemis)
    private var allResults = mutableMapOf<String, List<PlatformJobSearchService.JobListing>>()

    // Secili ilanlar
    private val _selectedJobs = MutableStateFlow<Set<String>>(emptySet())
    val selectedJobs: StateFlow<Set<String>> = _selectedJobs.asStateFlow()

    // Basvuru durumu
    private val _isApplying = MutableStateFlow(false)
    val isApplying: StateFlow<Boolean> = _isApplying.asStateFlow()

    private val _applicationProgress = MutableStateFlow(0f)
    val applicationProgress: StateFlow<Float> = _applicationProgress.asStateFlow()

    private val _applicationResults = MutableStateFlow<List<ApplicationResult>>(emptyList())
    val applicationResults: StateFlow<List<ApplicationResult>> = _applicationResults.asStateFlow()

    // Hata mesaji
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    data class ApplicationResult(
        val job: PlatformJobSearchService.JobListing,
        val success: Boolean,
        val message: String,
        val coverLetter: String = ""
    )

    // Populer aramalar
    val popularSearches = listOf(
        "Yazilim Gelistirici", "Veri Analisti", "Proje Yoneticisi",
        "Dijital Pazarlama", "Grafik Tasarim", "Muhasebe",
        "Insan Kaynaklari", "Satis Danismani", "Musteri Hizmetleri",
        "Yapay Zeka", "Siber Guvenlik", "Flutter Developer",
        "React Developer", "Python Developer", "Full Stack",
        "Backend Developer", "Frontend Developer", "DevOps",
        "Part Time", "Remote", "Stajyer"
    )

    // Filtreler
    private val _jobTypeFilter = MutableStateFlow("all")
    val jobTypeFilter: StateFlow<String> = _jobTypeFilter.asStateFlow()

    /**
     * Arama yap
     */
    fun search() {
        if (_searchQuery.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val results = platformService.searchAllPlatforms(_searchQuery.value)
                allResults = results.toMutableMap()
                applyFilters()
            } catch (e: Exception) {
                _errorMessage.value = "Arama hatasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Tek platformda ara
     */
    fun searchPlatform(platform: String) {
        if (_searchQuery.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _selectedPlatform.value = platform
            _errorMessage.value = null

            try {
                val results = platformService.searchPlatform(platform, _searchQuery.value)
                allResults = mutableMapOf(platform to results)
                applyFilters()
            } catch (e: Exception) {
                _errorMessage.value = "$platform arama hatasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedPlatform(platform: String) {
        _selectedPlatform.value = platform
        applyFilters()
    }

    fun updateJobTypeFilter(filter: String) {
        _jobTypeFilter.value = filter
        applyFilters()
    }

    /**
     * Filtreleri uygula
     */
    private fun applyFilters() {
        val filter = _jobTypeFilter.value
        val platform = _selectedPlatform.value

        var filtered = allResults.toMap()

        // Platform filtresi
        if (platform != "all") {
            filtered = filtered.filterKeys { it == platform }
        }

        // Is tipi filtresi (basit keyword eslesme)
        if (filter != "all") {
            val filteredByType = mutableMapOf<String, List<PlatformJobSearchService.JobListing>>()
            for ((key, jobs) in filtered) {
                filteredByType[key] = jobs.filter { job ->
                    val jobInfo = "${job.title} ${job.description} ${job.jobType}".lowercase()
                    when (filter) {
                        "full-time" -> jobInfo.contains("tam zamanli") || jobInfo.contains("full-time") || jobInfo.contains("full time")
                        "part-time" -> jobInfo.contains("yar zamanli") || jobInfo.contains("part-time") || jobInfo.contains("part time")
                        "remote" -> jobInfo.contains("uzaktan") || jobInfo.contains("remote") || jobInfo.contains("evden")
                        else -> true
                    }
                }
            }
            filtered = filteredByType
        }

        _searchResults.value = filtered
    }

    /**
     * Ilan sec/kaldir
     */
    fun toggleJobSelection(jobId: String) {
        val current = _selectedJobs.value.toMutableSet()
        if (current.contains(jobId)) {
            current.remove(jobId)
        } else {
            current.add(jobId)
        }
        _selectedJobs.value = current
    }

    /**
     * Tumunu sec/kaldir
     */
    fun toggleSelectAll() {
        val allJobs = _searchResults.value.values.flatten().map { it.id }
        if (_selectedJobs.value.size == allJobs.size) {
            _selectedJobs.value = emptySet()
        } else {
            _selectedJobs.value = allJobs.toSet()
        }
    }

    /**
     * Secimleri temizle
     */
    fun clearSelection() {
        _selectedJobs.value = emptySet()
    }

    /**
     * Secili ilanlari al
     */
    fun getSelectedJobListings(): List<PlatformJobSearchService.JobListing> {
        val allJobs = _searchResults.value.values.flatten()
        return allJobs.filter { _selectedJobs.value.contains(it.id) }
    }

    /**
     * Secili ilanlara toplu basvuru yap
     */
    fun applyToSelectedJobs(
        senderName: String,
        senderEmail: String,
        senderSkills: List<String>,
        coverLetterTemplate: String = "",
        attachmentUris: List<Uri> = emptyList()
    ) {
        val selectedJobsList = getSelectedJobListings()
        if (selectedJobsList.isEmpty()) return

        viewModelScope.launch {
            _isApplying.value = true
            _applicationProgress.value = 0f
            _applicationResults.value = emptyList()

            val results = mutableListOf<ApplicationResult>()

            for ((index, job) in selectedJobsList.withIndex()) {
                try {
                    val personalizedCoverLetter = if (coverLetterTemplate.isNotEmpty()) {
                        coverLetterTemplate
                            .replace("{sirket}", job.company)
                            .replace("{pozisyon}", job.title)
                            .replace("{konum}", job.location)
                            .replace("{isim}", senderName)
                    } else {
                        generateCoverLetter(
                            senderName = senderName,
                            companyName = job.company,
                            position = job.title,
                            skills = senderSkills
                        )
                    }

                    val subject = "$senderName - Is Basvurusu - ${job.title} @ ${job.company}"
                    
                    val sendResult = smtpService.sendJobApplication(
                        to = job.email.ifEmpty { "hr@${job.company.lowercase().replace(" ", "")}.com" },
                        company = job.company,
                        position = job.title,
                        coverLetter = personalizedCoverLetter,
                        senderName = senderName,
                        attachmentUris = attachmentUris
                    )

                    results.add(ApplicationResult(
                        job = job,
                        success = sendResult.isSuccess,
                        message = sendResult.getOrNull() ?: sendResult.exceptionOrNull()?.message ?: "Bilinmeyen hata",
                        coverLetter = personalizedCoverLetter
                    ))

                    _applicationProgress.value = (index + 1).toFloat() / selectedJobsList.size
                    _applicationResults.value = results.toList()

                    kotlinx.coroutines.delay(3000)
                } catch (e: Exception) {
                    results.add(ApplicationResult(
                        job = job,
                        success = false,
                        message = e.message ?: "Bilinmeyen hata"
                    ))
                }
            }

            _isApplying.value = false
            _applicationResults.value = results
        }
    }

    private suspend fun generateCoverLetter(
        senderName: String,
        companyName: String,
        position: String,
        skills: List<String>
    ): String {
        return try {
            aiService.generateCoverLetterSimple(
                companyName = companyName,
                position = position,
                senderName = senderName,
                skills = skills
            )
        } catch (e: Exception) {
            """
Sayin $companyName Insan Kaynaklari,

$position pozisyonunda gore almak istedigimi belirtmek isterim.

Sahip oldugum yetenekler: ${skills.joinToString(", ")}

Firsati degerlendirmenizi saygiyla rica ederim.

Saygilariyla,
$senderName
            """.trimIndent()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
