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
import org.json.JSONArray
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
        private const val PREF_API_PROVIDER = "api_provider"
        private const val MAX_PROMPT_LENGTH = 4000
    }

    fun sanitizeInput(input: String): String {
        return input.replace("\n", " ").replace("\r", " ").replace("\t", " ").trim().take(MAX_PROMPT_LENGTH)
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

    // Desteklenen tum saglayicilar
    val supportedProviders = listOf(
        ProviderInfo("openai", "OpenAI (ChatGPT)", "sk-...", "platform.openai.com/api-keys"),
        ProviderInfo("anthropic", "Anthropic (Claude)", "sk-ant-...", "console.anthropic.com/settings/keys"),
        ProviderInfo("nvidia", "NVIDIA NIM", "nvapi-...", "build.nvidia.com"),
        ProviderInfo("gemini", "Google Gemini", "AIza... veya AQ...", "aistudio.google.com/apikey")
    )

    data class ProviderInfo(val id: String, val name: String, val keyPrefix: String, val url: String)

    suspend fun generateCoverLetter(
        company: Company, profile: UserProfile,
        position: String = "Pozisyon", tone: String = "profesyonel"
    ): String = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext generateFallbackCoverLetter(company, profile, position)
        val prompt = buildString {
            appendLine("Sen profesyonel bir is basvuru mektubu yazicisisin. Turkce on yazi yaz.")
            appendLine("3 paragrafi gecme, profesyonel ama samimi ton kullan.")
            appendLine()
            appendLine("=== ADAY ===")
            appendLine("Ad: ${sanitizeInput(profile.firstName)} ${sanitizeInput(profile.lastName)}")
            appendLine("Ozet: ${sanitizeInput(profile.summary)}")
            appendLine("Yetenekler: ${sanitizeInput(profile.skills.joinToString(", "))}")
            profile.experiences.forEach { exp -> appendLine("- ${exp.position} @ ${exp.company} (${exp.startDate}-${exp.endDate})") }
            profile.educations.forEach { edu -> appendLine("- ${edu.degree} ${edu.department} @ ${edu.school}") }
            appendLine()
            appendLine("=== IS ===")
            appendLine("Sirket: ${sanitizeInput(company.name)}")
            appendLine("Sektor: ${sanitizeInput(company.industry)}")
            appendLine("Aciklama: ${sanitizeInput(company.description)}")
            appendLine("Konum: ${sanitizeInput(company.location)}")
            appendLine()
            appendLine("Son paragrafta gorusme talep et. Greeting ve signature ekleme.")
        }
        try {
            val response = callAiApi(prompt)
            response.ifEmpty { generateFallbackCoverLetter(company, profile, position) }
        } catch (e: Exception) { generateFallbackCoverLetter(company, profile, position) }
    }

    suspend fun generateCoverLetterSimple(
        companyName: String, position: String, senderName: String, skills: List<String>
    ): String = withContext(Dispatchers.IO) {
        val prompt = "$senderName icin $companyName sirketindeki $position icin Turkce on yazi yaz. Yetenekler: ${skills.joinToString(", ")}. Kisa, profesyonel, max 200 kelime. Greeting ve signature ekleme."
        try {
            val response = callAiApi(prompt)
            response.ifEmpty { generateFallbackSimple(companyName, position, senderName, skills) }
        } catch (e: Exception) { generateFallbackSimple(companyName, position, senderName, skills) }
    }

    suspend fun generateOutreachEmail(
        company: Company, profile: UserProfile, purpose: String = "is basvurusu"
    ): String = withContext(Dispatchers.IO) {
        val prompt = buildString {
            appendLine("Kisa ve etkileyici bir outreach email yaz. Turkce. 1 paragraf.")
            appendLine("Sirket: ${company.name}, Sektor: ${company.industry}")
            appendLine("Aday: ${profile.firstName} ${profile.lastName}, Yetenekler: ${profile.skills.joinToString(", ")}")
            appendLine("Amac: $purpose")
        }
        try { callAiApi(prompt) } catch (e: Exception) { "Merhaba, $purpose icin basvuruyorum. Saygilarla, ${profile.firstName}" }
    }

    suspend fun summarizeCompany(company: Company): String = withContext(Dispatchers.IO) {
        val prompt = "'${company.name}' hakkinda kisa Turkce ozet yaz. Sektor: ${company.industry}, Aciklama: ${company.description}"
        try { callAiApi(prompt) } catch (e: Exception) { "${company.name} - ${company.description}" }
    }

    private suspend fun callAiApi(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val provider = getProvider()

        when (provider) {
            "nvidia" -> callNvidiaApi(apiKey, prompt)
            "gemini" -> callGeminiApi(apiKey, prompt)
            "openai" -> callOpenAiApi(apiKey, prompt)
            "anthropic" -> callAnthropicApi(apiKey, prompt)
            else -> throw Exception("Bilinmeyen saglayici: $provider")
        }
    }

    private fun callOpenAiApi(apiKey: String, prompt: String): String {
        val json = JsonObject().apply {
            addProperty("model", "gpt-3.5-turbo")
            addProperty("max_tokens", 1000)
            addProperty("temperature", 0.7)
            val messages = com.google.gson.JsonArray().apply {
                add(JsonObject().apply { addProperty("role", "system"); addProperty("content", "Turkce yanit ver.") })
                add(JsonObject().apply { addProperty("role", "user"); addProperty("content", prompt) })
            }
            add("messages", messages)
        }
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(json).toRequestBody("application/json".toMediaType()))
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Bos yanit")
        if (!response.isSuccessful) throw Exception("OpenAI HTTP ${response.code}")
        val jo = JsonParser.parseString(body).asJsonObject
        return jo.getAsJsonArray("choices").get(0).asJsonObject.getAsJsonObject("message").get("content").asString
    }

    private fun callAnthropicApi(apiKey: String, prompt: String): String {
        val json = JsonObject().apply {
            addProperty("model", "claude-3-haiku-20240307")
            addProperty("max_tokens", 1000)
            val messages = com.google.gson.JsonArray().apply {
                add(JsonObject().apply { addProperty("role", "user"); addProperty("content", prompt) })
            }
            add("messages", messages)
        }
        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .addHeader("anthropic-version", "2023-06-01")
            .post(gson.toJson(json).toRequestBody("application/json".toMediaType()))
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Bos yanit")
        if (!response.isSuccessful) throw Exception("Anthropic HTTP ${response.code}")
        val jo = JsonParser.parseString(body).asJsonObject
        return jo.getAsJsonArray("content").get(0).asJsonObject.get("text").asString
    }

    private fun callNvidiaApi(apiKey: String, prompt: String): String {
        val models = listOf("deepseek-ai/deepseek-v4-flash-0731", "meta/llama-3.3-70b-instruct")
        for (model in models) {
            try {
                val json = org.json.JSONObject().apply {
                    put("model", model)
                    put("messages", org.json.JSONArray().apply {
                        put(org.json.JSONObject().apply { put("role", "user"); put("content", prompt) })
                    })
                    put("max_tokens", 1024)
                    put("temperature", 0.7)
                    put("stream", false)
                }
                val request = Request.Builder()
                    .url("https://integrate.api.nvidia.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: continue
                if (!response.isSuccessful) continue
                val jo = org.json.JSONObject(body)
                return jo.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            } catch (e: Exception) { logger.w("AiService", "NVIDIA $model hatasi: ${e.message}") }
        }
        throw Exception("NVIDIA API calismadi. Public API Endpoints izni gerekebilir.")
    }

    private fun callGeminiApi(apiKey: String, prompt: String): String {
        val models = listOf("gemini-2.0-flash", "gemini-1.5-flash")
        for (model in models) {
            try {
                val body = org.json.JSONObject().apply {
                    put("contents", org.json.JSONArray().apply {
                        put(org.json.JSONObject().apply {
                            put("parts", org.json.JSONArray().apply {
                                put(org.json.JSONObject().apply { put("text", prompt) })
                            })
                        })
                    })
                    put("generationConfig", org.json.JSONObject().apply {
                        put("temperature", 0.7)
                        put("maxOutputTokens", 1024)
                    })
                }
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(body.toString().toRequestBody("application/json".toMediaType()))
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: continue
                if (!response.isSuccessful) continue
                val jo = org.json.JSONObject(responseBody)
                return jo.getJSONArray("candidates").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            } catch (e: Exception) { logger.w("AiService", "Gemini $model hatasi: ${e.message}") }
        }
        throw Exception("Gemini API calismadi. API key'i kontrol edin.")
    }

    private fun generateFallbackCoverLetter(company: Company, profile: UserProfile, position: String): String {
        return "Sayin ${company.name} Insan Kaynaklari,\n\n${company.name} bnyesinde calismak istedigimi belirtmek isterim.\n\n${profile.summary}\n\nYeteneklerim: ${profile.skills.joinToString(", ")}\n\nBu firsati degerlendirmenizi saygiyla rica ederim.\n\nSaygilariyla,\n${profile.firstName} ${profile.lastName}\n${profile.email}"
    }

    private fun generateFallbackSimple(companyName: String, position: String, senderName: String, skills: List<String>): String {
        return "Sayin $companyName Insan Kaynaklari,\n\n$position pozisyonunda calismak istedigimi belirtmek isterim.\n\nYeteneklerim: ${skills.joinToString(", ")}\n\nBu firsati degerlendirmenizi saygiyla rica ederim.\n\nSaygilariyla,\n$senderName"
    }
}
