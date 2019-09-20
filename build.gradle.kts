/* React Native Build Gradle Plugin */
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
    }
}

plugins {
    /* https://plugins.gradle.org/plugin/com.gradle.build-scan */
    id("com.gradle.build-scan") version "2.4.2"

    kotlin("jvm") version "1.3.50" apply false

    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    id("java-gradle-plugin")

    /* https://plugins.gradle.org/plugin/io.gitlab.arturbosch.detekt */
    id("io.gitlab.arturbosch.detekt") version "1.0.1"

    /* https://plugins.gradle.org/plugin/com.gradle.plugin-publish */
    id("com.gradle.plugin-publish") version "0.10.1"

    /* https://github.com/ben-manes/gradle-versions-plugin */
    id("com.github.ben-manes.versions") version "0.25.0"
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    // ./gradlew build -Dscan.capture-task-input-files
    //isCaptureTaskInputFiles = true

    publishAlwaysIf(!System.getenv("CI").isNullOrEmpty())
    tag("CI")

    // https://github.com/klarna/gradle-react-native/tree/master
    link("VCS", "https://github.com/klarna/gradle-react-native/tree/${System.getProperty("vcs.branch")}")
}

allprojects {
    ext {
        set("buildToolsVersion", "28.0.3")
        set("minSdkVersion", 16)
        set("compileSdkVersion", 28)
        set("targetSdkVersion", 28)
        set("supportLibVersion", "28.0.0")
    }
    repositories {
        google()
        jcenter()
    }
}

dependencies {
    // Make the root project archives configuration depend on every subproject
    subprojects.forEach { archives(it) }
}

/** Always use ALL distribution not BINARY only. */
tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

//region dependencyUpdates
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    // disallow release candidates as upgradable versions from stable versions
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }

    outputFormatter = "json,xml"
}
//endregion

/**
 * References:
 * https://github.com/gradle/kotlin-dsl-samples/blob/master/samples/multi-kotlin-project/
 * */