package com.jobhunter.app.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Merkezi loglama sistemi
 * Tüm uygulama işlemleri bu sınıf üzerinden loglanır
 */
class AppLogger private constructor(context: Context) {

    companion object {
        private const val TAG = "JobHunter"
        private const val LOG_FILE = "jobhunter_log.txt"
        private const val MAX_LOG_SIZE = 5 * 1024 * 1024 // 5MB

        @Volatile
        private var instance: AppLogger? = null

        fun getInstance(context: Context): AppLogger {
            return instance ?: synchronized(this) {
                instance ?: AppLogger(context.applicationContext).also { instance = it }
            }
        }
    }

    private val logDir: File = File(context.filesDir, "logs")
    private val logFile: File
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        logFile = File(logDir, LOG_FILE)
    }

    // ===== LOG SEVİYELERİ =====

    fun d(tag: String, message: String) {
        Log.d("$TAG:$tag", message)
        writeToFile("DEBUG", tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i("$TAG:$tag", message)
        writeToFile("INFO", tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w("$TAG:$tag", message)
        writeToFile("WARN", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$TAG:$tag", message, throwable)
        writeToFile("ERROR", tag, message, throwable)
    }

    // ===== ÖZEL LOG METODLARI =====

    /**
     * Ekran açılışını logla
     */
    fun logScreenOpen(screenName: String) {
        i("SCREEN", "Ekran açıldı: $screenName")
    }

    /**
     * Buton tıklamasını logla
     */
    fun logButtonClick(buttonName: String, screen: String) {
        d("UI", "Buton tıklandı: $buttonName ($screen)")
    }

    /**
     * API çağrısını logla
     */
    fun logApiCall(provider: String, endpoint: String, success: Boolean, duration: Long = 0) {
        val status = if (success) "BAŞARILI" else "BAŞARISIZ"
        i("API", "$provider - $endpoint [$status] (${duration}ms)")
    }

    /**
     * Veritabanı işlemini logla
     */
    fun logDatabase(operation: String, table: String, details: String = "") {
        d("DATABASE", "$operation - $table $details")
    }

    /**
     * AI çağrısını logla
     */
    fun logAiCall(provider: String, promptLength: Int, responseLength: Int, success: Boolean) {
        val status = if (success) "BAŞARILI" else "BAŞARISIZ"
        i("AI", "$provider - Prompt: ${promptLength}char, Response: ${responseLength}char [$status]")
    }

    /**
     * E-posta gönderimini logla
     */
    fun logEmailSend(to: String, subject: String, method: String, success: Boolean) {
        val status = if (success) "BAŞARILI" else "BAŞARISIZ"
        i("EMAIL", "Gönderim: $to - \"$subject\" [$method] [$status]")
    }

    /**
     * PDF oluşturmayı logla
     */
    fun logPdfGeneration(fileName: String, success: Boolean, fileSize: Long = 0) {
        val status = if (success) "BAŞARILI" else "BAŞARISIZ"
        i("PDF", "Oluşturma: $fileName [$status] (${fileSize} bytes)")
    }

    /**
     * Arama işlemini logla
     */
    fun logSearch(query: String, provider: String, resultCount: Int) {
        i("SEARCH", "Arama: \"$query\" [$provider] - $resultCount sonuç")
    }

    /**
     * Hata detayını logla
     */
    fun logError(category: String, operation: String, error: String, throwable: Throwable? = null) {
        e("ERROR", "[$category] $operation: $error", throwable)
    }

    /**
     * Kullanıcı işlemini logla
     */
    fun logUserAction(action: String, details: String = "") {
        i("USER", "$action $details".trim())
    }

    // ===== DOSYA YAZMA =====

    private fun writeToFile(level: String, tag: String, message: String, throwable: Throwable? = null) {
        try {
            // Log boyutu kontrolü
            if (logFile.exists() && logFile.length() > MAX_LOG_SIZE) {
                rotateLogFile()
            }

            val timestamp = dateFormat.format(Date())
            val logEntry = StringBuilder().apply {
                append("[$timestamp] ")
                append("[$level] ")
                append("[$tag] ")
                append(message)
                append("\n")
                throwable?.let {
                    append("  HATA: ${it.message}\n")
                    it.stackTrace.take(5).forEach { element ->
                        append("    at $element\n")
                    }
                }
            }

            FileWriter(logFile, true).use { writer ->
                writer.append(logEntry)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Log yazma hatası", e)
        }
    }

    private fun rotateLogFile() {
        try {
            val archiveName = "jobhunter_log_${fileDateFormat.format(Date())}.txt"
            val archiveFile = File(logDir, archiveName)
            if (logFile.exists()) {
                logFile.renameTo(archiveFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Log rotasyon hatası", e)
        }
    }

    // ===== RAPOR OLUŞTURMA =====

    fun getLogContent(): String {
        return if (logFile.exists()) {
            logFile.readText()
        } else {
            "Log dosyası bulunamadı"
        }
    }

    fun getLogStats(): Map<String, Int> {
        val content = getLogContent()
        return mapOf(
            "DEBUG" to content.lines().count { it.contains("[DEBUG]") },
            "INFO" to content.lines().count { it.contains("[INFO]") },
            "WARN" to content.lines().count { it.contains("[WARN]") },
            "ERROR" to content.lines().count { it.contains("[ERROR]") }
        )
    }

    fun clearLogs() {
        try {
            if (logFile.exists()) {
                logFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Log temizleme hatası", e)
        }
    }
}
