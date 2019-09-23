/* React Native Build Gradle Plugin */
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

/** Default repositories for plugin search */
buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
    }
}

/* List of all included plugins */
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

    /* https://github.com/JLLeitschuh/ktlint-gradle */
    id("org.jlleitschuh.gradle.ktlint") version "8.2.0"

    jacoco
}

allprojects {
    ext {
        set("buildToolsVersion", "29.0.2")
        set("minSdkVersion", 16)
        set("compileSdkVersion", 29)
        set("targetSdkVersion", 29)
        set("supportLibVersion", "28.0.0")
    }
    repositories {
        google()
        jcenter()
    }

    //region ktlint
    // We want to apply ktlint at all project level because it also checks build gradle files
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    // Ktlint configuration for sub-projects
    ktlint {
        /* https://github.com/pinterest/ktlint */
        version.set("0.34.2")

        verbose.set(true)
        android.set(true)
        reporters.set(setOf(
                ReporterType.CHECKSTYLE,
                ReporterType.JSON
        ))

        additionalEditorconfigFile.set(file(".editorconfig"))
        // Unsupported now by current version of the plugin.
        // diabledRules should be placed into .editconfig file temporary
//        disabledRules.set(setOf(
//                "import-ordering"
//        ))

        filter {
            exclude { element -> element.file.path.contains("generated/") }
        }
    }
    //endregion
}

subprojects {
    //region detekt
    apply(plugin = "io.gitlab.arturbosch.detekt")
    detekt {
        config = files("${project.rootDir}/.circleci/detekt.yml")
        parallel = true
    }
    //endregion

    apply(plugin = "jacoco")
    jacoco {
        toolVersion = "0.8.4"
        reportsDir = file("$buildDir/reports/jacoco")
    }

    tasks.withType<JacocoReport>() {
        reports {
            xml.isEnabled = true
        }
    }

    tasks.withType<Test>() {
        testLogging {
            showStandardStreams = true
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }
}

dependencies {
    // Make the root project archives configuration depend on every subproject
    subprojects.forEach { archives(it) }
}

//region High level tasks
/* Introduce high-level build tasks: assembleDebug && assembleRelease */
arrayOf("debug", "release").forEach { buildType ->
    arrayOf("").forEach { flavor ->
        val bf = "${flavor.capitalize()}${buildType.capitalize()}"
        tasks.register("assemble$bf") {
            dependsOn(gradle.includedBuild("ReactNativePlugin").task(":app:assemble$bf"))
        }
    }
}

/* Join dependencies of the composed porject with plugin project. ct*/
tasks.findByName("dependencies")?.dependsOn(
        gradle.includedBuild("ReactNativePlugin").task(":app:dependencies")
)
tasks.register("lint") {
    dependsOn(gradle.includedBuild("ReactNativePlugin").task(":app:lintDebug"))
}
tasks.jacocoTestReport {
    dependsOn(":plugin:test")
}
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}
//endregion

/** Always use ALL distribution not BINARY only. */
tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

//region buildScan
/* Configuration of build scan tool */
buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

// ./gradlew build -Dscan.capture-task-input-files
// isCaptureTaskInputFiles = true

    publishAlwaysIf(!System.getenv("CI").isNullOrEmpty())
    tag("CI")

// https://github.com/klarna/gradle-react-native/tree/master
    val URL = "https://github.com/klarna/gradle-react-native/tree"
    val branch = System.getProperty("vcs.branch")
    link("VCS", "$URL/$branch")
}
//endregion

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