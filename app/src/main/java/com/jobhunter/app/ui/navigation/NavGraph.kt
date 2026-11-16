package com.jobhunter.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Ana Ekran", Icons.Default.Home)
    data object PlatformJobSearch : Screen("platform_job_search", "İş Ara", Icons.Default.Search)
    data object PlatformWebView : Screen("platform_webview/{platform}", "Platform", Icons.Default.Language) {
        fun createRoute(platform: String) = "platform_webview/$platform"
    }
    data object Email : Screen("email", "E-posta", Icons.Default.Email)
    data object Profile : Screen("profile", "Profilim", Icons.Default.Person)
    data object ProfileSetup : Screen("profile_setup", "Profil Ayarları", Icons.Default.Edit)
    data object ApplicationTracking : Screen("applications", "Başvurular", Icons.Default.Assignment)
    data object Settings : Screen("settings", "Ayarlar", Icons.Default.Settings)
    data object SmtpSettings : Screen("smtp_settings", "E-posta Ayarları", Icons.Default.Mail)
    data object Companies : Screen("companies", "Şirketler", Icons.Default.Business)
    data object AiAssistant : Screen("ai_assistant", "AI Asistan", Icons.Default.AutoAwesome)
    data object CvImport : Screen("cv_import", "CV Yukle", Icons.Default.Upload)
    data object CompanyResearch : Screen("company_research", "Sirket Arastirma", Icons.Default.Business)
    data object BulkApply : Screen("bulk_apply/{jobIds}", "Toplu Başvuru", Icons.Default.Send) {
        fun createRoute(jobIds: String) = "bulk_apply/$jobIds"
    }
    data object CoverLetterEdit : Screen("cover_letter_edit", "On Yazi Duzenle", Icons.Default.Edit)
    data object JobDetail : Screen("job_detail/{title}/{company}/{location}/{url}/{source}", "Is Detayı", Icons.Default.Article) {
        fun createRoute(title: String, company: String, location: String, url: String, source: String): String {
            return "job_detail/${java.net.URLEncoder.encode(title, "UTF-8")}/${java.net.URLEncoder.encode(company, "UTF-8")}/${java.net.URLEncoder.encode(location, "UTF-8")}/${java.net.URLEncoder.encode(url, "UTF-8")}/${java.net.URLEncoder.encode(source, "UTF-8")}"
        }
    }
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.PlatformJobSearch,
    Screen.Companies,
    Screen.Email,
    Screen.ApplicationTracking,
    Screen.Profile
)
