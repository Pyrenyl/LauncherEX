plugins {
    id("com.android.application") version "9.2.1"
    id("com.android.library") version "9.2.1" apply false
    id("com.google.devtools.ksp") version "2.3.9"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
}

val cfgReleaseStoreFile = providers.environmentVariable("RELEASE_STORE_FILE").orNull
val cfgReleaseStorePassword = providers.environmentVariable("RELEASE_STORE_PASSWORD").orNull
val cfgReleaseKeyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").orNull
val cfgReleaseKeyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").orNull

android {
    namespace = "com.android.launcher3"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.android.launcher3.ex"
        minSdk = 31
        targetSdk = 37
        versionCode = 2026072900
        versionName = "2026072900"

        buildConfigField("boolean", "IS_STUDIO_BUILD", "false")
        buildConfigField("boolean", "QSB_ON_FIRST_SCREEN", "false")
        buildConfigField("boolean", "IS_DEBUG_DEVICE", "false")
        buildConfigField("boolean", "WIDGETS_ENABLED", "true")
        buildConfigField("boolean", "NOTIFICATION_DOTS_ENABLED", "true")
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("standalone/AndroidManifest.xml")
            val sourceDirectories =
                listOf(
                    "src",
                    "src_no_quickstep",
                    "src_plugins",
                    "dagger/src",
                    "modules/concurrent/src",
                    "shared/src",
                    "standalone/generated/proto",
                    "standalone/generated/flags",
                    "standalone/aosp/iconloader/src",
                    "standalone/aosp/animation/src",
                    "standalone/aosp/msdl/src",
                    "standalone/aosp/usertype/src",
                    "standalone/src",
                )
            java.directories.clear()
            java.directories.addAll(sourceDirectories)
            kotlin.directories.clear()
            kotlin.directories.addAll(sourceDirectories)
            res.directories.clear()
            res.directories.addAll(
                listOf(
                    "res",
                    "standalone/res",
                    "standalone/aosp/iconloader/res",
                    "standalone/aosp/animation/res",
                )
            )
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    signingConfigs {
        create("release") {
            storeFile = cfgReleaseStoreFile?.let { file(it) }
            storePassword = cfgReleaseStorePassword
            keyAlias = cfgReleaseKeyAlias
            keyPassword = cfgReleaseKeyPassword
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard.flags",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging.resources.excludes += setOf(
        "META-INF/AL2.0",
        "META-INF/LGPL2.1",
        "META-INF/LICENSE*",
        "META-INF/NOTICE*",
    )
}

dependencies {
    implementation(project(":widgetpicker"))

    implementation("androidx.annotation:annotation:1.10.0")
    implementation("androidx.appcompat:appcompat:1.8.0-alpha01")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.core:core-animation:1.0.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.1.0")
    implementation("androidx.fragment:fragment-ktx:1.9.0-alpha01")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0-alpha03")
    implementation("androidx.navigation:navigation-compose:2.10.0-alpha03")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.slice:slice-view:1.1.0-alpha02")
    implementation("androidx.slice:slice-core:1.1.0-alpha02")
    implementation("androidx.window:window:1.5.1")
    implementation("androidx.graphics:graphics-shapes:1.1.0")

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.foundation:foundation:1.12.0-alpha01")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("androidx.compose.material3:material3:1.5.0-alpha16")
    implementation("androidx.compose.runtime:runtime:1.12.0-alpha01")
    implementation("androidx.compose.ui:ui:1.12.0-alpha01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.12.0-alpha01")

    implementation("com.google.android.material:material:1.13.0")
    implementation("com.google.dagger:dagger:2.59.2")
    ksp("com.google.dagger:dagger-compiler:2.59.2")
    implementation("com.google.guava:guava:33.5.0-android")
    implementation("com.google.protobuf:protobuf-javalite:4.33.5")
    implementation("javax.inject:javax.inject:1")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
