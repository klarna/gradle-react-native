/* Gradle React Native Plugin */

import org.jetbrains.dokka.gradle.DokkaTask
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

/*
    NOTES:
        `version` - injectede from `gradle.properties` of the root project
        `group` - injectede from `gradle.properties` of the root project
*/

plugins {
    kotlin("jvm")
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    id("org.jetbrains.dokka")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.adarshr.test-logger")
}

repositories {
    jcenter()
    google()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))

    // register APIs for gradle plugin
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    // https://developer.android.com/studio/releases/gradle-plugin
    compileOnly("com.android.tools.build:gradle:3.5.0")

    /* required for proper class finding in functional Tests */
    runtimeOnly("com.android.tools.build:gradle:3.5.0")

    // https://docs.gradle.org/current/userguide/test_kit.html
    testImplementation(gradleTestKit())

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    testImplementation("com.android.tools.build:gradle:3.5.0")
    testImplementation(gradleApi())
    testImplementation(gradleKotlinDsl())
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
    /* Include source code of the plugin */
    java {
        setSrcDirs(listOf("src/main/kotlin"))
    }
}

//region Publishing
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

pluginBundle {
    website = "https://github.com/klarna/gradle-react-native"
    vcsUrl = "https://github.com/klarna/gradle-react-native"
    description = "Gradle React Native applications build plugin"

    (plugins) {
        "reactnative" {
            displayName = "ReactNative build plugin"
            tags = listOf("gradle", "plugin", "reactnative")
        }
    }
}
//endregion

//region Functional Tests
/* Create custom configuration for functional tests. */
configurations {
    "functionalTestImplementation" {
        extendsFrom(getByName("testImplementation"))
    }
}

/* Force expanded archive folder creation. */
val expandJacocoAgentJar by tasks.registering(Copy::class) {
    configurations.getByName("jacocoAgent").forEach {
        logger.info("~> found jacoco agent: $it")
        from(zipTree(it))
        into(file("$buildDir/tmp/expandedArchives/${it.name}"))
    }
}

/* Add a task to run the functional tests */
val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath += functionalTestSourceSet.runtimeClasspath

    outputs.dir(file("$buildDir/jacoco/functionalTest"))
    outputs.file(file("$buildDir/jacoco/functionalTest.exec"))

    ignoreFailures = true
}

/* compose functional tests report */
val jacocoFunctionalTestReport by tasks.registering(JacocoReport::class) {
    dependsOn(functionalTest)

    setOnlyIf { functionalTest.enabled }
    outputs.upToDateWhen { false } // force 'always run' mode

    sourceDirectories.from(files(functionalTestSourceSet.allSource.srcDirs))

    // Functional tests `.withPluginClasspath()` injects `build/classes/kotlin/main` binaries
    classDirectories.from(files(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output))

    // attach all *.exec files from functional tests
    doFirst {
        file("$buildDir/jacoco").walk()
            .filter { it.name.endsWith(".exec") }
            .filter { it.absolutePath.contains("functionalTest") }
            .forEach {
                executionData(it)
                logger.info("~> $it")
            }
    }

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        csv.isEnabled = false
    }
}

/* Run the functional tests as part of `check` */
val check by tasks.getting(Task::class) {
    dependsOn(functionalTest)
}

/* Compose kotlin classes documentation into javadoc folder */
val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

/* Disable standard javadoc tasks for kotlin project. */
val javadoc by tasks.getting(Javadoc::class) {
    setEnabled(false)

    options {
        (this as StandardJavadocDocletOptions).apply {
            if (JavaVersion.current().isJava9Compatible) {
                addBooleanOption("html5", true)
            }

            addBooleanOption("-allow-script-in-comments", true)
            addStringOption("Xdoclint:none", "-quiet")

            locale = "en"
            encoding = "UTF-8"
            charSet = "UTF-8"
            header(
                """
                <script src=\"http://cdn.jsdelivr.net/highlight.js/8.6/highlight.min.js\"></script>
                """.trimIndent()
            )
            footer(
                """
                <script type=\"text/javascript\">\nhljs.initHighlightingOnLoad();\n</script>
                """.trimIndent()
            )

            tags("")
        }
    }

    setSource(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].allSource)
}

/* Compose documentation JAR. */
val javadocJar by tasks.registering(Jar::class) {
    group = "documentation"

    dependsOn(dokka)

    archiveClassifier.set("javadoc")
//    from(tasks.javadoc.get().destinationDir)
    from(dokka.outputDirectory)
}

/* Compose source code JAR. */
val sourcesJar by tasks.registering(Jar::class) {
    description = "Creates a jar of java sources, classified -sources"
    archiveClassifier.set("sources")

    from(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].allSource)
}

artifacts {
    add("archives", javadocJar)
    add("archives", sourcesJar)
}

/* Create extra dependencies between tasks. */
tasks.named("functionalTestClasses") {
    finalizedBy(expandJacocoAgentJar)
    finalizedBy(tasks.named("pluginUnderTestMetadata"))
}
//endregion

//region Quality tools
/* Ktlint configuration for sub-projects */
ktlint {
    /* https://github.com/pinterest/ktlint */
    version.set("0.34.2")

    verbose.set(true)
    android.set(true)
    reporters.set(
        setOf(
            ReporterType.CHECKSTYLE,
            ReporterType.JSON
        )
    )

    additionalEditorconfigFile.set(file(".editorconfig"))
    // Unsupported now by current version of the plugin.
    // disabledRules should be placed into .editorconfig file temporary
//        disabledRules.set(setOf(
//                "import-ordering"
//        ))

    filter {
        exclude { element -> element.file.path.contains("generated/") }
    }
}
//endregion

/**
 * References:
 * https://github.com/FRI-DAY/elasticmq-gradle-plugin
 *   https://medium.com/friday-insurance/how-to-write-a-gradle-plugin-in-kotlin-68d7a3534e71
 * User Manual available at https://docs.gradle.org/5.6.2/userguide/custom_plugins.html
 * https://guides.gradle.org/testing-gradle-plugins/
 * https://docs.gradle.org/current/userguide/test_kit.html
 * https://gist.github.com/aalmiray/e6f54aa4b3803be0bcac
 * */
