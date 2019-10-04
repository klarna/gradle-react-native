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
