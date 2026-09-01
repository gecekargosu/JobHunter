package com.jobhunter.app.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.gson.Gson
import com.jobhunter.app.models.Company
import com.jobhunter.app.models.UserProfile
import com.jobhunter.app.utils.AppLogger
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

/**
 * LinkedIn Entegrasyon Servisi
 * LinkedIn profil bilgilerini okur ve iş ilanlarını tarar
 */
class LinkedInService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("linkedin_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val LINKEDIN_BASE = "https://www.linkedin.com"
        private const val PREF_LINKEDIN_URL = "linkedin_url"
        private const val PREF_IS_CONFIGURED = "is_configured"
    }

    /**
     * LinkedIn URL'sini kaydet
     */
    fun saveLinkedInUrl(url: String) {
        prefs.edit()
            .putString(PREF_LINKEDIN_URL, url)
            .putBoolean(PREF_IS_CONFIGURED, true)
            .apply()
        logger.i("LinkedIn", "LinkedIn URL kaydedildi: $url")
    }

    /**
     * LinkedIn URL'sini getir
     */
    fun getLinkedInUrl(): String {
        return prefs.getString(PREF_LINKEDIN_URL, "") ?: ""
    }

    /**
     * LinkedIn yapılandırılmış mı?
     */
    fun isConfigured(): Boolean {
        return prefs.getBoolean(PREF_IS_CONFIGURED, false)
    }

    /**
     * LinkedIn bağlantısını kes
     */
    fun disconnect() {
        prefs.edit().clear().apply()
        logger.i("LinkedIn", "LinkedIn bağlantısı kesildi")
    }

    /**
     * LinkedIn profil sayfasını aç
     */
    fun openLinkedInProfile() {
        val url = getLinkedInUrl()
        if (url.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            logger.logUserAction("LinkedIn Profil Açma", url)
        }
    }

    /**
     * LinkedIn job search sayfasını aç
     */
    fun openLinkedInJobSearch(keywords: String = "", location: String = "") {
        val query = if (location.isNotEmpty()) "$keywords $location" else keywords
        val url = "$LINKEDIN_BASE/jobs/search/?keywords=${Uri.encode(query)}"
        
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        logger.logUserAction("LinkedIn İş Arama", query)
    }

    /**
     * LinkedIn'den profil bilgilerini çıkarmaya çalış
     * (Not: Gerçek LinkedIn API OAuth gerektirir, bu basit bir scraping)
     */
    suspend fun scrapeLinkedInProfile(url: String): LinkedInProfile? {
        logger.i("LinkedIn", "LinkedIn profili taranıyor: $url")
        val startTime = System.currentTimeMillis()

        return try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .timeout(15000)
                .get()

            // Başlık bilgisi
            val title = doc.title()
            val name = title.split(" - ").firstOrNull()?.trim() ?: ""
            val headline = title.split(" - ").getOrNull(1)?.trim() ?: ""

            // Konum
            val location = doc.selectFirst(".text-body-small.inline.t-black--light.break-words")?.text() ?: ""

            // Özet
            val summary = doc.selectFirst(".pv-about__summary-text")?.text() ?: ""

            val duration = System.currentTimeMillis() - startTime
            logger.i("LinkedIn", "Profil tarandı: $name (${duration}ms)")

            LinkedInProfile(
                name = name,
                headline = headline,
                location = location,
                summary = summary,
                profileUrl = url
            )
        } catch (e: Exception) {
            logger.logError("LinkedIn", "Profil tarama", e.message ?: "Bilinmeyen hata", e)
            null
        }
    }

    /**
     * LinkedIn job ilanını tara
     */
    suspend fun scrapeLinkedInJob(url: String): LinkedInJob? {
        logger.i("LinkedIn", "İş ilanı taranıyor: $url")

        return try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .timeout(15000)
                .get()

            val title = doc.selectFirst("h1")?.text() ?: ""
            val company = doc.selectFirst(".job-details-jobs-unified-top-card__company-name")?.text() ?: ""
            val location = doc.selectFirst(".job-details-jobs-unified-top-card__bullet")?.text() ?: ""
            val description = doc.selectFirst(".job-description__text")?.text() ?: ""

            LinkedInJob(
                title = title,
                company = company,
                location = location,
                description = description,
                jobUrl = url
            )
        } catch (e: Exception) {
            logger.logError("LinkedIn", "İş ilanı tarama", e.message ?: "Bilinmeyen hata", e)
            null
        }
    }

    /**
     * UserProfile'ı LinkedIn formatına dönüştür
     */
    fun profileToLinkedInText(profile: UserProfile): String {
        return buildString {
            appendLine("=== LINKEDIN PROFİL ===")
            appendLine("Ad: ${profile.firstName} ${profile.lastName}")
            appendLine("E-posta: ${profile.email}")
            appendLine("Konum: ${profile.location}")
            appendLine()
            
            if (profile.summary.isNotEmpty()) {
                appendLine("=== ÖZET ===")
                appendLine(profile.summary)
                appendLine()
            }
            
            if (profile.experiences.isNotEmpty()) {
                appendLine("=== DENEYİMLER ===")
                profile.experiences.forEach { exp ->
                    appendLine("${exp.position} @ ${exp.company}")
                    appendLine("${exp.startDate} - ${exp.endDate}")
                    if (exp.description.isNotEmpty()) {
                        appendLine(exp.description)
                    }
                    appendLine()
                }
            }
            
            if (profile.skills.isNotEmpty()) {
                appendLine("=== YETENEKLER ===")
                appendLine(profile.skills.joinToString(", "))
            }
        }
    }
}

/**
 * LinkedIn profil modeli
 */
data class LinkedInProfile(
    val name: String,
    val headline: String,
    val location: String,
    val summary: String,
    val profileUrl: String
)

/**
 * LinkedIn iş ilanı modeli
 */
data class LinkedInJob(
    val title: String,
    val company: String,
    val location: String,
    val description: String,
    val jobUrl: String
)
