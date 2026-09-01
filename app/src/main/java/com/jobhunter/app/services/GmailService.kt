package com.jobhunter.app.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.gson.Gson
import com.jobhunter.app.models.EmailConfig
import com.jobhunter.app.models.EmailDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Base64
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GmailService(private val context: Context) {

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
        try {
            val config = getConfig()
            if (!config.isConnected || config.accessToken.isEmpty()) {
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
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Create RFC 2822 email message
     */
    private fun createEmailMessage(from: String, to: String, subject: String, body: String): String {
        val properties = Properties().apply {
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
            put("mail.smtp.auth", "true")
        }

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(from, "")
            }
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            this.subject = subject
            setText(body)
        }

        val outputStream = java.io.ByteArrayOutputStream()
        message.writeTo(outputStream)
        return outputStream.toString()
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
     * Send email using device's email app (fallback)
     */
    fun sendEmailViaDevice(to: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "E-posta gönder"))
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
