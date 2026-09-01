package com.jobhunter.app.data.dao

import androidx.room.*
import com.jobhunter.app.models.EmailTemplate
import com.jobhunter.app.models.SentEmail
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDao {

    // Templates
    @Query("SELECT * FROM email_templates ORDER BY createdAt DESC")
    fun getAllTemplates(): Flow<List<EmailTemplate>>

    @Query("SELECT * FROM email_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): EmailTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: EmailTemplate): Long

    @Update
    suspend fun updateTemplate(template: EmailTemplate)

    @Delete
    suspend fun deleteTemplate(template: EmailTemplate)

    // Sent Emails
    @Query("SELECT * FROM sent_emails ORDER BY sentAt DESC")
    fun getAllSentEmails(): Flow<List<SentEmail>>

    @Query("SELECT * FROM sent_emails WHERE companyId = :companyId")
    fun getEmailsByCompany(companyId: Long): Flow<List<SentEmail>>

    @Query("SELECT * FROM sent_emails WHERE id = :id")
    suspend fun getSentEmailById(id: Long): SentEmail?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSentEmail(email: SentEmail): Long

    @Query("SELECT COUNT(*) FROM sent_emails")
    fun getSentEmailCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sent_emails WHERE status = 'sent'")
    fun getSuccessfulEmailCount(): Flow<Int>
}
