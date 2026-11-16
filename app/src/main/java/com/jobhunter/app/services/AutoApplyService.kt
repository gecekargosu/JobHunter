package com.jobhunter.app.services

import android.content.Context
import android.net.Uri
import com.jobhunter.app.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Otomatik Basvuru Servisi
 * 1. Is ilanindan sirket adini cikar
 * 2. Google'dan sirketin email adresini bul
 * 3. AI ile kisiye ozel on yazi olustur
 * 4. SMTP ile email gonder (CV ekli)
 */
class AutoApplyService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)
    private val companyResearch = CompanyResearchService(context)
    private val nvidiaAi = NvidiaNimService(context)
    private val smtp = SmtpEmailService(context)

    /**
     * Tek ilana basvuru yap
     */
    suspend fun applyToJob(
        jobTitle: String,
        companyName: String,
        jobDescription: String,
        senderName: String,
        senderEmail: String,
        senderSkills: List<String>,
        cvText: String = "",
        cvAttachmentUri: Uri? = null,
        additionalAttachmentUris: List<Uri> = emptyList()
    ): ApplyResult = withContext(Dispatchers.IO) {
        logger.i("AutoApply", "Basvuru basladi: $jobTitle @ $companyName")

        // 1. Adim: Sirketin email adresini bul
        val companyInfo = companyResearch.researchCompany(companyName)
        val email = companyInfo.emails.firstOrNull()

        if (email.isNullOrEmpty()) {
            logger.w("AutoApply", "$companyName icin email bulunamadi")
            return@withContext ApplyResult(
                success = false,
                companyEmail = "",
                message = "$companyName icin email adresi bulunamadi"
            )
        }

        logger.i("AutoApply", "Email bulundu: $email")

        // 2. Adim: AI ile on yazi olustur
        val coverLetter = try {
            nvidiaAi.generateCoverLetter(
                jobTitle = jobTitle,
                company = companyName,
                jobDescription = jobDescription,
                userSkills = senderSkills,
                userName = senderName
            )
        } catch (e: Exception) {
            // Fallback on yazi
            """
Sayin $companyName Insan Kaynaklari,

$companyName sirketinde $jobTitle pozisyonunda gore almak istedigimi belirtmek isterim.
Sahip oldugum yetenekler: ${senderSkills.joinToString(", ")}
Bu firsati degerlendirmenizi saygiyla rica ederim.

Saygilariyla,
$senderName
            """.trimIndent()
        }

        logger.i("AutoApply", "On yazi olusturuldu (${coverLetter.length} karakter)")

        // 3. Adim: Email gonder (ekli veya eksiz)
        val subject = "$senderName - $jobTitle Basvurusu - $companyName"

        val allAttachments = mutableListOf<Uri>()
        if (cvAttachmentUri != null) allAttachments.add(cvAttachmentUri)
        allAttachments.addAll(additionalAttachmentUris)

        val sendResult = if (allAttachments.isNotEmpty()) {
            smtp.sendEmailWithAttachment(
                to = email,
                subject = subject,
                body = coverLetter,
                attachmentUris = allAttachments
            )
        } else {
            smtp.sendEmail(
                to = email,
                subject = subject,
                body = coverLetter
            )
        }

        if (sendResult.isSuccess) {
            val attachmentInfo = if (allAttachments.isNotEmpty()) " (${allAttachments.size} ek)" else ""
            logger.i("AutoApply", "Email gonderildi$attachmentInfo: $email")
            ApplyResult(
                success = true,
                companyEmail = email,
                coverLetter = coverLetter,
                message = "Basvuru gonderildi$attachmentInfo: $email"
            )
        } else {
            val errorMsg = sendResult.exceptionOrNull()?.message ?: "Bilinmeyen hata"
            logger.logError("AutoApply", "Email gonderme hatasi", errorMsg)
            ApplyResult(
                success = false,
                companyEmail = email,
                message = "Email gonderilemedi: $errorMsg"
            )
        }
    }

    /**
     * Toplu basvuru yap (arka planda tek tek gonder)
     */
    suspend fun applyToMultipleJobs(
        jobs: List<JobToApply>,
        senderName: String,
        senderEmail: String,
        senderSkills: List<String>,
        cvText: String = "",
        cvAttachmentUri: Uri? = null,
        additionalAttachmentUris: List<Uri> = emptyList(),
        onProgress: (Int, Int, ApplyResult) -> Unit = { _, _, _ -> }
    ): List<ApplyResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ApplyResult>()

        for ((index, job) in jobs.withIndex()) {
            logger.i("AutoApply", "Toplu basvuru ${index + 1}/${jobs.size}: ${job.title} @ ${job.company}")

            val result = applyToJob(
                jobTitle = job.title,
                companyName = job.company,
                jobDescription = job.description,
                senderName = senderName,
                senderEmail = senderEmail,
                senderSkills = senderSkills,
                cvText = cvText,
                cvAttachmentUri = cvAttachmentUri,
                additionalAttachmentUris = additionalAttachmentUris
            )

            results.add(result)
            onProgress(index + 1, jobs.size, result)

            // Rate limiting: her email arasi 3 saniye bekle
            if (index < jobs.size - 1) {
                kotlinx.coroutines.delay(3000)
            }
        }

        val successCount = results.count { it.success }
        logger.i("AutoApply", "Toplu basvuru tamamlandi: $successCount/${jobs.size} basarili")

        results
    }

    data class JobToApply(
        val title: String,
        val company: String,
        val description: String = "",
        val url: String = ""
    )

    data class ApplyResult(
        val success: Boolean,
        val companyEmail: String,
        val coverLetter: String = "",
        val message: String
    )
}
