package com.jobhunter.app.services

import android.content.Context
import android.net.Uri
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/**
 * SMTP Email Servisi - OAuth gerektirmez!
 * Gmail, Outlook, Yahoo ve tüm SMTP sunucularıyla çalışır.
 *
 * Gmail için: Google hesabında "Uygulama Şifresi" oluştur
 * https://myaccount.google.com/apppasswords
 */
class SmtpEmailService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)
    private val prefs = context.getSharedPreferences("smtp_prefs", Context.MODE_PRIVATE)

    data class SmtpConfig(
        val host: String = "smtp.gmail.com",
        val port: String = "587",
        val username: String = "",
        val password: String = "",
        val fromName: String = "",
        val useTls: Boolean = true
    )

    /**
     * SMTP yapılandırmasını kaydet
     */
    fun saveConfig(config: SmtpConfig) {
        prefs.edit()
            .putString("smtp_host", config.host)
            .putString("smtp_port", config.port)
            .putString("smtp_username", config.username)
            .putString("smtp_password", config.password)
            .putString("smtp_from_name", config.fromName)
            .putBoolean("smtp_use_tls", config.useTls)
            .putBoolean("smtp_configured", true)
            .apply()
        logger.i("SmtpEmail", "SMTP yapılandırması kaydedildi: ${config.host}:${config.port}")
    }

    /**
     * Kaydedilmiş SMTP yapılandırmasını al
     */
    fun getConfig(): SmtpConfig {
        return SmtpConfig(
            host = prefs.getString("smtp_host", "smtp.gmail.com") ?: "smtp.gmail.com",
            port = prefs.getString("smtp_port", "587") ?: "587",
            username = prefs.getString("smtp_username", "") ?: "",
            password = prefs.getString("smtp_password", "") ?: "",
            fromName = prefs.getString("smtp_from_name", "") ?: "",
            useTls = prefs.getBoolean("smtp_use_tls", true)
        )
    }

    /**
     * SMTP yapılandırılmış mı kontrol et
     */
    fun isConfigured(): Boolean {
        val config = getConfig()
        return prefs.getBoolean("smtp_configured", false) &&
                config.username.isNotEmpty() &&
                config.password.isNotEmpty()
    }

    /**
     * SMTP yapılandırmasını sil
     */
    fun clearConfig() {
        prefs.edit().clear().apply()
        logger.i("SmtpEmail", "SMTP yapılandırması silindi")
    }

    /**
     * SMTP bağlantısını test et
     */
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val config = getConfig()
            val properties = createProperties(config)
            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(config.username, config.password)
                }
            })

            // Basit bir bağlantı testi
            val transport = session.getTransport("smtp")
            transport.connect(config.host, config.port.toInt(), config.username, config.password)
            transport.close()

            val msg = "SMTP bağlantısı başarılı! (${config.host}:${config.port})"
            logger.i("SmtpEmail", msg)
            Result.success(msg)
        } catch (e: Exception) {
            val errorMsg = "SMTP bağlantı hatası: ${e.message}"
            logger.logError("SmtpEmail", "Bağlantı testi", errorMsg, e)
            Result.failure(e)
        }
    }

    /**
     * Tek e-posta gönder (ek yok)
     */
    suspend fun sendEmail(
        to: String,
        subject: String,
        body: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val config = getConfig()
            if (!isConfigured()) {
                return@withContext Result.failure(Exception("SMTP yapılandırılmamış"))
            }

            val properties = createProperties(config)
            val session = createSession(config, properties)

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.username, config.fromName.ifEmpty { config.username }))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject, "UTF-8")
                setText(body, "UTF-8", "plain")
            }

            Transport.send(message)

            val msg = "E-posta gönderildi: $to"
            logger.i("SmtpEmail", msg)
            Result.success(msg)
        } catch (e: Exception) {
            val errorMsg = "E-posta gönderme hatası: ${e.message}"
            logger.logError("SmtpEmail", "Gönderme", errorMsg, e)
            Result.failure(e)
        }
    }

    /**
     * Ek ile e-posta gönder
     */
    suspend fun sendEmailWithAttachment(
        to: String,
        subject: String,
        body: String,
        attachmentUri: Uri? = null,
        attachmentUris: List<Uri> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val config = getConfig()
            if (!isConfigured()) {
                return@withContext Result.failure(Exception("SMTP yapılandırılmamış"))
            }

            val properties = createProperties(config)
            val session = createSession(config, properties)

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.username, config.fromName.ifEmpty { config.username }))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject, "UTF-8")
            }

            // Multipart mesaj oluştur (metin + ekler)
            val multipart: Multipart = MimeMultipart().apply {
                // Metin kısmı
                val textPart = MimeBodyPart().apply {
                    setText(body, "UTF-8", "plain")
                }
                addBodyPart(textPart)

                // Ekler
                val allAttachments = mutableListOf<Uri>()
                if (attachmentUri != null) allAttachments.add(attachmentUri)
                allAttachments.addAll(attachmentUris)

                for (uri in allAttachments) {
                    val attachmentPart = MimeBodyPart().apply {
                        // Dosya adını al
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        val fileName = cursor?.use {
                            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (nameIndex >= 0 && it.moveToFirst()) it.getString(nameIndex) else "attachment"
                        } ?: "attachment"

                        // InputStream'i gecici dosyaya kaydet (FileDataSource calismasi icin)
                        val tempFile = java.io.File(context.cacheDir, "email_attachment_$fileName")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        } ?: return@apply

                        dataHandler = DataHandler(FileDataSource(tempFile))
                        setFileName(fileName)
                        disposition = MimeBodyPart.ATTACHMENT
                    }
                    addBodyPart(attachmentPart)
                }
            }

            message.setContent(multipart)
            Transport.send(message)

            val attachmentCount = (if (attachmentUri != null) 1 else 0) + attachmentUris.size
            val msg = "E-posta gönderildi ($attachmentCount ek): $to"
            logger.i("SmtpEmail", msg)
            Result.success(msg)
        } catch (e: Exception) {
            val errorMsg = "Ekli e-posta gönderme hatası: ${e.message}"
            logger.logError("SmtpEmail", "Ekli gönderme", errorMsg, e)
            Result.failure(e)
        }
    }

    /**
     * Toplu e-posta gönder
     */
    suspend fun sendBulkEmails(
        recipients: List<Pair<String, String>>, // Pair<email, name>
        subject: String,
        bodyTemplate: String,
        attachmentUris: List<Uri> = emptyList()
    ): Result<BulkSendResult> = withContext(Dispatchers.IO) {
        val result = BulkSendResult()
        val config = getConfig()

        if (!isConfigured()) {
            return@withContext Result.failure(Exception("SMTP yapılandırılmamış"))
        }

        for ((email, name) in recipients) {
            try {
                val personalizedBody = bodyTemplate
                    .replace("{name}", name)
                    .replace("{email}", email)

                val sendResult = sendEmailWithAttachment(
                    to = email,
                    subject = subject,
                    body = personalizedBody,
                    attachmentUris = attachmentUris
                )

                if (sendResult.isSuccess) {
                    result.successful.add(email)
                } else {
                    result.failed.add(email to (sendResult.exceptionOrNull()?.message ?: "Bilinmeyen hata"))
                }

                // Rate limiting: her e-posta arası 2 saniye bekle
                kotlinx.coroutines.delay(2000)
            } catch (e: Exception) {
                result.failed.add(email to (e.message ?: "Bilinmeyen hata"))
            }
        }

        logger.i("SmtpEmail", "Toplu gönderim tamamlandı: ${result.successful.size} başarılı, ${result.failed.size} başarısız")
        Result.success(result)
    }

    /**
     * İş başvurusu e-postası gönder
     */
    suspend fun sendJobApplication(
        to: String,
        company: String,
        position: String,
        coverLetter: String,
        senderName: String,
        attachmentUris: List<Uri> = emptyList()
    ): Result<String> {
        val subject = "$senderName - İş Başvurusu - $position @ $company"
        return sendEmailWithAttachment(
            to = to,
            subject = subject,
            body = coverLetter,
            attachmentUris = attachmentUris
        )
    }

    // === Yardımcı Fonksiyonlar ===

    private fun createProperties(config: SmtpConfig): Properties {
        return Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", config.host)
            put("mail.smtp.port", config.port)
            put("mail.smtp.starttls.enable", config.useTls.toString())
            put("mail.smtp.ssl.protocols", "TLSv1.2")
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
        }
    }

    private fun createSession(config: SmtpConfig, properties: Properties): Session {
        return Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(config.username, config.password)
            }
        })
    }

    data class BulkSendResult(
        val successful: MutableList<String> = mutableListOf(),
        val failed: MutableList<Pair<String, String>> = mutableListOf()
    ) {
        val total get() = successful.size + failed.size
        val successRate get() = if (total > 0) (successful.size * 100 / total) else 0
    }
}
