/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.klarna.gradle.reactnative

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.klarna.gradle.reactnative.ReactNativeExtension as RnConfig

/** Unique plugin name. */
const val PLUGIN_NAME_ID = "com.klarna.gradle.reactnative"

/** React Native Gradle Build Plugin.
 *
 * @see <a href="https://github.com/leleliu008/BintrayUploadGradlePlugin">Bintray Upload Plugin<a>
 * */
open class GradleReactNativePlugin : Plugin<Project> {
    /** Register extensions and tasks for provided project. */
    override fun apply(project: Project) {
        with(project) {
            // Create the NamedDomainObjectContainers

            // Register extensions and forward project instance to it as parameter
            extensions.create(RnConfig.EXTENSION, RnConfig::class.java, project)

            // register tasks
            tasks.register(CompileRnBundleTask.NAME, CompileRnBundleTask::class.java, project)
            tasks.register(CopyRnBundleTask.NAME, CopyRnBundleTask::class.java, project)
        }

        // callbacks
        project.afterEvaluate(this::afterProjectEvaluate)
        project.gradle.addListener(this)
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
            checkNotNull(android) { GradleReactNativePlugin.EXCEPTION_NO_ANDROID_PLUGIN }

            return android
        }
    }
}
