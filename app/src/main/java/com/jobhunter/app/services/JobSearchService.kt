package com.jobhunter.app.services

import com.jobhunter.app.models.JobListing
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Çoklu Kaynak İş Arama Servisi
 */
class JobSearchService(private val logger: AppLogger? = null) {

    private val kariyerNetService = KariyerNetService(logger)

    /**
     * Tüm kaynaklardan eş zamanlı iş ara
     */
    suspend fun searchAllSources(
        query: String,
        location: String = "",
        jobType: String = "",
        experienceLevel: String = "",
        sources: List<JobSource> = JobSource.values().toList(),
        limit: Int = 50
    ): List<JobListing> = coroutineScope {
        logger?.logSearch(query, "Multi-Source", 0)
        val startTime = System.currentTimeMillis()
        val allResults = mutableListOf<JobListing>()

        // Kariyer.net'te ara
        if (sources.contains(JobSource.KARIYER_NET)) {
            val job = async(Dispatchers.IO) {
                try {
                    kariyerNetService.searchJobs(
                        query = query,
                        location = location,
                        jobType = jobType,
                        experienceLevel = experienceLevel,
                        limit = limit / sources.size
                    )
                } catch (e: Exception) {
                    logger?.logError("JobSearch", "Kariyer.net", e.message ?: "Hata", e)
                    emptyList()
                }
            }
            allResults.addAll(job.await())
        }

        // Google'da ara
        if (sources.contains(JobSource.GOOGLE_JOBS)) {
            val job = async(Dispatchers.IO) {
                try {
                    searchGoogleJobs(query, location, limit / sources.size)
                } catch (e: Exception) {
                    logger?.logError("JobSearch", "Google", e.message ?: "Hata", e)
                    emptyList()
                }
            }
            allResults.addAll(job.await())
        }

        // Tekrar edenleri kaldır
        val uniqueResults = allResults.distinctBy {
            "${it.companyName.lowercase()}_${it.title.lowercase()}"
        }

        val duration = System.currentTimeMillis() - startTime
        logger?.i("JobSearch", "Toplam ${uniqueResults.size} benzersiz ilan bulundu (${duration}ms)")

        uniqueResults.take(limit)
    }

    /**
     * Google'da ara
     */
    private suspend fun searchGoogleJobs(
        query: String,
        location: String,
        limit: Int
    ): List<JobListing> = withContext(Dispatchers.IO) {
        val results = mutableListOf<JobListing>()

        try {
            val searchQuery = "$query iş ilanları $location site:kariyer.net OR site:linkedin.com"
            val url = "https://www.google.com/search?q=${java.net.URLEncoder.encode(searchQuery, "UTF-8")}&num=$limit&hl=tr"

            val doc = org.jsoup.Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36")
                .timeout(15000)
                .get()

            val searchResults = doc.select("div.g, div[data-sokoban-container]")

            for (result in searchResults) {
                try {
                    val titleElement = result.selectFirst("h3")
                    val linkElement = result.selectFirst("a[href]")
                    val snippetElement = result.selectFirst("div[data-sncf], span[data-sncf], div.VwiC3b")

                    val title = titleElement?.text() ?: continue
                    val href = linkElement?.attr("href") ?: ""
                    val snippet = snippetElement?.text() ?: ""

                    if (href.isNotEmpty() && href.startsWith("http")) {
                        val company = extractCompanyFromTitle(title)
                        results.add(
                            JobListing(
                                source = "google",
                                title = cleanJobTitle(title),
                                companyName = company,
                                description = snippet,
                                url = href
                            )
                        )
                    }
                } catch (e: Exception) {
                    // atla
                }
            }
        } catch (e: Exception) {
            logger?.logError("JobSearch", "Google Jobs", e.message ?: "Hata", e)
        }

        results.take(limit)
    }

    private fun extractCompanyFromTitle(title: String): String {
        val separators = listOf(" - ", " | ", " @ ", " / ", " • ")
        for (sep in separators) {
            if (title.contains(sep)) {
                return title.split(sep).lastOrNull()?.trim() ?: ""
            }
        }
        return ""
    }

    private fun cleanJobTitle(title: String): String {
        val separators = listOf(" - ", " | ", " @ ", " / ", " • ")
        for (sep in separators) {
            if (title.contains(sep)) {
                return title.split(sep).firstOrNull()?.trim() ?: title
            }
        }
        return title
    }

    fun getPopularSearches(): List<String> {
        return listOf(
            "yazılım geliştirici", "yapay zeka", "backend developer",
            "frontend developer", "mobile developer", "full stack",
            "data scientist", "devops", "proje yöneticisi",
            "part time yazılım", "uzaktan çalışma", "stajyer yazılım"
        )
    }
}

/**
 * İş ilanı kaynakları
 */
enum class JobSource(val displayName: String, val emoji: String) {
    KARIYER_NET("Kariyer.net", "🏢"),
    GOOGLE_JOBS("Google Jobs", "🔍"),
    LINKEDIN("LinkedIn", "💼"),
    INDEED("Indeed", "📋")
}
