package com.jobhunter.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val domain: String = "",
    val industry: String = "",
    val location: String = "",
    val description: String = "",
    val website: String = "",
    val logoUrl: String = "",
    val email: String = "",
    val phone: String = "",
    val linkedinUrl: String = "",
    val founded: String = "",
    val employeeCount: String = "",
    val isContacted: Boolean = false,
    val contactedAt: Long = 0,
    val notes: String = "",
    val rating: Int = 0, // 1-5 stars
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class CompanySearchResult(
    val name: String,
    val domain: String,
    val description: String,
    val location: String,
    val industry: String,
    val website: String,
    val emails: List<String> = emptyList(),
    val phone: String = "",
    val employees: String = "",
    val founded: String = "",
    val linkedinUrl: String = "",
    val source: String = "" // kariyer.net, google, linkedin
)

data class SearchFilter(
    val query: String = "",
    val location: String = "",
    val industry: String = "",
    val minEmployees: Int = 0,
    val maxEmployees: Int = Int.MAX_VALUE,
    val hasEmail: Boolean = false
)
