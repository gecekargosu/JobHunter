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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jobhunter.app.services.AiService
import com.jobhunter.app.services.GmailService
import com.jobhunter.app.ui.navigation.Screen
import com.jobhunter.app.ui.navigation.bottomNavItems
import com.jobhunter.app.ui.screens.*
import com.jobhunter.app.ui.theme.JobHunterTheme
import com.jobhunter.app.utils.AppLogger
import com.jobhunter.app.viewmodels.*

class MainActivity : ComponentActivity() {
    
    private lateinit var logger: AppLogger
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger = AppLogger.getInstance(this)
        logger.i("App", "Uygulama başlatıldı")
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

    // ViewModels
    val dashboardViewModel: DashboardViewModel = viewModel()
    val companySearchViewModel: CompanySearchViewModel = viewModel()
    val emailViewModel: EmailViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    // Services (would need proper DI in production)
    val context = androidx.compose.ui.platform.LocalContext.current
    val gmailService = remember { GmailService(context) }
    val aiService = remember { AiService(context) }
    val logger = remember { AppLogger.getInstance(context) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
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
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Settings.route) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
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
                    onNavigateToSearch = { navController.navigate(Screen.CompanySearch.route) },
                    onNavigateToCompanies = { navController.navigate(Screen.Companies.route) },
                    onNavigateToEmail = { navController.navigate(Screen.Email.route) }
                )
            }

            composable(Screen.CompanySearch.route) {
                CompanySearchScreen(viewModel = companySearchViewModel)
            }

            composable(Screen.Companies.route) {
                CompaniesScreen(
                    companyViewModel = companySearchViewModel,
                    emailViewModel = emailViewModel
                )
            }

            composable(Screen.Email.route) {
                EmailScreen(viewModel = emailViewModel)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = profileViewModel)
            }

            composable(Screen.ApplicationTracking.route) {
                val applicationTrackingViewModel: ApplicationTrackingViewModel = viewModel()
                ApplicationTrackingScreen(viewModel = applicationTrackingViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    gmailService = gmailService,
                    aiService = aiService
                )
            }
        }
    }
}
