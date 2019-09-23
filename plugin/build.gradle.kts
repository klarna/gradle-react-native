/* Gradle React Native Plugin */

import org.gradle.api.artifacts.Configuration
/*
    NOTES:
        `version` - injectede from `gradle.properties` of the root project
        `group` - injectede from `gradle.properties` of the root project
*/

plugins {
    kotlin("jvm")

    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
}

repositories {
    jcenter()
    google()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // https://docs.gradle.org/current/userguide/test_kit.html
    testImplementation(gradleTestKit())

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            allWarningsAsErrors = true
        }
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
    /* ... */
}

gradlePlugin {
    // Define the plugin
    plugins {
        create("reactnative") {
            id = "com.klarna.gradle.reactnative"
            implementationClass = "com.klarna.gradle.reactnative.GradleReactNativePlugin"
        }
    }

    testSourceSets(functionalTestSourceSet)
}

var configurationTestImpl: Configuration = configurations.getByName("testImplementation")
configurations.getByName("functionalTestImplementation").extendsFrom(configurationTestImpl)

// Add a task to run the functional tests
val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

// Run the functional tests as part of `check`
val check by tasks.getting(Task::class) {
    dependsOn(functionalTest)
}

/**
 * References:
 * https://github.com/FRI-DAY/elasticmq-gradle-plugin
 *   https://medium.com/friday-insurance/how-to-write-a-gradle-plugin-in-kotlin-68d7a3534e71
 * User Manual available at https://docs.gradle.org/5.6.2/userguide/custom_plugins.html
 * https://guides.gradle.org/testing-gradle-plugins/
 * https://docs.gradle.org/current/userguide/test_kit.html
 * */
