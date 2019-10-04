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

import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import java.io.Serializable

import javax.inject.Inject
import org.gradle.api.NamedDomainObjectContainer as DlsContainer

/** Plugin root extension name. */
const val EXTENSION_ROOT_NAME = "react"

/**
 * React Native Plugin extension.
 * <pre>
 *     react {
 *       root "../.."
 *       bundleAssetName "index.android.bundle"
 *       entryFile "index.android.js"
 *       bundleCommand "ram-bundle"
 *       enableHermes false
 *
 *       inputExcludes = ["android", "ios"]
 *       nodeExecutableAndArgs = ["node"]
 *       extraPackagerArgs = []
 *
 *       buildTypes { /*...*/ }
 *       productFlavors { /* ... */ }
 *     }
 * </pre>
 * @see <a href="https://docs.gradle.org/current/userguide/custom_plugins.html">Custom Plugins</a>
 * @see <a href="https://www.oreilly.com/library/view/gradle-beyond-the/9781449373801/ch02.html">Beyond the Basics</a>
 *
 * @constructor Create instance of extension with binding to a specific project.
 * @property project reference on attached project
 *
 * */
open class ReactNativeExtension
@Inject constructor(private val project: Project) : Serializable {
    /** Root path to the React Native project folder. Folder where we have `node_modules` used
     * in android binary (i.e. where "package.json" lives). */
    var root: String? = "../../"
    /** JavaScript bundle name used when we inject bundle into android assets. */
    var bundleAssetName: String? = "index.android.bundle"
    /** JavaScript bundle start point. */
    var entryFile: String? = "index.android.js"
    /** Type of the react native bundle that we build. */
    var bundleCommand: String? = "ram-bundle"
    /** Enable compatibility mode with old RN build scripts. */
    var enableCompatibility: Boolean = true
    /** Collection of the build types. */
    var buildTypes: DlsContainer<BuildTypes> =
        project.container(BuildTypes::class.java)
    /** Collection of the flavors. */
    var productFlavors: DlsContainer<FlavorTypes> =
        project.container(FlavorTypes::class.java)
    /** Excludes from inputs used for detecting JS code changes */
    var inputExcludes: List<String> = listOf("android/**", "ios/**")
    /** Default node tool arguments. Override which node gets called and with what
     * additional arguments. */
    var nodeExecutableAndArgs: List<String> = listOf("node")
    /** Supply additional arguments to the packager */
    var extraPackagerArgs: List<String> = emptyList()
    /** CLI tool path. */
    var cliPath: String? = "node_modules/react-native/cli.js"
    /** Sourcemap file destination. */
    var composeSourceMapsPath: String? = "node_modules/react-native/scripts/compose-source-maps.js"
    /** Custom configuration for react native bandler. */
    var bundleConfig: String? = null
    /** Cleanup in native libs, remove un-needed `*.so` files. */
    var enableVmCleanup: Boolean? = true
    /** Path to Hermes engine binary. `%OS-BIN%` will be replaced
     * by corresponding to OS folder name: win64-bin, osx-bin, linux64-bin. */
    var hermesCommand: String? = "../../node_modules/hermesvm/%OS-BIN%/hermes"
    /** Default server port. By default: 8081 */
    var reactNativeDevServerPort: String? = "8081"
    /** Inspector proxy port. By default: 8081 */
    var reactNativeInspectorProxyPort: String? = reactNativeDevServerPort

    /** Initialize class instance. */
    init {
        with(project) {
            logger.log(LogLevel.DEBUG, "'react' extension registered to ${project.name}")
            logger.info("registered container: $buildTypes")
            logger.info("registered container: $productFlavors")
        }
    }

    /** dump class configuration in groovy format */
    override fun toString(): String = """
        $EXTENSION {
           root = "$root"
           bundleAssetName = "$bundleAssetName"
           entryFile = "$entryFile"
           bundleCommand = "$bundleCommand"
           buildTypes {
             ${buildTypes.joinToString(separator = "\n") { it.toString() }}
           }
           productFlavors {
             ${productFlavors.joinToString(separator = "\n") { it.toString() }}
           }
        }
        """.trimIndent()

    /** Build types definition helpers. Closure. */
    fun buildTypes(configuration: Closure<in BuildTypes>): DlsContainer<BuildTypes> =
        buildTypes.configure(configuration)

    /** Product flavors definition helpers. Closure. */
    fun productFlavors(configuration: Closure<in FlavorTypes>): DlsContainer<FlavorTypes> =
        productFlavors.configure(configuration)

    /**
     * Allows in one line apply common packing configuration required for non-conflict JSC usage.
     * ```gradle
     *     android {
     *         packagingOptions {
     *             pickFirst '** /armeabi-v7a/libc++_shared.so'
     *             pickFirst '** /x86/libc++_shared.so'
     *             pickFirst '** /arm64-v8a/libc++_shared.so'
     *             pickFirst '** /x86_64/libc++_shared.so'
     *             pickFirst '** /x86/libjsc.so'
     *             pickFirst '** /armeabi-v7a/libjsc.so'
     *         }
     *     }
     * ```
     * */
    fun applyJscPackagingOptions() {
        val android = GradleReactNativePlugin.getAndroidConfiguration(project)

        /* Troubleshoot: https://github.com/react-native-community/jsc-android-buildscripts */
        android.packagingOptions.apply {
            pickFirst("**/x86/libjsc.so")
            pickFirst("**/armeabi-v7a/libjsc.so")

            pickFirst("**/x86/libc++_shared.so")
            pickFirst("**/x86_64/libc++_shared.so")
            pickFirst("**/armeabi-v7a/libc++_shared.so")
            pickFirst("**/arm64-v8a/libc++_shared.so")
        }
    }

    companion object {
        /** Serialization UID. */
        const val serialVersionUID = 1L
        /** Extension name. */
        const val EXTENSION = EXTENSION_ROOT_NAME
    }
}
