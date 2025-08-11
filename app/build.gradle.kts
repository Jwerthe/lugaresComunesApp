plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.lugarescomunes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lugarescomunes"
        minSdk = 24
        targetSdk = 35
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Dependencias existentes
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // RecyclerView y CardView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Coordinator Layout para pantalla de detalles
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Network libraries para Supabase
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // Permission handling
    implementation("pub.devrel:easypermissions:3.0.0")

    // Shimmer effect for loading
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Lottie animations (optional)
    implementation("com.airbnb.android:lottie:6.1.0")

    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    // Para futuras integraciones cuando estén listas:
    // Supabase SDK
    // implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.4")
    // implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.4")
    // implementation("io.ktor:ktor-client-android:2.3.7")

    // what3words SDK
    // implementation("com.what3words:w3w-android-wrapper:3.1.22")

    // Supabase SDK (cuando lo necesitemos)
    // implementation("io.supabase:postgrest-kt:1.4.7")
    // implementation("io.supabase:storage-kt:1.4.7")
    // implementation("io.supabase:gotrue-kt:1.4.7")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}