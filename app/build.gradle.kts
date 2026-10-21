// Fork 变更说明：本文件基于 Ackites/Nrfr 修改，适配 Android 16 兼容依赖。
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localPropertiesFile = rootProject.file("local.properties")
val releaseSigningProperties = localPropertiesFile
    .takeIf { it.isFile }
    ?.let { file ->
        Properties().apply {
            file.inputStream().use { input -> load(input) }
        }
    }
    .let { local ->
        (local ?: Properties()).apply {
            mapOf(
                "signing.storeFile" to "NRFR_SIGNING_STORE_FILE",
                "signing.storePassword" to "NRFR_SIGNING_STORE_PASSWORD",
                "signing.keyAlias" to "NRFR_SIGNING_KEY_ALIAS",
                "signing.keyPassword" to "NRFR_SIGNING_KEY_PASSWORD",
            ).forEach { (key, environment) ->
                System.getenv(environment)?.takeIf { it.isNotBlank() }?.let { setProperty(key, it) }
            }
        }
    }
    .takeIf { properties ->
        listOf(
            "signing.storeFile",
            "signing.storePassword",
            "signing.keyAlias",
            "signing.keyPassword",
        ).all { key -> !properties.getProperty(key).isNullOrBlank() }
    }

android {
    namespace = "com.github.nrfr"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.github.nrfr"
        minSdk = 26
        targetSdk = 34
        versionCode = providers.gradleProperty("nrfr.versionCode").get().toInt()
        versionName = providers.gradleProperty("nrfr.versionName").get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        releaseSigningProperties?.let { properties ->
            create("release") {
                storeFile = rootProject.file(properties.getProperty("signing.storeFile"))
                storePassword = properties.getProperty("signing.storePassword")
                keyAlias = properties.getProperty("signing.keyAlias")
                keyPassword = properties.getProperty("signing.keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (releaseSigningProperties != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        compose = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Shizuku
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:4.3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.03"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
