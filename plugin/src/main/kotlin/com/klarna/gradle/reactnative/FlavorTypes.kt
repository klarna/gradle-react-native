/*
 *
 * Copyright (C) 2013-2019 Klarna AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
    open var bundleIn: Boolean? = null
    /** Should developer mode be disabled for this flavor or not. */
    open var devDisabledIn: Boolean? = null
    /** Should we do `hermes` post processing for the JavaScript bundle. */
    open var enableHermes: Boolean? = null

    /** initialize the instance. */
    init {
        project?.logger?.info("'productFlavors' extension registered to $name")
    }

    /** Get capitalized name of the flavor. */
    @Suppress("DefaultLocale")
    val title: String = name.capitalize()

    /** dump class configuration in groovy format */
    override fun toString(): String = "$name " +
        "{ bundleIn = $bundleIn" +
        ", devDisabledIn = $devDisabledIn" +
        ", enableHermes = $enableHermes" +
        " }"

    companion object {
        /** Serialization UID. */
        const val serialVersionUID = 1L
        /** Extension name. Mostly used for testing. */
        const val EXTENSION = EXTENSION_FLAVOR_TYPES_NAME
        /** Empty flavor type */
        val Empty = FlavorTypes("")
    }
}
