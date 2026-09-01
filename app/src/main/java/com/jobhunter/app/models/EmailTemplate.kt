package com.jobhunter.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "email_templates")
data class EmailTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val subject: String,
    val body: String,
    val isAiGenerated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sent_emails")
data class SentEmail(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: Long,
    val companyName: String,
    val recipientEmail: String,
    val subject: String,
    val body: String,
    val sentAt: Long = System.currentTimeMillis(),
    val status: String = "sent", // sent, failed, pending
    val templateId: Long = 0
)

@Serializable
data class EmailDraft(
    val to: String,
    val subject: String,
    val body: String,
    val companyName: String = ""
)

data class EmailConfig(
    val gmailAddress: String = "",
    val accessToken: String = "",
    val refreshToken: String = "",
    val isConnected: Boolean = false
)
