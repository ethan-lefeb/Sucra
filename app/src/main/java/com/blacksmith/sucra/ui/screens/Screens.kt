// app/src/main/java/com/blacksmith/sucra/ui/screens/Screens.kt
package com.blacksmith.sucra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToLog: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            SucraTopBar(
                title = "Sucra",
                showBack = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Sucra",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Your all-in-one diabetes companion",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNavigateToCalculator,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Calculator")
            }

            Button(
                onClick = onNavigateToLog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Log")
            }

            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Settings")
            }
        }
    }
}

@Composable
fun LogScreen(onBack: () -> Unit) {
    SimpleScreen(
        title = "Log",
        content = "TODO: Log blood glucose, carbs, insulin, and exercise here.",
        onBack = onBack
    )
}

@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    SimpleScreen(
        title = "Calculator",
        content = "TODO: Build insulin / carb / correction calculator UI.",
        onBack = onBack
    )
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    SimpleScreen(
        title = "Settings",
        content = "TODO: Configure targets, ratios, and units.",
        onBack = onBack
    )
}

@Composable
private fun SimpleScreen(
    title: String,
    content: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            SucraTopBar(
                title = title,
                showBack = true,
                onBack = onBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = content, style = MaterialTheme.typography.bodyLarge)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SucraTopBar(
    title: String,
    showBack: Boolean,
    onBack: (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (showBack && onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }
    )
}
