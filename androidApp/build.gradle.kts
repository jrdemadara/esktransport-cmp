import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "org.noztek.esktransport"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.noztek.esktransport"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "v0.1.0-alpha.1"

        val localProperties = Properties().apply {
            val localFile = rootProject.file("local.properties")
            if (localFile.exists()) {
                localFile.inputStream().use(::load)
            }
        }
        val pusherAppKey = localProperties.getProperty("PUSHER_APP_KEY", "")
        val pusherAppCluster = localProperties.getProperty("PUSHER_APP_CLUSTER", "")
        val pusherAuthEndpoint = localProperties.getProperty("PUSHER_AUTH_ENDPOINT", "/broadcasting/auth")

        buildConfigField("String", "PUSHER_APP_KEY", "\"$pusherAppKey\"")
        buildConfigField("String", "PUSHER_APP_CLUSTER", "\"$pusherAppCluster\"")
        buildConfigField("String", "PUSHER_AUTH_ENDPOINT", "\"$pusherAuthEndpoint\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}
