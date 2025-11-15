package com.blacksmith.sucra.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class AlarmEntry(
    val id: String = "",
    val hour: Int = 8,
    val minute: Int = 0,
    val label: String = "Check glucose"
)

class AlarmsViewModel : ViewModel() {

    var alarms by mutableStateOf<List<AlarmEntry>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        observeAlarms()
    }

    private fun observeAlarms() {
        val user = auth.currentUser ?: return

        firestore.collection("users")
            .document(user.uid)
            .collection("alarms")
            .orderBy("hour", Query.Direction.ASCENDING)
            .orderBy("minute", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    errorMessage = e.localizedMessage ?: "Failed to load alarms."
                    return@addSnapshotListener
                }

                val docs = snapshot?.documents ?: emptyList()
                alarms = docs.map { doc ->
                    AlarmEntry(
                        id = doc.id,
                        hour = (doc.getLong("hour") ?: 8L).toInt(),
                        minute = (doc.getLong("minute") ?: 0L).toInt(),
                        label = doc.getString("label") ?: "Check glucose"
                    )
                }
            }
    }

    fun addAlarm(hour: Int, minute: Int, label: String) {
        val user = auth.currentUser ?: run {
            errorMessage = "You must be signed in to add alarms."
            return
        }

        if (hour !in 0..23 || minute !in 0..59) {
            errorMessage = "Please enter a valid time."
            return
        }

        isLoading = true
        errorMessage = null

        val data = mapOf(
            "hour" to hour,
            "minute" to minute,
            "label" to label.ifBlank { "Check glucose" }
        )

        firestore.collection("users")
            .document(user.uid)
            .collection("alarms")
            .add(data)
            .addOnSuccessListener {
                isLoading = false
                // NOTE: scheduling system notifications can be added here later.
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.localizedMessage ?: "Failed to save alarm."
            }
    }

    fun deleteAlarm(alarm: AlarmEntry) {
        val user = auth.currentUser ?: return

        firestore.collection("users")
            .document(user.uid)
            .collection("alarms")
            .document(alarm.id)
            .delete()
    }
}
