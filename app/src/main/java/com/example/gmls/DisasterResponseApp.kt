package com.example.gmls

import android.app.Application
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.example.gmls.utils.CrashPrevention
import android.content.Context
import android.widget.Toast

/**
 * Application class for initializing app-wide components with crash prevention
 */
@HiltAndroidApp
class DisasterResponseApp : Application() {

    companion object {
        private const val TAG = "DisasterResponseApp"
        lateinit var instance: DisasterResponseApp
            private set
    }

    // Global exception handler for coroutines
    private val globalExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "Global exception handler called", exception)
        handleGlobalException(exception)
    }

    // Application-wide coroutine scope with exception handling
    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main + globalExceptionHandler
    )

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize crash prevention system first
        try {
            CrashPrevention.initialize(this)
            Log.d(TAG, "Crash prevention system initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize crash prevention", e)
        }

        // Set up global exception handler for uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", exception)
            CrashPrevention.recordCrash(exception, "UncaughtException")
            handleGlobalException(exception)
            // Let the system handle the crash after logging
            System.exit(1)
        }

        // Check if app is in unstable state
        if (CrashPrevention.isAppUnstable()) {
            Log.w(TAG, "App detected as unstable, enabling safe mode")
            enableSafeMode()
        }

        try {
            checkGooglePlayServicesAvailability()
            initializeFirebase()
            initializeOtherServices()
            Log.d(TAG, "DisasterResponseApp initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during app initialization", e)
            CrashPrevention.recordCrash(e, "AppInitialization")
            handleGlobalException(e)
        }
    }

    private fun checkGooglePlayServicesAvailability() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        
        when (resultCode) {
            ConnectionResult.SUCCESS -> {
                Log.d(TAG, "Google Play Services is available and up to date")
            }
            ConnectionResult.SERVICE_MISSING -> {
                Log.w(TAG, "Google Play Services is missing")
                showToast(getString(R.string.google_play_services_missing))
            }
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                Log.w(TAG, "Google Play Services needs to be updated")
                showToast(getString(R.string.google_play_services_update_required))
            }
            ConnectionResult.SERVICE_DISABLED -> {
                Log.w(TAG, "Google Play Services is disabled")
                showToast(getString(R.string.google_play_services_disabled))
            }
            else -> {
                Log.w(TAG, "Google Play Services error: $resultCode")
                if (googleApiAvailability.isUserResolvableError(resultCode)) {
                    Log.d(TAG, "Google Play Services error is user resolvable")
                }
            }
        }
    }

    private fun enableSafeMode() {
        try {
            // Clear some caches and reset to safe defaults
            val cacheDir = cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.contains("temp")) {
                    file.delete()
                }
            }
            
            // Clear crash data after handling
            CrashPrevention.clearCrashData()
            
            Log.d(TAG, "Safe mode enabled - cleared temporary data")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling safe mode", e)
        }
    }

    private fun initializeFirebase() {
        CrashPrevention.safeExecute(TAG, Unit) {
            try {
                // Initialize Firebase with error handling
                if (FirebaseApp.getApps(this).isEmpty()) {
                    FirebaseApp.initializeApp(this)
                    Log.d(TAG, "Firebase initialized successfully")
                } else {
                    Log.d(TAG, "Firebase already initialized")
                }

                // Set up Firebase Cloud Messaging with error handling
                applicationScope.launch(CrashPrevention.createSafeExceptionHandler("FirebaseMessaging")) {
                    CrashPrevention.safeExecuteAsync(TAG, defaultValue = Unit) {
                        try {
                            FirebaseMessaging.getInstance().isAutoInitEnabled = true
                            
                            // Get FCM token with proper error handling
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                                    return@addOnCompleteListener
                                }

                                // Get new FCM registration token
                                val token = task.result
                                Log.d(TAG, "FCM Registration Token: $token")
                            }.addOnFailureListener { exception ->
                                Log.e(TAG, "Failed to get FCM token", exception)
                            }
                            
                            Log.d(TAG, "Firebase Messaging initialized successfully")
                        } catch (e: SecurityException) {
                            Log.e(TAG, "SecurityException in Firebase Messaging initialization", e)
                            // Continue without FCM if there's a security issue
                        } catch (e: Exception) {
                            Log.e(TAG, "Error initializing Firebase Messaging", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Firebase", e)
                throw e
            }
        }
    }

    private fun initializeOtherServices() {
        CrashPrevention.safeExecute(TAG, Unit) {
            try {
                // Initialize other services here with proper error handling
                Log.d(TAG, "Other services initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing other services", e)
            }
        }
    }

    private fun handleGlobalException(exception: Throwable) {
        applicationScope.launch(globalExceptionHandler) {
            try {
                // Log the exception details
                val exceptionInfo = buildString {
                    appendLine("Exception: ${exception.javaClass.simpleName}")
                    appendLine("Message: ${exception.message}")
                    appendLine("Thread: ${Thread.currentThread().name}")
                    appendLine("Timestamp: ${System.currentTimeMillis()}")
                    
                    // Add stack trace
                    exception.stackTrace.take(10).forEach { stackElement ->
                        appendLine("  at $stackElement")
                    }
                }
                
                Log.e(TAG, "Global exception details:\n$exceptionInfo")
                
                // Handle specific exception types
                when (exception) {
                    is SecurityException -> {
                        Log.e(TAG, "Security exception occurred", exception)
                        if (exception.message?.contains("Unknown calling package") == true) {
                            Log.e(TAG, "Google Play Services package verification failed")
                            showToast(getString(R.string.google_play_auth_issue))
                        }
                    }
                    is RuntimeException -> {
                        Log.e(TAG, "Runtime exception occurred", exception)
                        showToast(getString(R.string.unexpected_error_occurred))
                    }
                    else -> {
                        Log.e(TAG, "Unknown exception type", exception)
                        showToast(getString(R.string.error_occurred_please_try_again))
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in exception handler", e)
            }
        }
    }

    private fun saveCrashInfo(exception: Throwable) {
        try {
            val sharedPrefs = getSharedPreferences("app_crashes", Context.MODE_PRIVATE)
            val crashInfo = buildString {
                appendLine("Crash Timestamp: ${System.currentTimeMillis()}")
                appendLine("Exception: ${exception.javaClass.simpleName}")
                appendLine("Message: ${exception.message}")
                appendLine("Stack Trace:")
                exception.stackTrace.forEach { stackElement ->
                    appendLine("  at $stackElement")
                }
            }
            
            sharedPrefs.edit()
                .putString("last_crash", crashInfo)
                .putLong("crash_timestamp", System.currentTimeMillis())
                .apply()
                
            Log.d(TAG, "Crash information saved")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving crash info", e)
        }
    }

    private fun restartApp() {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.let {
                it.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it)
            }
            android.os.Process.killProcess(android.os.Process.myPid())
        } catch (e: Exception) {
            Log.e(TAG, "Error restarting app", e)
        }
    }

    private fun showToast(message: String) {
        try {
            applicationScope.launch(Dispatchers.Main) {
                Toast.makeText(this@DisasterResponseApp, message, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast", e)
        }
    }

    // Public method to get application scope
    fun getApplicationScope(): CoroutineScope = applicationScope

    // Public method to get global exception handler
    fun getGlobalExceptionHandler(): CoroutineExceptionHandler = globalExceptionHandler
}
