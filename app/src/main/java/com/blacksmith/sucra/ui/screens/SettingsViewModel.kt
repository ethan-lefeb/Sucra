package com.blacksmith.sucra.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class UserSettings(
    val carbRatio: Double = 10.0,   // grams of carbs per 1 unit
    val glucoseRatio: Double = 50.0 // mg/dL per 1 unit (correction factor)
)

class SettingsViewModel : ViewModel() {

    var carbRatioText by mutableStateOf("10")
        private set

    var glucoseRatioText by mutableStateOf("50")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val user = auth.currentUser ?: return

        isLoading = true
        firestore.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val carb = doc.getDouble("carbRatio") ?: 10.0
                val glucose = doc.getDouble("glucoseRatio") ?: 50.0

                carbRatioText = carb.toString()
                glucoseRatioText = glucose.toString()
                isLoading = false
            }
            .addOnFailureListener { e ->
                message = e.localizedMessage ?: "Failed to load settings."
                isLoading = false
            }
    }

    fun onCarbRatioChange(new: String) {
        carbRatioText = new
    }

    fun onGlucoseRatioChange(new: String) {
        glucoseRatioText = new
    }

    fun saveSettings() {
        val user = auth.currentUser ?: run {
            message = "You must be signed in to save settings."
            return
        }

        val carb = carbRatioText.toDoubleOrNull()
        val glucose = glucoseRatioText.toDoubleOrNull()

        if (carb == null || glucose == null || carb <= 0.0 || glucose <= 0.0) {
            message = "Please enter valid positive numbers for both ratios."
            return
        }

        isLoading = true
        message = null

        val data = mapOf(
            "carbRatio" to carb,
            "glucoseRatio" to glucose
        )

        firestore.collection("users")
            .document(user.uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                isLoading = false
                message = "Settings saved."
            }
            .addOnFailureListener { e ->
                isLoading = false
                message = e.localizedMessage ?: "Failed to save settings."
            }
    }

    fun currentSettingsOrDefaults(): UserSettings {
        val carb = carbRatioText.toDoubleOrNull() ?: 10.0
        val glucose = glucoseRatioText.toDoubleOrNull() ?: 50.0
        return UserSettings(carbRatio = carb, glucoseRatio = glucose)
    }
}
