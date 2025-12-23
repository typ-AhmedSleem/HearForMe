package com.typ.hearforme.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.typ.hearforme.presentation.alerts.AlertOverlay
import com.typ.hearforme.presentation.communication.CommunicationScreen
import com.typ.hearforme.presentation.dashboard.DashboardScreen
import com.typ.hearforme.presentation.dashboard.DashboardViewModel
import com.typ.hearforme.presentation.history.HistoryScreen
import com.typ.hearforme.presentation.onboarding.OnboardingScreen
import com.typ.hearforme.presentation.settings.SettingsScreen
import com.typ.hearforme.presentation.welcome.WelcomeScreen
import org.koin.compose.viewmodel.koinViewModel

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Onboarding : Screen("onboarding/{step}") {
        fun createRoute(step: Int) = "onboarding/$step"
    }

    object Dashboard : Screen("dashboard")
    object Settings : Screen("settings")
    object History : Screen("history")
    object Communication : Screen("communication")
}

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val activeAlert by viewModel.activeAlert.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    Box {
        NavHost(
            navController = navController,
            startDestination = Screen.Welcome.route
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onGetStarted = {
                        navController.navigate(Screen.Onboarding.createRoute(1))
                    }
                )
            }

            composable(Screen.Onboarding.route) { backStackEntry ->
                val step = backStackEntry.arguments?.getString("step")?.toIntOrNull() ?: 1
                OnboardingScreen(
                    step = step,
                    onNext = {
                        if (step < 4) {
                            navController.navigate(Screen.Onboarding.createRoute(step + 1))
                        } else {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    currentEvent = activeAlert,
                    history = history,
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    },
                    onNavigateToCommunication = {
                        navController.navigate(Screen.Communication.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Communication.route) {
                CommunicationScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Overlay is show on top of any screen
        activeAlert?.let { alert ->
            AlertOverlay(
                event = alert,
                onDismiss = { viewModel.dismissAlert() }
            )
        }
    }
}
