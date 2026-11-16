package com.jobhunter.app.services

import android.content.Context
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

/**
 * Sirket Arastirma Servisi
 * Sirket adindan web sitesi, email, LinkedIn, diger bilgileri cikarir
 */
class CompanyResearchService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    /**
     * Sirket hakkinda bilgi toplar
     */
    suspend fun researchCompany(companyName: String): CompanyInfo = withContext(Dispatchers.IO) {
        val info = CompanyInfo(name = companyName)
        
        try {
            // Google'da sirket ara
            val googleResults = searchGoogle(companyName)
            info.webSites = googleResults.webSites
            info.emails = googleResults.emails
            info.linkedinUrl = googleResults.linkedinUrl
            info.description = googleResults.description
            info.otherJobListings = googleResults.otherJobListings
            
            logger.i("CompanyResearch", "$companyName icin bilgi toplandı: ${info.webSites.size} web sitesi, ${info.emails.size} email")
        } catch (e: Exception) {
            logger.logError("CompanyResearch", "Arastirma hatasi", e.message ?: "Hata", e)
        }
        
        info
    }

    /**
     * Google'da sirket ara
     */
    private fun searchGoogle(companyName: String): CompanyInfo {
        val info = CompanyInfo(name = companyName)
        
        try {
            val searchUrl = "https://www.google.com/search?q=${companyName.replace(" ", "+")}"
            val request = Request.Builder()
                .url(searchUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .addHeader("Accept-Language", "tr-TR,tr;q=0.9")
                .build()
            
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return info
            val doc = Jsoup.parse(html)
            
            // Web sitelerini cikar
            val links = doc.select("a[href]")
            for (link in links) {
                val href = link.attr("abs:href")
                if (href.isNotEmpty() && !href.contains("google.com") && !href.contains("youtube.com")) {
                    if (href.contains(companyName.lowercase().replace(" ", "")) || 
                        link.text().lowercase().contains(companyName.lowercase())) {
                        if (!info.webSites.any { it == href }) {
                            info.webSites.add(href)
                        }
                    }
                }
            }
            
            // LinkedIn URL'si
            val linkedinPattern = Regex("""https?://(?:www\.)?linkedin\.com/company/[^"&\s]+""")
            val linkedinMatches = linkedinPattern.findAll(html)
            for (match in linkedinMatches) {
                if (info.linkedinUrl.isEmpty()) {
                    info.linkedinUrl = match.value
                }
            }
            
            // Email adresleri
            val emailPattern = Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}""")
            val emailMatches = emailPattern.findAll(html)
            for (match in emailMatches) {
                val email = match.value.lowercase()
                if (!email.contains("google.com") && !email.contains("example.com") && 
                    !email.contains("sentry.io") && !info.emails.contains(email)) {
                    info.emails.add(email)
                }
            }
            
            // Aciklama
            val descEl = doc.select(".VwiC3b, .IsZvec, [data-sncf]").first()
            if (descEl != null) {
                info.description = descEl.text().take(500)
            }
            
        } catch (e: Exception) {
            logger.w("CompanyResearch", "Google arama hatasi: ${e.message}")
        }
        
        return info
    }

    data class CompanyInfo(
        val name: String,
        var webSites: MutableList<String> = mutableListOf(),
        var emails: MutableList<String> = mutableListOf(),
        var linkedinUrl: String = "",
        var description: String = "",
        var otherJobListings: MutableList<String> = mutableListOf()
    )
}
