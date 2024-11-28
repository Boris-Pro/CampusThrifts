// App-level build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    id("org.jetbrains.kotlin.kapt") // Adding kapt plugin to apply annotation processing
}

android {
    namespace = "com.example.campusthrifts"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.campusthrifts"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    kapt {
        javacOptions {
            option("-source", "17")
            option("-target", "17")
        }
    }
}

dependencies {
    // Import the BoM for the Firebase platform
    implementation(platform(libs.google.firebase.bom))
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // Firebase dependencies managed by BoM (no version specified)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.messaging)

    // Firebase UI Library
    implementation(libs.firebase.ui.auth)
    implementation(libs.firebase.ui.database)

    // Google Sign In SDK
    implementation(libs.play.services.auth)

    // Other dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Dependency for image loading
    implementation(libs.coil)

    // Dependency for image cropping with uCrop
    implementation(libs.ucrop)
    implementation(libs.firebase.dataconnect)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.core.splashscreen)

    // Image loading dependency
    implementation("io.coil-kt:coil:2.0.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
