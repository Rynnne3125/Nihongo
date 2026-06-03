import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" // Giữ nguyên chuẩn 2.0.0
    id("com.google.gms.google-services")
}

// Đọc file local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.nihongo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nihongo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Đọc API Key từ local.properties
        val apiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

// KHÓA CỨNG KOTLIN VERSION ĐỂ TRÁNH XUNG ĐỘT
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")
        force("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
    }
}

dependencies {
    // --- 1. KOTLIN BOM ---
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.0.0"))

    // --- 2. ĐƯỢC QUẢN LÝ BỞI VERSION CATALOG (libs.versions.toml) ---

    // Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.text)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    kapt(libs.androidx.room3.compiler) // Chuyển thành kapt cho chuẩn

    // Coroutines & WorkManager
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.work.runtime.ktx)

    // Firebase (Dùng platform quản lý phiên bản, gọi các module từ libs)
    implementation(platform("com.google.firebase:firebase-bom:32.1.1"))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)


    // --- 3. CÁC THƯ VIỆN CHƯA CÓ TRONG TOML (Khai báo tự do) ---

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.gms:play-services-auth:21.1.0")

    // Gmail & Cloudinary
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
    implementation("com.cloudinary:cloudinary-android:2.3.1")

    // Media & UI Utils
    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation("com.google.android.exoplayer:exoplayer:2.18.1")
    implementation("com.google.accompanist:accompanist-flowlayout:0.32.0")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.5.0")

    // Admin & Notification Services
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.18.0")
    implementation("com.onesignal:OneSignal:[5.1.6, 5.1.99]")
}