package com.klarna.gradle.reactnative

import org.gradle.api.DefaultTask
import javax.inject.Inject

/** Base class for plugin tasks. Allows to keep all the tasks in one logical group. */
open class CommonRnTask
@Inject constructor() : DefaultTask() {
    init {
        group = "reactnative"
    }
}
