package com.blacksmith.sucra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.ZoneId


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
fun LogScreen(
    onBack: () -> Unit,
    viewModel: LogViewModel = viewModel()
) {
    val entries = viewModel.logEntries
    val isLoading = viewModel.isLoading
    val backendError = viewModel.errorMessage

    var showEntryForm by remember { mutableStateOf(false) }
    var bgText by remember { mutableStateOf("") }
    var insulinText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var showInsulinField by remember { mutableStateOf(false) }
    var showCarbField by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            SucraTopBar(
                title = "Log",
                showBack = true,
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent entries",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = { showEntryForm = !showEntryForm }
                ) {
                    Text(if (showEntryForm) "Close" else "New entry")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Entry form (collapsed by default)
            if (showEntryForm) {
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Add log entry",
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = bgText,
                            onValueChange = { bgText = it },
                            label = { Text("Blood glucose (mg/dL)*") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.material3.TextButton(
                                onClick = { showInsulinField = !showInsulinField }
                            ) {
                                Text(
                                    if (showInsulinField) "Remove insulin" else "Add insulin"
                                )
                            }
                            androidx.compose.material3.TextButton(
                                onClick = { showCarbField = !showCarbField }
                            ) {
                                Text(
                                    if (showCarbField) "Remove carbs" else "Add carbs"
                                )
                            }
                        }

                        if (showInsulinField) {
                            OutlinedTextField(
                                value = insulinText,
                                onValueChange = { insulinText = it },
                                label = { Text("Insulin (units)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (showCarbField) {
                            OutlinedTextField(
                                value = carbsText,
                                onValueChange = { carbsText = it },
                                label = { Text("Carbohydrates (g)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        validationError?.let { msg ->
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } ?: backendError?.let { msg ->
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Button(
                            onClick = {
                                val bg = bgText.toIntOrNull()
                                if (bg == null) {
                                    validationError =
                                        "Please enter a valid blood glucose value."
                                    return@Button
                                }

                                var insulinValue: Double? = null
                                if (showInsulinField) {
                                    if (insulinText.isBlank()) {
                                        validationError =
                                            "Please enter insulin units or remove the insulin field."
                                        return@Button
                                    }
                                    insulinValue = insulinText.toDoubleOrNull()
                                    if (insulinValue == null) {
                                        validationError =
                                            "Please enter a valid insulin amount."
                                        return@Button
                                    }
                                }

                                var carbsValue: Double? = null
                                if (showCarbField) {
                                    if (carbsText.isBlank()) {
                                        validationError =
                                            "Please enter carbs or remove the carbs field."
                                        return@Button
                                    }
                                    carbsValue = carbsText.toDoubleOrNull()
                                    if (carbsValue == null) {
                                        validationError =
                                            "Please enter a valid carbs amount."
                                        return@Button
                                    }
                                }

                                validationError = null
                                viewModel.addEntry(bg, insulinValue, carbsValue)

                                // Clear form & collapse
                                bgText = ""
                                insulinText = ""
                                carbsText = ""
                                showInsulinField = false
                                showCarbField = false
                                showEntryForm = false
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(end = 8.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            Text("Save entry")
                        }
                    }
                }
            }

            if (entries.isEmpty()) {
                Text(
                    text = "No entries yet. Add your first log.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entries) { entry ->
                        LogEntryRow(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryRow(entry: LogEntry) {
    val formattedTime = remember(entry.timestamp) {
        val instant = Instant.ofEpochMilli(entry.timestamp)
        val zoned = instant.atZone(ZoneId.systemDefault())
        val date = zoned.toLocalDate()
        val time = zoned.toLocalTime().withSecond(0).withNano(0)
        "$date $time"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "BG: ${entry.bloodGlucose} mg/dL",
            style = MaterialTheme.typography.bodyLarge
        )

        val parts = buildList {
            entry.insulinUnits?.let { add("Insulin: $it U") }
            entry.carbsGrams?.let { add("Carbs: $it g") }
        }

        Text(
            text = if (parts.isEmpty()) "No insulin/carb recorded" else parts.joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}



@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            SucraTopBar(
                title = "Calculator",
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
            Text(
                text = "TODO: Build insulin / carb / correction calculator UI.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            SucraTopBar(
                title = "Settings",
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
            Text(
                text = "TODO: Configure targets, ratios, and units.",
                style = MaterialTheme.typography.bodyLarge
            )
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
