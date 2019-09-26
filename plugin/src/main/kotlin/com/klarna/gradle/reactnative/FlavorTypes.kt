package com.klarna.gradle.reactnative

import org.gradle.api.Project
import java.io.Serializable
import javax.inject.Inject

/** Sub-extension name. */
const val EXTENSION_FLAVOR_TYPES_NAME = "productFlavors"

/**
 * Flavor type that is corresponding to the Android productFlavor sections.
 *
 * @constructor Create named instance of the product flavor.
 * @property name unique name of the product flavor.
 * */
open class FlavorTypes
@Inject constructor(open val name: String) : Serializable {
    /** Project reference. */
    open var project: Project? = null
    /** Should specific flavor include JavaScript bundle into final binary or not. */
    open var bundleIn: Boolean = false
    /** Should we do `hermes` post processing for the JavaScript bundle. */
    open var enableHermes: Boolean = false

    /** initialize the instance. */
    init {
        project?.logger?.info("'productFlavors' extension registered to $name")
    }

    companion object {
        /** Serialization UID. */
        const val serialVersionUID = 1L
        /** Extension name. Mostly used for testing. */
        const val EXTENSION = EXTENSION_FLAVOR_TYPES_NAME
    }
}
