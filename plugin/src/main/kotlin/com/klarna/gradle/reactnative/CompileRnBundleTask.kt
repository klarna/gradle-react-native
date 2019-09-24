package com.klarna.gradle.reactnative

import org.gradle.api.Project
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

const val COMPILE_RN_BUNDLE_TASK_NAME = "compileRnBundle"

@CacheableTask
open class CompileRnBundleTask
@Inject constructor(project: Project) : CommonRnTask() {
    init {
        description = """
            Compile JavaScript Bundle for '${project.name}', configuration: '<undefined>'
        """.trimIndent()
    }

    @TaskAction
    fun doAction() {
        println(CompileRnBundleTask.DUMMY)
    }

    companion object {
        const val NAME = COMPILE_RN_BUNDLE_TASK_NAME
        const val DUMMY = "Hello from plugin 'com.klarna.gradle.reactnative'"
    }
}
