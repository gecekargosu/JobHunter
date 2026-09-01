package com.jobhunter.app.viewmodels

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jobhunter.app.data.JobHunterDatabase
import com.jobhunter.app.models.*
import com.jobhunter.app.services.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class EmailViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JobHunterDatabase.getDatabase(application)
    private val emailDao = database.emailDao()
    private val companyDao = database.companyDao()
    private val profileDao = database.userProfileDao()
    private val gmailService = GmailService(application)
    private val aiService = AiService(application)
    private val pdfService = PdfService(application)

    // Gmail state
    val isGmailConnected: StateFlow<Boolean> = flow {
        emit(gmailService.isConnected())
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    // Templates
    val templates: StateFlow<List<EmailTemplate>> = emailDao.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Sent emails
    val sentEmails: StateFlow<List<SentEmail>> = emailDao.getAllSentEmails()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Compose state
    private val _toEmail = MutableStateFlow("")
    val toEmail: StateFlow<String> = _toEmail.asStateFlow()

    private val _subject = MutableStateFlow("")
    val subject: StateFlow<String> = _subject.asStateFlow()

    private val _body = MutableStateFlow("")
    val body: StateFlow<String> = _body.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _sendResult = MutableStateFlow<String?>(null)
    val sendResult: StateFlow<String?> = _sendResult.asStateFlow()

    // AI generation state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // PDF state
    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _generatedPdfPath = MutableStateFlow<String?>(null)
    val generatedPdfPath: StateFlow<String?> = _generatedPdfPath.asStateFlow()

    fun updateToEmail(email: String) { _toEmail.value = email }
    fun updateSubject(subject: String) { _subject.value = subject }
    fun updateBody(body: String) { _body.value = body }

    /**
     * Generate email with AI using email field to find company
     */
    fun generateEmailWithAiFromEmail(purpose: String = "iş başvurusu") {
        val email = _toEmail.value.trim()
        if (email.isEmpty()) {
            _sendResult.value = "Önce alıcı e-posta adresini girin"
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val profile = profileDao.getProfileSync()
                if (profile == null) {
                    _sendResult.value = "Profil bulunamadı. Önce Profilim ekranından bilgilerinizi girin."
                    return@launch
                }

                // Try to find company by email
                val companies = companyDao.getAllCompaniesSync()
                val company = companies.find { it.email == email }

                val generatedSubject: String
                val generatedBody: String

                if (company != null) {
                    // Found company, generate personalized email
                    generatedBody = aiService.generateOutreachEmail(company, profile, purpose)
                    generatedSubject = if (purpose == "iş başvurusu") {
                        "${profile.firstName} ${profile.lastName} - İş Başvurusu - ${company.name}"
                    } else {
                        "${company.name} Hakkında"
                    }
                } else {
                    // No company found, generate generic email
                    generatedBody = aiService.generateOutreachEmail(
                        Company(name = "", domain = "", industry = "", location = "", description = ""),
                        profile,
                        purpose
                    )
                    generatedSubject = if (purpose == "iş başvurusu") {
                        "${profile.firstName} ${profile.lastName} - İş Başvurusu"
                    } else {
                        "İş Başvurusu"
                    }
                }

                _subject.value = generatedSubject
                _body.value = generatedBody
            } catch (e: Exception) {
                _sendResult.value = "AI oluşturma hatası: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    /**
     * Generate email with AI
     */
    fun generateEmailWithAi(companyId: Long, purpose: String = "iş başvurusu") {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val company = companyDao.getCompanyById(companyId)
                val profile = profileDao.getProfileSync()

                if (company != null && profile != null) {
                    val generatedBody = aiService.generateOutreachEmail(
                        company = company,
                        profile = profile,
                        purpose = purpose
                    )

                    val generatedSubject = if (purpose == "iş başvurusu") {
                        "${profile.firstName} ${profile.lastName} - İş Başvurusu"
                    } else {
                        "${company.name} Hakkında"
                    }

                    _toEmail.value = company.email
                    _subject.value = generatedSubject
                    _body.value = generatedBody
                }
            } catch (e: Exception) {
                _sendResult.value = "AI oluşturma hatası: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    /**
     * Generate cover letter with AI
     */
    fun generateCoverLetter(companyId: Long) {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val company = companyDao.getCompanyById(companyId)
                val profile = profileDao.getProfileSync()

                if (company != null && profile != null) {
                    val coverLetter = aiService.generateCoverLetter(
                        company = company,
                        profile = profile
                    )
                    _body.value = coverLetter
                }
            } catch (e: Exception) {
                _sendResult.value = "Ön yazı oluşturma hatası: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    /**
     * Generate CV PDF
     */
    fun generateCvPdf() {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                val profile = profileDao.getProfileSync()
                if (profile != null) {
                    val pdfFile = pdfService.generateCvPdf(profile)
                    _generatedPdfPath.value = pdfFile.absolutePath
                    _sendResult.value = "CV PDF oluşturuldu: ${pdfFile.name}"
                } else {
                    _sendResult.value = "Profil bulunamadı. Önce Profilim ekranından bilgilerinizi girin."
                }
            } catch (e: Exception) {
                _sendResult.value = "PDF oluşturma hatası: ${e.message}"
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    /**
     * Share CV PDF via intent
     */
    fun shareCvPdf() {
        val path = _generatedPdfPath.value ?: return
        try {
            val file = File(path)
            val uri = FileProvider.getUriForFile(
                getApplication(),
                "${getApplication<Application>().packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Özgeçmişim")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "CV paylaş")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(chooser)
        } catch (e: Exception) {
            _sendResult.value = "Paylaşma hatası: ${e.message}"
        }
    }

    /**
     * Send email
     */
    fun sendEmail() {
        val to = _toEmail.value.trim()
        val subject = _subject.value.trim()
        val body = _body.value.trim()

        if (to.isEmpty() || subject.isEmpty() || body.isEmpty()) {
            _sendResult.value = "Tüm alanları doldurun"
            return
        }

        viewModelScope.launch {
            _isSending.value = true
            try {
                val draft = EmailDraft(
                    to = to,
                    subject = subject,
                    body = body
                )

                val success = if (gmailService.isConnected()) {
                    gmailService.sendEmail(draft)
                } else {
                    // Fallback: open device email app
                    gmailService.sendEmailViaDevice(to, subject, body)
                }

                // Save to database
                val sentEmail = SentEmail(
                    companyId = 0,
                    companyName = "",
                    recipientEmail = to,
                    subject = subject,
                    body = body,
                    status = if (success) "sent" else "failed"
                )
                emailDao.insertSentEmail(sentEmail)

                _sendResult.value = if (success) "E-posta gönderildi!" else "Gönderme başarısız"

                // Don't clear form - let user decide if they want to send again
            } catch (e: Exception) {
                _sendResult.value = "Hata: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    /**
     * Send email to company
     */
    fun sendEmailToCompany(companyId: Long) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                val company = companyDao.getCompanyById(companyId)
                val profile = profileDao.getProfileSync()

                if (company != null && profile != null && company.email.isNotEmpty()) {
                    val coverLetter = aiService.generateCoverLetter(company, profile)

                    val subject = "${profile.firstName} ${profile.lastName} - İş Başvurusu - ${company.name}"
                    val draft = EmailDraft(
                        to = company.email,
                        subject = subject,
                        body = coverLetter,
                        companyName = company.name
                    )

                    val success = if (gmailService.isConnected()) {
                        gmailService.sendEmail(draft)
                    } else {
                        gmailService.sendEmailViaDevice(company.email, subject, coverLetter)
                    }

                    val sentEmail = SentEmail(
                        companyId = companyId,
                        companyName = company.name,
                        recipientEmail = company.email,
                        subject = subject,
                        body = coverLetter,
                        status = if (success) "sent" else "failed"
                    )
                    emailDao.insertSentEmail(sentEmail)

                    // Mark company as contacted
                    companyDao.updateCompany(company.copy(
                        isContacted = true,
                        contactedAt = System.currentTimeMillis()
                    ))

                    _sendResult.value = if (success) "${company.name} - E-posta gönderildi!" else "Gönderme başarısız"
                } else if (company?.email?.isEmpty() == true) {
                    _sendResult.value = "${company.name} için e-posta adresi bulunamadı"
                }
            } catch (e: Exception) {
                _sendResult.value = "Hata: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    /**
     * Bulk send emails to multiple companies (using WorkManager)
     */
    fun bulkSendEmails(companyIds: List<Long>) {
        BulkEmailWorker.scheduleBulkEmail(
            context = getApplication(),
            companyIds = companyIds,
            delayBetweenSeconds = 30
        )
        _sendResult.value = "Toplu gönderim başlatıldı! ${companyIds.size} şirkete e-posta gönderilecek."
    }

    /**
     * Cancel bulk email sending
     */
    fun cancelBulkSend() {
        BulkEmailWorker.cancelBulkEmail(getApplication())
        _sendResult.value = "Toplu gönderim iptal edildi."
    }

    /**
     * Save email as template
     */
    fun saveAsTemplate(name: String) {
        viewModelScope.launch {
            val template = EmailTemplate(
                name = name,
                subject = _subject.value,
                body = _body.value
            )
            emailDao.insertTemplate(template)
        }
    }

    /**
     * Load template
     */
    fun loadTemplate(template: EmailTemplate) {
        _subject.value = template.subject
        _body.value = template.body
    }

    /**
     * Delete template
     */
    fun deleteTemplate(template: EmailTemplate) {
        viewModelScope.launch {
            emailDao.deleteTemplate(template)
        }
    }

    fun clearResult() {
        _sendResult.value = null
    }
}
