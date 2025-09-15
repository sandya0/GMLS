plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    alias(libs.plugins.compose.compiler)
}

// Load secret properties
val secretFile = rootProject.file("secret.properties")
val secretProperties = org.jetbrains.kotlin.konan.properties.Properties()
secretFile.inputStream().use { 
    secretProperties.load(it)
}

android {
    namespace = "com.example.gmls"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gmls"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        manifestPlaceholders["MAPS_API_KEY"] = secretProperties.getProperty("MAPS_API_KEY", "")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["MAPS_API_KEY"] = secretProperties.getProperty("MAPS_API_KEY", "")
            buildConfigField("boolean", "ENABLE_CRASH_REPORTING", "true")
        }
        debug {
            isDebuggable = true
            buildConfigField("boolean", "ENABLE_CRASH_REPORTING", "false")
            manifestPlaceholders["MAPS_API_KEY"] = secretProperties.getProperty("MAPS_API_KEY", "")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }

    // Ensure proper dependency resolution
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.0")
        }
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose BOM and UI dependencies
    implementation(platform(libs.androidx.compose.bom.v20250501))
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.ui.graphics)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    implementation(libs.androidx.compose.material3.material3)
    implementation(libs.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose.v290)
    implementation(libs.androidx.hilt.navigation.compose)


    // Hilt for dependency injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler.v2511)

    // Room database dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Firebase dependencies
    implementation(platform(libs.firebase.bom.v33140))
    implementation(libs.com.google.firebase.firebase.auth.ktx)
    implementation(libs.google.firebase.firestore.ktx)
    implementation(libs.google.firebase.storage.ktx)
    implementation(libs.google.firebase.messaging.ktx)
    implementation(libs.firebase.analytics.ktx)

    // Google Play Services
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.base)
    implementation(libs.play.services.basement)
    implementation(libs.play.services.tasks)
    implementation(libs.maps.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)

    // Date picker
    implementation(libs.datetime)

    // Permission handling
    implementation(libs.accompanist.permissions)

    // Image loading
    // implementation(libs.coil.compose)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Crash reporting and analytics (optional)
    // implementation("com.google.firebase:firebase-crashlytics-ktx")
    // implementation("com.google.firebase:firebase-analytics-ktx")

    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.core.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom.v20250501))
    androidTestImplementation(libs.ui.test.junit4)

    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

}

kapt {
    correctErrorTypes = true
    useBuildCache = true
}
