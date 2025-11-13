package com.blacksmith.sucra.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.blacksmith.sucra.ui.screens.CalculatorScreen
import com.blacksmith.sucra.ui.screens.HomeScreen
import com.blacksmith.sucra.ui.screens.LogScreen
import com.blacksmith.sucra.ui.screens.SettingsScreen

enum class SucraDestination {
    HOME,
    LOG,
    CALCULATOR,
    SETTINGS
}

@Composable
fun SucraNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = SucraDestination.HOME.name
    ) {
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
