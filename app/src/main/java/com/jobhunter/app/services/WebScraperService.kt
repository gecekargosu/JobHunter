package com.jobhunter.app.services

import com.jobhunter.app.models.CompanySearchResult
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class WebScraperService(private val logger: AppLogger? = null) {
    // Rate limiting
    private var lastRequestTime = 0L
    private val minDelayBetweenRequests = 2000L // 2 seconds

    private suspend fun rateLimit() {
        val now = System.currentTimeMillis()
        val timeSinceLastRequest = now - lastRequestTime
        if (timeSinceLastRequest < minDelayBetweenRequests) {
            kotlinx.coroutines.delay(minDelayBetweenRequests - timeSinceLastRequest)
        }
        lastRequestTime = System.currentTimeMillis()
    }

    // Jsoup handles its own HTTP connections internally

    /**
     * Search for companies using Google
     */
    suspend fun searchCompaniesGoogle(
        query: String,
        location: String = "",
        limit: Int = 20
    ): List<CompanySearchResult> = withContext(Dispatchers.IO) {
        logger?.logSearch(query, "Google", 0)
        val results = mutableListOf<CompanySearchResult>()
        val startTime = System.currentTimeMillis()

        try {
            rateLimit()
            val searchQuery = if (location.isNotEmpty()) {
                "$query şirketi $location iletişim email"
            } else {
                "$query şirketi iletişim email"
            }

            val url = "https://www.google.com/search?q=${java.net.URLEncoder.encode(searchQuery, "UTF-8")}&num=$limit&hl=tr"
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36")
                .timeout(15000)
                .get()

            // Extract search results
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
                        val domain = extractDomain(href)
                        val email = extractEmailFromText(snippet)

                        results.add(
                            CompanySearchResult(
                                name = title.replace(" - ", " | ").split("|").firstOrNull()?.trim() ?: title,
                                domain = domain,
                                description = snippet,
                                location = location,
                                industry = "",
                                website = href,
                                emails = if (email.isNotEmpty()) listOf(email) else emptyList(),
                                source = "google"
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Skip this result
                }
            }
        } catch (e: Exception) {
            logger?.logError("SCRAPER", "Google Arama", e.message ?: "Bilinmeyen hata", e)
            e.printStackTrace()
        }

        val duration = System.currentTimeMillis() - startTime
        logger?.logSearch(query, "Google", results.size)
        results.take(limit)
    }

    /**
     * Search for companies using DuckDuckGo (more reliable, less blocking)
     */
    suspend fun searchCompaniesDuckDuckGo(
        query: String,
        limit: Int = 20
    ): List<CompanySearchResult> = withContext(Dispatchers.IO) {
        logger?.logSearch(query, "DuckDuckGo", 0)
        val results = mutableListOf<CompanySearchResult>()
        val startTime = System.currentTimeMillis()

        try {
            rateLimit()
            val searchQuery = "$query company contact email"
            val url = "https://html.duckduckgo.com/html/?q=${java.net.URLEncoder.encode(searchQuery, "UTF-8")}"

            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .timeout(15000)
                .get()

            val searchResults = doc.select("div.result, div.results_links")

            for (result in searchResults) {
                try {
                    val titleElement = result.selectFirst("a.result__a")
                    val snippetElement = result.selectFirst("a.result__snippet")
                    val urlElement = result.selectFirst("a.result__url")

                    val title = titleElement?.text() ?: continue
                    val href = titleElement?.attr("href") ?: ""
                    val snippet = snippetElement?.text() ?: ""
                    val displayUrl = urlElement?.text() ?: ""

                    val domain = extractDomain(if (href.isNotEmpty()) href else displayUrl)
                    val email = extractEmailFromText(snippet)

                    if (domain.isNotEmpty()) {
                        results.add(
                            CompanySearchResult(
                                name = title,
                                domain = domain,
                                description = snippet,
                                location = "",
                                industry = "",
                                website = "https://$domain",
                                emails = if (email.isNotEmpty()) listOf(email) else emptyList(),
                                source = "duckduckgo"
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Skip
                }
            }
        } catch (e: Exception) {
            logger?.logError("SCRAPER", "DuckDuckGo Arama", e.message ?: "Bilinmeyen hata", e)
            e.printStackTrace()
        }

        val duration = System.currentTimeMillis() - startTime
        logger?.logSearch(query, "DuckDuckGo", results.size)
        results.take(limit)
    }

    /**
     * Scrape company website for contact information
     */
    suspend fun scrapeCompanyWebsite(url: String): CompanySearchResult = withContext(Dispatchers.IO) {
        logger?.d("SCRAPER", "Web sitesi taranıyor: $url")
        val defaultResult = CompanySearchResult(
            name = extractDomain(url),
            domain = extractDomain(url),
            description = "",
            location = "",
            industry = "",
            website = url
        )

        try {
            rateLimit()
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .timeout(15000)
                .get()

            val emails = mutableSetOf<String>()
            val phones = mutableSetOf<String>()

            // Extract emails from page
            val pageText = doc.text()
            emails.addAll(extractEmailsFromText(pageText))

            // Check common contact pages
            val contactPaths = listOf("/contact", "/iletisim", "/about", "/hakkimizda", "/biz")
            for (path in contactPaths) {
                try {
                    val contactUrl = if (url.endsWith("/")) "$url${path.drop(1)}" else "$url$path"
                    val contactDoc = Jsoup.connect(contactUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get()

                    val contactText = contactDoc.text()
                    emails.addAll(extractEmailsFromText(contactText))
                    phones.addAll(extractPhonesFromText(contactText))
                } catch (e: Exception) {
                    // Page doesn't exist
                }
            }

            // Extract meta info
            val title = doc.title()
            val description = doc.selectFirst("meta[name=description]")?.attr("content") ?: ""
            val ogImage = doc.selectFirst("meta[property=og:image]")?.attr("content") ?: ""

            defaultResult.copy(
                name = title.split("|").firstOrNull()?.split("-")?.firstOrNull()?.trim() ?: extractDomain(url),
                description = description,
                emails = emails.toList(),
                phone = phones.firstOrNull() ?: ""
            )
        } catch (e: Exception) {
            defaultResult
        }
    }

    /**
     * Extract emails from a webpage
     */
    fun extractEmailsFromText(text: String): Set<String> {
        val emailPattern = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        )
        val matcher = emailPattern.matcher(text)
        val emails = mutableSetOf<String>()

        while (matcher.find()) {
            val email = matcher.group().lowercase()
            // Filter out common false positives
            if (!email.endsWith(".png") && !email.endsWith(".jpg") &&
                !email.endsWith(".gif") && !email.contains("example.com") &&
                !email.contains("email.com") && !email.contains("domain.com")) {
                emails.add(email)
            }
        }

        return emails
    }

    fun extractEmailFromText(text: String): String {
        return extractEmailsFromText(text).firstOrNull() ?: ""
    }

    fun extractPhonesFromText(text: String): Set<String> {
        val phonePattern = Pattern.compile(
            "(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{2,4}"
        )
        val matcher = phonePattern.matcher(text)
        val phones = mutableSetOf<String>()

        while (matcher.find()) {
            val phone = matcher.group().trim()
            if (phone.length >= 10) {
                phones.add(phone)
            }
        }

        return phones
    }

    fun extractDomain(url: String): String {
        return try {
            val cleanUrl = url.replace(Regex("^https?://"), "").replace(Regex("/.*$"), "")
            cleanUrl.replace("www.", "")
        } catch (e: Exception) {
            url
        }
    }
}
