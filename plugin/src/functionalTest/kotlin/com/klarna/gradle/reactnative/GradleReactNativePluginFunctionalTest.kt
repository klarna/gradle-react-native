/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.klarna.gradle.reactnative

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TestName
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

const val DOLLAR = "\$"
/** @see <a href="https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:plugins_resolution_strategy">Plugin resolution strategy<a> */
const val ANDROID_PLUGIN = """
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if(requested.id.namespace == "com.android") {
                useModule("com.android.tools.build:gradle:$DOLLAR{requested.version}")
            }
        }
    }
}    
"""
/** Gradle build script dependencies needed for android projects. */
const val GRADLE_DEPENDENCIES = """
buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.5.0'
    }
}
"""
/** Minimal Android app configuration section */
const val ANDROID_SECTION = """
android {
    compileSdkVersion 29
    buildToolsVersion "29.0.2"
    defaultConfig {
        applicationId "dummy.myapplication"
        minSdkVersion 19
        targetSdkVersion 29
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
        }
    }
} 
"""
/** Gradle default build configuration. */
const val GRADLE_PLUGINS = """
plugins {
    id('${GradleReactNativePlugin.ANDROID_APP_PLUGIN}') version "3.5.0"
    id('${GradleReactNativePlugin.PLUGIN}')
}

$ANDROID_SECTION
"""

/** Functional tests. Try to run plugin in different modes. */
class GradleReactNativePluginFunctionalTest {
    private val projectDir: File = File("build/functionalTest")
    private val jacocoDir: File = File("build/jacoco/functionalTest")

    @Rule
    @JvmField
    public val testName: TestName = TestName()

    @BeforeTest
    fun initializeDirectory() {
        projectDir.mkdirs()

        // make empty files
        projectDir.resolve("settings.gradle").writeText("$ANDROID_PLUGIN")
        projectDir.resolve("build.gradle").writeText("")

        // runtime jacoco attaching
        val expandedDir = "build/tmp/expandedArchives"
        val jacocoVer = "org.jacoco.agent-0.8.4.jar_982888894296538c98d7324f3ca78d8f"
        val jacocoRuntime: File = File("$expandedDir/$jacocoVer/jacocoagent.jar")
        val testName = testName.methodName.replace(' ', '_').replace('\'', '_')
        projectDir.resolve("gradle.properties").writeText("""
            # method=${this.testName.methodName}
            org.gradle.jvmargs=-javaagent:${jacocoRuntime.absolutePath}=destfile=${jacocoDir.absolutePath}/$testName.exec
        """.trimIndent())
    }

    @Test
    fun `fail on no android app plugin`() {
        // Setup the test build
        projectDir.resolve("build.gradle").writeText("""
            plugins {
                id('${GradleReactNativePlugin.PLUGIN}')
            }
        """.trimIndent())

        // Run the build
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments(CompileRnBundleTask.NAME)
            .buildAndFail()

        assertTrue(result.output.contains("Android application build plug-in not found"))
    }

    @Test
    fun `can run compile task`() {
        // Setup the test build
        projectDir.resolve("build.gradle").writeText("""
            $GRADLE_DEPENDENCIES
            $GRADLE_PLUGINS
        """.trimIndent())

        // Run the build
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments(CompileRnBundleTask.NAME)
            .build()

        // Verify the result
        assertTrue(result.output.contains(CompileRnBundleTask.DUMMY))
    }

    @Test
    fun `can run copy task`() {
        // Setup the test build
        projectDir.resolve("build.gradle").writeText("""
            $GRADLE_DEPENDENCIES
            $GRADLE_PLUGINS
        """.trimIndent())

        // Run the build
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments(CopyRnBundleTask.NAME)
            .build()

        // Verify the result
        assertTrue(result.output.contains(CopyRnBundleTask.DUMMY))
    }

    @Test
    fun `can configure extension`() {
        // Setup the test build
        projectDir.resolve("build.gradle").writeText("""
            $GRADLE_DEPENDENCIES
            $GRADLE_PLUGINS
            react {
               root "../.."
               bundleAssetName "index.android.bundle"
               entryFile "index.android.js"
            }
        """.trimIndent())

        // Run the build
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments(CompileRnBundleTask.NAME)
            .build()

        // Verify the result
        assertTrue(result.output.contains(CompileRnBundleTask.DUMMY))
    }

    @Test
    fun `welcome to android plugin`() {
        // Setup the test build
        projectDir.resolve("build.gradle").writeText("""
            $GRADLE_DEPENDENCIES
            $GRADLE_PLUGINS
            react {
               root "../.."
               bundleAssetName "index.android.bundle"
               entryFile "index.android.js"
            }
        """.trimIndent())
    }
}
