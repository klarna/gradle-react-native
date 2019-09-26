package com.klarna.gradle.reactnative

import org.gradle.api.Project
import java.io.Serializable
import javax.inject.Inject

const val EXTENSION_FLAVOR_TYPES_NAME = "productFlavors"

open class FlavorTypes
@Inject constructor(open val name: String) : Serializable {
    open var project: Project? = null
    open var bundleIn: Boolean = false
    open var enableHermes: Boolean = false

    init {
        project?.logger?.info("'productFlavors' extension registered to $name")
    }

    companion object {
        const val serialVersionUID = 1L
        const val EXTENSION = EXTENSION_FLAVOR_TYPES_NAME
    }
}
