package com.blacksmith.sucra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt


@Composable
fun HomeScreen(
    onNavigateToCalculator: () -> Unit,
    onNavigateToSettings: () -> Unit,
    logViewModel: LogViewModel = viewModel(),
    alarmsViewModel: AlarmsViewModel = viewModel()
) {
    val entries = logViewModel.logEntries
    val alarms = alarmsViewModel.alarms

    Scaffold(
        topBar = {
            SucraTopBar(
                title = "Dashboard",
                showBack = false,
                actions = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Today at a glance",
                        style = MaterialTheme.typography.titleMedium
                    )

                    val today = LocalDate.now()
                    val todaysEntries = entries.filter {
                        val instant = Instant.ofEpochMilli(it.timestamp)
                        instant.atZone(ZoneId.systemDefault()).toLocalDate() == today
                    }

                    if (todaysEntries.isEmpty()) {
                        Text(
                            text = "No readings logged yet today.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        val avgBg =
                            todaysEntries.map { it.bloodGlucose }.average().roundToInt()
                        Text(
                            text = "Average BG today: $avgBg mg/dL",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Entries today: ${todaysEntries.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Today's glucose trend",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TodayGlucoseGraph(entries = entries)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Upcoming alarms",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (alarms.isEmpty()) {
                        Text(
                            text = "No alarms set.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        alarms.take(3).forEach { alarm ->
                            val hh = alarm.hour.toString().padStart(2, '0')
                            val mm = alarm.minute.toString().padStart(2, '0')
                            Text(
                                text = "$hh:$mm – ${alarm.label}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (alarms.size > 3) {
                            Text(
                                text = "+ ${alarms.size - 3} more",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onNavigateToCalculator,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open calculator")
            }
        }
    }
}

@Composable
private fun TodayGlucoseGraph(entries: List<LogEntry>) {
    val today = LocalDate.now()
    val todaysValues = entries
        .filter {
            val instant = Instant.ofEpochMilli(it.timestamp)
            instant.atZone(ZoneId.systemDefault()).toLocalDate() == today
        }
        .sortedBy { it.timestamp }
        .map { it.bloodGlucose }

    if (todaysValues.isEmpty()) {
        Text(
            text = "No data for today yet.",
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    val max = todaysValues.maxOrNull() ?: 1
    val min = todaysValues.minOrNull() ?: 0
    val range = (max - min).coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        todaysValues.forEach { value ->
            val normalized = (value - min).toFloat() / range.toFloat()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(normalized)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            )
        }
    }

    Text(
        text = "Min: $min   Max: $max",
        style = MaterialTheme.typography.bodySmall
    )
}


@Composable
fun AlarmsScreen(
    onBack: () -> Unit,
    viewModel: AlarmsViewModel = viewModel()
) {
    val alarms = viewModel.alarms
    val isLoading = viewModel.isLoading
    val backendError = viewModel.errorMessage

    var showNewAlarmForm by remember { mutableStateOf(false) }
    var hourText by remember { mutableStateOf("") }
    var minuteText by remember { mutableStateOf("") }
    var labelText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            SucraTopBar(
                title = "Alarms",
                showBack = false // bottom nav handles main nav
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reminders to check",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { showNewAlarmForm = !showNewAlarmForm }) {
                    Text(if (showNewAlarmForm) "Close" else "New alarm")
                }
            }

            if (showNewAlarmForm) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Add alarm",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = hourText,
                                onValueChange = { hourText = it },
                                label = { Text("Hour (0–23)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = minuteText,
                                onValueChange = { minuteText = it },
                                label = { Text("Minute (0–59)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = labelText,
                            onValueChange = { labelText = it },
                            label = { Text("Label") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        val errorToShow = validationError ?: backendError
                        if (errorToShow != null) {
                            Text(
                                text = errorToShow,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Button(
                            onClick = {
                                val hour = hourText.toIntOrNull()
                                val minute = minuteText.toIntOrNull()
                                if (hour == null || minute == null) {
                                    validationError = "Please enter a valid time."
                                } else {
                                    validationError = null
                                    viewModel.addAlarm(hour, minute, labelText)
                                    hourText = ""
                                    minuteText = ""
                                    labelText = ""
                                    showNewAlarmForm = false
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save alarm")
                        }
                    }
                }
            }

            if (alarms.isEmpty()) {
                Text(
                    text = "No alarms yet. Add your first reminder above.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(alarms) { alarm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val hh = alarm.hour.toString().padStart(2, '0')
                            val mm = alarm.minute.toString().padStart(2, '0')
                            Column {
                                Text(
                                    text = "$hh:$mm",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = alarm.label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            TextButton(onClick = { viewModel.deleteAlarm(alarm) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
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
    val backendError = viewModel.errorMessage
    val isLoading = viewModel.isLoading

    var showForm by remember { mutableStateOf(false) }
    var bgText by remember { mutableStateOf("") }
    var showInsulin by remember { mutableStateOf(false) }
    var insulinText by remember { mutableStateOf("") }
    var showCarbs by remember { mutableStateOf(false) }
    var carbsText by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    var entryToDelete by remember { mutableStateOf<LogEntry?>(null) }

    Scaffold(
        topBar = {
            SucraTopBar(
                title = "Log",
                showBack = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Entries",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { showForm = !showForm }) {
                    Text(if (showForm) "Close form" else "New entry")
                }
            }

            if (showForm) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = bgText,
                            onValueChange = { bgText = it },
                            label = { Text("Blood glucose (mg/dL)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(onClick = { showInsulin = !showInsulin }) {
                                Text(
                                    if (showInsulin) "Hide insulin" else "Add insulin dose"
                                )
                            }
                            TextButton(onClick = { showCarbs = !showCarbs }) {
                                Text(
                                    if (showCarbs) "Hide carbs" else "Add carb amount"
                                )
                            }
                        }

                        if (showInsulin) {
                            OutlinedTextField(
                                value = insulinText,
                                onValueChange = { insulinText = it },
                                label = { Text("Insulin (units)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (showCarbs) {
                            OutlinedTextField(
                                value = carbsText,
                                onValueChange = { carbsText = it },
                                label = { Text("Carbs (g)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        val errorToShow = localError ?: backendError
                        if (errorToShow != null) {
                            Text(
                                text = errorToShow,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Button(
                            onClick = {
                                val bg = bgText.toIntOrNull()
                                if (bg == null) {
                                    localError = "Please enter a valid blood glucose value."
                                    return@Button
                                }

                                val insulin = insulinText.toDoubleOrNull()
                                    .takeIf { showInsulin && insulinText.isNotBlank() }
                                val carbs = carbsText.toDoubleOrNull()
                                    .takeIf { showCarbs && carbsText.isNotBlank() }

                                localError = null
                                viewModel.addEntry(
                                    bloodGlucose = bg,
                                    insulinUnits = insulin,
                                    carbsGrams = carbs
                                )

                                bgText = ""
                                insulinText = ""
                                carbsText = ""
                                showInsulin = false
                                showCarbs = false
                                showForm = false
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save entry")
                        }
                    }
                }
            }

            if (entries.isEmpty()) {
                Text(
                    text = "No entries yet.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entries) { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val instant = Instant.ofEpochMilli(entry.timestamp)
                                val localDateTime =
                                    instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${localDateTime.toLocalDate()} " +
                                                    "%02d:%02d".format(
                                                        localDateTime.hour,
                                                        localDateTime.minute
                                                    ),
                                            style = MaterialTheme.typography.bodySmall
                                        )

                                        Text(
                                            text = "BG: ${entry.bloodGlucose} mg/dL",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        entry.insulinUnits?.let {
                                            Text(
                                                text = "Insulin: $it u",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        entry.carbsGrams?.let {
                                            Text(
                                                text = "Carbs: $it g",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }

                                    TextButton(onClick = { entryToDelete = entry }) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        entryToDelete?.let { entry ->
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                title = { Text("Delete entry?") },
                text = {
                    Text(
                        "This will permanently delete this log entry. " +
                                "This action cannot be undone."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteEntry(entry)
                            entryToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { entryToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}




@Composable
fun CalculatorScreen(
    onBack: () -> Unit,
    logViewModel: LogViewModel = viewModel(),
    onNavigateToLog: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings = settingsViewModel.currentSettingsOrDefaults()
    val backendError = logViewModel.errorMessage

    var bgText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var resultUnits by remember { mutableStateOf<Double?>(null) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            SucraTopBar(
                title = "Calculator",
                showBack = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Using carb ratio ${settings.carbRatio} g/unit " +
                        "and glucose ratio ${settings.glucoseRatio} mg/dL per unit.",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(
                value = bgText,
                onValueChange = { bgText = it },
                label = { Text("Current blood glucose (mg/dL)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = carbsText,
                onValueChange = { carbsText = it },
                label = { Text("Carbs (g)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val bg = bgText.toDoubleOrNull()
                    val carbs = carbsText.toDoubleOrNull() ?: 0.0

                    if (bg == null) {
                        resultUnits = null
                        resultMessage = "Please enter a valid blood glucose."
                        return@Button
                    }

                    // units_for_carbs = carbs / carbRatio
                    // units_for_correction = (bg - target) / glucoseRatio
                    // (if total <= 0 => no correction)
                    val carbRatio = settings.carbRatio.coerceAtLeast(0.1)
                    val glucoseRatio = settings.glucoseRatio.coerceAtLeast(0.1)
                    val targetBg = 110.0

                    val unitsForCarbs = carbs / carbRatio
                    val unitsForCorrection = (bg - targetBg) / glucoseRatio
                    val total = unitsForCarbs + unitsForCorrection

                    if (total <= 0.0) {
                        resultUnits = null
                        resultMessage =
                            "No correction needed based on current glucose and carb amount."
                    } else {
                        // round to 0.1 u
                        val rounded = kotlin.math.round(total * 10.0) / 10.0
                        resultUnits = rounded
                        resultMessage = "Suggested dose: $rounded units."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate")
            }

            if (resultMessage != null) {
                Text(
                    text = resultMessage!!,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (backendError != null) {
                Text(
                    text = backendError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (resultUnits != null) {
                Button(
                    onClick = {
                        val bg = bgText.toIntOrNull()
                        val carbs = carbsText.toDoubleOrNull()

                        if (bg == null) {
                            resultMessage = "Cannot save: BG must be a whole number."
                            return@Button
                        }

                        logViewModel.addEntry(
                            bloodGlucose = bg,
                            insulinUnits = resultUnits,
                            carbsGrams = carbs
                        )

                        onNavigateToLog()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save this calculation as a log entry")
                }
            }
        }
    }
}



@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    val carbRatioText = viewModel.carbRatioText
    val glucoseRatioText = viewModel.glucoseRatioText
    val isLoading = viewModel.isLoading
    val message = viewModel.message

    Scaffold(
        topBar = {
            SucraTopBar(
                title = "Settings",
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium
                )

                if (user == null) {
                    Text(
                        text = "You’re not signed in. You can use Sucra fully offline.\n" +
                                "Sign in to enable cloud backup and sync.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = onNavigateToAuth) {
                        Text("Sign in / Create account")
                    }
                } else {
                    Text(
                        text = "Signed in as ${user.email ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { auth.signOut() }) {
                        Text("Sign out")
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Insulin settings",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = carbRatioText,
                    onValueChange = viewModel::onCarbRatioChange,
                    label = { Text("Carb ratio (g carbs per 1 unit)") },
                    supportingText = {
                        Text("Default is 10 g carbs per 1 unit.")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = glucoseRatioText,
                    onValueChange = viewModel::onGlucoseRatioChange,
                    label = { Text("Glucose ratio (mg/dL per 1 unit)") },
                    supportingText = {
                        Text("Default is 50 mg/dL per 1 unit.")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { viewModel.saveSettings() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLoading) "Saving..." else "Save settings")
                }

                if (message != null) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SucraTopBar(
    title: String,
    showBack: Boolean,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
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
        },
        actions = actions
    )
}
