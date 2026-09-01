package com.jobhunter.app.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.jobhunter.app.models.Company
import com.jobhunter.app.models.UserProfile
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AiService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("ai_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val PREF_API_KEY = "api_key"
        private const val PREF_API_PROVIDER = "api_provider" // openai, anthropic, custom
    }

    fun setApiKey(key: String, provider: String = "openai") {
        prefs.edit()
            .putString(PREF_API_KEY, key)
            .putString(PREF_API_PROVIDER, provider)
            .apply()
    }

    fun getApiKey(): String = prefs.getString(PREF_API_KEY, "") ?: ""
    fun getProvider(): String = prefs.getString(PREF_API_PROVIDER, "openai") ?: "openai"
    fun isConfigured(): Boolean = getApiKey().isNotEmpty()

    /**
     * Generate a personalized cover letter
     */
    suspend fun generateCoverLetter(
        company: Company,
        profile: UserProfile,
        position: String = "Pozisyon",
        tone: String = "profesyonel"
    ): String = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext generateFallbackCoverLetter(company, profile, position)
        }

        val prompt = buildString {
            // AIHawk'tan ilham alınan gelişmiş prompt yapısı
            appendLine("Compose a brief and impactful cover letter based on the provided job description and resume.")
            appendLine("The letter should be no longer than three paragraphs and should be written in a professional, yet conversational tone.")
            appendLine("Avoid using any placeholders, and ensure that the letter flows naturally and is tailored to the job.")
            appendLine()
            appendLine("Analyze the job description to identify key qualifications and requirements.")
            appendLine("Introduce the candidate succinctly, aligning their career objectives with the role.")
            appendLine("Highlight relevant skills and experiences from the resume that directly match the job's demands,")
            appendLine("using specific examples to illustrate these qualifications.")
            appendLine("Reference notable aspects of the company, such as its mission or values, that resonate with the candidate's professional goals.")
            appendLine("Conclude with a strong statement of why the candidate is a good fit for the position, expressing a desire to discuss further.")
            appendLine()
            appendLine("Lütfen ön yazıyı Türkçe yazın.")
            appendLine()
            appendLine("=== ADAY BİLGİLERİ (RESUME) ===")
            appendLine("Ad: ${profile.firstName} ${profile.lastName}")
            appendLine("E-posta: ${profile.email}")
            appendLine("Özet: ${profile.summary}")
            appendLine("Yetenekler: ${profile.skills.joinToString(", ")}")
            appendLine("Deneyimler:")
            profile.experiences.forEach { exp ->
                appendLine("- ${exp.position} @ ${exp.company} (${exp.startDate} - ${exp.endDate})")
                if (exp.description.isNotEmpty()) {
                    appendLine("  ${exp.description}")
                }
            }
            appendLine("Eğitim:")
            profile.educations.forEach { edu ->
                appendLine("- ${edu.degree} - ${edu.department} @ ${edu.school}")
            }
            appendLine()
            appendLine("=== ŞİRKET BİLGİLERİ (JOB DESCRIPTION) ===")
            appendLine("Şirket: ${company.name}")
            appendLine("Sektör: ${company.industry}")
            appendLine("Açıklama: ${company.description}")
            appendLine("Konum: ${company.location}")
            appendLine()
            appendLine("=== TALİMATLAR ===")
            appendLine("- Profesyonel ama samimi bir ton kullan")
            appendLine("- 3 paragrafı geçme")
            appendLine("- Şirketin değerlerine ve sektörüne uygun ol")
            appendLine("- Adayın deneyimlerini ve yeteneklerini vurgula")
            appendLine("- Somut örnekler kullan")
            appendLine("- Son paragrafta görüşme talep et")
            appendLine("- Greeting veya signature ekleme")
        }

        try {
            val response = callAiApi(prompt)
            response.ifEmpty { generateFallbackCoverLetter(company, profile, position) }
        } catch (e: Exception) {
            generateFallbackCoverLetter(company, profile, position)
        }
    }

    /**
     * Generate a short email for company outreach
     */
    suspend fun generateOutreachEmail(
        company: Company,
        profile: UserProfile,
        purpose: String = "iş başvurusu"
    ): String = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext generateFallbackOutreachEmail(company, profile, purpose)
        }

        val prompt = buildString {
            // AIHawk'tan ilham alınan outreach email promptu
            appendLine("Compose a brief and impactful outreach email for a job application.")
            appendLine("The email should be no longer than one paragraph and should be written in a professional, yet conversational tone.")
            appendLine("Avoid using any placeholders, and ensure that the email flows naturally.")
            appendLine()
            appendLine("Highlight the candidate's most relevant skills and experiences that match the company's needs.")
            appendLine("Reference the company's mission or values that resonate with the candidate's professional goals.")
            appendLine("Conclude with a clear call to action.")
            appendLine()
            appendLine("Lütfen e-postayı Türkçe yazın.")
            appendLine()
            appendLine("=== ŞİRKET BİLGİLERİ ===")
            appendLine("Şirket: ${company.name}")
            appendLine("Sektör: ${company.industry}")
            appendLine("Açıklama: ${company.description}")
            appendLine()
            appendLine("=== ADAY BİLGİLERİ ===")
            appendLine("Ad: ${profile.firstName} ${profile.lastName}")
            appendLine("Yetenekler: ${profile.skills.joinToString(", ")}")
            appendLine("Özet: ${profile.summary}")
            appendLine()
            appendLine("=== AMAÇ ===")
            appendLine(purpose)
        }.toString()

        try {
            val response = callAiApi(prompt)
            response.ifEmpty { generateFallbackOutreachEmail(company, profile, purpose) }
        } catch (e: Exception) {
            generateFallbackOutreachEmail(company, profile, purpose)
        }
    }

    /**
     * Summarize company information
     */
    suspend fun summarizeCompany(company: Company): String = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext "${company.name} - ${company.industry} sektöründe, ${company.location} konumunda faaliyet gösteren bir şirkettir."
        }

        val prompt = """
Şirket: ${company.name}
Sektör: ${company.industry}
Açıklama: ${company.description}
Web: ${company.website}

Bu şirket hakkında kısa bir özet oluştur (2-3 cümle). Türkçe yaz.
        """.trimIndent()

        try {
            val response = callAiApi(prompt)
            response.ifEmpty { "${company.name} - ${company.description}" }
        } catch (e: Exception) {
            "${company.name} - ${company.description}"
        }
    }

    /**
     * Call AI API based on provider - FIXED: proper JSON construction
     */
    private suspend fun callAiApi(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val provider = getProvider()
        val startTime = System.currentTimeMillis()
        logger.logAiCall(provider, prompt.length, 0, false) // Başlangıç logu

        // Use JsonObject for proper JSON construction (prevents injection)
        val requestBody = when (provider) {
            "openai" -> {
                val json = JsonObject().apply {
                    addProperty("model", "gpt-3.5-turbo")
                    addProperty("max_tokens", 1000)
                    addProperty("temperature", 0.7)
                    val messages = com.google.gson.JsonArray().apply {
                        add(JsonObject().apply {
                            addProperty("role", "system")
                            addProperty("content", "Sen profesyonel bir iş başvurusu asistanısın. Türkçe yazıyorsun.")
                        })
                        add(JsonObject().apply {
                            addProperty("role", "user")
                            addProperty("content", prompt)
                        })
                    }
                    add("messages", messages)
                }
                gson.toJson(json)
            }
            "anthropic" -> {
                val json = JsonObject().apply {
                    addProperty("model", "claude-3-haiku-20240307")
                    addProperty("max_tokens", 1000)
                    val messages = com.google.gson.JsonArray().apply {
                        add(JsonObject().apply {
                            addProperty("role", "user")
                            addProperty("content", prompt)
                        })
                    }
                    add("messages", messages)
                }
                gson.toJson(json)
            }
            else -> {
                val json = JsonObject().apply {
                    addProperty("model", "gpt-3.5-turbo")
                    addProperty("max_tokens", 1000)
                    val messages = com.google.gson.JsonArray().apply {
                        add(JsonObject().apply {
                            addProperty("role", "user")
                            addProperty("content", prompt)
                        })
                    }
                    add("messages", messages)
                }
                gson.toJson(json)
            }
        }

        val url = when (provider) {
            "openai" -> "https://api.openai.com/v1/chat/completions"
            "anthropic" -> "https://api.anthropic.com/v1/messages"
            else -> "https://api.openai.com/v1/chat/completions"
        }

        val headerKey = when (provider) {
            "anthropic" -> "x-api-key"
            else -> "Authorization"
        }
        val headerValue = when (provider) {
            "anthropic" -> apiKey
            else -> "Bearer $apiKey"
        }

        val request = Request.Builder()
            .url(url)
            .addHeader(headerKey, headerValue)
            .addHeader("Content-Type", "application/json")
            .addHeader("anthropic-version", "2023-06-01")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        try {
            if (!response.isSuccessful) {
                throw Exception("API hatası: ${response.code} - $responseBody")
            }

            // Parse response based on provider
            val result = when (provider) {
                "openai" -> {
                    val json = JsonParser.parseString(responseBody).asJsonObject
                    val choices = json.getAsJsonArray("choices")
                    val message = choices?.get(0)?.asJsonObject?.getAsJsonObject("message")
                    message?.get("content")?.asString ?: ""
                }
                "anthropic" -> {
                    val json = JsonParser.parseString(responseBody).asJsonObject
                    val content = json.getAsJsonArray("content")
                    content?.get(0)?.asJsonObject?.get("text")?.asString ?: ""
                }
                else -> ""
            }
            
            val duration = System.currentTimeMillis() - startTime
            logger.logAiCall(provider, prompt.length, result.length, true)
            logger.d("AI", "API yanıtı alındı: ${result.length} karakter (${duration}ms)")
            
            result
        } finally {
            response.close() // Important: close response body to prevent memory leak
        }
    }

    /**
     * Fallback cover letter when AI is not configured
     */
    private fun generateFallbackCoverLetter(
        company: Company,
        profile: UserProfile,
        position: String
    ): String {
        return """
Sayın ${company.name} İnsan Kaynakları,

${company.name} bünyesinde görev almak istediğimi belirtmek isterim.

${profile.summary}

Yeteneklerim: ${profile.skills.joinToString(", ")}

Deneyimlerim:
${profile.experiences.joinToString("\n") { "- ${it.position} @ ${it.company} (${it.startDate} - ${it.endDate})" }}

Bu fırsatı değerlendirmenizi saygıyla rica ederim.

Saygılarımla,
${profile.firstName} ${profile.lastName}
${profile.email}
${profile.phone}
        """.trimIndent()
    }

    /**
     * Fallback outreach email
     */
    private fun generateFallbackOutreachEmail(
        company: Company,
        profile: UserProfile,
        purpose: String
    ): String {
        return """
Merhaba,

Ben ${profile.firstName} ${profile.lastName}. ${company.name} hakkında araştırma yaparken şirketinizi buldum ve çok ilgimi çekti.

${profile.summary}

$purpose ile ilgileniyorum. Uygun bir zamanda görüşme fırsatı yaratmanızı rica ederim.

Saygılarımla,
${profile.firstName}
${profile.email}
        """.trimIndent()
    }
}
