package com.blacksmith.sucra.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.blacksmith.sucra.ui.screens.HomeScreen
import com.blacksmith.sucra.ui.screens.LogScreen
import com.blacksmith.sucra.ui.screens.CalculatorScreen
import com.blacksmith.sucra.ui.screens.AlarmsScreen
import com.blacksmith.sucra.ui.screens.SettingsScreen
import com.blacksmith.sucra.ui.screens.AuthRoute


enum class SucraDestination {
    HOME,
    LOG,
    CALCULATOR,
    ALARMS,
    SETTINGS,
    AUTH
}

@Composable
fun SucraNavHost(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomItems = listOf(
        SucraDestination.HOME,
        SucraDestination.CALCULATOR,
        SucraDestination.LOG,
        SucraDestination.ALARMS
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = SucraDestination.HOME.name
            ) {
                composable(SucraDestination.HOME.name) {
                    HomeScreen(
                        onNavigateToCalculator = {
                            navController.navigate(SucraDestination.CALCULATOR.name)
                        },
                        onNavigateToSettings = {
                            navController.navigate(SucraDestination.SETTINGS.name)
                        }
                    )
                }

                composable(SucraDestination.LOG.name) {
                    LogScreen(onBack = { /* bottom nav handles navigation */ })
                }

                composable(SucraDestination.CALCULATOR.name) {
                    CalculatorScreen(
                        onBack = { /* bottom nav handles navigation */ },
                        onNavigateToLog = {
                            navController.navigate(SucraDestination.LOG.name) {
                                popUpTo(SucraDestination.HOME.name) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(SucraDestination.ALARMS.name) {
                    AlarmsScreen(onBack = { /* bottom nav handles navigation */ })
                }

                composable(SucraDestination.SETTINGS.name) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToAuth = {
                            navController.navigate(SucraDestination.AUTH.name)
                        }
                    )
                }

                composable(SucraDestination.AUTH.name) {
                    AuthRoute(
                        onAuthSuccess = {
                            // After sign in / sign up, go back to Settings
                            navController.popBackStack()
                        }
                    )
                }
            }
        }

        if (currentRoute != SucraDestination.SETTINGS.name &&
            currentRoute != SucraDestination.AUTH.name
        ) {
            NavigationBar {
                bottomItems.forEach { destination ->
                    val selected = currentRoute == destination.name
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(destination.name) {
                                    popUpTo(SucraDestination.HOME.name) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            when (destination) {
                                SucraDestination.HOME ->
                                    Icon(Icons.Filled.Home, contentDescription = "Home")
                                SucraDestination.CALCULATOR ->
                                    Icon(Icons.Filled.Home, contentDescription = "Calculator")
                                SucraDestination.LOG ->
                                    Icon(Icons.Filled.List, contentDescription = "Log")
                                SucraDestination.ALARMS ->
                                    Icon(Icons.Filled.List, contentDescription = "Alarms")
                                else -> {}
                            }
                        },
                        label = {
                            Text(
                                when (destination) {
                                    SucraDestination.HOME -> "Home"
                                    SucraDestination.CALCULATOR -> "Calc"
                                    SucraDestination.LOG -> "Log"
                                    SucraDestination.ALARMS -> "Alarms"
                                    else -> ""
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}