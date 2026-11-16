package com.jobhunter.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jobhunter.app.services.AiService
import com.jobhunter.app.services.GmailService
import com.jobhunter.app.services.SmtpEmailService
import com.jobhunter.app.ui.navigation.Screen
import com.jobhunter.app.ui.navigation.bottomNavItems
import com.jobhunter.app.ui.screens.*
import com.jobhunter.app.ui.theme.JobHunterTheme
import com.jobhunter.app.utils.AppLogger
import com.jobhunter.app.viewmodels.*
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    private lateinit var logger: AppLogger
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger = AppLogger.getInstance(this)
        logger.i("App", "Uygulama başlatıldı v5.0.0")
        enableEdgeToEdge()
        setContent {
            JobHunterTheme {
                JobHunterApp()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        logger.i("App", "Uygulama kapatıldı")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobHunterApp() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val dashboardViewModel: DashboardViewModel = viewModel()
    val companySearchViewModel: CompanySearchViewModel = viewModel()
    val emailViewModel: EmailViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val platformJobSearchViewModel: PlatformJobSearchViewModel = viewModel()

    val context = androidx.compose.ui.platform.LocalContext.current
    val gmailService = remember { GmailService(context) }
    val smtpService = remember { SmtpEmailService(context) }
    val aiService = remember { AiService(context) }
    val logger = remember { AppLogger.getInstance(context) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, maxLines = 1) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                logger.logButtonClick(screen.title, "NavigationBar")
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Screen.Dashboard.route) {
                Row {
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.AiAssistant.route) },
                        containerColor = androidx.compose.ui.graphics.Color(0xFF00D4FF),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Asistan")
                    }
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.Settings.route) },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToSearch = { navController.navigate(Screen.PlatformJobSearch.route) },
                    onNavigateToCompanies = { navController.navigate(Screen.Companies.route) },
                    onNavigateToEmail = { navController.navigate(Screen.Email.route) },
                    onNavigateToApplications = { navController.navigate(Screen.ApplicationTracking.route) },
                    onNavigateToWebView = { platform ->
                        navController.navigate(Screen.PlatformWebView.createRoute(platform))
                    },
                    onNavigateToAiAssistant = { navController.navigate(Screen.AiAssistant.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToCvImport = { navController.navigate(Screen.CvImport.route) },
                    onNavigateToCompanyResearch = { navController.navigate(Screen.CompanyResearch.route) }
                )
            }

            composable(Screen.PlatformJobSearch.route) {
                PlatformJobSearchScreen(
                    viewModel = platformJobSearchViewModel,
                    onNavigateToWebView = { platform ->
                        navController.navigate(Screen.PlatformWebView.createRoute(platform))
                    }
                )
            }

            composable(Screen.PlatformWebView.route) { backStackEntry ->
                val platform = backStackEntry.arguments?.getString("platform") ?: "kariyer"
                val platformConfig = when (platform) {
                    "kariyer" -> PlatformConfig("Kariyer.net", "\uD83C\uDFAF", androidx.compose.ui.graphics.Color(0xFF1976D2), "https://www.kariyer.net/is-ilanlari", "Türkiye'nin en büyük iş platformu")
                    "linkedin" -> PlatformConfig("LinkedIn", "\uD83D\uDCBC", androidx.compose.ui.graphics.Color(0xFF0077B5), "https://www.linkedin.com/jobs/search/", "Profesyonel iş ağı")
                    "indeed" -> PlatformConfig("Indeed", "\uD83D\uDD0D", androidx.compose.ui.graphics.Color(0xFF2164F3), "https://tr.indeed.com/jobs", "Dünyanın en büyük iş sitesi")
                    "secretcv" -> PlatformConfig("Secretcv.com", "\uD83D\uDCDD", androidx.compose.ui.graphics.Color(0xFFE91E63), "https://www.secretcv.com/is-ilanlari", "Alternatif Turk is platformu")
                    "eleman" -> PlatformConfig("Eleman.net", "\uD83C\uDFF3", androidx.compose.ui.graphics.Color(0xFFFF6B00), "https://www.eleman.net/is-ilanlari", "Turk is platformu")
                    "yenibiris" -> PlatformConfig("Yenibiris.com", "\uD83D\uDCDD", androidx.compose.ui.graphics.Color(0xFF00BCD4), "https://www.yenibiris.com/is-ilanlari", "Turk is platformu")
                    "isbul" -> PlatformConfig("Isbul.net", "\uD83D\uDD0D", androidx.compose.ui.graphics.Color(0xFF795548), "https://www.isbul.net", "Turk is platformu")
                    else -> PlatformConfig("Google Jobs", "\uD83C\uDF10", androidx.compose.ui.graphics.Color(0xFF4285F4), "https://www.google.com/search?q=jobs+near+me", "Google'ın iş arama motoru")
                }
                PlatformWebView(platform = platformConfig, onBack = { navController.popBackStack() })
            }

            composable(Screen.Companies.route) {
                CompaniesScreen(companyViewModel = companySearchViewModel, emailViewModel = emailViewModel)
            }

            composable(Screen.Email.route) {
                EmailScreen(viewModel = emailViewModel)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToProfileSetup = { navController.navigate(Screen.ProfileSetup.route) }
                )
            }

            composable(Screen.ProfileSetup.route) {
                ProfileSetupScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.ApplicationTracking.route) {
                val factory = JobHunterViewModelFactory(context.applicationContext as android.app.Application)
                val vm: ApplicationTrackingViewModel = viewModel(factory = factory)
                ApplicationTrackingScreen(viewModel = vm)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    gmailService = gmailService,
                    aiService = aiService,
                    smtpService = smtpService,
                    onNavigateToSmtpSettings = { navController.navigate(Screen.SmtpSettings.route) }
                )
            }

            composable(Screen.SmtpSettings.route) {
                SmtpSettingsScreen(smtpService = smtpService)
            }

            composable(Screen.CvImport.route) {
                CvImportScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.CompanyResearch.route) {
                CompanyResearchScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.AiAssistant.route) {
                AiAssistantScreen()
            }

            composable(Screen.BulkApply.route) { backStackEntry ->
                val jobIdsJson = backStackEntry.arguments?.getString("jobIds") ?: "[]"
                BulkApplyScreen(
                    selectedJobs = emptyList(), // TODO: decode jobIdsJson
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CoverLetterEdit.route) {
                CoverLetterEditScreen(
                    coverLetter = it.arguments?.getString("coverLetter") ?: "",
                    jobTitle = it.arguments?.getString("jobTitle") ?: "",
                    company = it.arguments?.getString("company") ?: "",
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.JobDetail.route) { backStackEntry ->
                JobDetailScreen(
                    jobTitle = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", "UTF-8"),
                    company = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("company") ?: "", "UTF-8"),
                    location = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("location") ?: "", "UTF-8"),
                    url = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", "UTF-8"),
                    source = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("source") ?: "", "UTF-8"),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
