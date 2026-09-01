package com.jobhunter.app.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ViewModel Factory - AndroidViewModel için
 */
class JobHunterViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ApplicationTrackingViewModel::class.java) -> {
                ApplicationTrackingViewModel(application) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(application) as T
            }
            modelClass.isAssignableFrom(CompanySearchViewModel::class.java) -> {
                CompanySearchViewModel(application) as T
            }
            modelClass.isAssignableFrom(EmailViewModel::class.java) -> {
                EmailViewModel(application) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
