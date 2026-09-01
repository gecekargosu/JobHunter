package com.jobhunter.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Ana Ekran", Icons.Default.Home)
    data object CompanySearch : Screen("company_search", "Şirket Ara", Icons.Default.Search)
    data object Companies : Screen("companies", "Şirketler", Icons.Default.Business)
    data object Email : Screen("email", "E-posta", Icons.Default.Email)
    data object Profile : Screen("profile", "Profilim", Icons.Default.Person)
    data object ApplicationTracking : Screen("applications", "Başvurular", Icons.Default.Assignment)
    data object Settings : Screen("settings", "Ayarlar", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.CompanySearch,
    Screen.Companies,
    Screen.Email,
    Screen.ApplicationTracking
)
