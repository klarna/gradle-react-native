/*
 *
 * Copyright (C) 2013-2019 Klarna AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.klarna.gradle.reactnative

import org.gradle.testkit.runner.GradleRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

//region Constants
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
        classpath 'com.android.tools.build:gradle:3.5.1'
    }
}
"""
/** Minimal Android app configuration section */
const val ANDROID_SECTION = """
    compileSdkVersion 29
    buildToolsVersion "29.0.2"
    defaultConfig {
        applicationId "dummy.myapplication"
        minSdkVersion 19
        targetSdkVersion 29
        versionCode 1
        versionName "1.0"
    }
"""
/** buildTypes configuration for `android { ... }` */
const val ANDROID_BUILD_TYPES_SECTION = """
    buildTypes {
        release {
            minifyEnabled false
        }
    }
"""
/** productFlavors configuration for `android { ... }` */
const val ANDROID_FLAVORS_SECTION = """
    flavorDimensions "dummy"
    productFlavors {
        local {
        }
        yellow {
        }
        pink {
        }
    }
"""
/** Gradle default build configuration. */
const val GRADLE_PLUGINS = """
plugins {
    id('${GradleReactNativePlugin.ANDROID_APP_PLUGIN}') version "3.5.0"
    id('${GradleReactNativePlugin.PLUGIN}')
}
"""
//endregion

/** Functional tests. Try to run plugin in different modes. */
class GradleReactNativePluginFunctionalTest() : CommonFunctionalTest() {
    @BeforeTest
    override fun initializeDirectory() {
        super.initializeDirectory()
    }

    @Test
    fun `fail on no android app plugin`() {
        // Setup the test build
        projectDir.resolve("build.gradle").writeText(
            """
            plugins {
                id('${GradleReactNativePlugin.PLUGIN}')
            }
            """.trimIndent()
        )

        // Run the build
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath(loadClasspath())
            .withProjectDir(projectDir)
            .withArguments(CompileRnBundleTask.NAME)
            .buildAndFail()

        assertTrue(result.output.contains(GradleReactNativePlugin.EXCEPTION_NO_ANDROID_PLUGIN))
    }

    @Test
    fun `can configure extension`() {
        // Setup the test build
        projectDir.resolve("build.gradle").writeText(
            """
            $GRADLE_DEPENDENCIES
            $GRADLE_PLUGINS
            android {
                $ANDROID_SECTION
            }
            ${ReactNativeExtension.EXTENSION} {
               root "../.."
               bundleAssetName "index.android.bundle"
               entryFile "index.android.js"
            }
            """.trimIndent()
        )

        // Run the build
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath(loadClasspath())
            .withProjectDir(projectDir)
            .withArguments(CompileRnBundleTask.NAME)
            .build()

        // Verify the result
        assertTrue(result.output.contains(CompileRnBundleTask.DUMMY))
    }

    @Test
    fun `welcome to react native plugin`() {
        // Setup the test build
        projectDir.resolve("build.gradle").writeText(
            """
            $GRADLE_DEPENDENCIES
            $GRADLE_PLUGINS
            android {
                $ANDROID_SECTION
                $ANDROID_BUILD_TYPES_SECTION
                $ANDROID_FLAVORS_SECTION
            }
            ${ReactNativeExtension.EXTENSION} {
                root "${DOLLAR}buildDir/../.."
                bundleAssetName "index.bundle"
                entryFile "index.js"
               
                buildTypes {
                    debug {
                        enableHermes = true
                        jsBundleDir = "${DOLLAR}buildDir/intermediates/assets/debug"
                    }
                    release {
                        bundleIn = true
                        jsBundleDir = "${DOLLAR}buildDir/intermediates/assets/release"
                    }
                }
            }
            """.trimIndent()
        )

        // Run the build
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath(loadClasspath())
            .withProjectDir(projectDir)
            .withArguments(CompileRnBundleTask.NAME)
            .build()

        assertTrue(result.output.contains(CompileRnBundleTask.DUMMY))
    }
}
