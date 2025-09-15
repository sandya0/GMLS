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
import com.example.gmls.ui.viewmodels.AuthState
import com.example.gmls.ui.viewmodels.AuthViewModel
import com.example.gmls.ui.viewmodels.DisasterViewModel
import com.example.gmls.ui.viewmodels.AdminViewModel
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
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import com.example.gmls.util.AdminBootstrap
import android.content.Context
import android.content.res.Configuration
import java.util.Locale
import javax.inject.Inject
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var themePreferenceManager: ThemePreferenceManager
    
    @Inject
    lateinit var adminBootstrap: AdminBootstrap
    
    // Exception handler for this activity's coroutines
    private val activityExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "Activity coroutine exception", exception)
        handleActivityException(exception)
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false
        } else {
            true
        }

        if (fineLocationGranted && coarseLocationGranted && backgroundLocationGranted) {
            Log.d(TAG, "All location permissions granted")
        } else {
            Log.w(TAG, "Some location permissions denied")
            // Optionally, show a message to the user explaining why permissions are needed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force Indonesian locale
        setAppLocale(this, "id")
        
        try {
            initializeActivity()
            requestLocationPermissions()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            handleActivityException(e)
            showErrorScreen(e)
        }
    }

    private fun requestLocationPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        val allPermissionsGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allPermissionsGranted) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    
    /**
     * Set the app locale to Indonesian
     */
    private fun setAppLocale(context: Context, languageCode: String) {
        try {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            Log.d(TAG, "App locale set to: $languageCode")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting app locale", e)
        }
    }
    
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { updateBaseContextLocale(it, "id") })
    }
    
    /**
     * Update base context locale to Indonesian
     */
    private fun updateBaseContextLocale(context: Context, languageCode: String): Context {
        return try {
            val locale = Locale(languageCode)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating base context locale", e)
            context
        }
    }
    
    private fun initializeActivity() {
        try {
        themePreferenceManager = ThemePreferenceManager(this)
            
        setContent {
            var appTheme by rememberSaveable { mutableStateOf(AppTheme.SYSTEM) }
                var initializationError by remember { mutableStateOf<String?>(null) }
                var isInitializing by remember { mutableStateOf(true) }

                // Safe theme loading with error handling and admin bootstrap
            LaunchedEffect(Unit) {
                    try {
                        // First, check Google Play Services before attempting Firebase operations
                        try {
                            Log.d(TAG, "Checking Google Play Services availability...")
                            val googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
                            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this@MainActivity)
                            
                            if (resultCode == com.google.android.gms.common.ConnectionResult.SUCCESS) {
                                Log.d(TAG, "Google Play Services is available, proceeding with admin bootstrap...")
                                
                                // Try to bootstrap admin user to fix permission issues
                                val bootstrapResult = adminBootstrap.checkAndBootstrapAdmin()
                                if (bootstrapResult.isFailure) {
                                    Log.e(TAG, "Admin bootstrap failed: ${bootstrapResult.exceptionOrNull()?.message}")
                                    // Continue without blocking app startup
                                }

                                val fixResult = adminBootstrap.fixPermissionIssues()
                                if (fixResult.isFailure) {
                                    Log.e(TAG, "Permission issues fix failed: ${fixResult.exceptionOrNull()?.message}")
                                    // Continue without blocking app startup
                                }
                            } else {
                                Log.w(TAG, "Google Play Services not available (code: $resultCode), skipping admin bootstrap")
                                // Continue without admin bootstrap to prevent crashes
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error during initialization, continuing anyway", e)
                            // Don't rethrow to prevent app crash
                        }
                        
                        // Load theme preferences
                        themePreferenceManager.themeFlow.collect { savedTheme: AppTheme ->
                            appTheme = savedTheme
                            isInitializing = false
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading theme", e)
                        initializationError = getString(R.string.failed_to_load_theme_preferences)
                        isInitializing = false
                    }
                }

                // Show loading or error state during initialization
                if (isInitializing) {
                    LoadingScreen()
                } else if (initializationError != null) {
                    ErrorScreen(
                        error = initializationError!!,
                        onRetry = {
                            initializationError = null
                            isInitializing = true
                            // Retry initialization
                            lifecycleScope.launch(activityExceptionHandler) {
                                delay(1000) // Brief delay before retry
                themePreferenceManager.themeFlow.collect { savedTheme: AppTheme ->
                    appTheme = savedTheme
                                    isInitializing = false
                }
            }
                        }
                    )
                } else {
                    // Main app content with error boundary
                    SafeAppContent(
                        appTheme = appTheme,
                        authViewModel = authViewModel,
                        onThemeChange = { newTheme: AppTheme ->
                            try {
                                appTheme = newTheme
                                lifecycleScope.launch(activityExceptionHandler) {
                                    themePreferenceManager.setTheme(newTheme)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error changing theme", e)
                                handleActivityException(e)
                            }
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeActivity", e)
            throw e
        }
    }
    
    private fun handleActivityException(exception: Throwable) {
        // Log the exception
        Log.e(TAG, "Activity exception handled", exception)
        
        // Save crash information
        try {
            val sharedPrefs = getSharedPreferences("activity_crashes", MODE_PRIVATE)
            val crashInfo = """
                Activity: ${this.javaClass.simpleName}
                Timestamp: ${System.currentTimeMillis()}
                Exception: ${exception.javaClass.simpleName}
                Message: ${exception.message}
                
            """.trimIndent()
            
            sharedPrefs.edit()
                .putString("last_crash", crashInfo)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving crash info", e)
        }
    }
    
    private fun showErrorScreen(exception: Throwable) {
        try {
            setContent {
                GMLSTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ErrorScreen(
                            error = getString(R.string.application_failed_to_start) + ": ${exception.message}",
                            onRetry = {
                                try {
                                    recreate()
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error recreating activity", e)
                                    finish()
                                }
                            }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing error screen", e)
            finish()
        }
    }
}

@Composable
fun SafeAppContent(
    appTheme: AppTheme,
    authViewModel: AuthViewModel,
    onThemeChange: (AppTheme) -> Unit
) {
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    if (hasError) {
        ErrorScreen(
            error = errorMessage,
            onRetry = {
                hasError = false
                errorMessage = ""
            }
        )
    } else {
            GMLSTheme(appTheme = appTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent(
                        authViewModel = authViewModel,
                        currentTheme = appTheme,
                        onThemeChange = onThemeChange
                    )
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
        disasterViewModel = hiltViewModel<DisasterViewModel>(),
        adminViewModel = hiltViewModel<AdminViewModel>(),
        currentTheme = currentTheme,
        onThemeChange = onThemeChange
    )
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.initializing_application),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Terjadi kesalahan saat memuat aplikasi",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aplikasi mengalami masalah dan tidak dapat melanjutkan proses. Silakan coba lagi. Jika masalah berlanjut, hubungi dukungan dengan menyertakan detail kesalahan di bawah ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Detail kesalahan: " + error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}
