package com.example.gmls


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.gmls.ui.navigation.DisasterResponseNavHost
import com.example.gmls.ui.screens.auth.RegistrationData
import com.example.gmls.ui.screens.disaster.DisasterReport
import com.example.gmls.ui.theme.DisasterResponseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DisasterResponseTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DisasterResponseApp()
                }
            }
        }
    }
}

@Composable
fun DisasterResponseApp() {
    var isAuthenticated by remember { mutableStateOf(false) }
    val navController = rememberNavController()

    DisasterResponseNavHost(
        navController = navController,
        isAuthenticated = isAuthenticated,
        onLogin = { email, password ->
            // In a real app, you would integrate with Firebase Authentication
            isAuthenticated = true
        },
        onRegister = { registrationData ->
            // In a real app, you would save the user data to Firebase
            isAuthenticated = true
        },
        onDisasterReport = { report ->
            // In a real app, you would save the report to Firebase
        },
        onLogout = {
            // In a real app, you would sign out from Firebase
            isAuthenticated = false
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DisasterResponseTheme {
        DisasterResponseApp()
    }
}