package com.klarna.gradle.reactnative

import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

const val COPY_JS_BUNDLE_TASK : String = "copyJsBundle"

open class CopyRnBundleTask
@Inject constructor(project: Project) : CommonRnTask() {

    init {
        description = "Copy React native JavaScript Bundle into project ${project.name}"
    }

    @TaskAction
    fun doAction() {

    }

    companion object {
        const val NAME = COPY_JS_BUNDLE_TASK;
    }
}