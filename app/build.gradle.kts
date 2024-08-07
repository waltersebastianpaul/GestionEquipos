plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.gestionequipos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gestionequipos"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.ui.text.android)
    implementation(libs.androidx.annotation)
    implementation(libs.protolite.well.known.types)
    implementation(libs.filament.android)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.core.i18n)
    implementation(libs.androidx.datastore.core.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ... otras dependencias

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.material.v140)

    implementation(libs.androidx.constraintlayout)

    // Para solicitudes de red
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Dependencias para Coroutines (para realizar solicitudes de red asíncronas)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    implementation ("androidx.paging:paging-runtime-ktx:3.3.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2") // Asegúrate de tener la última versión
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Asegúrate de tener la última versión

}