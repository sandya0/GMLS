package com.example.gmls.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.gmls.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper class to handle Google Play Services availability and errors
 */
object GooglePlayServicesHelper {
    private const val TAG = "GooglePlayServicesHelper"
    private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

    /**
     * Check if Google Play Services is available
     */
    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    /**
     * Check Google Play Services availability and handle errors
     */
    fun checkGooglePlayServices(context: Context): GooglePlayServicesStatus {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        
        return when (resultCode) {
            ConnectionResult.SUCCESS -> {
                Log.d(TAG, "Google Play Services is available and up to date")
                GooglePlayServicesStatus.Available
            }
            ConnectionResult.SERVICE_MISSING -> {
                Log.w(TAG, "Google Play Services is missing")
                GooglePlayServicesStatus.Missing
            }
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                Log.w(TAG, "Google Play Services needs to be updated")
                GooglePlayServicesStatus.UpdateRequired
            }
            ConnectionResult.SERVICE_DISABLED -> {
                Log.w(TAG, "Google Play Services is disabled")
                GooglePlayServicesStatus.Disabled
            }
            ConnectionResult.SERVICE_INVALID -> {
                Log.w(TAG, "Google Play Services is invalid")
                GooglePlayServicesStatus.Invalid
            }
            else -> {
                Log.w(TAG, "Google Play Services error: $resultCode")
                GooglePlayServicesStatus.Error(resultCode)
            }
        }
    }

    /**
     * Handle Google Play Services resolution if possible
     */
    fun handleGooglePlayServicesError(
        activity: Activity,
        resultCode: Int,
        onResolved: () -> Unit = {},
        onNotResolvable: () -> Unit = {}
    ) {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        
        if (googleApiAvailability.isUserResolvableError(resultCode)) {
            try {
                val dialog = googleApiAvailability.getErrorDialog(
                    activity,
                    resultCode,
                    PLAY_SERVICES_RESOLUTION_REQUEST
                ) { onNotResolvable() }
                
                dialog?.show()
                Log.d(TAG, "Showing Google Play Services resolution dialog")
            } catch (e: Exception) {
                Log.e(TAG, "Error showing Google Play Services dialog", e)
                onNotResolvable()
            }
        } else {
            Log.w(TAG, "Google Play Services error is not user resolvable")
            onNotResolvable()
        }
    }

    /**
     * Show appropriate message for Google Play Services status
     */
    fun showGooglePlayServicesMessage(context: Context, status: GooglePlayServicesStatus) {
        val message = when (status) {
            is GooglePlayServicesStatus.Available -> return // No message needed
            is GooglePlayServicesStatus.Missing -> 
                context.getString(R.string.google_play_missing)
            is GooglePlayServicesStatus.UpdateRequired -> 
                context.getString(R.string.google_play_update_required)
            is GooglePlayServicesStatus.Disabled -> 
                context.getString(R.string.google_play_disabled)
            is GooglePlayServicesStatus.Invalid -> 
                context.getString(R.string.google_play_invalid)
            is GooglePlayServicesStatus.Error -> 
                context.getString(R.string.google_play_error)
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                Log.d(TAG, "Showed Google Play Services message: $message")
            } catch (e: Exception) {
                Log.e(TAG, "Error showing toast message", e)
            }
        }
    }

    /**
     * Safe execution of Google Play Services dependent code
     */
    fun <T> executeWithGooglePlayServices(
        context: Context,
        action: () -> T,
        fallback: () -> T,
        showErrorMessage: Boolean = true
    ): T {
        return try {
            val status = checkGooglePlayServices(context)
            when (status) {
                is GooglePlayServicesStatus.Available -> {
                    Log.d(TAG, "Executing action with Google Play Services")
                    action()
                }
                else -> {
                    if (showErrorMessage) {
                        showGooglePlayServicesMessage(context, status)
                    }
                    Log.d(TAG, "Executing fallback action due to Google Play Services unavailability")
                    fallback()
                }
            }
        } catch (e: SecurityException) {
            if (e.message?.contains("Unknown calling package") == true) {
                Log.w(TAG, "Google Play Services security exception, using fallback", e)
                if (showErrorMessage) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, context.getString(R.string.authentication_issue_google_play), Toast.LENGTH_LONG).show()
                    }
                }
                fallback()
            } else {
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error with Google Play Services, using fallback", e)
            if (showErrorMessage) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, context.getString(R.string.error_google_play_offline), Toast.LENGTH_LONG).show()
                }
            }
            fallback()
        }
    }

    /**
     * Safely initialize Firebase Auth with Google Play Services checks
     */
    fun safelyInitializeFirebaseAuth(context: Context, onSuccess: () -> Unit, onFailure: () -> Unit) {
        executeWithGooglePlayServices(
            context = context,
            action = {
                Log.d(TAG, "Initializing Firebase Auth with Google Play Services")
                onSuccess()
            },
            fallback = {
                Log.w(TAG, "Firebase Auth initialization skipped due to Google Play Services issues")
                onFailure()
            },
            showErrorMessage = false // We'll handle messaging in the calling code
        )
    }
}

/**
 * Sealed class representing Google Play Services status
 */
sealed class GooglePlayServicesStatus {
    object Available : GooglePlayServicesStatus()
    object Missing : GooglePlayServicesStatus()
    object UpdateRequired : GooglePlayServicesStatus()
    object Disabled : GooglePlayServicesStatus()
    object Invalid : GooglePlayServicesStatus()
    data class Error(val code: Int) : GooglePlayServicesStatus()
} 
