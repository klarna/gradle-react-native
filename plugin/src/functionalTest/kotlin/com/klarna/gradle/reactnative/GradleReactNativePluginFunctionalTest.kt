/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.klarna.gradle.reactnative

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/** Functional tests. Try to run plugin in different modes. */
class GradleReactNativePluginFunctionalTest {
    var projectDir: File = File("/")

    @BeforeTest
    fun initializeDirectory() {
        projectDir = File("build/functionalTest")
        projectDir.mkdirs()

        // make empty files
        projectDir.resolve("settings.gradle").writeText("")
        projectDir.resolve("build.gradle").writeText("")
    }

    @Test
    fun `can run task`() {
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
                .build()

        // Verify the result
        assertTrue(result.output.contains(CompileRnBundleTask.DUMMY))
    }

    @Test
    fun `can configure extension`() {
        // Setup the test build
        projectDir.resolve("build.gradle").writeText("""
            plugins {
                id('${GradleReactNativePlugin.PLUGIN}')
            }
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
            plugins {
                id 'com.android.application'
                id 'kotlin-android'
                id "org.jetbrains.kotlin.kapt" version "1.3.50"
                id('${GradleReactNativePlugin.PLUGIN}')
            }
            react {
               root "../.."
               bundleAssetName "index.android.bundle"
               entryFile "index.android.js"
            }
        """.trimIndent())
    }
}
