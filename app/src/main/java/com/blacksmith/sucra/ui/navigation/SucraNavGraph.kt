package com.blacksmith.sucra.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.blacksmith.sucra.ui.screens.AuthRoute
import com.blacksmith.sucra.ui.screens.CalculatorScreen
import com.blacksmith.sucra.ui.screens.HomeScreen
import com.blacksmith.sucra.ui.screens.LogScreen
import com.blacksmith.sucra.ui.screens.SettingsScreen

enum class SucraDestination {
    AUTH,
    HOME,
    LOG,
    CALCULATOR,
    SETTINGS
}

@Composable
fun SucraNavHost(
    navController: NavHostController,
    startDestination: SucraDestination
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.name
    ) {
        composable(SucraDestination.AUTH.name) {
            AuthRoute(
                onAuthSuccess = {
                    navController.navigate(SucraDestination.HOME.name) {
                        // clear auth from back stack so back button doesn't go back to the login screen
                        popUpTo(SucraDestination.AUTH.name) { inclusive = true }
                    }
                }
            )
        }

        composable(SucraDestination.HOME.name) {
            HomeScreen(
                onNavigateToLog = { navController.navigate(SucraDestination.LOG.name) },
                onNavigateToCalculator = { navController.navigate(SucraDestination.CALCULATOR.name) },
                onNavigateToSettings = { navController.navigate(SucraDestination.SETTINGS.name) }
            )
        }

        composable(SucraDestination.LOG.name) {
            LogScreen(onBack = { navController.popBackStack() })
        }

        composable(SucraDestination.CALCULATOR.name) {
            CalculatorScreen(onBack = { navController.popBackStack() })
        }

        composable(SucraDestination.SETTINGS.name) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
