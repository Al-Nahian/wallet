import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

// local.properties is git-ignored (never committed) — Supabase credentials live only here and
// in each developer's own machine, read into BuildConfig below rather than hardcoded.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        load(FileInputStream(file))
    }
}

android {
    namespace = "com.expensetracker.wallet"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.expensetracker.wallet"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("supabase.url", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("supabase.anonKey", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Supabase-kt's Auth module requires API 26+ (java.time etc.) — desugaring keeps minSdk
        // 24 (real Bangladeshi users still on Android 7) instead of dropping them to satisfy one
        // dependency.
        isCoreLibraryDesugaringEnabled = true
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
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets {
        getByName("androidTest") {
            // Exported Room schema JSON (room.schemaLocation below), so
            // MigrationTestHelper can validate/migrate against real past versions.
            assets.srcDirs("$projectDir/schemas")
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

configurations.all {
    resolutionStrategy {
        // Gradle's default "highest version wins" would otherwise pull in auth-kt's transitive
        // androidx.browser:browser:1.10.0 (needs compileSdk 36) over the explicit 1.8.0 above.
        force("androidx.browser:browser:1.8.0")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2025.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Liquid glass refraction prototype (FAB only) — plain Android release (pre-KMP split),
    // pinned to the version whose declared Compose dependency (1.9.4) matches the BOM above.
    implementation("io.github.kyant0:backdrop:1.0.0")

    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-process:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.1")

    // Hilt — bumped from 2.51.1: that version's annotation processor NPEs under Kotlin 2.x.
    // Pinned to 2.56.2 rather than latest: 2.6x's Gradle plugin requires AGP 9.0+, which this
    // project (AGP 8.6.1) doesn't have. Compiler now runs via KSP, not kapt (see plugins block).
    implementation("com.google.dagger:hilt-android:2.56.2")
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // WorkManager (plans/11-recurring-goals.md — the recurring-transaction engine's daily job)
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Room — bumped from 2.6.1: that version's compiler only supports KSP1, not the KSP2 engine
    // this project's KSP version now runs (confirmed via a diagnostic build that failed
    // identically with the backdrop library removed — Room's own DatabaseProcessor was the
    // actual crash site, not anything liquid-glass-related).
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Supabase (Phase 16 — Auth/Postgrest; see plan.md §38-§43, §84-§85). Ktor's OkHttp engine
    // reuses the OkHttp dependency already above rather than pulling in a second HTTP stack.
    // Pinned to 3.1.4 (not the latest 3.6.0): supabase-kt moved to Kotlin 2.2 metadata at BOM
    // 3.2.0, incompatible with this project's Kotlin 2.1.21 compiler plugin. 3.1.4 is the newest
    // patch still built against kotlin-stdlib 2.1.20. ktor-client-okhttp is pinned to the matching
    // 3.1.2 for the same reason (its own newer releases pull a newer kotlin-stdlib too).
    implementation(platform("io.github.jan-tennert.supabase:bom:3.1.4"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.ktor:ktor-client-okhttp:3.1.2")
    // auth-kt pulls in androidx.browser (Custom Tabs for the OAuth flow) transitively; its newer
    // releases require compileSdk 36 + a newer AGP than this project is on. Pinned to the last
    // version compatible with compileSdk 35/AGP 8.6.1 rather than bumping the whole toolchain.
    implementation("androidx.browser:browser:1.8.0")

    // Required by isCoreLibraryDesugaringEnabled above (Supabase-kt's Auth module needs API 26+
    // APIs; this backports them onto minSdk 24).
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.room:room-testing:2.7.2")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
