/* */

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
    }
}

rootProject.name = "gradle-react-native"

include("plugin")

/*
  Compositive builds:
    https://docs.gradle.org/current/userguide/composite_builds.html
*/
includeBuild("sample/ReactNativePlugin/android")
