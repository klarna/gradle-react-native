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
    /** Collection of the build types. */
    var buildTypes: DlsContainer<BuildTypes> =
        project.container(BuildTypes::class.java)
    /** Collection of the flavors. */
    var productFlavors: DlsContainer<FlavorTypes> =
        project.container(FlavorTypes::class.java)

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

    companion object {
        /** Serialization UID. */
        const val serialVersionUID = 1L
        /** Extension name. */
        const val EXTENSION = EXTENSION_ROOT_NAME
    }
}
