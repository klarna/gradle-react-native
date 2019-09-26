package com.klarna.gradle.reactnative

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/** Copy task name. */
const val COPY_JS_BUNDLE_TASK: String = "copyJsBundle"

/**
 * Copy compiled JavaScript Bundle and assets into android build folder for injecting
 * into final APK.
 *
 * @constructor create instance of task attached to a specific project
 * @property project reference on project where tasks created.
 * */
@CacheableTask
open class CopyRnBundleTask
@Inject constructor(project: Project) : CommonRnTask() {
    /** Create unique tasks description. */
    init {
        description = "Copy React native JavaScript Bundle into project ${project.name}"
    }

    /** Do the copying of the source to destination. */
    @TaskAction
    fun doAction() {
        println(DUMMY)
        project.logger.log(LogLevel.DEBUG, "Copy task executed for project: ${project.name}")
    }

    companion object {
        /** Task name. */
        const val NAME = COPY_JS_BUNDLE_TASK
        /** Dummy output used for testing the fact of execution. */
        const val DUMMY = "Plugin 'com.klarna.gradle.reactnative', task: CopyRnBundleTask"
    }
}
