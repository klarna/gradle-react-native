package com.klarna.gradle.reactnative

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

const val COPY_JS_BUNDLE_TASK: String = "copyJsBundle"

@CacheableTask
open class CopyRnBundleTask
@Inject constructor(project: Project) : CommonRnTask() {

    init {
        description = "Copy React native JavaScript Bundle into project ${project.name}"
    }

    @TaskAction
    fun doAction() {
        println(DUMMY)
        project.logger.log(LogLevel.DEBUG, "Copy task executed for project: ${project.name}")
    }

    companion object {
        const val NAME = COPY_JS_BUNDLE_TASK
        const val DUMMY = "Hello from plugin 'com.klarna.gradle.reactnative', task: CopyRnBundleTask"
    }
}
