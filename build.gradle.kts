/* React Native Build Gradle Plugin */

buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
    }
}

plugins {
//    base
    kotlin("jvm") version "1.3.41" apply false

    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    id("java-gradle-plugin")

    // Static analysis tools
    id("io.gitlab.arturbosch.detekt") version "1.0.1"

    // plugin's publishing
    id("com.gradle.plugin-publish") version "0.10.1"
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
/**
 * References:
 * https://github.com/gradle/kotlin-dsl-samples/blob/master/samples/multi-kotlin-project/
 * */