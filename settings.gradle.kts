import org.gradle.internal.impldep.org.bouncycastle.util.Properties

rootProject.name = "asktransport-cmp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val localProps = java.util.Properties().apply {
    val localFile = file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use(::load)
    }
}

val mapboxDownloadsToken = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN")
    .orElse(providers.environmentVariable("MAPBOX_DOWNLOADS_TOKEN"))
    .orElse(localProps.getProperty("MAPBOX_DOWNLOADS_TOKEN", ""))
    .get()

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("org.chromium")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("org.chromium")
            }
        }
        mavenCentral()
        maven("https://api.mapbox.com/downloads/v2/releases/maven") {
            credentials {
                username = "mapbox"
                password = mapboxDownloadsToken
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

include(":androidApp")
include(":shared")
