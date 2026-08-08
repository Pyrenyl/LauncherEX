plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.android.launcher3.widgetpicker"
    compileSdk = 37

    defaultConfig {
        minSdk = 31
    }

    sourceSets {
        getByName("main") {
            java.directories.clear()
            java.directories.add("src")
            kotlin.directories.clear()
            kotlin.directories.add("src")
            res.directories.clear()
            res.directories.add("res")
            manifest.srcFile("AndroidManifest.xml")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.annotation:annotation:1.10.0")
    implementation("androidx.compose.foundation:foundation:1.12.0-alpha01")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("androidx.compose.material3:material3:1.5.0-alpha16")
    implementation("androidx.compose.runtime:runtime:1.12.0-alpha01")
    implementation("androidx.compose.ui:ui:1.12.0-alpha01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.12.0-alpha01")
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0-alpha03")
    implementation("androidx.window:window:1.5.1")
    implementation("com.google.dagger:dagger:2.59.2")
    ksp("com.google.dagger:dagger-compiler:2.59.2")
    implementation("javax.inject:javax.inject:1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
