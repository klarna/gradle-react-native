package com.klarna.gradle.reactnative

import org.gradle.api.DefaultTask

open class CommonRnTask() : DefaultTask() {
    init {
        group = "reactnative"
    }
}