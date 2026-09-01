package com.jobhunter.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jobhunter.app.data.JobHunterDatabase
import com.jobhunter.app.models.Company
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val logger = AppLogger.getInstance(application)
    private val database = JobHunterDatabase.getDatabase(application)
    private val companyDao = database.companyDao()
    private val emailDao = database.emailDao()
    
    init {
        logger.logScreenOpen("Dashboard")
    }

    // Stats
    val companyCount: StateFlow<Int> = companyDao.getCompanyCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val contactableCount: StateFlow<Int> = companyDao.getContactableCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val sentEmailCount: StateFlow<Int> = emailDao.getSentEmailCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val successfulEmailCount: StateFlow<Int> = emailDao.getSuccessfulEmailCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Recent companies
    val recentCompanies: StateFlow<List<Company>> = companyDao.getAllCompanies()
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
