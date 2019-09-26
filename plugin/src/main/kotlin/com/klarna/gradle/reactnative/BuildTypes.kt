package com.klarna.gradle.reactnative

import org.gradle.api.Project
import java.io.Serializable
import javax.inject.Inject

/** name of the sub-extension used for this class. */
const val EXTENSION_BUILD_TYPES_NAME = "buildTypes"

/**
 * Android Build Type extension class. Each android build type configuration will have 1-to-1 binding to RN Build type.
 *  <pre>
 *      react {
 *          /* ... */
 *          buildTypes {
 *              debug {
 *                  bundleIn = false
 *                  devDisabled = true
 *                  enableHermes = false
 *                  jsBundleDir "$buildDir/intermediates/assets/debug"
 *                  resourcesDir "$buildDir/intermediates/res/merged/debug"
 *              }
 *              /* ... more build type configurations ... */
 *          }
 *      }
 *  </pre>
 * @see <a href="https://docs.gradle.org/current/dsl/org.gradle.api.plugins.ExtensionAware.html">Nested Extensions</a>
 * @constructor Create build type with a specific name.
 * @property name Unique build type name.
 * */
open class BuildTypes
@Inject constructor(open val name: String) : Serializable {
    /** Reference on project */
    open var project: Project? = null
    /** Should JavaScript Bundle included into final binary or not.
     * By default only `debug` build types do not need including of the bundle.
     * */
    open var bundleIn: Boolean = (name != DEBUG)
    /** Is debug information generation required for a specific build type. */
    open var devDisabled = true
    /** Enable `Hermes` JSC addition. */
    open var enableHermes: Boolean = false
    /** Destination directory of the JavaScript Bundle composed by bundler. */
    open var jsBundleDir: String = "intermediates/assets/$name"
    /** Destination directory of the Assets. where to put drawable resources / React Native
     * assets, e.g. the ones you use via require('./image.png')) */
    open var resourcesDir: String = "intermediates/res/merged/$name"

    init {
        project?.logger?.info("'buildTypes' extension registered to $name")
    }

    companion object {
        /** Serialization UID. */
        const val serialVersionUID = 1L
        /** Default build type that introduced by android/java plugin. */
        const val DEBUG = "debug"
        /** Default build type that introduced by android/java plugin. */
        const val RELEASE = "release"
        /** Easy access to the extension name. Mostly needed for Unit tests. */
        const val EXTENSION = EXTENSION_BUILD_TYPES_NAME
    }
}
