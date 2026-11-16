package com.jobhunter.app.services

import android.content.Context
import android.net.Uri
import com.jobhunter.app.utils.AppLogger
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * CV Okuma ve Bilgi Cikarma Servisi
 * PDF ve text dosyalarindan bilgi cikarir
 */
class CvReaderService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)

    init {
        // PDFBox resource loader init
        PDFBoxResourceLoader.init(context)
    }

    /**
     * PDF dosyasindan metin cikarir (PDFBox ile gercek PDF okuma)
     */
    suspend fun extractTextFromPdf(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext ""

            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            inputStream.close()

            logger.i("CvReader", "PDF'den ${text.length} karakter metin cikarildi")
            text
        } catch (e: Exception) {
            logger.logError("CvReader", "PDF okuma hatasi", e.message ?: "Hata", e)
            // Fallback:PdfRenderer ile sayfalari goruntu olarak ac
            extractTextFromPdfFallback(uri)
        }
    }

    /**
     * Fallback: PDFBox calismazsa basit cozum
     */
    private fun extractTextFromPdfFallback(uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
            // PDF dosyasini oldugu gibi okumaya calis
            val bytes = inputStream.readBytes()
            inputStream.close()

            // PDF icindeki metinleri regex ile cikar
            val text = String(bytes, Charsets.ISO_8859_1)
            val extractedTexts = mutableListOf<String>()

            // PDF text stream pattern
            val patterns = listOf(
                Regex("""BT\s*\n(.*?)\s*ET""", RegexOption.DOT_MATCHES_ALL),
                Regex("""\((.*?)\)"""),
                Regex("""<([0-9A-Fa-f]+)>""")
            )

            for (pattern in patterns) {
                val matches = pattern.findAll(text)
                for (match in matches) {
                    val t = match.groupValues[1]
                        .replace(Regex("""\\[()\\]"""), "")
                        .trim()
                    if (t.length > 2 && t.any { it.isLetter() }) {
                        extractedTexts.add(t)
                    }
                }
            }

            val result = extractedTexts.joinToString(" ")
            if (result.isNotEmpty()) {
                logger.i("CvReader", "Fallback ile ${result.length} karakter cikarildi")
                result
            } else {
                "[PDF dosyasindan metin cikarilamadi. Lutfen .txt olarak kaydedip yukleyin]"
            }
        } catch (e: Exception) {
            "[PDF okuma hatasi: ${e.message}]"
        }
    }

    /**
     * Text dosyasindan icerik okur
     */
    suspend fun extractTextFromFile(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: ""
        } catch (e: Exception) {
            logger.logError("CvReader", "Dosya okuma hatasi", e.message ?: "Hata", e)
            ""
        }
    }

    /**
     * Dosya turune gore metin cikarir
     */
    suspend fun extractText(uri: Uri, mimeType: String): String {
        return when {
            mimeType.contains("pdf") -> extractTextFromPdf(uri)
            mimeType.contains("text") -> extractTextFromFile(uri)
            mimeType.contains("image") -> "[Goruntu dosyasi - metin cikarilamadi]"
            else -> extractTextFromFile(uri)
        }
    }

    /**
     * CV metninden bilgi cikarir
     */
    fun parseCvInfo(cvText: String): CvInfo {
        val lines = cvText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        val skills = extractSkills(cvText)
        val experiences = extractExperiences(lines)
        val educations = extractEducations(lines)
        val summary = extractSummary(lines)

        return CvInfo(
            fullText = cvText,
            skills = skills,
            experiences = experiences,
            educations = educations,
            summary = summary
        )
    }

    private fun extractSkills(text: String): List<String> {
        val allSkills = mutableListOf<String>()

        val knownSkills = listOf(
            // Programlama
            "Java", "Kotlin", "Python", "JavaScript", "TypeScript", "C\\+\\+", "C#",
            "PHP", "Ruby", "Go", "Rust", "Swift", "Dart", "Scala", "R",
            // Frameworkler
            "React", "Angular", "Vue", "Node\\.js", "Django", "Flask", "Spring",
            "Android", "iOS", "Flutter", "React Native", "Next\\.js", "Nuxt",
            // Veritabani
            "MySQL", "PostgreSQL", "MongoDB", "Redis", "SQLite", "Firebase",
            "Oracle", "SQL Server", "DynamoDB", "Cassandra",
            // DevOps
            "Docker", "Kubernetes", "AWS", "Azure", "GCP", "Jenkins", "Git",
            "Linux", "CI/CD", "Terraform", "Ansible",
            // Diger
            "HTML", "CSS", "SASS", "REST API", "GraphQL", "Microservices",
            "Agile", "Scrum", "Jira", "Confluence",
            // Turkce
            "yazilim", "gelistirici", "muhendis", "proje yonetimi", "takim calismasi",
            "iletisim", "problem cozme", "analitik dusunme",
            // Pazarlama
            "SEO", "SEM", "Google Ads", "Facebook Ads", "Instagram", "TikTok",
            "dijital pazarlama", "icerik uretimi", "sosyal medya",
            // Tasarim
            "Figma", "Adobe", "Photoshop", "Illustrator", "UI", "UX",
            // Veri
            "Excel", "Power BI", "Tableau", "SQL", "Python", "R",
            "machine learning", "yapay zeka", "data science", "istatistik",
            // Muhasebe
            "SAP", "QuickBooks", "muhasebe", "finans",
            // Diger
            "ingilizce", "almanca", "fransizca", "yabanci dil",
            "liderlik"
        )

        for (skill in knownSkills) {
            if (Regex(skill, RegexOption.IGNORE_CASE).containsMatchIn(text)) {
                allSkills.add(skill.replace("\\", ""))
            }
        }

        return allSkills.distinct()
    }

    private fun extractExperiences(lines: List<String>): List<String> {
        val experiences = mutableListOf<String>()
        var inExperienceSection = false

        val expHeaders = listOf("deneyim", "experience", "work", "is tecrubes", "is deneyimi", "professional", "isci tecrube")

        for (line in lines) {
            val lower = line.lowercase()

            if (expHeaders.any { lower.contains(it) }) {
                inExperienceSection = true
                continue
            }

            if (inExperienceSection) {
                val sectionHeaders = listOf("egitim", "education", "yetenek", "skills", "sertifika", "certificate", "proje", "project", "referans")
                if (sectionHeaders.any { lower.contains(it) }) {
                    break
                }

                if (Regex("""\d{4}""").containsMatchIn(line) ||
                    Regex("""(ocak|subat|mart|nisan|mayis|haziran|temmuz|agustos|eylul|ekim|kasim|aralik|jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|current|present|gunumuz)""", RegexOption.IGNORE_CASE).containsMatchIn(line)) {
                    experiences.add(line)
                }
            }
        }

        return experiences
    }

    private fun extractEducations(lines: List<String>): List<String> {
        val educations = mutableListOf<String>()
        var inEduSection = false

        val eduHeaders = listOf("egitim", "education", "okul", "school", "universite", "university", "lise", "fakulte", "lisans")

        for (line in lines) {
            val lower = line.lowercase()

            if (eduHeaders.any { lower.contains(it) }) {
                inEduSection = true
                continue
            }

            if (inEduSection) {
                val sectionHeaders = listOf("deneyim", "experience", "yetenek", "skills", "sertifika", "proje")
                if (sectionHeaders.any { lower.contains(it) }) break

                if (line.length > 5) {
                    educations.add(line)
                }
            }
        }

        return educations
    }

    private fun extractSummary(lines: List<String>): String {
        val summaryHeaders = listOf("ozet", "summary", "hakkinda", "about", "profile", "bio", "tanitim")

        for (i in lines.indices) {
            val lower = lines[i].lowercase()
            if (summaryHeaders.any { lower.contains(it) }) {
                // Bir sonraki satiri ozet olarak al
                val summary = lines.drop(i + 1).take(5).joinToString(" ")
                if (summary.length > 20) {
                    return summary
                }
            }
        }

        return ""
    }

    data class CvInfo(
        val fullText: String,
        val skills: List<String>,
        val experiences: List<String>,
        val educations: List<String>,
        val summary: String
    )
}
