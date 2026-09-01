package com.jobhunter.app.services

import android.content.Context
import com.jobhunter.app.models.Company
import com.jobhunter.app.models.UserProfile
import com.jobhunter.app.utils.AppLogger

/**
 * ATS (Applicant Tracking System) Optimizasyon Servisi
 * CV'nin ATS sistemlerinden geçme şansını artırır
 */
class AtsService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)

    /**
     * İş ilanı ile CV arasındaki uyum skorunu hesapla
     */
    fun calculateAtsScore(
        jobDescription: String,
        requiredSkills: List<String>,
        requiredExperience: Int,
        profile: UserProfile
    ): AtsResult {
        logger.i("ATS", "ATS skoru hesaplanıyor...")
        val startTime = System.currentTimeMillis()

        var score = 0f
        val matchedSkills = mutableListOf<String>()
        val missingSkills = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        // 1. Beceri eşleşmesi (40 puan)
        val skillScore = calculateSkillScore(requiredSkills, profile.skills, matchedSkills, missingSkills)
        score += skillScore

        // 2. Deneyim uyumu (30 puan)
        val experienceScore = calculateExperienceScore(requiredExperience, profile, recommendations)
        score += experienceScore

        // 3. Anahtar kelime yoğunluğu (20 puan)
        val keywordScore = calculateKeywordScore(jobDescription, profile, recommendations)
        score += keywordScore

        // 4. Format uyumluluğu (10 puan)
        val formatScore = calculateFormatScore(profile, recommendations)
        score += formatScore

        val duration = System.currentTimeMillis() - startTime
        logger.i("ATS", "ATS skoru hesaplandı: $score/100 (${duration}ms)")

        return AtsResult(
            score = score.coerceIn(0f, 100f),
            matchedSkills = matchedSkills,
            missingSkills = missingSkills,
            recommendations = recommendations,
            grade = getAtsGrade(score)
        )
    }

    /**
     * Beceri puanını hesapla
     */
    private fun calculateSkillScore(
        requiredSkills: List<String>,
        candidateSkills: List<String>,
        matched: MutableList<String>,
        missing: MutableList<String>
    ): Float {
        if (requiredSkills.isEmpty()) return 20f

        for (skill in requiredSkills) {
            val isMatched = candidateSkills.any { 
                it.contains(skill, ignoreCase = true) || 
                skill.contains(it, ignoreCase = true) 
            }
            if (isMatched) {
                matched.add(skill)
            } else {
                missing.add(skill)
            }
        }

        val matchRatio = matched.size.toFloat() / requiredSkills.size
        return matchRatio * 40f
    }

    /**
     * Deneyim puanını hesapla
     */
    private fun calculateExperienceScore(
        requiredYears: Int,
        profile: UserProfile,
        recommendations: MutableList<String>
    ): Float {
        val candidateYears = calculateTotalExperience(profile)
        
        return when {
            candidateYears >= requiredYears -> {
                30f
            }
            candidateYears >= requiredYears * 0.8f -> {
                recommendations.add("Deneyim yılınız gereksinime çok yakın, projelerinizi vurgulayın")
                24f
            }
            candidateYears >= requiredYears * 0.5f -> {
                recommendations.add("Deneyim yılınız eksik, staj ve gönüllü çalışmalarınızı ekleyin")
                15f
            }
            else -> {
                recommendations.add("Deneyim yılınız çok düşük, ilgili projelerinizi öne çıkarın")
                8f
            }
        }
    }

    /**
     * Toplam deneyim yılını hesapla
     */
    private fun calculateTotalExperience(profile: UserProfile): Int {
        var totalMonths = 0
        for (exp in profile.experiences) {
            val start = parseDate(exp.startDate)
            val end = if (exp.isCurrent) System.currentTimeMillis() else parseDate(exp.endDate)
            if (start > 0 && end > 0) {
                totalMonths += ((end - start) / (30L * 24 * 60 * 60 * 1000)).toInt()
            }
        }
        return totalMonths / 12
    }

    /**
     * Tarih string'ini timestamp'e çevir
     */
    private fun parseDate(dateStr: String): Long {
        return try {
            val parts = dateStr.split("-", "/")
            val year = parts[0].toLongOrNull() ?: return 0
            val month = (parts.getOrNull(1)?.toLongOrNull() ?: 1) - 1
            year * 365 * 24 * 60 * 60 * 1000 + month * 30 * 24 * 60 * 60 * 1000
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Anahtar kelime puanını hesapla
     */
    private fun calculateKeywordScore(
        jobDescription: String,
        profile: UserProfile,
        recommendations: MutableList<String>
    ): Float {
        val jobWords = jobDescription.lowercase()
            .replace(Regex("[^a-zA-ZğüşıöçĞÜŞİÖÇ\\s]"), " ")
            .split("\\s+".toRegex())
            .filter { it.length > 3 }
            .distinct()

        val profileText = buildString {
            append(profile.summary)
            append(" ")
            append(profile.skills.joinToString(" "))
            profile.experiences.forEach { append(" ${it.description} ${it.position}") }
        }.lowercase()

        var matchedCount = 0
        for (word in jobWords.take(20)) {
            if (profileText.contains(word)) {
                matchedCount++
            }
        }

        val ratio = matchedCount.toFloat() / minOf(20, jobWords.size)
        
        if (ratio < 0.3f) {
            recommendations.add("İş ilanındaki anahtar kelimeleri CV'nize ekleyin")
        }

        return ratio * 20f
    }

    /**
     * Format puanını hesapla
     */
    private fun calculateFormatScore(
        profile: UserProfile,
        recommendations: MutableList<String>
    ): Float {
        var score = 10f

        if (profile.summary.isEmpty()) {
            score -= 3f
            recommendations.add("Özet bölümü ekleyin")
        }
        if (profile.skills.isEmpty()) {
            score -= 3f
            recommendations.add("Yetenekler bölümü ekleyin")
        }
        if (profile.experiences.isEmpty()) {
            score -= 2f
            recommendations.add("Deneyim bölümü ekleyin")
        }
        if (profile.educations.isEmpty()) {
            score -= 2f
            recommendations.add("Eğitim bölümü ekleyin")
        }

        return score.coerceAtLeast(0f)
    }

    /**
     * ATS notunu belirle
     */
    private fun getAtsGrade(score: Float): String {
        return when {
            score >= 90 -> "Mükemmel (A+)"
            score >= 80 -> "Çok İyi (A)"
            score >= 70 -> "İyi (B+)"
            score >= 60 -> "Orta (B)"
            score >= 50 -> "Kabul Edilebilir (C)"
            else -> "Geliştirilmeli (D)"
        }
    }

    /**
     * CV'yi ATS dostu formata dönüştür
     */
    fun optimizeForAts(profile: UserProfile, jobDescription: String): UserProfile {
        logger.i("ATS", "CV ATS dostu formata dönüştürülüyor...")

        // İş ilanından anahtar kelimeleri çıkar
        val keywords = extractKeywords(jobDescription)

        // Mevcut yeteneklere eksik anahtar kelimeleri ekle
        val optimizedSkills = profile.skills.toMutableList()
        for (keyword in keywords) {
            if (!optimizedSkills.any { it.contains(keyword, ignoreCase = true) }) {
                optimizedSkills.add(keyword)
            }
        }

        // Özet bölümünü iyileştir
        val optimizedSummary = if (profile.summary.isEmpty()) {
            generateAtsSummary(profile, keywords)
        } else {
            profile.summary
        }

        logger.i("ATS", "CV optimize edildi: ${optimizedSkills.size} yetenek")
        return profile.copy(
            skills = optimizedSkills.take(15), // Max 15 yetenek
            summary = optimizedSummary
        )
    }

    /**
     * İş ilanından anahtar kelimeleri çıkar
     */
    private fun extractKeywords(jobDescription: String): List<String> {
        val commonKeywords = listOf(
            "java", "kotlin", "python", "javascript", "typescript",
            "android", "ios", "react", "angular", "vue",
            "sql", "nosql", "mongodb", "postgresql",
            "aws", "azure", "gcp", "docker", "kubernetes",
            "git", "ci/cd", "agile", "scrum",
            "communication", "teamwork", "problem solving",
            "team player", "leadership", "analytical"
        )

        val jobLower = jobDescription.lowercase()
        return commonKeywords.filter { keyword ->
            jobLower.contains(keyword)
        }.take(10)
    }

    /**
     * ATS dostu özet oluştur
     */
    private fun generateAtsSummary(profile: UserProfile, keywords: List<String>): String {
        val skills = profile.skills.take(5).joinToString(", ")
        val experience = if (profile.experiences.isNotEmpty()) {
            "${profile.experiences.size} yıl deneyim"
        } else {
            "Yeni mezun"
        }

        return "$experience sahibiyim. Uzmanlık alanlarım: $skills. " +
               "Takım çalışmasına yatkın, analitik düşünme yeteneğine sahibim."
    }
}

/**
 * ATS sonuç modeli
 */
data class AtsResult(
    val score: Float,
    val matchedSkills: List<String>,
    val missingSkills: List<String>,
    val recommendations: List<String>,
    val grade: String
)
