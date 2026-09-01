package com.jobhunter.app.services

import android.content.Context
import com.google.gson.Gson
import com.jobhunter.app.models.Company
import com.jobhunter.app.models.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AiService(private val context: Context) {

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
            appendLine("Aşağıdaki bilgilere göre profesyonel bir ön yazı oluştur:")
            appendLine()
            appendLine("=== ADAY BİLGİLERİ ===")
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
            appendLine("=== ŞİRKET BİLGİLERİ ===")
            appendLine("Şirket: ${company.name}")
            appendLine("Sektör: ${company.industry}")
            appendLine("Açıklama: ${company.description}")
            appendLine("Konum: ${company.location}")
            appendLine()
            appendLine("=== TALİMATLAR ===")
            appendLine("Ton: $tone")
            appendLine("Dil: Türkçe")
            appendLine("Ön yazı 3-4 paragraf olmalı")
            appendLine("Şirketin sektörüne ve değerlerine uygun olmalı")
            appendLine("Adayın deneyimlerini ve yeteneklerini vurgulamalı")
            appendLine("Sonuç paragrafında görüşme talep edilmeli")
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

        val prompt = """
Şirket: ${company.name}
Aday: ${profile.firstName} ${profile.lastName}
Yetenekler: ${profile.skills.joinToString(", ")}
Amaç: $purpose

Kısa, profesyonel ve etkileyici bir e-posta oluştur. 
1 paragraf olsun. Türkçe yaz.
        """.trimIndent()

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
     * Call AI API based on provider
     */
    private suspend fun callAiApi(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val provider = getProvider()

        val requestBody = when (provider) {
            "openai" -> {
                """
                {
                    "model": "gpt-3.5-turbo",
                    "messages": [
                        {"role": "system", "content": "Sen profesyonel bir iş başvurusu asistanısın. Türkçe yazıyorsun."},
                        {"role": "user", "content": ${gson.toJson(prompt)}}
                    ],
                    "max_tokens": 1000,
                    "temperature": 0.7
                }
                """.trimIndent()
            }
            "anthropic" -> {
                """
                {
                    "model": "claude-3-haiku-20240307",
                    "max_tokens": 1000,
                    "messages": [
                        {"role": "user", "content": ${gson.toJson(prompt)}}
                    ]
                }
                """.trimIndent()
            }
            else -> {
                """
                {
                    "prompt": ${gson.toJson(prompt)},
                    "max_tokens": 1000
                }
                """.trimIndent()
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
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("API error: ${response.code} - $responseBody")
        }

        // Parse response based on provider
        when (provider) {
            "openai" -> {
                val json = gson.fromJson(responseBody, Map::class.java)
                val choices = json["choices"] as? List<*>
                val message = choices?.firstOrNull() as? Map<*, *>
                val content = message?.get("message") as? Map<*, *>
                content?.get("content") as? String ?: ""
            }
            "anthropic" -> {
                val json = gson.fromJson(responseBody, Map::class.java)
                val content = json["content"] as? List<*>
                val textBlock = content?.firstOrNull() as? Map<*, *>
                textBlock?.get("text") as? String ?: ""
            }
            else -> ""
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
