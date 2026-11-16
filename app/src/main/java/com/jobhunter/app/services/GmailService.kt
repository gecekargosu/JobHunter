package com.jobhunter.app.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.gson.Gson
import com.jobhunter.app.models.EmailConfig
import com.jobhunter.app.models.EmailDraft
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Base64

class GmailService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("gmail_prefs", Context.MODE_PRIVATE)

    companion object {
        // Gmail API endpoints (would need OAuth in production)
        private const val GMAIL_API_BASE = "https://gmail.googleapis.com/gmail/v1"
    }

    /**
     * Save Gmail configuration
     */
    fun saveConfig(config: EmailConfig) {
        prefs.edit()
            .putString("gmail_address", config.gmailAddress)
            .putString("access_token", config.accessToken)
            .putString("refresh_token", config.refreshToken)
            .putBoolean("is_connected", config.isConnected)
            .apply()
    }

    /**
     * Get saved Gmail configuration
     */
    fun getConfig(): EmailConfig {
        return EmailConfig(
            gmailAddress = prefs.getString("gmail_address", "") ?: "",
            accessToken = prefs.getString("access_token", "") ?: "",
            refreshToken = prefs.getString("refresh_token", "") ?: "",
            isConnected = prefs.getBoolean("is_connected", false)
        )
    }

    /**
     * Check if Gmail is connected
     */
    fun isConnected(): Boolean {
        return getConfig().isConnected
    }

    /**
     * Disconnect Gmail
     */
    fun disconnect() {
        prefs.edit().clear().apply()
    }

    /**
     * Send email via Gmail API (requires OAuth token)
     * In production, you'd implement full OAuth 2.0 flow
     */
    suspend fun sendEmail(draft: EmailDraft): Boolean = withContext(Dispatchers.IO) {
        logger.logEmailSend(draft.to, draft.subject, "Gmail API", false) // Başlangıç
        val startTime = System.currentTimeMillis()
        try {
            val config = getConfig()
            if (!config.isConnected || config.accessToken.isEmpty()) {
                logger.w("Gmail", "Gmail bağlı değil veya token eksik")
                return@withContext false
            }

            // Create RFC 2822 email message
            val emailContent = createEmailMessage(
                from = config.gmailAddress,
                to = draft.to,
                subject = draft.subject,
                body = draft.body
            )

            // Base64url encode
            val encodedEmail = Base64.getUrlEncoder().encodeToString(emailContent.toByteArray())

            // Send via Gmail API
            val client = OkHttpClient()

            val jsonBody = """
                {
                    "raw": "$encodedEmail"
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("$GMAIL_API_BASE/users/me/messages/send")
                .addHeader("Authorization", "Bearer ${config.accessToken}")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val duration = System.currentTimeMillis() - startTime
            logger.logEmailSend(draft.to, draft.subject, "Gmail API", response.isSuccessful)
            logger.d("Gmail", "Gmail API yanıtı: ${response.code} (${duration}ms)")
            response.isSuccessful
        } catch (e: Exception) {
            logger.logError("Gmail", "E-posta gönderme", e.message ?: "Bilinmeyen hata", e)
            e.printStackTrace()
            false
        }
    }

    /**
     * Create RFC 2822 email message (simple version without javax.mail)
     */
    private fun createEmailMessage(from: String, to: String, subject: String, body: String): String {
        val sb = StringBuilder()
        sb.append("From: $from\r\n")
        sb.append("To: $to\r\n")
        sb.append("Subject: $subject\r\n")
        sb.append("Content-Type: text/plain; charset=UTF-8\r\n")
        sb.append("\r\n")
        sb.append(body)
        return sb.toString()
    }

    /**
     * Open Gmail compose in browser as fallback
     */
    fun openGmailCompose(to: String, subject: String, body: String) {
        val uri = Uri.Builder()
            .scheme("https")
            .authority("mail.google.com")
            .appendPath("mail")
            .appendPath("u")
            .appendPath("0")
            .appendQueryParameter("view", "cm")
            .appendQueryParameter("fs", "1")
            .appendQueryParameter("to", to)
            .appendQueryParameter("su", subject)
            .appendQueryParameter("body", body)
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Send email using device's email app (fallback - ANA YÖNTEM)
     * OAuth gerektirmez, tüm e-posta uygulamalarıyla çalışır
     * @return true if email app was opened, false otherwise
     */
    fun sendEmailViaDevice(to: String, subject: String, body: String): Boolean {
        logger.logEmailSend(to, subject, "Cihaz Uygulaması", false)
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "E-posta gönder"))
                logger.logEmailSend(to, subject, "Cihaz Uygulaması", true)
                return true
            } else {
                // Fallback: ACTION_SEND kullan
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (sendIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(Intent.createChooser(sendIntent, "E-posta gönder"))
                    logger.logEmailSend(to, subject, "Cihaz Uygulaması", true)
                    return true
                }
            }
            logger.w("Gmail", "E-posta uygulaması bulunamadı")
            return false
        } catch (e: Exception) {
            logger.logError("Gmail", "E-posta uygulaması açma", e.message ?: "Bilinmeyen hata", e)
            return false
        }
    }

    /**
     * Ek ile e-posta gönder (cihaz uygulaması ile - ANA YÖNTEM)
     * PDF, resim ve diğer dosyaları otomatik ekler
     */
    fun sendEmailWithAttachmentViaDevice(
        to: String,
        subject: String,
        body: String,
        attachmentUri: Uri? = null,
        attachmentUris: List<Uri> = emptyList()
    ): Boolean {
        logger.logEmailSend(to, subject, "Cihaz Uygulaması (Ekli)", false)
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                // Tek ek
                if (attachmentUri != null) {
                    putExtra(Intent.EXTRA_STREAM, attachmentUri)
                }
                // Birden fazla ek
                else if (attachmentUris.isNotEmpty()) {
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(attachmentUris))
                }
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "E-posta gönder"))
                logger.logEmailSend(to, subject, "Cihaz Uygulaması (Ekli)", true)
                return true
            } else {
                logger.w("Gmail", "E-posta uygulaması bulunamadı")
                return false
            }
        } catch (e: Exception) {
            logger.logError("Gmail", "Ekli e-posta gönderme", e.message ?: "Bilinmeyen hata", e)
            return false
        }
    }

    /**
     * Generate cover letter using template
     */
    fun generateCoverLetter(
        companyName: String,
        position: String = "Detecti̇f Pozisyon",
        senderName: String,
        senderSkills: List<String>,
        senderExperience: String,
        customNote: String = ""
    ): String {
        return """
Sayın $companyName İnsan Kaynakları,

$companyName bünyesinde $position pozisyonunda görev almak istediğimi belirtmek isterim.

$senderExperience

Sahip olduğum yetenekler: ${senderSkills.joinToString(", ")}

${if (customNote.isNotEmpty()) customNote else ""}

Bu fırsatı değerlendirmenizi ve benimle bir görüşme yapmanızı saygıyla rica ederim.

Saygılarımla,
$senderName
        """.trimIndent()
    }
}
