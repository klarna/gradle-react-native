package com.klarna.gradle.reactnative

import javax.inject.Inject

const val EXTENSION_BUILD_TYPES_NAME = "buildTypes"

/**
 * @see <a href="https://docs.gradle.org/current/dsl/org.gradle.api.plugins.ExtensionAware.html">Nested Extensions</a>
 * */
open class BuildTypes
@Inject constructor(name: String) {
    open var name: String = name
    open var bundleIn: Boolean = false
    open var enableHermes: Boolean = false

    init {
        println("'buildTypes' extension registered to $name")
    }

    companion object {
        const val EXTENSION = EXTENSION_BUILD_TYPES_NAME
    }
}
