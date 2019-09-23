package com.klarna.gradle.reactnative

import org.gradle.api.Project

/**
 * <pre>
 *     react {
 *       root "../.."
 *       bundleAssetName "index.android.bundle"
 *       entryFile "index.android.js"
 *       bundleCommand "ram-bundle"
 *       bundleInDebug false
 *       bundleInRelease true
 *       enableHermes false
 *     }
 * </pre>
 * @see <a href="https://docs.gradle.org/current/userguide/custom_plugins.html">Custom Plugins</a>
 * */
open class ReactNativeExtension(project: Project) {
    var root: String? = "../../"
    var bundleAssetName: String? = "index.android.bundle"
    var entryFile: String? = "index.android.js"

    var bundleCommand: String? = "ram-bundle"
    var bundleInDebug: Boolean = false
    var bundleInRelease: Boolean = true

    var enableHermes: Boolean = false

    init {
        println("Attached to: ${project.name}")
    }
}
