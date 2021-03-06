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
    /** Should developer mode be disabled for this flavor or not. */
    open var devDisabledIn: Boolean = (name != DEBUG)
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

    /** Get capitalized name of the build type. */
    @Suppress("DefaultLocale")
    val title: String = name.capitalize()

    /** dump class configuration in groovy format */
    override fun toString(): String = "$name " +
        "{ bundleIn = $bundleIn" +
        ", devDisabledIn = $devDisabledIn" +
        ", enableHermes = $enableHermes" +
        ", jsBundleDir = \"$jsBundleDir\"" +
        ", resourcesDir = \"$resourcesDir\"" +
        " }"

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
