// App-level build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Import the BoM for the Firebase platform
    implementation(platform(libs.google.firebase.bom))

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
    implementation(libs.androidx.lifecycle.viewmodel.android)

    //Glide dependencies
    implementation(libs.glide)
    implementation(libs.glide.complier)
    implementation(libs.glide.okhttp3)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.core.splashscreen)

    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation(libs.androidx.recyclerview)
    implementation (libs.androidx.cardview)
    implementation (libs.glide.v4160)
    annotationProcessor (libs.compiler)

}