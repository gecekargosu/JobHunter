package com.jobhunter.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1, // Single profile
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val linkedinUrl: String = "",
    val githubUrl: String = "",
    val portfolioUrl: String = "",
    val summary: String = "",

    // Experience
    val experiences: List<WorkExperience> = emptyList(),

    // Education
    val educations: List<Education> = emptyList(),

    // Skills
    val skills: List<String> = emptyList(),

    // Languages
    val languages: List<String> = emptyList(),

    // Certifications
    val certifications: List<String> = emptyList(),

    // CV File
    val cvFilePath: String = "",
    val coverLetterTemplate: String = "",

    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class WorkExperience(
    val id: String = java.util.UUID.randomUUID().toString(),
    val company: String = "",
    val position: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrent: Boolean = false,
    val description: String = "",
    val location: String = ""
)

@Serializable
data class Education(
    val id: String = java.util.UUID.randomUUID().toString(),
    val school: String = "",
    val department: String = "",
    val degree: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val gpa: String = ""
)

@Serializable
data class UserProfileSummary(
    val fullName: String,
    val title: String,
    val email: String,
    val phone: String,
    val location: String,
    val summary: String,
    val skills: List<String>,
    val experienceYears: Int
)
