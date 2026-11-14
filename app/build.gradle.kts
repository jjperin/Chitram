import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

val tmdbConfig = Properties().apply {
    val local = rootProject.file("tmdb.properties")
    if (local.exists()) local.inputStream().use { load(it) }
}
val playbackConfig = Properties().apply {
    val local = rootProject.file("playback.properties")
    if (local.exists()) local.inputStream().use { load(it) }
}
val signingConfigFile = rootProject.file("signing.properties")
val signingConfig = Properties().apply {
    if (signingConfigFile.exists()) signingConfigFile.inputStream().use { load(it) }
}
fun signingValue(key: String): String = System.getenv("CHITRAM_${key}") ?: signingConfig.getProperty(key, "")
fun buildString(value: String) = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.chitram.tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.chitram.tv"
        minSdk = 23
        targetSdk = 34
        versionCode = providers.gradleProperty("chitramVersionCode").orNull?.toIntOrNull() ?: 1
        versionName = providers.gradleProperty("chitramVersionName").orNull ?: "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("chitramRelease") {
            val path = signingValue("KEYSTORE_PATH")
            if (path.isNotBlank()) storeFile = rootProject.file(path)
            storePassword = signingValue("STORE_PASSWORD")
            keyAlias = signingValue("KEY_ALIAS")
            keyPassword = signingValue("KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "TMDB_READ_TOKEN", buildString(tmdbConfig.getProperty("readToken", "")))
            buildConfigField("String", "TEST_PLAYBACK_SOURCE_ID", buildString(playbackConfig.getProperty("sourceId", "")))
            buildConfigField("String", "TEST_PLAYBACK_URL", buildString(playbackConfig.getProperty("url", "")))
        }
        release {
            isMinifyEnabled = false
            if (signingValue("KEYSTORE_PATH").isNotBlank()) signingConfig = signingConfigs.getByName("chitramRelease")
            // Public GitHub APKs must never embed the owner's private token.
            buildConfigField("String", "TMDB_READ_TOKEN", "\"\"")
            buildConfigField("String", "TEST_PLAYBACK_SOURCE_ID", "\"\"")
            buildConfigField("String", "TEST_PLAYBACK_URL", "\"\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.tv:tv-material:1.0.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.robolectric:robolectric:4.12.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

kapt {
    correctErrorTypes = true
    arguments { arg("room.schemaLocation", "$projectDir/schemas") }
}
