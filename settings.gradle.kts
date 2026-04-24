pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CamScanner"
include(":app")

// OpenCV SDK as local module (pre-built .so, no CMake)
val opencvSdk = "C:\\Users\\thedu\\Downloads\\opencv-4.8.0-android-sdk\\OpenCV-android-sdk"
include(":opencv")
project(":opencv").projectDir = File("$opencvSdk/sdk")
