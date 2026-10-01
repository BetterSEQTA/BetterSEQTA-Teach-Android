import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

fun Project.cloudflareProperty(name: String): String {
    val secrets = Properties()
    rootProject.file("secrets.properties").takeIf { it.exists() }?.inputStream()?.use {
        secrets.load(it)
    }
    val local = Properties()
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use {
        local.load(it)
    }
    val raw = secrets.getProperty(name)?.takeIf { it.isNotBlank() }
        ?: local.getProperty(name, "")
    return raw.replace("\\", "\\\\").replace("\"", "\\\"")
}

android {
    namespace = "org.betterseqta.betterseqtateachandroid"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "org.betterseqta.betterseqtateachandroid"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CF_ACCOUNT_ID", "\"${cloudflareProperty("CF_ACCOUNT_ID")}\"")
        buildConfigField("String", "CF_AUTH_TOKEN", "\"${cloudflareProperty("CF_AUTH_TOKEN")}\"")
        buildConfigField("String", "CLOUDFLARE_API_KEY", "\"${cloudflareProperty("CLOUDFLARE_API_KEY")}\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment.ktx)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.lifecycle.process)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
