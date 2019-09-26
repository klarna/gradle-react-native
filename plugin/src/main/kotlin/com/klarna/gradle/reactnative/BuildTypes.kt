package com.klarna.gradle.reactnative

import org.gradle.api.Project
import java.io.Serializable
import javax.inject.Inject

/** name of the sub-extension used for this class. */
const val EXTENSION_BUILD_TYPES_NAME = "buildTypes"

/**
 * Android Build Type extension class. Each android build type configuration will have 1-to-1 binding to RN Build type.
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
    open var bundleIn: Boolean = (name != "debug")
    /** Enable `Hermes` JSC addition. */
    open var enableHermes: Boolean = false

    init {
        project?.logger?.info("'buildTypes' extension registered to $name")
    }

    companion object {
        /** Serialization UID. */
        const val serialVersionUID = 1L
        /** Easy access to the extension name. Mostly needed for Unit tests. */
        const val EXTENSION = EXTENSION_BUILD_TYPES_NAME
    }
}
