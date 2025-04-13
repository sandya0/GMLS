// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.0") // Use the latest version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22") // Use the latest version
        classpath("com.google.gms:google-services:4.4.1") // Add this line
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51")
    }
}