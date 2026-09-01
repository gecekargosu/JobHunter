package com.jobhunter.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jobhunter.app.data.JobHunterDatabase
import com.jobhunter.app.models.Company
import com.jobhunter.app.models.CompanySearchResult
import com.jobhunter.app.services.AiService
import com.jobhunter.app.services.WebScraperService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CompanySearchViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JobHunterDatabase.getDatabase(application)
    private val companyDao = database.companyDao()
    private val scraperService = WebScraperService()
    private val aiService = AiService(application)

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CompanySearchResult>>(emptyList())
    val searchResults: StateFlow<List<CompanySearchResult>> = _searchResults.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    // Saved companies
    val savedCompanies: StateFlow<List<Company>> = companyDao.getAllCompanies()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Filter
    private val _locationFilter = MutableStateFlow("")
    val locationFilter: StateFlow<String> = _locationFilter.asStateFlow()

    private val _industryFilter = MutableStateFlow("")
    val industryFilter: StateFlow<String> = _industryFilter.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateLocationFilter(location: String) {
        _locationFilter.value = location
    }

    fun updateIndustryFilter(industry: String) {
        _industryFilter.value = industry
    }

    /**
     * Search for companies
     */
    fun searchCompanies() {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null
            _searchResults.value = emptyList()

            try {
                // Search using DuckDuckGo (more reliable)
                val results = scraperService.searchCompaniesDuckDuckGo(
                    query = "$query ${_locationFilter.value}",
                    limit = 20
                )

                // Also try Google
                val googleResults = scraperService.searchCompaniesGoogle(
                    query = query,
                    location = _locationFilter.value,
                    limit = 10
                )

                // Merge and deduplicate
                val allResults = (results + googleResults)
                    .distinctBy { it.domain }
                    .take(20)

                _searchResults.value = allResults
            } catch (e: Exception) {
                _searchError.value = "Arama sırasında hata: ${e.message}"
            } finally {
                _isSearching.value = false
            }
        }
    }

    /**
     * Scrape specific company website for details
     */
    fun scrapeCompanyDetails(result: CompanySearchResult) {
        viewModelScope.launch {
            try {
                val detailedResult = scraperService.scrapeCompanyWebsite(result.website)

                // Update search results with detailed info
                val updatedResults = _searchResults.value.map {
                    if (it.domain == result.domain) {
                        it.copy(
                            emails = (it.emails + detailedResult.emails).distinct(),
                            phone = detailedResult.phone.ifEmpty { it.phone },
                            description = detailedResult.description.ifEmpty { it.description }
                        )
                    } else it
                }
                _searchResults.value = updatedResults
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    /**
     * Save company to database
     */
    fun saveCompany(result: CompanySearchResult) {
        viewModelScope.launch {
            val company = Company(
                name = result.name,
                domain = result.domain,
                industry = result.industry,
                location = result.location,
                description = result.description,
                website = result.website,
                email = result.emails.firstOrNull() ?: "",
                phone = result.phone,
                linkedinUrl = result.linkedinUrl
            )
            companyDao.insertCompany(company)
        }
    }

    /**
     * Save company with all emails
     */
    fun saveCompanyWithEmails(result: CompanySearchResult) {
        viewModelScope.launch {
            val existing = companyDao.getCompanyByDomain(result.domain)
            if (existing == null) {
                val company = Company(
                    name = result.name,
                    domain = result.domain,
                    industry = result.industry,
                    location = result.location,
                    description = result.description,
                    website = result.website,
                    email = result.emails.firstOrNull() ?: "",
                    phone = result.phone,
                    linkedinUrl = result.linkedinUrl,
                    notes = if (result.emails.size > 1) {
                        "Diğer e-postalar: ${result.emails.drop(1).joinToString(", ")}"
                    } else ""
                )
                companyDao.insertCompany(company)
            }
        }
    }

    /**
     * Delete company
     */
    fun deleteCompany(company: Company) {
        viewModelScope.launch {
            companyDao.deleteCompany(company)
        }
    }
}
