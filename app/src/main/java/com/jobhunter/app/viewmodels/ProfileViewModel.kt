package com.jobhunter.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jobhunter.app.data.JobHunterDatabase
import com.jobhunter.app.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JobHunterDatabase.getDatabase(application)
    private val profileDao = database.userProfileDao()

    // Current profile
    val profile: StateFlow<UserProfile?> = profileDao.getProfile()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Edit state
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    fun startEditing() { _isEditing.value = true }
    fun stopEditing() { _isEditing.value = false }

    /**
     * Save or update profile
     */
    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            profileDao.insertOrUpdateProfile(profile.copy(
                updatedAt = System.currentTimeMillis()
            ))
            _isEditing.value = false
        }
    }

    /**
     * Update basic info
     */
    fun updateBasicInfo(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        location: String,
        linkedinUrl: String,
        githubUrl: String,
        portfolioUrl: String,
        summary: String
    ) {
        viewModelScope.launch {
            val current = profileDao.getProfileSync() ?: UserProfile()
            profileDao.insertOrUpdateProfile(current.copy(
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                location = location,
                linkedinUrl = linkedinUrl,
                githubUrl = githubUrl,
                portfolioUrl = portfolioUrl,
                summary = summary,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Add work experience
     */
    fun addExperience(experience: WorkExperience) {
        viewModelScope.launch {
            val current = profileDao.getProfileSync() ?: UserProfile()
            val updatedExperiences = current.experiences + experience
            profileDao.insertOrUpdateProfile(current.copy(
                experiences = updatedExperiences,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Update work experience
     */
    fun updateExperience(updatedExperience: WorkExperience) {
        viewModelScope.launch {
            val current = profileDao.getProfileSync() ?: UserProfile()
            val updatedExperiences = current.experiences.map {
                if (it.id == updatedExperience.id) updatedExperience else it
            }
            profileDao.insertOrUpdateProfile(current.copy(
                experiences = updatedExperiences,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Delete work experience
     */
    fun deleteExperience(experienceId: String) {
        viewModelScope.launch {
            val current = profileDao.getProfileSync() ?: UserProfile()
            val updatedExperiences = current.experiences.filter { it.id != experienceId }
            profileDao.insertOrUpdateProfile(current.copy(
                experiences = updatedExperiences,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Add education
     */
    fun addEducation(education: Education) {
        viewModelScope.launch {
            val current = profileDao.getProfileSync() ?: UserProfile()
            val updatedEducations = current.educations + education
            profileDao.insertOrUpdateProfile(current.copy(
                educations = updatedEducations,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Update education
     */
    fun updateEducation(updatedEducation: Education) {
        viewModelScope.launch {
            val current = profileDao.getProfileSync() ?: UserProfile()
            val updatedEducations = current.educations.map {
                if (it.id == updatedEducation.id) updatedEducation else it
            }
            profileDao.insertOrUpdateProfile(current.copy(
                educations = updatedEducations,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Delete education
     */
    fun deleteEducation(educationId: String) {
        viewModelScope.launch {
            val current = profileDao.getProfileSync() ?: UserProfile()
            val updatedEducations = current.educations.filter { it.id != educationId }
            profileDao.insertOrUpdateProfile(current.copy(
                educations = updatedEducations,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Update skills
     */
    fun updateSkills(skills: List<String>) {
        viewModelScope.launch {
            val current = profileDao.getProfileSync() ?: UserProfile()
            profileDao.insertOrUpdateProfile(current.copy(
                skills = skills,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Add a skill
     */
    fun addSkill(skill: String) {
        viewModelScope.launch {
            val current = profileDao.getProfileSync() ?: UserProfile()
            if (skill.isNotBlank() && !current.skills.contains(skill)) {
                profileDao.insertOrUpdateProfile(current.copy(
                    skills = current.skills + skill,
                    updatedAt = System.currentTimeMillis()
                ))
            }
        }
    }

    /**
     * Remove a skill
     */
    fun removeSkill(skill: String) {
        viewModelScope.launch {
            val current = profileDao.getProfileSync() ?: UserProfile()
            profileDao.insertOrUpdateProfile(current.copy(
                skills = current.skills - skill,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Update languages
     */
    fun updateLanguages(languages: List<String>) {
        viewModelScope.launch {
            val current = profileDao.getProfileSync() ?: UserProfile()
            profileDao.insertOrUpdateProfile(current.copy(
                languages = languages,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }
}
