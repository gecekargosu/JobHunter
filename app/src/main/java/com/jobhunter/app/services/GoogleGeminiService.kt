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
 * Google Gemini API Servisi
 * NVIDIA NIM calismazsa alternatif olarak kullanilir
 * Ucretsiz: 1500 istek/gun
 *
 * API Key: https://aistudio.google.com/apikey
 */
class GoogleGeminiService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("gemini_prefs", Context.MODE_PRIVATE)

    // Default API key - otomatik yuklu
    private val defaultKey = ""   // anahtar depoda tutulmaz, Ayarlar ekranindan girilir

    fun getApiKey(): String {
        val saved = prefs.getString("gemini_api_key", "")
        return if (saved.isNullOrEmpty()) defaultKey else saved
    }

    fun setApiKey(key: String) { prefs.edit().putString("gemini_api_key", key).apply() }
    fun isConfigured(): Boolean = getApiKey().isNotEmpty()

    /**
     * On yazi olustur
     */
    suspend fun generateCoverLetter(
        jobTitle: String,
        company: String,
        jobDescription: String,
        userSkills: List<String>,
        userName: String,
        customTemplate: String = ""
    ): String = withContext(Dispatchers.IO) {
        try {
            val templateSection = if (customTemplate.isNotEmpty()) "\nKullanici sablonu: $customTemplate" else ""

            val prompt = """
Sen profesyonel bir is basvuru mektubu yazicisisin. Turkce on yazi yaz.

Is: $jobTitle
Sirket: $company
Aciklama: $jobDescription
Yetenekler: ${userSkills.joinToString(", ")}
Ad: $userName$templateSection

Kurallar: Profesyonel, 150-250 kelime, sadece on yazi metnini don.
            """.trimIndent()

            callGeminiApi(prompt)
        } catch (e: Exception) {
            logger.logError("Gemini", "On yazi hatasi", e.message ?: "Hata", e)
            throw e
        }
    }

    /**
     * Sirket hakkinda bilgi ver
     */
    suspend fun researchCompany(companyName: String): String = withContext(Dispatchers.IO) {
        try {
            callGeminiApi("'$companyName' hakkinda Turkce ozet yaz. 200-300 kelime. Web sitesi, sektor, buyukluk, iletisim bilgileri.")
        } catch (e: Exception) { "Hata: ${e.message}" }
    }

    /**
     * CV analiz et
     */
    suspend fun analyzeCv(cvText: String, targetJob: String = ""): String = withContext(Dispatchers.IO) {
        try {
            val prompt = "CV'yi incele ve oneriler sun:\n$cvText\n${if (targetJob.isNotEmpty()) "Hedef: $targetJob" else ""}"
            callGeminiApi(prompt)
        } catch (e: Exception) { "Hata: ${e.message}" }
    }

    /**
     * Is eslestirme puani
     */
    suspend fun scoreJobMatch(
        jobTitle: String,
        jobDescription: String,
        userSkills: List<String>,
        userExperience: String
    ): NvidiaNimService.JobMatchResult = withContext(Dispatchers.IO) {
        try {
            val prompt = """
Is: $jobTitle - $jobDescription
Yetenekler: ${userSkills.joinToString(", ")}
Deneyim: $userExperience
1-100 puan ver ve acikla.
            """.trimIndent()

            val response = callGeminiApi(prompt)
            val score = Regex("""(\d+)""").find(response)?.groupValues?.get(1)?.toIntOrNull() ?: 50
            NvidiaNimService.JobMatchResult(score, response, emptyList(), userSkills)
        } catch (e: Exception) {
            NvidiaNimService.JobMatchResult(0, "Hata: ${e.message}", emptyList(), emptyList())
        }
    }

    /**
     * Gemini API'yi cagir
     */
    private fun callGeminiApi(prompt: String): String {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) throw Exception("Gemini API key girilmemis")

        // Desteklenen modelleri sirayla dene
        val models = listOf(
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-1.5-pro"
        )

        for (model in models) {
            try {
                val result = callWithModel(apiKey, model, prompt)
                if (result.isNotEmpty()) return result
            } catch (e: Exception) {
                logger.w("Gemini", "$model hatasi: ${e.message}")
            }
        }
        throw Exception("Tum Gemini modelleri basarisiz")
    }

    private fun callWithModel(apiKey: String, model: String, prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 1024)
            })
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Bos yanit")

        if (!response.isSuccessful) {
            val error = try {
                JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
            } catch (e: Exception) { responseBody }
            throw Exception("Gemini HTTP ${response.code}: $error")
        }

        val json = JSONObject(responseBody)
        val candidates = json.getJSONArray("candidates")
        if (candidates.length() == 0) throw Exception("Sonuc yok")

        val content = candidates.getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        return content
    }
}
