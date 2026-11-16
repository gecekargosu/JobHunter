package com.jobhunter.app.services

import android.content.Context
import com.jobhunter.app.utils.AppLogger

/**
 * LinkedIn Servisi - Profil bilgilerini tutar
 */
class LinkedInService(private val context: Context) {

    private val logger = AppLogger.getInstance(context)
    private val prefs = context.getSharedPreferences("linkedin_prefs", Context.MODE_PRIVATE)

    fun isConfigured(): Boolean {
        return getLinkedInUrl().isNotEmpty()
    }

    fun getLinkedInUrl(): String {
        return prefs.getString("linkedin_url", "") ?: ""
    }

    fun saveLinkedInUrl(url: String) {
        prefs.edit().putString("linkedin_url", url).apply()
        logger.i("LinkedIn", "LinkedIn URL kaydedildi: $url")
    }

    fun disconnect() {
        prefs.edit().clear().apply()
        logger.i("LinkedIn", "LinkedIn bağlantısı kesildi")
    }
}
