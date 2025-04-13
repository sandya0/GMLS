package com.example.gmls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.gmls.ui.navigation.DisasterResponseNavHost
import com.example.gmls.ui.theme.DisasterResponseTheme
import com.example.gmls.ui.viewmodels.AuthState
import com.example.gmls.ui.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gmls.ui.theme.GMLSTheme
import com.example.gmls.ui.theme.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.gmls.data.local.ThemePreferenceManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var themePreferenceManager: ThemePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePreferenceManager = ThemePreferenceManager(this)
        setContent {
            var appTheme by rememberSaveable { mutableStateOf(AppTheme.SYSTEM) }

            // Load theme from DataStore
            LaunchedEffect(Unit) {
                themePreferenceManager.themeFlow.collect { savedTheme ->
                    appTheme = savedTheme
                }
            }

            GMLSTheme(appTheme = appTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent(
                        authViewModel = authViewModel,
                        currentTheme = appTheme,
                        onThemeChange = { newTheme ->
                            appTheme = newTheme
                            lifecycleScope.launch {
                                themePreferenceManager.setTheme(newTheme)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    authViewModel: AuthViewModel,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    DisasterResponseNavHost(
        modifier = Modifier,
        authViewModel = authViewModel,
        disasterViewModel = hiltViewModel(),
        currentTheme = currentTheme,
        onThemeChange = onThemeChange
    )
}
