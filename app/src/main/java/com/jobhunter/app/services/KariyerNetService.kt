package com.jobhunter.app.services

import com.jobhunter.app.models.JobListing
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Kariyer.net İş İlanı Tarama Servisi
 */
class KariyerNetService(private val logger: AppLogger? = null) {

    companion object {
        private const val BASE_URL = "https://www.kariyer.net"
        private const val SEARCH_URL = "$BASE_URL/is-ilanlari"
        private const val API_URL = "https://candidatewebapigw.kariyer.net/api/v1/search/job"
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36"
        private const val REQUEST_DELAY = 3000L
    }

    private var lastRequestTime = 0L
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private suspend fun rateLimit() {
        val now = System.currentTimeMillis()
        val timeSinceLastRequest = now - lastRequestTime
        if (timeSinceLastRequest < REQUEST_DELAY) {
            kotlinx.coroutines.delay(REQUEST_DELAY - timeSinceLastRequest)
        }
        lastRequestTime = System.currentTimeMillis()
    }

    /**
     * Kariyer.net'te iş ara
     */
    suspend fun searchJobs(
        query: String,
        location: String = "",
        jobType: String = "",
        experienceLevel: String = "",
        page: Int = 1,
        limit: Int = 20
    ): List<JobListing> = withContext(Dispatchers.IO) {
        logger?.logSearch(query, "Kariyer.net", 0)
        val results = mutableListOf<JobListing>()
        val startTime = System.currentTimeMillis()

        // Önce API'yi dene
        try {
            rateLimit()
            val apiResults = searchViaApi(query, location, page, limit)
            if (apiResults.isNotEmpty()) {
                logger?.i("KariyerNet", "API ile ${apiResults.size} ilan bulundu")
                return@withContext apiResults
            }
        } catch (e: Exception) {
            logger?.d("KariyerNet", "API başarısız, HTML scraping deneniyor: ${e.message}")
        }

        // API çalışmazsa HTML scraping
        try {
            rateLimit()
            val searchParams = buildString {
                append("?q=${java.net.URLEncoder.encode(query, "UTF-8")}")
                if (location.isNotEmpty()) {
                    append("&city=${java.net.URLEncoder.encode(location, "UTF-8")}")
                }
                append("&page=$page")
            }
            val url = "$SEARCH_URL$searchParams"

            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(15000)
                .get()

            // data-test attribute'larını kullanarak ilanları bul
            val jobCards = doc.select("div[data-test=ad-card], a[data-test=ad-card-item]")

            for (card in jobCards) {
                try {
                    val title = card.select("span[data-test=ad-card-title]").text()
                    val company = card.select("span[data-test=subtitle]").text()
                    val locationText = card.select("span[data-test=location]").text()
                    val workType = card.select("span[data-test=text]").text()
                    val href = card.attr("href")

                    if (title.isNotEmpty()) {
                        val fullUrl = if (href.startsWith("http")) href
                        else if (href.startsWith("/")) "$BASE_URL$href"
                        else href

                        results.add(
                            JobListing(
                                source = "kariyer.net",
                                title = title,
                                companyName = company,
                                location = locationText,
                                jobType = workType,
                                url = fullUrl
                            )
                        )
                    }
                } catch (e: Exception) {
                    logger?.d("KariyerNet", "İlan ayrıştırma hatası: ${e.message}")
                }
            }

            logger?.i("KariyerNet", "HTML scraping: ${results.size} ilan bulundu")
            val duration = System.currentTimeMillis() - startTime
            logger?.logSearch(query, "Kariyer.net", results.size)

        } catch (e: Exception) {
            logger?.logError("KariyerNet", "Arama", e.message ?: "Hata", e)
        }

        results.take(limit)
    }

    /**
     * API ile ara
     */
    private fun searchViaApi(query: String, location: String, page: Int, limit: Int): List<JobListing> {
        val results = mutableListOf<JobListing>()

        val jsonBody = JSONObject().apply {
            put("query", query)
            if (location.isNotEmpty()) put("city", location)
            put("page", page)
            put("pageSize", limit)
        }.toString()

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", USER_AGENT)
            .addHeader("Accept", "application/json")
            .addHeader("Origin", "https://www.kariyer.net")
            .addHeader("Referer", "https://www.kariyer.net/is-ilanlari")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (response.isSuccessful) {
            val json = JSONObject(responseBody)
            val jobArray = json.optJSONArray("data")
                ?: json.optJSONArray("jobs")
                ?: json.optJSONArray("items")

            if (jobArray != null) {
                for (i in 0 until jobArray.length()) {
                    try {
                        val job = jobArray.getJSONObject(i)
                        results.add(
                            JobListing(
                                source = "kariyer.net",
                                title = job.optString("title", job.optString("positionName", "")),
                                companyName = job.optString("companyName", job.optString("employer", "")),
                                location = job.optString("city", job.optString("location", "")),
                                jobType = job.optString("workType", job.optString("employmentType", "")),
                                url = "$BASE_URL${job.optString("url", job.optString("slug", ""))}",
                                description = job.optString("description", job.optString("snippet", ""))
                            )
                        )
                    } catch (e: Exception) {
                        // atla
                    }
                }
            }
        }

        return results
    }

    /**
     * Popüler arama kelimeleri
     */
    fun getPopularSearches(): List<String> {
        return listOf(
            "yazılım geliştirici",
            "yapay zeka",
            "backend developer",
            "frontend developer",
            "mobile developer",
            "full stack",
            "data scientist",
            "devops",
            "proje yöneticisi",
            "product manager",
            "part time yazılım",
            "uzaktan çalışma",
            "stajyer yazılım",
            "junior developer",
            "senior developer"
        )
    }

    fun getJobTypes(): List<Pair<String, String>> {
        return listOf(
            "full-time" to "Tam Zamanlı",
            "part-time" to "Yarı Zamanlı",
            "internship" to "Staj",
            "freelance" to "Serbest Çalışma",
            "remote" to "Uzaktan"
        )
    }

    fun getExperienceLevels(): List<Pair<String, String>> {
        return listOf(
            "junior" to "Junior (0-2 yıl)",
            "mid" to "Mid (2-5 yıl)",
            "senior" to "Senior (5+ yıl)",
            "lead" to "Lead / Team Lead"
        )
    }
}
