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
 * Platform Bazlı İş İlanı Çekme Servisi
 * Gerçek veri çeken scraping sistemi
 */
class PlatformJobSearchService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    data class JobListing(
        val id: String,
        val title: String,
        val company: String,
        val location: String,
        val description: String = "",
        val url: String,
        val salary: String = "",
        val date: String = "",
        val source: String,
        val logoUrl: String = "",
        val jobType: String = "",
        val experience: String = "",
        val isRemote: Boolean = false,
        val email: String = ""
    )

    suspend fun searchAllPlatforms(
        query: String,
        location: String = "",
        jobType: String = ""
    ): Map<String, List<JobListing>> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, List<JobListing>>()

        val kariyerResults = try { searchKariyerNet(query, location) } catch (e: Exception) { 
            logger.w("PlatformSearch", "Kariyer.net hatası: ${e.message}")
            emptyList() 
        }
        results["Kariyer.net"] = kariyerResults

        val indeedResults = try { searchIndeed(query, location) } catch (e: Exception) { 
            logger.w("PlatformSearch", "Indeed hatası: ${e.message}")
            emptyList() 
        }
        results["Indeed"] = indeedResults

        val linkedinResults = try { searchLinkedIn(query, location) } catch (e: Exception) { 
            logger.w("PlatformSearch", "LinkedIn hatası: ${e.message}")
            emptyList() 
        }
        results["LinkedIn"] = linkedinResults

        val googleResults = try { searchGoogleJobs(query, location) } catch (e: Exception) { 
            logger.w("PlatformSearch", "Google Jobs hatası: ${e.message}")
            emptyList() 
        }
        results["Google Jobs"] = googleResults

        // Eğer hiçbir sonuç yoksa Google ile fallback
        if (results.values.all { it.isEmpty() }) {
            val fallbackResults = try { searchGoogleFallback(query, location) } catch (e: Exception) {
                logger.w("PlatformSearch", "Fallback hatası: ${e.message}")
                emptyList()
            }
            if (fallbackResults.isNotEmpty()) {
                results["Google Jobs"] = fallbackResults
            }
        }

        logger.i("PlatformSearch", "Arama tamamlandı: Kariyer=${kariyerResults.size}, Indeed=${indeedResults.size}, LinkedIn=${linkedinResults.size}, Google=${googleResults.size}")
        results
    }

    suspend fun searchPlatform(
        platform: String,
        query: String,
        location: String = ""
    ): List<JobListing> = withContext(Dispatchers.IO) {
        when (platform.lowercase()) {
            "kariyer", "kariyer.net" -> searchKariyerNet(query, location)
            "indeed" -> searchIndeed(query, location)
            "linkedin" -> searchLinkedIn(query, location)
            "google", "google jobs" -> searchGoogleJobs(query, location)
            else -> emptyList()
        }
    }

    // ===================================================================
    // KARIYER.NET - Regex tabanlı çıkarma (Jsoup değil!)
    // ===================================================================
    private fun searchKariyerNet(query: String, location: String): List<JobListing> {
        val jobs = mutableListOf<JobListing>()
        try {
            val searchUrl = "https://www.kariyer.net/is-ilanlari?q=${query.replace(" ", "+")}"

            val request = Request.Builder()
                .url(searchUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Language", "tr-TR,tr;q=0.9,en-US;q=0.8")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return jobs

            // Pozisyon isimlerini çıkar (positionName attribute'undan)
            val positions = Regex("""positionName="([^"]+)""").findAll(html).map { it.groupValues[1] }.toList()
            
            // Linkleri çıkar (/is-ilani/ ile başlayan)
            val links = Regex("""href="(/is-ilani/[^"]+)"""").findAll(html).map { it.groupValues[1] }.toList()
            
            // Konumları çıkar (location class'ından)
            val locations = Regex("""class="[^"]*location[^"]*"[^>]*>([^<]+)<""").findAll(html).map { it.groupValues[1].trim() }.toList()

            // Eşleştir: pozisyon + link + konum
            val count = minOf(positions.size, links.size)
            for (i in 0 until count) {
                val title = positions[i]
                val link = "https://www.kariyer.net${links[i]}"
                
                // Linkten şirket adını çıkar
                val slug = links[i].split("/").lastOrNull() ?: ""
                val companyFromSlug = slug.split("-").dropLast(1).joinToString(" ").replaceFirstChar { it.uppercase() }
                
                // Konum (eşleşme varsa)
                val loc = if (i < locations.size) locations[i] else "Türkiye"

                if (title.isNotBlank() && title.length > 2) {
                    jobs.add(JobListing(
                        id = "kariyer_${i}_${System.currentTimeMillis()}",
                        title = title,
                        company = companyFromSlug.ifEmpty { "Bilinmeyen Şirket" },
                        location = loc,
                        url = link,
                        source = "Kariyer.net"
                    ))
                }
            }

            logger.i("KariyerNet", "${jobs.size} iş ilanı çekildi (pozisyon=${positions.size}, link=${links.size})")
        } catch (e: Exception) {
            logger.logError("KariyerNet", "Scraping", e.message ?: "Hata", e)
        }
        return jobs
    }

    // ===================================================================
    // INDEED - Regex tabanlı çıkarma
    // ===================================================================
    private fun searchIndeed(query: String, location: String): List<JobListing> {
        val jobs = mutableListOf<JobListing>()
        try {
            val searchUrl = "https://tr.indeed.com/jobs?q=${query.replace(" ", "+")}&l=${location.replace(" ", "+")}"

            val request = Request.Builder()
                .url(searchUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Language", "tr-TR,tr;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return jobs

            // Indeed job card'ları regex ile çıkar
            // Başlıklar: <a id="job_..." class="..." ...>TITLE</a>
            val titlePattern = Regex("""<a[^>]*id="job_[^"]*"[^>]*>([^<]+)</a>""")
            val titles = titlePattern.findAll(html).map { it.groupValues[1].trim() }.toList()
            
            // Linkler: href="/rc/clk?jk=..."
            val linkPattern = Regex("""href="(/rc/clk\?jk=[^"]+)"""")
            val links = linkPattern.findAll(html).map { it.groupValues[1] }.toList()
            
            // Şirketler: data-testid="company-name"
            val companyPattern = Regex("""data-testid="company-name"[^>]*>([^<]+)<""")
            val companies = companyPattern.findAll(html).map { it.groupValues[1].trim() }.toList()
            
            // Konumlar: data-testid="text-location"
            val locPattern = Regex("""data-testid="text-location"[^>]*>([^<]+)<""")
            val locations = locPattern.findAll(html).map { it.groupValues[1].trim() }.toList()

            val count = minOf(titles.size, links.size)
            for (i in 0 until count) {
                val title = titles[i]
                val link = "https://tr.indeed.com${links[i]}"
                val company = if (i < companies.size) companies[i] else "Bilinmeyen Şirket"
                val loc = if (i < locations.size) locations[i] else location.ifEmpty { "Türkiye" }

                if (title.isNotBlank() && title.length > 2) {
                    jobs.add(JobListing(
                        id = "indeed_${i}_${System.currentTimeMillis()}",
                        title = title,
                        company = company,
                        location = loc,
                        url = link,
                        source = "Indeed"
                    ))
                }
            }

            // Eğer regex ile bulunamadıysa Jsoup dene
            if (jobs.isEmpty()) {
                val doc = Jsoup.parse(html)
                val jobCards = doc.select("div.job_seen_beacon, div.jobsearch-SerpJobCard")
                for ((index, card) in jobCards.withIndex()) {
                    val titleEl = card.select("h2 a, .jobTitle a").first()
                    val companyEl = card.select(".companyName, .company").first()
                    val locationEl = card.select(".companyLocation, .location").first()
                    val linkEl = titleEl?.attr("href") ?: ""

                    val title = titleEl?.text()?.trim() ?: continue
                    val company = companyEl?.text()?.trim() ?: "Bilinmeyen Şirket"
                    val loc = locationEl?.text()?.trim() ?: "Belirtilmemiş"

                    if (title.isNotBlank()) {
                        jobs.add(JobListing(
                            id = "indeed_jsoup_${index}_${System.currentTimeMillis()}",
                            title = title,
                            company = company,
                            location = loc,
                            url = if (linkEl.startsWith("http")) linkEl else "https://tr.indeed.com$linkEl",
                            source = "Indeed"
                        ))
                    }
                }
            }

            logger.i("Indeed", "${jobs.size} iş ilanı çekildi")
        } catch (e: Exception) {
            logger.logError("Indeed", "Scraping", e.message ?: "Hata", e)
        }
        return jobs
    }

    // ===================================================================
    // LINKEDIN - Public job listings
    // ===================================================================
    private fun searchLinkedIn(query: String, location: String): List<JobListing> {
        val jobs = mutableListOf<JobListing>()
        try {
            val searchUrl = "https://www.linkedin.com/jobs/search/?keywords=${query.replace(" ", "+")}&location=${location.replace(" ", "+")}"

            val request = Request.Builder()
                .url(searchUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Language", "tr-TR,tr;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return jobs

            // LinkedIn public job cards - regex ile
            val titlePattern = Regex("""<h3[^>]*class="[^"]*base-card__title[^"]*"[^>]*>\s*<a[^>]*>([^<]+)</a>""")
            val titles = titlePattern.findAll(html).map { it.groupValues[1].trim() }.toList()
            
            val linkPattern = Regex("""<a[^>]*href="(https://www\.linkedin\.com/jobs/view/[^"]+)"[^>]*class="[^"]*base-card__full-link""")
            val links = linkPattern.findAll(html).map { it.groupValues[1] }.toList()
            
            val companyPattern = Regex("""<h4[^>]*class="[^"]*base-card__subtitle[^"]*"[^>]*>\s*<a[^>]*>([^<]+)</a>""")
            val companies = companyPattern.findAll(html).map { it.groupValues[1].trim() }.toList()
            
            val locPattern = Regex("""<span[^>]*class="[^"]*job-search-card__location[^"]*"[^>]*>([^<]+)<""")
            val locations = locPattern.findAll(html).map { it.groupValues[1].trim() }.toList()

            val count = minOf(titles.size, links.size)
            for (i in 0 until count) {
                val title = titles[i]
                val link = links[i]
                val company = if (i < companies.size) companies[i] else "Bilinmeyen Şirket"
                val loc = if (i < locations.size) locations[i] else "Belirtilmemiş"

                if (title.isNotBlank()) {
                    jobs.add(JobListing(
                        id = "linkedin_${i}_${System.currentTimeMillis()}",
                        title = title,
                        company = company,
                        location = loc,
                        url = link,
                        source = "LinkedIn"
                    ))
                }
            }

            // Jsoup fallback
            if (jobs.isEmpty()) {
                val doc = Jsoup.parse(html)
                val jobCards = doc.select("div.job-search-card, li.jobs-search__result-card")
                for ((index, card) in jobCards.withIndex()) {
                    val titleEl = card.select("h3 a, .base-search-card__title a").first()
                    val companyEl = card.select("h4, .base-search-card__subtitle").first()
                    val locationEl = card.select(".job-search-card__location").first()
                    val linkEl = titleEl?.attr("href") ?: ""

                    val title = titleEl?.text()?.trim() ?: continue
                    val company = companyEl?.text()?.trim() ?: "Bilinmeyen Şirket"
                    val loc = locationEl?.text()?.trim() ?: "Belirtilmemiş"

                    if (title.isNotBlank()) {
                        jobs.add(JobListing(
                            id = "linkedin_jsoup_${index}_${System.currentTimeMillis()}",
                            title = title,
                            company = company,
                            location = loc,
                            url = linkEl,
                            source = "LinkedIn"
                        ))
                    }
                }
            }

            logger.i("LinkedIn", "${jobs.size} iş ilanı çekildi")
        } catch (e: Exception) {
            logger.logError("LinkedIn", "Scraping", e.message ?: "Hata", e)
        }
        return jobs
    }

    // ===================================================================
    // GOOGLE JOBS - Google arama sonuçlarından iş ilanları
    // ===================================================================
    private fun searchGoogleJobs(query: String, location: String): List<JobListing> {
        val jobs = mutableListOf<JobListing>()
        try {
            val searchQuery = "$query iş ilanları ${location}".replace(" ", "+")
            val searchUrl = "https://www.google.com/search?q=$searchQuery&ibp=htl;jobs"

            val request = Request.Builder()
                .url(searchUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Language", "tr-TR,tr;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return jobs

            // Google Jobs JSON verisini bul
            val jsonPattern = Regex("""\{"jobTitle":"([^"]+)","companyName":"([^"]+)","location":"([^"]*)".*?"searchUrl":"([^"]*)"}""")
            val jsonMatches = jsonPattern.findAll(html)
            for ((index, match) in jsonMatches.withIndex()) {
                jobs.add(JobListing(
                    id = "google_json_${index}_${System.currentTimeMillis()}",
                    title = match.groupValues[1],
                    company = match.groupValues[2],
                    location = match.groupValues[3].ifEmpty { "Belirtilmemiş" },
                    url = match.groupValues[4].ifEmpty { "https://www.google.com/search?q=${query}+jobs" },
                    source = "Google Jobs"
                ))
            }

            // JSON bulunamadıysa HTML parser
            if (jobs.isEmpty()) {
                val doc = Jsoup.parse(html)
                val jobCards = doc.select("div.iFjolb, div[data-ved], li.iFjolb")
                for ((index, card) in jobCards.withIndex()) {
                    val titleEl = card.select("h3, .BjJfJf, .MBeuO").first()
                    val companyEl = card.select(".vNEEBe, .KKh10d, .nJlQNd").first()
                    val locationEl = card.select(".Qk80Jf, .YMlIz").first()

                    val title = titleEl?.text()?.trim() ?: continue
                    val company = companyEl?.text()?.trim() ?: "Bilinmeyen Şirket"
                    val loc = locationEl?.text()?.trim() ?: "Belirtilmemiş"

                    if (title.isNotBlank()) {
                        jobs.add(JobListing(
                            id = "google_jsoup_${index}_${System.currentTimeMillis()}",
                            title = title,
                            company = company,
                            location = loc,
                            url = "https://www.google.com/search?q=${title}+${company}+jobs",
                            source = "Google Jobs"
                        ))
                    }
                }
            }

            logger.i("GoogleJobs", "${jobs.size} iş ilanı çekildi")
        } catch (e: Exception) {
            logger.logError("GoogleJobs", "Scraping", e.message ?: "Hata", e)
        }
        return jobs
    }

    // ===================================================================
    // GOOGLE FALLBACK - Tüm platformlardan sonuç yoksa
    // ===================================================================
    private fun searchGoogleFallback(query: String, location: String): List<JobListing> {
        val jobs = mutableListOf<JobListing>()
        try {
            val searchQuery = "$query iş ilanları ${location}".replace(" ", "+")
            val searchUrl = "https://www.google.com/search?q=$searchQuery"

            val request = Request.Builder()
                .url(searchUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Language", "tr-TR,tr;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return jobs

            val doc = Jsoup.parse(html)
            val resultCards = doc.select("div.g, div[data-sokoban-container]")
            val jobSites = listOf("kariyer.net", "linkedin.com", "indeed.com", "secretcv.com", "eleman.net", "isbul.net", "yenibiris.com")

            for ((index, card) in resultCards.withIndex()) {
                val titleEl = card.select("h3").first()
                val linkEl = card.select("a[href]").first()
                val snippetEl = card.select(".VwiC3b, .IsZvec").first()

                val title = titleEl?.text()?.trim() ?: continue
                val link = linkEl?.attr("abs:href") ?: ""
                val snippet = snippetEl?.text()?.trim() ?: ""

                val isJobSite = jobSites.any { link.lowercase().contains(it) }

                if (title.isNotBlank() && link.isNotBlank() && isJobSite) {
                    val source = jobSites.firstOrNull { link.lowercase().contains(it) } ?: "Google"
                    jobs.add(JobListing(
                        id = "google_fallback_${index}_${System.currentTimeMillis()}",
                        title = title,
                        company = source,
                        location = location.ifEmpty { "Türkiye" },
                        description = snippet,
                        url = link,
                        source = "Google Jobs"
                    ))
                }
            }

            logger.i("GoogleFallback", "${jobs.size} iş ilanı çekildi")
        } catch (e: Exception) {
            logger.logError("GoogleFallback", "Scraping", e.message ?: "Hata", e)
        }
        return jobs
    }
}
