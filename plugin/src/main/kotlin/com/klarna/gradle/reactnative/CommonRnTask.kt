package com.klarna.gradle.reactnative

import org.gradle.api.DefaultTask
import javax.inject.Inject

open class CommonRnTask
@Inject constructor() : DefaultTask() {
    init {
        group = "reactnative"
    }
}
