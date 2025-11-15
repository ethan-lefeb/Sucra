package com.blacksmith.sucra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.blacksmith.sucra.ui.navigation.SucraDestination
import com.blacksmith.sucra.ui.navigation.SucraNavHost
import com.blacksmith.sucra.ui.theme.SucraTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SucraTheme {
                SucraApp()
            }
        }
    }
}

@Composable
fun SucraApp() {
    val navController = rememberNavController()

    val startDestination = remember {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) SucraDestination.AUTH else SucraDestination.HOME
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        SucraNavHost(
            navController = navController,
            startDestination = startDestination
        )
    }
}
