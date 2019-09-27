package com.klarna.gradle.reactnative

import org.gradle.api.Project
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/** Name of the task. */
const val COMPILE_RN_BUNDLE_TASK_NAME = "compileRnBundle"

/** Task compiles React Native JavaScript bundle and store results into specified directory.
 * @constructor create instance of task attached to a specific project
 * @property project reference on project where tasks created.
 * */
@CacheableTask
open class CompileRnBundleTask
@Inject constructor(project: Project) : CommonRnTask() {
    /** Compose unique task description. */
    init {
        description = """
            Compile JavaScript Bundle for '${project.name}', configuration: '<undefined>'
        """.trimIndent()
    }

    /** Compile RN JavaScript Bundle. */
    @TaskAction
    fun doAction() {
        println(DUMMY)

        val react = GradleReactNativePlugin.getConfiguration(project)
        project.logger.info("$react")
    }

    companion object {
        /** Task name. */
        const val NAME = COMPILE_RN_BUNDLE_TASK_NAME
        /** Dummy output used for testing the fact of execution. */
        const val DUMMY = "Plugin 'com.klarna.gradle.reactnative', task: CompileRnBundleTask"
    }
}
