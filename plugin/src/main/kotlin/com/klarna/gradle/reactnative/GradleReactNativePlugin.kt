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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.utils.toImmutableList
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import com.klarna.gradle.reactnative.ReactNativeExtension as RnConfig

/** Unique plugin name. */
const val PLUGIN_NAME_ID = "com.klarna.gradle.reactnative"

/** React Native Gradle Build Plugin.
 *
 * @see <a href="https://github.com/leleliu008/BintrayUploadGradlePlugin">Bintray Upload Plugin<a>
 * */
open class GradleReactNativePlugin : Plugin<Project> {
    /** Extracted plugin version. */
    private val pluginVersion: String? = GradleReactNativePlugin::class.java
        .`package`.implementationVersion

    /** Register extensions and tasks for provided project. */
    override fun apply(project: Project) {
        with(project) {
            logger.info("plugin '$PLUGIN' version: $pluginVersion")

            // Register extensions and forward project instance to it as parameter
            extensions.create(RnConfig.EXTENSION, RnConfig::class.java, project)

            // register tasks
            tasks.register(CompileRnBundleTask.NAME, CompileRnBundleTask::class.java, project)
            tasks.register(CopyRnBundleTask.NAME, CopyRnBundleTask::class.java, project)

            // publish react extras properties on plugin attach
            if (!extra.has(REACT)) {
                val react = getConfiguration(project)
                if (react.enableCompatibility) {
                    composeCompatibilityExtensionConfig(
                        project,
                        getAndroidConfiguration(project), react
                    )
                }
            }
        }

        // callbacks
        project.beforeEvaluate(this::beforeProjectEvaluate)
        project.afterEvaluate(this::afterProjectEvaluate)
        project.gradle.addListener(this)
    }

    /** Preparations that we do before project configuration in completely evaluated. */
    private fun beforeProjectEvaluate(project: Project) {
        // Other gradle scripts may expect that a specific `ext` variable
        // be available for them.
        val react = getConfiguration(project)
        project.logger.info("configuration extracted: $react")
    }

    /** After project evaluation all plugins and extensions are applied and its right time
     * for attaching/configure plugin internals. */
    private fun afterProjectEvaluate(project: Project) {
        val react = getConfiguration(project)
        project.logger.info("configuration extracted: $react")

        // extract android plugin extension
        val android = getAndroidConfiguration(project)
        checkNotNull(android.buildTypes) { EXCEPTION_NO_BUILD_TYPES }
        check(android.buildTypes.size != 0) { EXCEPTION_NO_BUILD_TYPES }

        // synchronize build types and flavors
        synchronizeBuildTypes(project, android, react)
        synchronizeProductFlavors(project, android, react)

        if (react.enableCompatibility) {
            composeCompatibilityExtensionConfig(project, android, react)
        }
    }

    /** Compose `project.ext.react = [ ... ]` array of settings for compatibility with old build scripts. */
    @Suppress("UNCHECKED_CAST")
    private fun composeCompatibilityExtensionConfig(
        project: Project,
        android: AppExtension,
        react: RnConfig
    ) {
        project.logger.info("~> react: ${project.name} / $android / $react")

        // https://docs.gradle.org/current/dsl/org.gradle.api.plugins.ExtraPropertiesExtension.html
        if (!project.extra.has(REACT)) {
            project.extra.set(REACT, mutableMapOf<String, Any?>())
        }

        val extraReact = (project.extra[REACT] as Map<String, *>).toMutableMap()
        extraReact["root"] = react.root
        extraReact["bundleAssetName"] = react.bundleAssetName
        extraReact["entryFile"] = react.entryFile
        extraReact["bundleCommand"] = react.bundleCommand
        extraReact["enableCompatibility"] = react.enableCompatibility
        extraReact["enableHermes"] = false

        iterateVariants(react) { bt, fl ->
            val variant = fl.title + bt.title
            extraReact["bundleIn$variant"] = fl.bundleIn ?: bt.bundleIn
            extraReact["devDisabledIn$variant"] = fl.devDisabledIn ?: bt.devDisabledIn
            extraReact["enableHermes$variant"] = fl.enableHermes ?: bt.enableHermes
        }

        react.buildTypes.forEach { bt ->
            val buildName = bt.title
            extraReact["jsBundleDir$buildName"] = bt.jsBundleDir
            extraReact["resourcesDir$buildName"] = bt.resourcesDir
        }

        extraReact["cliPath"] = react.cliPath
        extraReact["composeSourceMapsPath"] = react.composeSourceMapsPath
        extraReact["bundleConfig"] = react.bundleConfig
        extraReact["enableVmCleanup"] = react.enableVmCleanup
        extraReact["hermesCommand"] = react.hermesCommand
        extraReact["reactNativeDevServerPort"] = react.reactNativeDevServerPort
        extraReact["reactNativeInspectorProxyPort"] = react.reactNativeInspectorProxyPort
        extraReact["inputExcludes"] = react.inputExcludes.toImmutableList()
        extraReact["nodeExecutableAndArgs"] = react.nodeExecutableAndArgs.toImmutableList()
        extraReact["extraPackagerArgs"] = react.extraPackagerArgs.toImmutableList()

        // update project by our copy
        project.extra[REACT] = extraReact
    }

    /** Iterate via all possible variants of builds. */
    private fun iterateVariants(react: RnConfig, predicate: (BuildTypes, FlavorTypes) -> Unit) {
        react.buildTypes.forEach { bt ->
            val flavors = if (react.productFlavors.size > 0) {
                react.productFlavors
            } else {
                listOf(FlavorTypes.Empty)
            }

            flavors.forEach { fl ->
                predicate(bt, fl)
            }
        }
    }

    /** For each android.productFlavor create/use corresponding react.productFlavor configuration */
    private fun synchronizeProductFlavors(
        project: Project,
        android: AppExtension,
        react: RnConfig
    ) {
        // create corresponding flavors
        android.productFlavors.forEach {
            project.logger.info("android product flavors: ${it.name}")

            react.productFlavors.maybeCreate(it.name).apply {
                this.project = project
            }
        }
        check(android.productFlavors.size == react.productFlavors.size) {
            EXCEPTION_OUT_OF_SYNC_FLAVORS
        }
    }

    /** For each android.buildType create/use corresponding react.buildType configuration */
    private fun synchronizeBuildTypes(
        project: Project,
        android: AppExtension,
        react: RnConfig
    ) {
        // create corresponding build types
        android.buildTypes.forEach {
            project.logger.info("android build types: ${it.name}")

            react.buildTypes.maybeCreate(it.name).apply {
                this.project = project
            }
        }
        check(android.buildTypes.size == react.buildTypes.size) {
            EXCEPTION_OUT_OF_SYNC_BUILDS
        }
    }

    /** Helpers. */
    companion object {
        /** Plugin name, ID. */
        const val PLUGIN = PLUGIN_NAME_ID
        /** Dependent android plugin name. */
        const val ANDROID_APP_PLUGIN = "com.android.application"
        /** Name of project.ext.react property. */
        const val REACT = "react"

        /** Exception. Raised when plugin cannot find any android plugin attached to the project. */
        const val EXCEPTION_NO_ANDROID_PLUGIN = "Expected android application plugin"
        /** Exception. Raised when we have bad buildTypes configuration. Should never happens. */
        const val EXCEPTION_NO_BUILD_TYPES = "Badly initialized build types"
        /** Exception. Raised when android section and react sections are not in sync to each
         * other in `productFlavors` sub-section. */
        const val EXCEPTION_OUT_OF_SYNC_FLAVORS =
            "`android` and `react` `productFlavors` configurations should be in sync"
        /** Exception. Raised when android section and react sections are not in sync to each
         * other in `buildTypes` sub-section. */
        const val EXCEPTION_OUT_OF_SYNC_BUILDS =
            "`android` and `react` `buildTypes` configurations should be in sync"

        /** Extract plugin configuration. */
        fun getConfiguration(project: Project): RnConfig =
            project.extensions.getByType(RnConfig::class.java)

        /** Extract android application project configuration. */
        fun getAndroidConfiguration(project: Project): AppExtension {
            val android = project.extensions.findByName("android") as? AppExtension
            checkNotNull(android) { EXCEPTION_NO_ANDROID_PLUGIN }

            return android
        }
    }
}
