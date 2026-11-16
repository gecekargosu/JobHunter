package com.jobhunter.app.services

import android.content.Context
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * NVIDIA NIM API Servisi
 * 81+ ucretsiz AI modeli - tek API key ile hepsi calisir
 * 
 * API Key'ler otomatik yukludur, kullanicinin girmesine gerek yok.
 * NVIDIA "Public API Endpoints" izni acildiginda calismaya baslayacak.
 */
class NvidiaNimService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("nvidia_prefs", Context.MODE_PRIVATE)
    
    // API Key'ler otomatik yuklu
    private val defaultKeys = listOf(
        "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",
        "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"
    )
    
    fun getApiKey(): String {
        val saved = prefs.getString("nvidia_api_key", "")
        return if (saved.isNullOrEmpty()) defaultKeys.first() else saved
    }
    
    fun setApiKey(key: String) { prefs.edit().putString("nvidia_api_key", key).apply() }
    fun isConfigured(): Boolean = true // Key'ler otomatik yuklu
    
    private val baseUrl = "https://integrate.api.nvidia.com/v1"
    
    // Desteklenen modeller (hepsi ayni key ile calisir)
    private val models = listOf(
        "moonshotai/kimi-k3",
        "deepseek-ai/deepseek-v4-flash-0731",
        "deepseek-ai/deepseek-v4-pro-0813",
        "meta/llama-3.3-70b-instruct",
        "nvidia/llama-3.1-nemotron-51b-instruct"
    )

    private var geminiService: GoogleGeminiService? = null
    
    private fun getGemini(context: android.content.Context): GoogleGeminiService {
        if (geminiService == null) {
            geminiService = GoogleGeminiService(context)
        }
        return geminiService!!
    }
    
    suspend fun generateCoverLetter(
        jobTitle: String, company: String, jobDescription: String,
        userSkills: List<String>, userName: String,
        customTemplate: String = ""
    ): String = withContext(Dispatchers.IO) {
        // Oncelikle Gemini dene (calisiyor)
        try {
            val gemini = getGemini(context)
            if (gemini.isConfigured()) {
                val result = gemini.generateCoverLetter(jobTitle, company, jobDescription, userSkills, userName, customTemplate)
                if (result.isNotEmpty()) return@withContext result
            }
        } catch (e: Exception) {
            logger.w("NvidiaNim", "Gemini basarisiz: ${e.message}")
        }
        
        // NVIDIA dene (calismayabilir - Public API Endpoints gerekli)
        try {
            val templateSection = if (customTemplate.isNotEmpty()) {
                "\nKullanici sablonu: $customTemplate"
            } else ""
            
            val result = callNvidiaApi("""
Sen profesyonel bir is basvuru mektubu yazicisisin. Turkce on yazi yaz.

Is: $jobTitle
Sirket: $company
Aciklama: $jobDescription
Yetenekler: ${userSkills.joinToString(", ")}
Ad: $userName$templateSection

Kurallar: Profesyonel, 150-250 kelime, sadece on yazi metnini don.
            """.trimIndent())
            if (result.isNotEmpty()) return@withContext result
        } catch (e: Exception) {
            logger.w("NvidiaNim", "NVIDIA da basarisiz: ${e.message}")
        }
        
        // Fallback sablon
        generateFallbackCoverLetter(jobTitle, company, userSkills, userName)
    }

    suspend fun scoreJobMatch(
        jobTitle: String, jobDescription: String,
        userSkills: List<String>,
        userExperience: String
    ): JobMatchResult = withContext(Dispatchers.IO) {
        // Oncelikle Gemini dene
        try {
            val gemini = getGemini(context)
            if (gemini.isConfigured()) {
                return@withContext gemini.scoreJobMatch(jobTitle, jobDescription, userSkills, userExperience)
            }
        } catch (e: Exception) { logger.w("NvidiaNim", "Gemini basarisiz: ${e.message}") }
        // NVIDIA dene
        try {
            val response = callNvidiaApi("""
Is: $jobTitle - $jobDescription
Yetenekler: ${userSkills.joinToString(", ")}
Deneyim: $userExperience
1-100 puan ver ve acikla.
            """.trimIndent())
            val score = Regex("""(\d+)""").find(response)?.groupValues?.get(1)?.toIntOrNull() ?: 50
            JobMatchResult(score, response, emptyList(), userSkills)
        } catch (e: Exception) { JobMatchResult(0, "Hata: ${e.message}", emptyList(), emptyList()) }
    }

    suspend fun researchCompany(companyName: String): String = withContext(Dispatchers.IO) {
        // Oncelikle Gemini dene
        try {
            val gemini = getGemini(context)
            if (gemini.isConfigured()) return@withContext gemini.researchCompany(companyName)
        } catch (e: Exception) { logger.w("NvidiaNim", "Gemini basarisiz: ${e.message}") }
        // NVIDIA dene
        try {
            callNvidiaApi("\"$companyName\" hakkinda Turkce ozet yaz. 200-300 kelime.")
        } catch (e: Exception) { "Hata: ${e.message}" }
    }

    suspend fun analyzeCv(cvText: String, targetJob: String = ""): String = withContext(Dispatchers.IO) {
        // Oncelikle Gemini dene
        try {
            val gemini = getGemini(context)
            if (gemini.isConfigured()) return@withContext gemini.analyzeCv(cvText, targetJob)
        } catch (e: Exception) { logger.w("NvidiaNim", "Gemini basarisiz: ${e.message}") }
        // NVIDIA dene
        try {
            callNvidiaApi("CV'yi incele ve oneriler sun:\n$cvText\n${if (targetJob.isNotEmpty()) "Hedef: $targetJob" else ""}")
        } catch (e: Exception) { "Hata: ${e.message}" }
    }

    private fun callNvidiaApi(prompt: String): String {
        // Once kullanici key'ini dene, sonra otomatik key'leri
        val keysToTry = mutableListOf(getApiKey())
        defaultKeys.forEach { if (!keysToTry.contains(it)) keysToTry.add(it) }
        
        for (apiKey in keysToTry) {
            for (model in models) {
                try {
                    val result = callWithModel(apiKey, model, prompt)
                    if (result.isNotEmpty()) return result
                } catch (e: Exception) {
                    logger.w("NvidiaNim", "$model ($apiKey.takeLast(8)) hata: ${e.message}")
                }
            }
        }
        throw Exception("NVIDIA API calismadi. help@build.nvidia.com'a email gonderildi mi?")
    }

    private fun callWithModel(apiKey: String, model: String, prompt: String): String {
        val body = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply { put("role", "system"); put("content", "Turkce yanit ver.") })
                put(JSONObject().apply { put("role", "user"); put("content", prompt) })
            })
            put("max_tokens", 1024)
            put("temperature", 0.7)
            put("stream", false)
        }

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Bos yanit")
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

        val json = JSONObject(responseBody)
        return json.getJSONArray("choices").getJSONObject(0)
            .getJSONObject("message").getString("content")
    }

    private fun generateFallbackCoverLetter(
        jobTitle: String, company: String, skills: List<String>, userName: String
    ): String = """
Sayin $company Insan Kaynaklari,

$company sirketinde $jobTitle pozisyonunda gore almak istedigimi belirtmek isterim.
Sahip oldugum yetenekler: ${skills.joinToString(", ")}
Bu firsati degerlendirmenizi saygiyla rica ederim.

Saygilariyla,
$userName
    """.trimIndent()

    suspend fun checkApiStatus(): Boolean = withContext(Dispatchers.IO) {
        // Oncelikle Gemini kontrol et
        try {
            val gemini = getGemini(context)
            if (gemini.isConfigured()) return@withContext true
        } catch (e: Exception) {}
        // NVIDIA dene
        try {
            val result = callWithModel(getApiKey(), models.first(), "OK")
            result.isNotBlank()
        } catch (e: Exception) { false }
    }

    data class JobMatchResult(
        val score: Int, val reason: String,
        val missingSkills: List<String>, val matchingSkills: List<String>
    )
}
