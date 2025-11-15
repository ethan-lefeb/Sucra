package com.blacksmith.sucra.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class LogEntry(
    val id: String = "",
    val timestamp: Long = 0L,
    val bloodGlucose: Int = 0,      // mg/dL
    val insulinUnits: Double? = null, // optional units
    val carbsGrams: Double? = null    // optional grams
)

class LogViewModel : ViewModel() {

    var logEntries by mutableStateOf<List<LogEntry>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        observeEntries()
    }

    private fun observeEntries() {
        val user = auth.currentUser ?: return

        firestore.collection("users")
            .document(user.uid)
            .collection("logEntries")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    errorMessage = e.localizedMessage ?: "Failed to load log entries."
                    return@addSnapshotListener
                }

                val docs = snapshot?.documents ?: emptyList()
                logEntries = docs.map { doc ->
                    LogEntry(
                        id = doc.id,
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        bloodGlucose = (doc.getLong("bloodGlucose") ?: 0L).toInt(),
                        insulinUnits = doc.getDouble("insulinUnits"),
                        carbsGrams = doc.getDouble("carbsGrams")
                    )
                }
            }
    }

    fun addEntry(
        bloodGlucose: Int,
        insulinUnits: Double?,
        carbsGrams: Double?
    ) {
        val user = auth.currentUser ?: run {
            errorMessage = "You must be signed in to add entries."
            return
        }

        isLoading = true
        errorMessage = null

        val data = mutableMapOf<String, Any>(
            "timestamp" to System.currentTimeMillis(),
            "bloodGlucose" to bloodGlucose,
        )

        if (insulinUnits != null) {
            data["insulinUnits"] = insulinUnits
        }
        if (carbsGrams != null) {
            data["carbsGrams"] = carbsGrams
        }

        firestore.collection("users")
            .document(user.uid)
            .collection("logEntries")
            .add(data)
            .addOnSuccessListener {
                isLoading = false
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.localizedMessage ?: "Failed to save entry."
            }
    }
}
