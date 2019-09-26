package com.klarna.gradle.reactnative

import org.gradle.api.Project
import java.io.Serializable
import javax.inject.Inject

const val EXTENSION_BUILD_TYPES_NAME = "buildTypes"

/**
 * @see <a href="https://docs.gradle.org/current/dsl/org.gradle.api.plugins.ExtensionAware.html">Nested Extensions</a>
 * */
open class BuildTypes
@Inject constructor(open val name: String) : Serializable {
    open var project: Project? = null
    open var bundleIn: Boolean = false
    open var enableHermes: Boolean = false

    init {
        project?.logger?.info("'buildTypes' extension registered to $name")
    }

    companion object {
        const val serialVersionUID = 1L
        const val EXTENSION = EXTENSION_BUILD_TYPES_NAME
    }
}
