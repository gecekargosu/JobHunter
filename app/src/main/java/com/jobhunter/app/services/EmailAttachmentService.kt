package com.jobhunter.app.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.jobhunter.app.models.Document
import com.jobhunter.app.utils.AppLogger
import java.io.File

/**
 * Ek Desteği olan E-posta Servisi
 * CV, sertifika ve diğer belgeleri ek olarak gönderir
 */
class EmailAttachmentService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)

    /**
     * Tek belge ile e-posta gönder
     */
    fun sendEmailWithAttachment(
        to: String,
        subject: String,
        body: String,
        attachment: Document
    ): Boolean {
        return try {
            val file = File(attachment.filePath)
            if (!file.exists()) {
                logger.w("EmailAttachment", "Dosya bulunamadı: ${attachment.filePath}")
                return false
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Cihaz uygulamasını kullan (OAuth gerektirmez)
            val gmailService = GmailService(context)
            gmailService.sendEmailWithAttachmentViaDevice(
                to = to,
                subject = subject,
                body = body,
                attachmentUri = uri
            )
        } catch (e: Exception) {
            logger.logError("EmailAttachment", "E-posta gönderme", e.message ?: "Hata", e)
            false
        }
    }

    /**
     * Birden fazla belge ile e-posta gönder
     */
    fun sendEmailWithMultipleAttachments(
        to: String,
        subject: String,
        body: String,
        attachments: List<Document>
    ): Boolean {
        return try {
            val uris = attachments.mapNotNull { doc ->
                val file = File(doc.filePath)
                if (file.exists()) {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } else null
            }

            if (uris.isEmpty()) {
                logger.w("EmailAttachment", "Geçerli ek bulunamadı")
                return false
            }

            // Cihaz uygulamasını kullan (OAuth gerektirmez)
            val gmailService = GmailService(context)
            gmailService.sendEmailWithAttachmentViaDevice(
                to = to,
                subject = subject,
                body = body,
                attachmentUris = uris
            )
        } catch (e: Exception) {
            logger.logError("EmailAttachment", "Toplu e-posta gönderme", e.message ?: "Hata", e)
            false
        }
    }

    /**
     * Toplu başvuru için e-posta gönder
     * (AI ile oluşturulmuş özelleştirilmiş ön yazı + varsayılan belgeler)
     */
    fun sendJobApplicationEmail(
        to: String,
        company: String,
        position: String,
        coverLetter: String,
        attachments: List<Document>,
        senderName: String
    ): Boolean {
        val subject = "$senderName - İş Başvurusu - $position @ $company"

        return sendEmailWithMultipleAttachments(
            to = to,
            subject = subject,
            body = coverLetter,
            attachments = attachments
        )
    }

    /**
     * Varsayılan belgeleri al (CV ve varsayılan sertifikalar)
     */
    fun getDefaultAttachments(documents: List<Document>): List<Document> {
        return documents.filter { doc ->
            doc.isDefault || 
            doc.type == com.jobhunter.app.models.DocumentType.CV ||
            doc.type == com.jobhunter.app.models.DocumentType.CERTIFICATE
        }
    }

    /**
     * Dosya boyutunu formatla
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    /**
     * MIME türünü al
     */
    fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".pdf") -> "application/pdf"
            fileName.endsWith(".doc") || fileName.endsWith(".docx") -> "application/msword"
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
            fileName.endsWith(".png") -> "image/png"
            fileName.endsWith(".txt") -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}
