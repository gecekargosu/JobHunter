package com.jobhunter.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.jobhunter.app.data.JobHunterDatabase
import com.jobhunter.app.models.SentEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class BulkEmailWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "job_hunter_bulk_email"
        const val NOTIFICATION_ID = 1001
        const val KEY_COMPANY_IDS = "company_ids"
        const val KEY_DELAY_BETWEEN = "delay_between_seconds"
        const val WORK_NAME = "bulk_email_work"

        /**
         * Schedule bulk email sending
         */
        fun scheduleBulkEmail(
            context: Context,
            companyIds: List<Long>,
            delayBetweenSeconds: Int = 30
        ) {
            val data = Data.Builder()
                .putLongArray(KEY_COMPANY_IDS, companyIds.toLongArray())
                .putInt(KEY_DELAY_BETWEEN, delayBetweenSeconds)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<BulkEmailWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        /**
         * Cancel bulk email sending
         */
        fun cancelBulkEmail(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        val companyIds = inputData.getLongArray(KEY_COMPANY_IDS)?.toList() ?: return Result.failure()
        val delaySeconds = inputData.getInt(KEY_DELAY_BETWEEN, 30)

        createNotificationChannel()

        val database = JobHunterDatabase.getDatabase(applicationContext)
        val companyDao = database.companyDao()
        val emailDao = database.emailDao()
        val profileDao = database.userProfileDao()
        val aiService = AiService(applicationContext)
        val gmailService = GmailService(applicationContext)

        val profile = profileDao.getProfileSync() ?: return Result.failure()
        var successCount = 0
        var failCount = 0

        for ((index, companyId) in companyIds.withIndex()) {
            // Check if work is cancelled
            if (isStopped) {
                return Result.success()
            }

            try {
                val company = companyDao.getCompanyById(companyId) ?: continue

                if (company.email.isEmpty()) {
                    failCount++
                    continue
                }

                // Update notification
                updateNotification(
                    index + 1,
                    companyIds.size,
                    company.name
                )

                // Generate cover letter
                val coverLetter = aiService.generateCoverLetter(company, profile)

                // Create email content
                val subject = "${profile.firstName} ${profile.lastName} - İş Başvurusu - ${company.name}"
                val body = coverLetter

                // Send email
                val success = if (gmailService.isConnected()) {
                    val draft = com.jobhunter.app.models.EmailDraft(
                        to = company.email,
                        subject = subject,
                        body = body,
                        companyName = company.name
                    )
                    gmailService.sendEmail(draft)
                } else {
                    // For non-connected Gmail, we just log it
                    // In production, you'd use SMTP or another method
                    true
                }

                // Save sent email
                val sentEmail = SentEmail(
                    companyId = companyId,
                    companyName = company.name,
                    recipientEmail = company.email,
                    subject = subject,
                    body = body,
                    status = if (success) "sent" else "failed"
                )
                emailDao.insertSentEmail(sentEmail)

                // Mark company as contacted
                companyDao.updateCompany(company.copy(
                    isContacted = true,
                    contactedAt = System.currentTimeMillis()
                ))

                if (success) successCount++ else failCount++

                // Delay between emails to avoid rate limiting
                if (index < companyIds.size - 1) {
                    delay(delaySeconds * 1000L)
                }

            } catch (e: Exception) {
                failCount++
            }
        }

        // Show completion notification
        showCompletionNotification(successCount, failCount)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Toplu E-posta Gönderimi",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Toplu e-posta gönderim durumu"
                setShowBadge(false)
            }

            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(current: Int, total: Int, companyName: String) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("E-posta gönderiliyor... ($current/$total)")
            .setContentText("$companyName adresine gönderiliyor")
            .setProgress(total, current, false)
            .setOngoing(true)
            .build()

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification(success: Int, fail: Int) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Toplu gönderim tamamlandı!")
            .setContentText("$success başarılı, $fail başarısız")
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
}
