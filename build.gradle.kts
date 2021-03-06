/* React Native Build Gradle Plugin */
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.Properties

/* Default repositories for plugin search */
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

    // Root Project shoud behave as a java library
    id("java-library")

    /* https://plugins.gradle.org/plugin/io.gitlab.arturbosch.detekt */
    id("io.gitlab.arturbosch.detekt") version "1.0.1" apply false

    /* https://plugins.gradle.org/plugin/com.gradle.plugin-publish */
    id("com.gradle.plugin-publish") version "0.10.1" apply false

    /* https://github.com/ben-manes/gradle-versions-plugin */
    id("com.github.ben-manes.versions") version "0.25.0"

    /* https://github.com/JLLeitschuh/ktlint-gradle */
    id("org.jlleitschuh.gradle.ktlint") version "9.0.0" apply false

    /* https://github.com/Kotlin/dokka */
    id("org.jetbrains.dokka") version "0.9.18" apply false

    jacoco

    /* https://github.com/radarsh/gradle-test-logger-plugin */
    id("com.adarshr.test-logger") version "1.7.0" apply false
}

/* Properties loader. */
fun readProperties(file: File) = Properties().apply {
    if (file.exists()) file.inputStream().use { load(it) }
}
/* Load credentials needed for plugin publishing. */
val credentials = readProperties(File(project.rootDir, "credentials.properties"))
if (credentials.size == 0) {
    logger.warn(
        "WARNING: " +
            "`credentials.properties` file is empty or not found. " +
            "You will not be able to publish plugin to gradle plugins public repository."
    )
}

/* Inject repositories and global variables. */
allprojects {
    ext {
        credentials.forEach { key, value -> set("$key", value) }

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
}

/* Apply gradle plugins */
allprojects {
}

/* Apply plugins and configurations for sub-projects. */
subprojects {
    //region detekt
//    apply(plugin = "io.gitlab.arturbosch.detekt")
//
//    detekt {
//        config = files("${project.rootDir}/.circleci/detekt.yml")
//        parallel = true
//    }
    //endregion

    //region JaCoCo
    apply(plugin = "jacoco")
    jacoco {
        toolVersion = "0.8.4"
        reportsDir = file("$buildDir/reports/jacoco")
    }

    /* Force XML reports and make execution dependency to a test tasks. */
    tasks.withType<JacocoReport>() {
        reports {
            xml.isEnabled = true
        }

        dependsOn(tasks.withType<Test>())
    }

    /* Print status of unit test in terminal */
    tasks.withType<Test>() {
        testLogging {
            showStandardStreams = true
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }
    //endregion
}

/* Make the root project archives configuration depend on every sub-project */
dependencies {
    subprojects.forEach { archives(it) }
}

//region High level tasks
/* Introduce high-level build tasks: assembleDebug && assembleRelease */
val samplePrj = gradle.includedBuild("ReactNativePlugin")
arrayOf("debug", "release").forEach { buildType ->
    arrayOf("").forEach { flavor ->
        val bf = "${flavor.capitalize()}${buildType.capitalize()}"
        tasks.register("assemble$bf") {
            dependsOn(samplePrj.task(":app:assemble$bf"))
        }
    }
}

/* Join dependencies of the composed project with plugin project. */
tasks.named("dependencies") {
    dependsOn(
        gradle.includedBuild("ReactNativePlugin").task(":app:dependencies")
    )
}

/* Print build environment for included Builds in addition to plugin project. */
tasks.named("buildEnvironment") {
    dependsOn(
        gradle.includedBuild("ReactNativePlugin").task(":buildEnvironment")
    )
}

/* Create `lint` tasks that triggers plugin lint in debug mode. */
tasks.register("lint") {
    dependsOn(gradle.includedBuild("ReactNativePlugin").task(":app:lintDebug"))
}

/* Merge results of multiple subprojects */
val jacocoMerge by tasks.registering(JacocoMerge::class) {
    subprojects {
        dependsOn(tasks.withType<JacocoReport>())
        executionData(tasks.withType<JacocoReport>().map { it.executionData })
    }

    outputs.upToDateWhen { false } // force 'always run' mode

    // attach all *.exec files from functional tests
    doFirst {
        file("${rootProject.rootDir}").walk()
            .filter { it.name.endsWith(".exec") }
            .forEach { executionData(it) }
    }

    destinationFile = file("$buildDir/jacoco")
}

/* Create consolidate JaCoCo report */
val jacocoRootReport by tasks.registering(JacocoReport::class) {
    dependsOn(jacocoMerge)

    sourceDirectories.from(files(subprojects.map {
        it.sourceSets[MAIN_SOURCE_SET_NAME].allSource.srcDirs
    }))
    classDirectories.from(files(subprojects.map {
        it.sourceSets[MAIN_SOURCE_SET_NAME].output
    }))
    executionData(jacocoMerge.get().destinationFile)

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        csv.isEnabled = false
    }
}
//endregion

/* Always use ALL distribution not BINARY only. */
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
 * https://github.com/bakdata/dedupe/blob/master/build.gradle.kts
 * https://github.com/gradle/gradle/blob/master/buildSrc/build.gradle.kts
 * */
