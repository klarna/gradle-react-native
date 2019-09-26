package com.klarna.gradle.reactnative

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import java.io.Serializable
import javax.inject.Inject

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
@Inject constructor(open val project: Project) : Serializable {
    /** Root path to the React Native project folder. Folder where we have `node_modules` used
     * in android binary (i.e. where "package.json" lives). */
    open var root: String? = "../../"
    /** JavaScript bundle name used when we inject bundle into android assets. */
    open var bundleAssetName: String? = "index.android.bundle"
    /** JavaScript bundle start point. */
    open var entryFile: String? = "index.android.js"
    /** Type of the react native bundle that we build. */
    open var bundleCommand: String? = "ram-bundle"
    /** Collection of the build types. */
    open var buildTypes: NamedDomainObjectContainer<BuildTypes> =
        project.container(BuildTypes::class.java)
    /** Collection of the flavors. */
    open var productFlavors: NamedDomainObjectContainer<FlavorTypes> =
        project.container(FlavorTypes::class.java)

    /** Initialize class instance. */
    init {
        with(project) {
            logger.log(LogLevel.DEBUG, "'react' extension registered to ${project.name}")
            logger.info("registered container: $buildTypes")
            logger.info("registered container: $productFlavors")
        }
    }

    /** Build types definition helpers. Closure. */
    fun buildTypes(name: String, configuration: Closure<BuildTypes>): BuildTypes =
        buildTypes.create(name, configuration)

    /** Build types definition helpers. Action. */
    fun buildTypes(name: String, configuration: Action<BuildTypes>): BuildTypes =
        buildTypes.create(name, configuration)

    companion object {
        /** Serialization UID. */
        const val serialVersionUID = 1L
        /** Extension name. */
        const val EXTENSION = EXTENSION_ROOT_NAME
    }
}
