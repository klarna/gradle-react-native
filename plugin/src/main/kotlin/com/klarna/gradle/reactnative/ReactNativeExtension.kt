package com.klarna.gradle.reactnative

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

const val EXTENSION_ROOT_NAME = "react"

/**
 * <pre>
 *     react {
 *       root "../.."
 *       bundleAssetName "index.android.bundle"
 *       entryFile "index.android.js"
 *       bundleCommand "ram-bundle"
 *       bundleInDebug false
 *       bundleInRelease true
 *       enableHermes false
 *     }
 * </pre>
 * @see <a href="https://docs.gradle.org/current/userguide/custom_plugins.html">Custom Plugins</a>
 * @see <a href="https://www.oreilly.com/library/view/gradle-beyond-the/9781449373801/ch02.html">Beyond the Basics</a>
 * */
open class ReactNativeExtension(project: Project) {
    open var root: String? = "../../"
    open var bundleAssetName: String? = "index.android.bundle"
    open var entryFile: String? = "index.android.js"
    open var bundleCommand: String? = "ram-bundle"

    open var buildTypes: NamedDomainObjectContainer<BuildTypes> =
        project.container(BuildTypes::class.java)

    open var productFlavors: NamedDomainObjectContainer<FlavorTypes> =
        project.container(FlavorTypes::class.java)

    init {
        with(project) {
            logger.log(LogLevel.DEBUG, "'react' extension registered to ${project.name}")
            logger.info("registered container: $buildTypes")
            logger.info("registered container: $productFlavors")
        }
    }

    fun buildTypes(name: String, configuration: Closure<BuildTypes>): BuildTypes =
        buildTypes.create(name, configuration)

    fun buildTypes(name: String, configuration: Action<BuildTypes>): BuildTypes =
        buildTypes.create(name, configuration)

    companion object {
        const val EXTENSION = EXTENSION_ROOT_NAME
    }
}
