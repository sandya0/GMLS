package com.example.gmls

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for initializing app-wide components
 */
@HiltAndroidApp
class DisasterResponseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Set up Firebase Cloud Messaging for notifications
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
    }
}